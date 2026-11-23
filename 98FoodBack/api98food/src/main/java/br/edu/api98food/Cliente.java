package br.edu.api98food;

public class Cliente implements Identificavel {
    private static final long serialVersionUID = 1L;
    private static int proximoId = 1;

    private final int id;
    private String nome;
    private String telefone;
    private String endereco;

    public Cliente(String nome, String telefone, String endereco) throws DadoInvalidoException {
        validar(nome, telefone);
        this.id = proximoId++;
        this.nome = nome.trim();
        this.telefone = somenteDigitos(telefone);
        this.endereco = endereco == null ? "" : endereco.trim();
    }

    public static void sincronizarContador(int maiorId) {
        if (maiorId >= proximoId) proximoId = maiorId + 1;
    }

    public static String somenteDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static void validar(String nome, String telefone) throws DadoInvalidoException {
        if (nome == null || nome.trim().isEmpty())
            throw new DadoInvalidoException("Nome do cliente é obrigatório.");
        int n = somenteDigitos(telefone).length();
        if (n != 10 && n != 11)
            throw new DadoInvalidoException("Telefone deve ter 10 ou 11 dígitos (com DDD).");
    }

    public void atualizar(String nome, String telefone, String endereco) throws DadoInvalidoException {
        validar(nome, telefone);
        this.nome = nome.trim();
        this.telefone = somenteDigitos(telefone);
        this.endereco = endereco == null ? "" : endereco.trim();
    }

    @Override public int getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " | Tel: " + telefone
                + (endereco.isEmpty() ? "" : " | " + endereco);
    }
}