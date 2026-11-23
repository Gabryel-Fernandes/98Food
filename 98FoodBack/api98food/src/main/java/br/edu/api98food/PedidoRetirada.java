package br.edu.api98food;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PedidoRetirada extends Pedido {
    private static final long serialVersionUID = 1L;

    private LocalDateTime horaEstimadaRetirada;

    public PedidoRetirada(int clienteId, LocalDateTime horaEstimadaRetirada) throws DadoInvalidoException {
        super(clienteId);
        validar(horaEstimadaRetirada, getDataHora());
        this.horaEstimadaRetirada = horaEstimadaRetirada;
        gerarId();
    }

    private static void validar(LocalDateTime hora, LocalDateTime referencia) throws DadoInvalidoException {
        if (hora == null)
            throw new DadoInvalidoException("Hora estimada de retirada é obrigatória.");
        if (hora.isBefore(referencia))
            throw new DadoInvalidoException("Hora de retirada não pode ser anterior ao horário do pedido.");
    }

    public void atualizar(LocalDateTime horaEstimadaRetirada) throws GerenciadorException {
        garantirNaoEncerrado();
        validar(horaEstimadaRetirada, getDataHora());
        this.horaEstimadaRetirada = horaEstimadaRetirada;
    }

    @Override protected boolean exigeCliente() { return true; }
    @Override public String getTipo() { return "Retirada"; }

    @Override
    public String getDetalhes() {
        return "Retirada às " + horaEstimadaRetirada.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    @Override
    public double calcularTotal() {
        return calcularSubtotal();
    }

    public LocalDateTime getHoraEstimadaRetirada() { return horaEstimadaRetirada; }
}