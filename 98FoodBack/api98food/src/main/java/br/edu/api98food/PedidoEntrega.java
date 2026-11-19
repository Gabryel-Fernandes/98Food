package br.edu.api98food;

public class PedidoEntrega extends Pedido {
    private static final long serialVersionUID = 1L;

    private double taxaEntrega;
    private String enderecoEntrega;

    public PedidoEntrega(int clienteId, double taxaEntrega, String enderecoEntrega)
            throws DadoInvalidoException {
        super(clienteId);
        validar(taxaEntrega, enderecoEntrega);
        this.taxaEntrega = taxaEntrega;
        this.enderecoEntrega = enderecoEntrega.trim();
        gerarId();
    }

    private static void validar(double taxa, String endereco) throws DadoInvalidoException {
        if (taxa < 0)
            throw new DadoInvalidoException("Taxa de entrega não pode ser negativa.");
        if (endereco == null || endereco.trim().isEmpty())
            throw new DadoInvalidoException("Endereço de entrega é obrigatório.");
    }

    public void atualizar(double taxaEntrega, String enderecoEntrega) throws GerenciadorException {
        garantirNaoEncerrado();
        validar(taxaEntrega, enderecoEntrega);
        this.taxaEntrega = taxaEntrega;
        this.enderecoEntrega = enderecoEntrega.trim();
    }

    @Override protected boolean exigeCliente() { return true; }
    @Override public String getTipo() { return "Entrega"; }

    @Override
    public String getDetalhes() {
        return "Entrega em " + enderecoEntrega + " (taxa " + formatarMoeda(taxaEntrega) + ")";
    }

    @Override
    public double calcularTotal() {
        return calcularSubtotal() + taxaEntrega;
    }

    public double getTaxaEntrega() { return taxaEntrega; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
}

