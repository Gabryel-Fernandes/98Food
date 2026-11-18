package br.edu.api98food;

import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        GerenciadorPedidos ger;
        try {
            ger = new GerenciadorPedidos("dados");
        } catch (PersistenciaException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erro ao abrir dados", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] opcoes = {"Incluir cliente", "Listar clientes", "Listar produtos", "Listar pedidos", "Sair"};

        while (true) {
            int escolha = JOptionPane.showOptionDialog(null, "Gerenciador de Pedidos", "Menu",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);

            if (escolha == -1 || escolha == 4) break; // fechou a janela ou "Sair"

            try {
                switch (escolha) {
                    case 0:
                        String nome = JOptionPane.showInputDialog("Nome:");
                        if (nome == null) break;
                        String tel = JOptionPane.showInputDialog("Telefone (com DDD):");
                        if (tel == null) break;
                        String end = JOptionPane.showInputDialog("Endereço (opcional):");
                        Cliente c = ger.incluirCliente(nome, tel, end);
                        JOptionPane.showMessageDialog(null, "Cadastrado:\n" + c);
                        break;
                    case 1:
                        mostrarLista("Clientes", ger.listarClientes());
                        break;
                    case 2:
                        mostrarLista("Produtos", ger.listarProdutos());
                        break;
                    case 3:
                        mostrarLista("Pedidos", ger.listarPedidos());
                        break;
                }
            } catch (GerenciadorException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void mostrarLista(String titulo, java.util.List<?> lista) {
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nenhum registro.", titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : lista) sb.append(o).append("\n");
        JOptionPane.showMessageDialog(null, sb.toString(), titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}