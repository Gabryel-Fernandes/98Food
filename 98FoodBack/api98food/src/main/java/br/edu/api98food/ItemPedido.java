package br.edu.api98food;

import java.io.Serializable;

public class ItemPedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int produtoId;
    private int quantidade;
    private final double precoUnitario; // preço congelado no momento da venda

    public ItemPedido(int produtoId, int quantidade, double precoUnitario) throws DadoInvalidoException {
        validarQuantidade(quantidade);
        if (precoUnitario < 0)
            throw new DadoInvalidoException("Preço unitário não pode ser negativo.");
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public static void validarQuantidade(int quantidade) throws DadoInvalidoException {
        if (quantidade <= 0)
            throw new DadoInvalidoException("Quantidade deve ser maior que zero.");
    }

    public void setQuantidade(int quantidade) throws DadoInvalidoException {
        validarQuantidade(quantidade);
        this.quantidade = quantidade;
    }

    public double getSubtotal() { return quantidade * precoUnitario; }

    public int getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }

    @Override
    public String toString() {
        return quantidade + "x produto " + produtoId + " @ " + Pedido.formatarMoeda(precoUnitario)
                + " = " + Pedido.formatarMoeda(getSubtotal());
    }
}