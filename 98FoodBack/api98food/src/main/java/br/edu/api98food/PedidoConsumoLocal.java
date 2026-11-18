package br.edu.api98food;

public class PedidoConsumoLocal extends Pedido {
    private static final long serialVersionUID = 1L;

    private int numeroMesa;
    private double taxaServicoPercentual;

    public PedidoConsumoLocal(int clienteId, int numeroMesa, double taxaServicoPercentual)
            throws DadoInvalidoException {
        super(clienteId);
        validar(numeroMesa, taxaServicoPercentual);
        this.numeroMesa = numeroMesa;
        this.taxaServicoPercentual = taxaServicoPercentual;
        gerarId();
    }

    private static void validar(int numeroMesa, double taxa) throws DadoInvalidoException {
        if (numeroMesa <= 0)
            throw new DadoInvalidoException("Número da mesa deve ser maior que zero.");
        if (taxa < 0 || taxa > 100)
            throw new DadoInvalidoException("Taxa de serviço deve estar entre 0 e 100%.");
    }

    public void atualizar(int numeroMesa, double taxaServicoPercentual) throws GerenciadorException {
        garantirNaoEncerrado();
        validar(numeroMesa, taxaServicoPercentual);
        this.numeroMesa = numeroMesa;
        this.taxaServicoPercentual = taxaServicoPercentual;
    }

    @Override protected boolean exigeCliente() { return false; }
    @Override public String getTipo() { return "Consumo no local"; }

    @Override
    public String getDetalhes() {
        return "Mesa " + numeroMesa + " | Taxa de serviço: " + taxaServicoPercentual + "%";
    }

    @Override
    public double calcularTotal() {
        return calcularSubtotal() * (1 + taxaServicoPercentual / 100.0);
    }

    public int getNumeroMesa() { return numeroMesa; }
    public double getTaxaServicoPercentual() { return taxaServicoPercentual; }
}