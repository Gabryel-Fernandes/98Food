package br.edu.api98food;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class TesteFuncional {
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

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("pedidos_teste");
        String pasta = tmp.toString();
        GerenciadorPedidos g = new GerenciadorPedidos(pasta);


        Cliente c1 = g.incluirCliente("Ana Souza", "(62) 99999-1111", "Rua A, 10");
        Cliente c2 = g.incluirCliente("Bruno Lima", "6233334444", "");
        verificar("IDs de cliente sequenciais", c2.getId() == c1.getId() + 1);
        verificar("Telefone guardado só com dígitos", c1.getTelefone().equals("62999991111"));
        verificar("Consulta de cliente", g.consultarCliente(c1.getId()).getNome().equals("Ana Souza"));
        g.alterarCliente(c1.getId(), "Ana S. Souza", "62999991111", "Rua A, 11");
        verificar("Alteração de cliente", g.consultarCliente(c1.getId()).getEndereco().equals("Rua A, 11"));
        verificar("Lista de clientes", g.listarClientes().size() == 2);
        verificar("Busca de cliente por nome", g.buscarClientesPorNome("bruno").size() == 1);
        esperaExcecao("Cliente sem nome", DadoInvalidoException.class,
                () -> g.incluirCliente("  ", "62999991111", ""));
        esperaExcecao("Telefone inválido", DadoInvalidoException.class,
                () -> g.incluirCliente("X", "123", ""));
        esperaExcecao("Cliente inexistente", RegistroNaoEncontradoException.class,
                () -> g.consultarCliente(999));


        Produto pizza = g.incluirProduto("Pizza Calabresa", 20.00, "Pizza", true);
        Produto refri = g.incluirProduto("Refrigerante", 10.00, "Bebida", true);
        Produto suco = g.incluirProduto("Suco", 8.00, "Bebida", false);
        Produto burger = g.incluirProduto("Hambúrguer", 30.00, "Lanche", true);
        verificar("IDs de produto sequenciais", refri.getId() == pizza.getId() + 1);
        verificar("Lista de produtos", g.listarProdutos().size() == 4);
        verificar("Produtos disponíveis", g.listarProdutosDisponiveis().size() == 3);
        verificar("Produtos por categoria", g.listarProdutosPorCategoria("bebida").size() == 2);
        g.alterarProduto(suco.getId(), "Suco de Laranja", 9.00, "Bebida", false);
        verificar("Alteração de produto", g.consultarProduto(suco.getId()).getNome().equals("Suco de Laranja"));
        esperaExcecao("Preço inválido", DadoInvalidoException.class,
                () -> g.incluirProduto("Grátis", 0, "Outros", true));


        PedidoConsumoLocal pl = g.criarPedidoConsumoLocal(0, 5, 10.0);
        PedidoRetirada pr = g.criarPedidoRetirada(c1.getId(), LocalDateTime.now().plusMinutes(30));
        PedidoEntrega pe = g.criarPedidoEntrega(c2.getId(), 6.0, "Rua B, 20");
        verificar("IDs de pedido sequenciais (compartilhados entre tipos)",
                pr.getId() == pl.getId() + 1 && pe.getId() == pr.getId() + 1);
        verificar("Pedido nasce ABERTO", pl.getStatus().equals(Pedido.STATUS_ABERTO));

        esperaExcecao("Entrega sem cliente", DadoInvalidoException.class,
                () -> g.criarPedidoEntrega(0, 5.0, "Rua X"));
        esperaExcecao("Mesa inválida", DadoInvalidoException.class,
                () -> g.criarPedidoConsumoLocal(0, 0, 10.0));
        esperaExcecao("Taxa de serviço > 100", DadoInvalidoException.class,
                () -> g.criarPedidoConsumoLocal(0, 3, 150.0));
        esperaExcecao("Retirada no passado", DadoInvalidoException.class,
                () -> g.criarPedidoRetirada(c1.getId(), LocalDateTime.now().minusHours(1)));
        esperaExcecao("Entrega sem endereço", DadoInvalidoException.class,
                () -> g.criarPedidoEntrega(c1.getId(), 5.0, " "));
        esperaExcecao("Pedido com cliente inexistente", RegistroNaoEncontradoException.class,
                () -> g.criarPedidoRetirada(999, LocalDateTime.now().plusHours(1)));

        PedidoRetirada pv = g.criarPedidoRetirada(c1.getId(), LocalDateTime.now().plusMinutes(10));
        verificar("Pedidos inválidos não consomem ID", pv.getId() == pe.getId() + 1);


        g.adicionarItem(pl.getId(), pizza.getId(), 1);
        g.adicionarItem(pl.getId(), pizza.getId(), 1);
        verificar("Item repetido soma a quantidade",
                g.listarItensDoPedido(pl.getId()).size() == 1
                        && g.listarItensDoPedido(pl.getId()).get(0).getQuantidade() == 2);
        g.adicionarItem(pl.getId(), refri.getId(), 1);
        verificar("Total consumo local (50 + 10%)", igual(pl.calcularTotal(), 55.0));

        g.alterarQuantidadeItem(pl.getId(), refri.getId(), 2);
        verificar("Alterar quantidade do item", igual(pl.calcularTotal(), 66.0));
        g.removerItem(pl.getId(), refri.getId());
        verificar("Remover item", igual(pl.calcularTotal(), 44.0));
        g.adicionarItem(pl.getId(), refri.getId(), 1);
        verificar("Total consumo local após reajustes", igual(pl.calcularTotal(), 55.0));

        g.adicionarItem(pr.getId(), refri.getId(), 2);
        verificar("Total retirada (sem taxa)", igual(pr.calcularTotal(), 20.0));
        g.adicionarItem(pe.getId(), burger.getId(), 1);
        verificar("Total entrega (30 + taxa 6)", igual(pe.calcularTotal(), 36.0));

        g.alterarProduto(burger.getId(), "Hambúrguer", 35.0, "Lanche", true);
        verificar("Preço do item é congelado na venda", igual(pe.calcularTotal(), 36.0));

        esperaExcecao("Produto indisponível", RegraNegocioException.class,
                () -> g.adicionarItem(pl.getId(), suco.getId(), 1));
        esperaExcecao("Quantidade zero", DadoInvalidoException.class,
                () -> g.adicionarItem(pl.getId(), pizza.getId(), 0));
        esperaExcecao("Remover item que não existe", RegistroNaoEncontradoException.class,
                () -> g.removerItem(pl.getId(), burger.getId()));


        g.alterarPedidoEntrega(pe.getId(), 8.0, "Rua C, 30");
        verificar("Alterar dados da entrega", igual(pe.calcularTotal(), 38.0)
                && pe.getEnderecoEntrega().equals("Rua C, 30"));
        esperaExcecao("Alterar com tipo errado", DadoInvalidoException.class,
                () -> g.alterarPedidoConsumoLocal(pe.getId(), 3, 10));


        esperaExcecao("Avançar pedido sem itens", RegraNegocioException.class,
                () -> g.avancarStatus(pv.getId()));
        g.excluirPedido(pv.getId());
        esperaExcecao("Pedido excluído some", RegistroNaoEncontradoException.class,
                () -> g.consultarPedido(pv.getId()));

        g.avancarStatus(pl.getId());
        verificar("ABERTO -> EM_PREPARO", pl.getStatus().equals(Pedido.STATUS_EM_PREPARO));
        esperaExcecao("Alterar itens fora de ABERTO", RegraNegocioException.class,
                () -> g.adicionarItem(pl.getId(), pizza.getId(), 1));
        esperaExcecao("Excluir pedido em preparo", RegraNegocioException.class,
                () -> g.excluirPedido(pl.getId()));
        g.avancarStatus(pl.getId());
        g.avancarStatus(pl.getId());
        verificar("Fluxo até FINALIZADO", pl.getStatus().equals(Pedido.STATUS_FINALIZADO));
        esperaExcecao("Avançar pedido finalizado", RegraNegocioException.class,
                () -> g.avancarStatus(pl.getId()));
        esperaExcecao("Cancelar pedido finalizado", RegraNegocioException.class,
                () -> g.cancelarPedido(pl.getId()));

        g.cancelarPedido(pr.getId());
        verificar("Cancelamento", pr.getStatus().equals(Pedido.STATUS_CANCELADO));
        esperaExcecao("Alterar pedido cancelado", RegraNegocioException.class,
                () -> g.alterarPedidoRetirada(pr.getId(), LocalDateTime.now().plusHours(2)));

        verificar("Listar por status", g.listarPedidosPorStatus(Pedido.STATUS_ABERTO).size() == 1);
        verificar("Listar por cliente", g.listarPedidosDoCliente(c2.getId()).size() == 1);
        verificar("Faturamento (só finalizados)", igual(g.calcularFaturamento(), 55.0));
        verificar("Descrição para GUI contém total", g.descreverPedido(pl).contains("R$ 55,00"));


        esperaExcecao("Excluir cliente com pedido", RegraNegocioException.class,
                () -> g.excluirCliente(c2.getId()));
        esperaExcecao("Excluir produto em pedido", RegraNegocioException.class,
                () -> g.excluirProduto(pizza.getId()));
        g.excluirPedido(pe.getId());
        g.excluirCliente(c2.getId());
        esperaExcecao("Cliente excluído some", RegistroNaoEncontradoException.class,
                () -> g.consultarCliente(c2.getId()));


        GerenciadorPedidos g2 = new GerenciadorPedidos(pasta); // reabre lendo dos arquivos
        verificar("Persistência: clientes", g2.listarClientes().size() == 1);
        verificar("Persistência: produtos", g2.listarProdutos().size() == 4);
        verificar("Persistência: pedidos", g2.listarPedidos().size() == 2);
        Pedido lido = g2.consultarPedido(pl.getId());
        verificar("Persistência mantém o tipo (polimorfismo)", lido instanceof PedidoConsumoLocal);
        verificar("Persistência mantém itens e total", igual(lido.calcularTotal(), 55.0));
        Cliente c3 = g2.incluirCliente("Carla", "62988887777", "");
        verificar("ID de cliente continua após reabrir", c3.getId() == c2.getId() + 1);
        PedidoEntrega novo = g2.criarPedidoEntrega(c3.getId(), 5.0, "Rua D");
        verificar("ID de pedido continua após reabrir", novo.getId() == pv.getId() + 1);

        System.out.println("\nResultado: " + passou + " passaram, " + falhou + " falharam.");
        if (falhou > 0) System.exit(1);
    }
}