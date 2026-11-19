package br.edu.api98food;

public class GerenciadorException extends Exception {
    private static final long serialVersionUID = 1L;
    public GerenciadorException(String msg) { super(msg); }
    public GerenciadorException(String msg, Throwable causa) { super(msg, causa); }
}