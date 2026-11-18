package br.edu.api98food;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Comanda implements Identificavel {
    private static final long serialVersionUID = 1L;
    private static int proximoId = 1;

    public static final String STATUS_ABERTA = "ABERTA";
    public static final String STATUS_FECHADA = "FECHADA";

    private final int id;
    private String identificacao;
    private final LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String status;
    private final List<Integer> pedidoIds = new ArrayList<>();

    public Comanda(String identificacao) throws DadoInvalidoException {
        validar(identificacao);
        this.id = proximoId++;
        this.identificacao = identificacao.trim();
        this.dataAbertura = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.status = STATUS_ABERTA;
    }

    public static void sincronizarContador(int maiorId) {
        if (maiorId >= proximoId) proximoId = maiorId + 1;
    }

    private static void validar(String identificacao) throws DadoInvalidoException {
        if (identificacao == null || identificacao.trim().isEmpty())
            throw new DadoInvalidoException("Identificação da comanda é obrigatória (ex.: Mesa 5).");
    }

    public void atualizar(String identificacao) throws GerenciadorException {
        garantirAberta();
        validar(identificacao);
        this.identificacao = identificacao.trim();
    }

    // ---------- Pedidos ----------
    public void adicionarPedido(int pedidoId) throws GerenciadorException {
        garantirAberta();
        if (pedidoIds.contains(pedidoId))
            throw new RegraNegocioException("Pedido " + pedidoId + " já está nesta comanda.");
        pedidoIds.add(pedidoId);
    }

    public void removerPedido(int pedidoId) throws GerenciadorException {
        garantirAberta();
        if (!pedidoIds.remove(Integer.valueOf(pedidoId)))
            throw new RegistroNaoEncontradoException("Pedido na comanda " + id + " -", pedidoId);
    }

    public boolean contemPedido(int pedidoId) {
        return pedidoIds.contains(pedidoId);
    }

    public List<Integer> getPedidoIds() {
        return new ArrayList<>(pedidoIds);
    }

    // ---------- Status ----------
    public void fechar() throws RegraNegocioException {
        garantirAberta();
        this.status = STATUS_FECHADA;
        this.dataFechamento = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public void reabrir() throws RegraNegocioException {
        if (isAberta())
            throw new RegraNegocioException("Comanda já está aberta.");
        this.status = STATUS_ABERTA;
        this.dataFechamento = null;
    }

    public boolean isAberta() {
        return STATUS_ABERTA.equals(status);
    }

    private void garantirAberta() throws RegraNegocioException {
        if (!isAberta())
            throw new RegraNegocioException("Comanda " + id + " está FECHADA. Reabra para alterar.");
    }

    // ---------- Getters ----------
    @Override public int getId() { return id; }
    public String getIdentificacao() { return identificacao; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "[" + id + "] " + identificacao + " | " + status + " | Pedidos: " + pedidoIds.size();
    }
}