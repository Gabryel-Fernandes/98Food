package br.edu.api98food;

public class PersistenciaException extends GerenciadorException {
    private static final long serialVersionUID = 1L;
    public PersistenciaException(String msg, Throwable causa) { super(msg, causa); }
}