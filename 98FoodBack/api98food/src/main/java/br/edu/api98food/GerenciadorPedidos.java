package br.edu.api98food;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorPedidos {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Repositorio<Cliente> clientes;
    private final Repositorio<Produto> produtos;
    private final Repositorio<Pedido> pedidos;
    private final Repositorio<Comanda> comandas;

    /** pasta onde ficam os arquivos .dat (ex.: "dados"). Criada se não existir. */
    public GerenciadorPedidos(String pasta) throws PersistenciaException {
        clientes = new Repositorio<>("Cliente", pasta + "/clientes.dat");
        produtos = new Repositorio<>("Produto", pasta + "/produtos.dat");
        pedidos = new Repositorio<>("Pedido", pasta + "/pedidos.dat");
        comandas = new Repositorio<>("Comanda", pasta + "/comandas.dat");

        clientes.carregar();
        produtos.carregar();
        pedidos.carregar();
        comandas.carregar();


        Cliente.sincronizarContador(clientes.maiorId());
        Produto.sincronizarContador(produtos.maiorId());
        Pedido.sincronizarContador(pedidos.maiorId());
        Comanda.sincronizarContador(comandas.maiorId());
    }

    private void persistir() throws PersistenciaException {
        clientes.salvar();
        produtos.salvar();
        pedidos.salvar();
        comandas.salvar();
    }


    public Cliente incluirCliente(String nome, String telefone, String endereco) throws GerenciadorException {
        Cliente c = new Cliente(nome, telefone, endereco);
        clientes.incluir(c);
        persistir();
        return c;
    }

    public Cliente alterarCliente(int id, String nome, String telefone, String endereco)
            throws GerenciadorException {
        Cliente c = clientes.consultar(id);
        c.atualizar(nome, telefone, endereco);
        clientes.alterar(c);
        persistir();
        return c;
    }

    public void excluirCliente(int id) throws GerenciadorException {
        clientes.consultar(id);
        for (Pedido p : pedidos.listar())
            if (p.getClienteId() == id)
                throw new RegraNegocioException("Cliente possui pedidos e não pode ser excluído.");
        clientes.excluir(id);
        persistir();
    }

    public Cliente consultarCliente(int id) throws RegistroNaoEncontradoException {
        return clientes.consultar(id);
    }

    public List<Cliente> listarClientes() { return clientes.listar(); }

    public List<Cliente> buscarClientesPorNome(String termo) {
        List<Cliente> r = new ArrayList<>();
        for (Cliente c : clientes.listar())
            if (c.getNome().toLowerCase().contains(termo.toLowerCase())) r.add(c);
        return r;
    }


    public Produto incluirProduto(String nome, double precoBase, String categoria, boolean disponivel)
            throws GerenciadorException {
        Produto p = new Produto(nome, precoBase, categoria, disponivel);
        produtos.incluir(p);
        persistir();
        return p;
    }

    public Produto alterarProduto(int id, String nome, double precoBase, String categoria, boolean disponivel)
            throws GerenciadorException {
        Produto p = produtos.consultar(id);
        p.atualizar(nome, precoBase, categoria, disponivel);
        produtos.alterar(p);
        persistir();
        return p;
    }

    public void excluirProduto(int id) throws GerenciadorException {
        produtos.consultar(id);
        for (Pedido p : pedidos.listar())
            if (p.contemProduto(id))
                throw new RegraNegocioException("Produto está em pedidos e não pode ser excluído. "
                        + "Marque como indisponível.");
        produtos.excluir(id);
        persistir();
    }

    public Produto consultarProduto(int id) throws RegistroNaoEncontradoException {
        return produtos.consultar(id);
    }

    public List<Produto> listarProdutos() { return produtos.listar(); }

    public List<Produto> listarProdutosDisponiveis() {
        List<Produto> r = new ArrayList<>();
        for (Produto p : produtos.listar()) if (p.isDisponivel()) r.add(p);
        return r;
    }

    public List<Produto> listarProdutosPorCategoria(String categoria) {
        List<Produto> r = new ArrayList<>();
        for (Produto p : produtos.listar())
            if (p.getCategoria().equalsIgnoreCase(categoria.trim())) r.add(p);
        return r;
    }


    public PedidoConsumoLocal criarPedidoConsumoLocal(int clienteId, int numeroMesa, double taxaServicoPercentual)
            throws GerenciadorException {
        verificarCliente(clienteId);
        PedidoConsumoLocal p = new PedidoConsumoLocal(clienteId, numeroMesa, taxaServicoPercentual);
        pedidos.incluir(p);
        persistir();
        return p;
    }

    public PedidoRetirada criarPedidoRetirada(int clienteId, LocalDateTime horaEstimada)
            throws GerenciadorException {
        verificarCliente(clienteId);
        PedidoRetirada p = new PedidoRetirada(clienteId, horaEstimada);
        pedidos.incluir(p);
        persistir();
        return p;
    }

    public PedidoEntrega criarPedidoEntrega(int clienteId, double taxaEntrega, String enderecoEntrega)
            throws GerenciadorException {
        verificarCliente(clienteId);
        PedidoEntrega p = new PedidoEntrega(clienteId, taxaEntrega, enderecoEntrega);
        pedidos.incluir(p);
        persistir();
        return p;
    }

    private void verificarCliente(int clienteId) throws RegistroNaoEncontradoException {
        if (clienteId > 0) clientes.consultar(clienteId);
    }


    public PedidoConsumoLocal alterarPedidoConsumoLocal(int pedidoId, int numeroMesa, double taxa)
            throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        if (!(p instanceof PedidoConsumoLocal))
            throw new DadoInvalidoException("O pedido " + pedidoId + " não é de consumo no local.");
        ((PedidoConsumoLocal) p).atualizar(numeroMesa, taxa);
        pedidos.alterar(p);
        persistir();
        return (PedidoConsumoLocal) p;
    }

    public PedidoRetirada alterarPedidoRetirada(int pedidoId, LocalDateTime horaEstimada)
            throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        if (!(p instanceof PedidoRetirada))
            throw new DadoInvalidoException("O pedido " + pedidoId + " não é de retirada.");
        ((PedidoRetirada) p).atualizar(horaEstimada);
        pedidos.alterar(p);
        persistir();
        return (PedidoRetirada) p;
    }

    public PedidoEntrega alterarPedidoEntrega(int pedidoId, double taxaEntrega, String enderecoEntrega)
            throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        if (!(p instanceof PedidoEntrega))
            throw new DadoInvalidoException("O pedido " + pedidoId + " não é de entrega.");
        ((PedidoEntrega) p).atualizar(taxaEntrega, enderecoEntrega);
        pedidos.alterar(p);
        persistir();
        return (PedidoEntrega) p;
    }


    public Pedido adicionarItem(int pedidoId, int produtoId, int quantidade) throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        Produto prod = produtos.consultar(produtoId);
        p.adicionarItem(prod, quantidade);
        pedidos.alterar(p);
        persistir();
        return p;
    }

    public Pedido alterarQuantidadeItem(int pedidoId, int produtoId, int novaQuantidade)
            throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        p.alterarQuantidadeItem(produtoId, novaQuantidade);
        pedidos.alterar(p);
        persistir();
        return p;
    }

    public Pedido removerItem(int pedidoId, int produtoId) throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        p.removerItem(produtoId);
        pedidos.alterar(p);
        persistir();
        return p;
    }

    public List<ItemPedido> listarItensDoPedido(int pedidoId) throws RegistroNaoEncontradoException {
        return pedidos.consultar(pedidoId).getItens();
    }


    public Pedido avancarStatus(int pedidoId) throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        p.avancarStatus();
        pedidos.alterar(p);
        persistir();
        return p;
    }

    public Pedido cancelarPedido(int pedidoId) throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        p.cancelar();
        pedidos.alterar(p);
        persistir();
        return p;
    }


    public void excluirPedido(int pedidoId) throws GerenciadorException {
        Pedido p = pedidos.consultar(pedidoId);
        if (!p.podeExcluir())
            throw new RegraNegocioException("Pedido " + p.getStatus() + " não pode ser excluído. Cancele ou finalize antes.");
        Comanda c = encontrarComandaDoPedido(pedidoId);
        if (c != null) {
            if (!c.isAberta())
                throw new RegraNegocioException("Pedido pertence à comanda " + c.getId()
                        + " (FECHADA). Reabra a comanda para excluir o pedido.");
            c.removerPedido(pedidoId);
            comandas.alterar(c);
        }
        pedidos.excluir(pedidoId);
        persistir();
    }

    public Pedido consultarPedido(int id) throws RegistroNaoEncontradoException {
        return pedidos.consultar(id);
    }

    public List<Pedido> listarPedidos() { return pedidos.listar(); }

    public List<Pedido> listarPedidosPorStatus(String status) throws DadoInvalidoException {
        if (!Pedido.STATUS_VALIDOS.contains(status))
            throw new DadoInvalidoException("Status inválido: " + status);
        List<Pedido> r = new ArrayList<>();
        for (Pedido p : pedidos.listar()) if (p.getStatus().equals(status)) r.add(p);
        return r;
    }

    public List<Pedido> listarPedidosDoCliente(int clienteId) {
        List<Pedido> r = new ArrayList<>();
        for (Pedido p : pedidos.listar()) if (p.getClienteId() == clienteId) r.add(p);
        return r;
    }


    public double calcularFaturamento() {
        double soma = 0;
        for (Pedido p : pedidos.listar())
            if (Pedido.STATUS_FINALIZADO.equals(p.getStatus())) soma += p.calcularTotal();
        return soma;
    }


    public Comanda abrirComanda(String identificacao) throws GerenciadorException {
        Comanda c = new Comanda(identificacao);
        comandas.incluir(c);
        persistir();
        return c;
    }

    public Comanda alterarComanda(int comandaId, String identificacao) throws GerenciadorException {
        Comanda c = comandas.consultar(comandaId);
        c.atualizar(identificacao);
        comandas.alterar(c);
        persistir();
        return c;
    }


    public void excluirComanda(int comandaId) throws GerenciadorException {
        Comanda c = comandas.consultar(comandaId);
        if (!c.getPedidoIds().isEmpty())
            throw new RegraNegocioException("Comanda possui pedidos. Remova-os antes de excluir.");
        comandas.excluir(comandaId);
        persistir();
    }

    public Comanda consultarComanda(int comandaId) throws RegistroNaoEncontradoException {
        return comandas.consultar(comandaId);
    }

    public List<Comanda> listarComandas() { return comandas.listar(); }

    public List<Comanda> listarComandasPorStatus(String status) throws DadoInvalidoException {
        if (!Comanda.STATUS_ABERTA.equals(status) && !Comanda.STATUS_FECHADA.equals(status))
            throw new DadoInvalidoException("Status de comanda inválido: " + status);
        List<Comanda> r = new ArrayList<>();
        for (Comanda c : comandas.listar()) if (c.getStatus().equals(status)) r.add(c);
        return r;
    }

    public List<Comanda> buscarComandasPorIdentificacao(String termo) {
        List<Comanda> r = new ArrayList<>();
        for (Comanda c : comandas.listar())
            if (c.getIdentificacao().toLowerCase().contains(termo.toLowerCase())) r.add(c);
        return r;
    }

    public Comanda adicionarPedidoNaComanda(int comandaId, int pedidoId) throws GerenciadorException {
        Comanda c = comandas.consultar(comandaId);
        Pedido p = pedidos.consultar(pedidoId);
        if (Pedido.STATUS_CANCELADO.equals(p.getStatus()))
            throw new RegraNegocioException("Pedido CANCELADO não pode entrar em comanda.");
        Comanda atual = encontrarComandaDoPedido(pedidoId);
        if (atual != null && atual.getId() != comandaId)
            throw new RegraNegocioException("Pedido já está na comanda " + atual.getId() + ".");
        c.adicionarPedido(pedidoId);
        comandas.alterar(c);
        persistir();
        return c;
    }

    public Comanda removerPedidoDaComanda(int comandaId, int pedidoId) throws GerenciadorException {
        Comanda c = comandas.consultar(comandaId);
        c.removerPedido(pedidoId);
        comandas.alterar(c);
        persistir();
        return c;
    }

    public List<Pedido> listarPedidosDaComanda(int comandaId) throws RegistroNaoEncontradoException {
        return pedidosDaComanda(comandas.consultar(comandaId));
    }

    /** Total da comanda: soma dos pedidos, ignorando os CANCELADOS. */
    public double calcularTotalComanda(int comandaId) throws RegistroNaoEncontradoException {
        return somarTotal(comandas.consultar(comandaId));
    }

    public Comanda buscarComandaDoPedido(int pedidoId) throws RegistroNaoEncontradoException {
        Comanda c = encontrarComandaDoPedido(pedidoId);
        if (c == null) throw new RegistroNaoEncontradoException("Comanda do pedido", pedidoId);
        return c;
    }

    /** Exige pelo menos 1 pedido e todos FINALIZADOS ou CANCELADOS. */
    public Comanda fecharComanda(int comandaId) throws GerenciadorException {
        Comanda c = comandas.consultar(comandaId);
        if (c.getPedidoIds().isEmpty())
            throw new RegraNegocioException("Comanda sem pedidos não pode ser fechada.");
        for (Pedido p : pedidosDaComanda(c))
            if (!p.isEncerrado())
                throw new RegraNegocioException("Pedido " + p.getId() + " ainda está " + p.getStatus()
                        + ". Finalize ou cancele antes de fechar a comanda.");
        c.fechar();
        comandas.alterar(c);
        persistir();
        return c;
    }

    public Comanda reabrirComanda(int comandaId) throws GerenciadorException {
        Comanda c = comandas.consultar(comandaId);
        c.reabrir();
        comandas.alterar(c);
        persistir();
        return c;
    }

    private Comanda encontrarComandaDoPedido(int pedidoId) {
        for (Comanda c : comandas.listar())
            if (c.contemPedido(pedidoId)) return c;
        return null;
    }

    private List<Pedido> pedidosDaComanda(Comanda c) {
        List<Pedido> r = new ArrayList<>();
        for (int id : c.getPedidoIds()) {
            try { r.add(pedidos.consultar(id)); } catch (RegistroNaoEncontradoException ignorada) { }
        }
        return r;
    }

    private double somarTotal(Comanda c) {
        double soma = 0;
        for (Pedido p : pedidosDaComanda(c))
            if (!Pedido.STATUS_CANCELADO.equals(p.getStatus())) soma += p.calcularTotal();
        return soma;
    }


    public String descreverPedido(Pedido p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Pedido #").append(p.getId()).append(" - ").append(p.getTipo())
                .append(" [").append(p.getStatus()).append("]\n");
        sb.append("Data: ").append(p.getDataHora().format(FORMATO)).append("\n");
        sb.append("Cliente: ").append(nomeCliente(p.getClienteId())).append("\n");
        sb.append(p.getDetalhes()).append("\n");
        sb.append("Itens:\n");
        if (p.getItens().isEmpty()) sb.append("  (nenhum)\n");
        for (ItemPedido i : p.getItens()) sb.append("  - ").append(descreverItem(i)).append("\n");
        sb.append("Subtotal: ").append(Pedido.formatarMoeda(p.calcularSubtotal())).append("\n");
        sb.append("Total: ").append(Pedido.formatarMoeda(p.calcularTotal()));
        return sb.toString();
    }

    public String descreverComanda(Comanda c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Comanda #").append(c.getId()).append(" - ").append(c.getIdentificacao())
                .append(" [").append(c.getStatus()).append("]\n");
        sb.append("Aberta em: ").append(c.getDataAbertura().format(FORMATO)).append("\n");
        if (c.getDataFechamento() != null)
            sb.append("Fechada em: ").append(c.getDataFechamento().format(FORMATO)).append("\n");
        sb.append("Pedidos:\n");
        List<Pedido> lista = pedidosDaComanda(c);
        if (lista.isEmpty()) sb.append("  (nenhum)\n");
        for (Pedido p : lista) {
            sb.append("  - #").append(p.getId()).append(" ").append(p.getTipo())
                    .append(" [").append(p.getStatus()).append("] ")
                    .append(Pedido.formatarMoeda(p.calcularTotal())).append("\n");
        }
        sb.append("Total da comanda: ").append(Pedido.formatarMoeda(somarTotal(c)));
        return sb.toString();
    }

    public String descreverItem(ItemPedido i) {
        String nome = "(removido)";
        try { nome = produtos.consultar(i.getProdutoId()).getNome(); } catch (GerenciadorException ignorada) { }
        return i.getQuantidade() + "x " + nome + " @ " + Pedido.formatarMoeda(i.getPrecoUnitario())
                + " = " + Pedido.formatarMoeda(i.getSubtotal());
    }

    private String nomeCliente(int clienteId) {
        if (clienteId <= 0) return "(sem cliente)";
        try { return clientes.consultar(clienteId).getNome(); } catch (GerenciadorException e) { return "(removido)"; }
    }
}