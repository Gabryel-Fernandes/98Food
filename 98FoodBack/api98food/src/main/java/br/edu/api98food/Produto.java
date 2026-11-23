package br.edu.api98food;

public class Produto implements Identificavel {
    private static final long serialVersionUID = 1L;
    private static int proximoId = 1;

    private final int id;
    private String nome;
    private double precoBase;
    private String categoria;
    private boolean disponivel;

    public Produto(String nome, double precoBase, String categoria, boolean disponivel)
            throws DadoInvalidoException {
        validar(nome, precoBase, categoria);
        this.id = proximoId++;
        this.nome = nome.trim();
        this.precoBase = precoBase;
        this.categoria = categoria.trim();
        this.disponivel = disponivel;
    }

    public static void sincronizarContador(int maiorId) {
        if (maiorId >= proximoId) proximoId = maiorId + 1;
    }

    private static void validar(String nome, double precoBase, String categoria) throws DadoInvalidoException {
        if (nome == null || nome.trim().isEmpty())
            throw new DadoInvalidoException("Nome do produto é obrigatório.");
        if (precoBase <= 0)
            throw new DadoInvalidoException("Preço base deve ser maior que zero.");
        if (categoria == null || categoria.trim().isEmpty())
            throw new DadoInvalidoException("Categoria é obrigatória.");
    }

    public void atualizar(String nome, double precoBase, String categoria, boolean disponivel)
            throws DadoInvalidoException {
        validar(nome, precoBase, categoria);
        this.nome = nome.trim();
        this.precoBase = precoBase;
        this.categoria = categoria.trim();
        this.disponivel = disponivel;
    }

    @Override public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPrecoBase() { return precoBase; }
    public String getCategoria() { return categoria; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " | " + categoria + " | " + Pedido.formatarMoeda(precoBase)
                + (disponivel ? " | Disponível" : " | Indisponível");
    }
}