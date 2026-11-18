// RegistroNaoEncontradoException.java
package br.edu.api98food;

public class RegistroNaoEncontradoException extends GerenciadorException {
    private static final long serialVersionUID = 1L;
    public RegistroNaoEncontradoException(String entidade, int id) {
        super(entidade + " com ID " + id + " não encontrado(a).");
    }
}