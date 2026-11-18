package br.edu.api98food;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class Pedido implements Identificavel {
    private static final long serialVersionUID = 1L;
    private static int proximoId = 1;

    public static final String STATUS_ABERTO = "ABERTO";
    public static final String STATUS_EM_PREPARO = "EM_PREPARO";
    public static final String STATUS_PRONTO = "PRONTO";
    public static final String STATUS_FINALIZADO = "FINALIZADO";
    public static final String STATUS_CANCELADO = "CANCELADO";
    public static final List<String> STATUS_VALIDOS = Collections.unmodifiableList(Arrays.asList(
            STATUS_ABERTO, STATUS_EM_PREPARO, STATUS_PRONTO, STATUS_FINALIZADO, STATUS_CANCELADO));

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private final LocalDateTime dataHora;
    private String status;
    private final int clienteId; // 0 = sem cliente (só permitido se exigeCliente() == false)
    private final List<ItemPedido> itens = new ArrayList<>();

    protected Pedido(int clienteId) throws DadoInvalidoException {
        if (clienteId < 0)
            throw new DadoInvalidoException("ID de cliente inválido.");
        if (exigeCliente() && clienteId == 0)
            throw new DadoInvalidoException("Pedido do tipo '" + getTipo() + "' exige um cliente.");
        this.clienteId = clienteId;
        this.dataHora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.status = STATUS_ABERTO;
    }

    /**
     * Gera o ID sequencial. Cada construtor concreto chama no FIM, depois de validar
     * seus campos, para que um pedido inválido não "gaste" um número.
     */
    protected final void gerarId() {
        this.id = proximoId++;
    }

    public static void sincronizarContador(int maiorId) {
        if (maiorId >= proximoId) proximoId = maiorId + 1;
    }

    public static String formatarMoeda(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }

    // ---------- Polimorfismo ----------
    protected abstract boolean exigeCliente();
    public abstract String getTipo();
    public abstract String getDetalhes();
    public abstract double calcularTotal();

    public double calcularSubtotal() {
        double soma = 0;
        for (ItemPedido i : itens) soma += i.getSubtotal();
        return soma;
    }

    // ---------- Itens ----------
    public void adicionarItem(Produto produto, int quantidade) throws GerenciadorException {
        garantirAberto();
        if (!produto.isDisponivel())
            throw new RegraNegocioException("Produto indisponível: " + produto.getNome());
        ItemPedido.validarQuantidade(quantidade);
        ItemPedido existente = buscarItem(produto.getId());
        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade() + quantidade);
        } else {
            itens.add(new ItemPedido(produto.getId(), quantidade, produto.getPrecoBase()));
        }
    }

    public void alterarQuantidadeItem(int produtoId, int quantidade) throws GerenciadorException {
        garantirAberto();
        exigirItem(produtoId).setQuantidade(quantidade);
    }

    public void removerItem(int produtoId) throws GerenciadorException {
        garantirAberto();
        itens.remove(exigirItem(produtoId));
    }

    public boolean contemProduto(int produtoId) {
        return buscarItem(produtoId) != null;
    }

    private ItemPedido buscarItem(int produtoId) {
        for (ItemPedido i : itens)
            if (i.getProdutoId() == produtoId) return i;
        return null;
    }

    private ItemPedido exigirItem(int produtoId) throws RegistroNaoEncontradoException {
        ItemPedido i = buscarItem(produtoId);
        if (i == null) throw new RegistroNaoEncontradoException("Item (produto) do pedido", produtoId);
        return i;
    }

    public List<ItemPedido> getItens() {
        return new ArrayList<>(itens);
    }

    // ---------- Status ----------
    public void avancarStatus() throws RegraNegocioException {
        switch (status) {
            case STATUS_ABERTO:
                if (itens.isEmpty())
                    throw new RegraNegocioException("Pedido sem itens não pode ir para preparo.");
                status = STATUS_EM_PREPARO;
                break;
            case STATUS_EM_PREPARO:
                status = STATUS_PRONTO;
                break;
            case STATUS_PRONTO:
                status = STATUS_FINALIZADO;
                break;
            default:
                throw new RegraNegocioException("Pedido " + status + " não pode avançar de status.");
        }
    }

    public void cancelar() throws RegraNegocioException {
        if (!STATUS_ABERTO.equals(status) && !STATUS_EM_PREPARO.equals(status))
            throw new RegraNegocioException("Só é possível cancelar pedido ABERTO ou EM_PREPARO (atual: " + status + ").");
        status = STATUS_CANCELADO;
    }

    public boolean isEncerrado() {
        return STATUS_FINALIZADO.equals(status) || STATUS_CANCELADO.equals(status);
    }

    public boolean podeExcluir() {
        return STATUS_ABERTO.equals(status) || isEncerrado();
    }

    protected void garantirAberto() throws RegraNegocioException {
        if (!STATUS_ABERTO.equals(status))
            throw new RegraNegocioException("Itens só podem ser alterados com o pedido ABERTO (atual: " + status + ").");
    }

    protected void garantirNaoEncerrado() throws RegraNegocioException {
        if (isEncerrado())
            throw new RegraNegocioException("Pedido " + status + " não pode ser alterado.");
    }

    // ---------- Getters ----------
    @Override public int getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getStatus() { return status; }
    public int getClienteId() { return clienteId; }

    @Override
    public String toString() {
        return "[" + id + "] " + getTipo() + " | " + dataHora.format(FORMATO) + " | " + status
                + " | " + getDetalhes() + " | Itens: " + itens.size()
                + " | Total: " + formatarMoeda(calcularTotal());
    }
}