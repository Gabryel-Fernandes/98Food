package br.edu.api98food;

import java.nio.file.Files;
import java.time.LocalDateTime;

public class TesteComanda {
    private static int passou = 0, falhou = 0;

    interface Acao { void executar() throws Exception; }

    private static void verificar(String nome, boolean condicao) {
        if (condicao) { passou++; System.out.println("[OK]    " + nome); }
        else { falhou++; System.out.println("[FALHA] " + nome); }
    }

    private static boolean igual(double a, double b) { return Math.abs(a - b) < 0.001; }

    private static void esperaExcecao(String nome, Class<? extends Exception> tipo, Acao acao) {
        try {
            acao.executar();
            falhou++;
            System.out.println("[FALHA] " + nome + " (nenhuma exceção lançada)");
        } catch (Exception e) {
            if (tipo.isInstance(e)) { passou++; System.out.println("[OK]    " + nome); }
            else { falhou++; System.out.println("[FALHA] " + nome + " (lançou " + e.getClass().getSimpleName() + ")"); }
        }
    }

    private static void finalizar(GerenciadorPedidos g, int pedidoId) throws Exception {
        for (int i = 0; i < 3; i++) g.avancarStatus(pedidoId); // ABERTO -> ... -> FINALIZADO
    }

    public static void main(String[] args) throws Exception {
        String pasta = Files.createTempDirectory("comandas_teste").toString();
        GerenciadorPedidos g = new GerenciadorPedidos(pasta);


        Cliente cli = g.incluirCliente("Ana Souza", "62999991111", "Rua A");
        Produto pizza = g.incluirProduto("Pizza", 20.00, "Pizza", true);
        Produto refri = g.incluirProduto("Refrigerante", 10.00, "Bebida", true);

        PedidoConsumoLocal p1 = g.criarPedidoConsumoLocal(0, 5, 10.0);
        g.adicionarItem(p1.getId(), pizza.getId(), 2);            // 40 + 10% = 44
        PedidoConsumoLocal p2 = g.criarPedidoConsumoLocal(0, 5, 10.0);
        g.adicionarItem(p2.getId(), refri.getId(), 3);            // 30 + 10% = 33
        PedidoEntrega p3 = g.criarPedidoEntrega(cli.getId(), 6.0, "Rua B");
        g.adicionarItem(p3.getId(), pizza.getId(), 1);            // 20 + 6 = 26
        PedidoRetirada p4 = g.criarPedidoRetirada(cli.getId(), LocalDateTime.now().plusMinutes(30));
        g.adicionarItem(p4.getId(), refri.getId(), 1);
        g.cancelarPedido(p4.getId());                             // CANCELADO
        PedidoConsumoLocal p5 = g.criarPedidoConsumoLocal(0, 6, 10.0);
        g.adicionarItem(p5.getId(), pizza.getId(), 1);


        Comanda cm1 = g.abrirComanda("Mesa 5");
        Comanda cm2 = g.abrirComanda("Balcão");
        verificar("IDs de comanda sequenciais", cm2.getId() == cm1.getId() + 1);
        verificar("Comanda nasce ABERTA", cm1.isAberta());
        verificar("Consulta de comanda", g.consultarComanda(cm1.getId()).getIdentificacao().equals("Mesa 5"));
        verificar("Lista de comandas", g.listarComandas().size() == 2);
        verificar("Busca por identificação", g.buscarComandasPorIdentificacao("balc").size() == 1);
        esperaExcecao("Comanda sem identificação", DadoInvalidoException.class,
                () -> g.abrirComanda("  "));
        esperaExcecao("Comanda inexistente", RegistroNaoEncontradoException.class,
                () -> g.consultarComanda(999));


        g.adicionarPedidoNaComanda(cm1.getId(), p1.getId());
        g.adicionarPedidoNaComanda(cm1.getId(), p2.getId());
        verificar("Pedidos da comanda", g.listarPedidosDaComanda(cm1.getId()).size() == 2);
        verificar("Total da comanda (44 + 33)", igual(g.calcularTotalComanda(cm1.getId()), 77.0));
        verificar("Achar comanda pelo pedido", g.buscarComandaDoPedido(p1.getId()).getId() == cm1.getId());
        esperaExcecao("Pedido sem comanda", RegistroNaoEncontradoException.class,
                () -> g.buscarComandaDoPedido(p3.getId()));
        esperaExcecao("Pedido em duas comandas", RegraNegocioException.class,
                () -> g.adicionarPedidoNaComanda(cm2.getId(), p1.getId()));
        esperaExcecao("Pedido repetido na mesma comanda", RegraNegocioException.class,
                () -> g.adicionarPedidoNaComanda(cm1.getId(), p1.getId()));
        esperaExcecao("Pedido cancelado na comanda", RegraNegocioException.class,
                () -> g.adicionarPedidoNaComanda(cm2.getId(), p4.getId()));

        g.adicionarPedidoNaComanda(cm2.getId(), p3.getId());
        verificar("Total da outra comanda", igual(g.calcularTotalComanda(cm2.getId()), 26.0));


        g.alterarComanda(cm2.getId(), "Balcão 2");
        verificar("Alteração de comanda", g.consultarComanda(cm2.getId()).getIdentificacao().equals("Balcão 2"));
        esperaExcecao("Alterar para identificação vazia", DadoInvalidoException.class,
                () -> g.alterarComanda(cm2.getId(), ""));


        PedidoConsumoLocal p6 = g.criarPedidoConsumoLocal(0, 7, 0.0);
        g.adicionarItem(p6.getId(), refri.getId(), 1);            // 10
        g.adicionarPedidoNaComanda(cm2.getId(), p6.getId());
        verificar("Total com pedido ativo (26 + 10)", igual(g.calcularTotalComanda(cm2.getId()), 36.0));
        g.cancelarPedido(p6.getId());
        verificar("Total ignora pedido cancelado", igual(g.calcularTotalComanda(cm2.getId()), 26.0));


        Comanda vazia = g.abrirComanda("Vazia");
        esperaExcecao("Fechar comanda sem pedidos", RegraNegocioException.class,
                () -> g.fecharComanda(vazia.getId()));
        g.excluirComanda(vazia.getId());
        esperaExcecao("Comanda vazia excluída some", RegistroNaoEncontradoException.class,
                () -> g.consultarComanda(vazia.getId()));

        esperaExcecao("Fechar com pedido em andamento", RegraNegocioException.class,
                () -> g.fecharComanda(cm1.getId()));
        finalizar(g, p1.getId());
        finalizar(g, p2.getId());
        g.fecharComanda(cm1.getId());
        verificar("Comanda FECHADA", !g.consultarComanda(cm1.getId()).isAberta());
        verificar("Data de fechamento preenchida", g.consultarComanda(cm1.getId()).getDataFechamento() != null);

        esperaExcecao("Adicionar pedido em comanda fechada", RegraNegocioException.class,
                () -> g.adicionarPedidoNaComanda(cm1.getId(), p5.getId()));
        esperaExcecao("Remover pedido de comanda fechada", RegraNegocioException.class,
                () -> g.removerPedidoDaComanda(cm1.getId(), p1.getId()));
        esperaExcecao("Alterar comanda fechada", RegraNegocioException.class,
                () -> g.alterarComanda(cm1.getId(), "Outro nome"));
        esperaExcecao("Excluir pedido de comanda fechada", RegraNegocioException.class,
                () -> g.excluirPedido(p1.getId()));
        esperaExcecao("Excluir comanda com pedidos", RegraNegocioException.class,
                () -> g.excluirComanda(cm1.getId()));


        g.reabrirComanda(cm1.getId());
        verificar("Comanda reaberta", g.consultarComanda(cm1.getId()).isAberta());
        esperaExcecao("Reabrir comanda já aberta", RegraNegocioException.class,
                () -> g.reabrirComanda(cm1.getId()));
        g.removerPedidoDaComanda(cm1.getId(), p2.getId());
        verificar("Remover pedido da comanda", igual(g.calcularTotalComanda(cm1.getId()), 44.0));
        esperaExcecao("Remover pedido que não está na comanda", RegistroNaoEncontradoException.class,
                () -> g.removerPedidoDaComanda(cm1.getId(), p2.getId()));

        g.excluirPedido(p1.getId());
        verificar("Excluir pedido tira da comanda aberta",
                g.consultarComanda(cm1.getId()).getPedidoIds().isEmpty());
        g.adicionarPedidoNaComanda(cm1.getId(), p2.getId());
        verificar("Descrição da comanda contém total", g.descreverComanda(cm1).contains("R$ 33,00"));


        verificar("Comandas ABERTAS", g.listarComandasPorStatus(Comanda.STATUS_ABERTA).size() == 2);
        finalizar(g, p3.getId());
        g.fecharComanda(cm2.getId());
        verificar("Comandas FECHADAS", g.listarComandasPorStatus(Comanda.STATUS_FECHADA).size() == 1);
        esperaExcecao("Status de comanda inválido", DadoInvalidoException.class,
                () -> g.listarComandasPorStatus("QUALQUER"));


        GerenciadorPedidos g2 = new GerenciadorPedidos(pasta);
        verificar("Persistência: comandas", g2.listarComandas().size() == 2);
        verificar("Persistência: status da comanda", !g2.consultarComanda(cm2.getId()).isAberta());
        verificar("Persistência: pedidos da comanda", g2.listarPedidosDaComanda(cm2.getId()).size() == 2);
        verificar("Persistência: total da comanda", igual(g2.calcularTotalComanda(cm2.getId()), 26.0));
        verificar("Persistência: pedido por comanda", g2.buscarComandaDoPedido(p3.getId()).getId() == cm2.getId());
        Comanda nova = g2.abrirComanda("Mesa 9");
        verificar("Nova comanda após reabrir tem ID maior", nova.getId() > cm2.getId());

        System.out.println("\nResultado: " + passou + " passaram, " + falhou + " falharam.");
        if (falhou > 0) System.exit(1);
    }
}