// DadoInvalidoException.java
package br.edu.api98food;

public class DadoInvalidoException extends GerenciadorException {
    private static final long serialVersionUID = 1L;
    public DadoInvalidoException(String msg) { super(msg); }
}