package br.edu.api98food;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/comandas")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ComandaController {

    private final GerenciadorPedidos gerenciador;

    public ComandaController(GerenciadorPedidos gerenciador) {
        this.gerenciador = gerenciador;
    }

    public static class ItemRequest {
        public int id;
        public String nome;
        public double preco;
        public String categoria;
    }

    public static class PedidoRequest {
        public String cliente;
        public String telefone;
        public String tipoPedido;
        public String endereco;
        public List<ItemRequest> itens;
        public double total;
    }

    public static class ClienteRequest {
        public String nome;
        public String telefone;
        public String endereco;
    }

    public static class ItemResponse {
        public String nome;
        public int quantidade;
        public double subtotal;

        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
        public double getSubtotal() { return subtotal; }
    }

    public static class PedidoResponse {
        public int id;
        public String cliente;
        public String tipo;
        public String status;
        public double total;
        public String detalhes;
        public List<ItemResponse> itens;

        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public String getTipo() { return tipo; }
        public String getStatus() { return status; }
        public double getTotal() { return total; }
        public String getDetalhes() { return detalhes; }
        public List<ItemResponse> getItens() { return itens; }
    }

    @GetMapping
    public List<Comanda> listarTodas() {
        return gerenciador.listarComandas();
    }

    @GetMapping("/pedidos")
    public List<PedidoResponse> listarPedidosDetalhados() {
        List<PedidoResponse> resposta = new ArrayList<>();

        for (Pedido p : gerenciador.listarPedidos()) {
            PedidoResponse dto = new PedidoResponse();
            dto.id = p.getId();
            dto.tipo = p.getTipo();
            dto.status = p.getStatus();
            dto.total = p.calcularTotal();
            dto.detalhes = p.getDetalhes();

            String nomeCliente = "Cliente Balcão";
            if (p.getClienteId() > 0) {
                try {
                    nomeCliente = gerenciador.consultarCliente(p.getClienteId()).getNome();
                } catch (Exception ignored) {
                }
            }
            dto.cliente = nomeCliente;

            List<ItemResponse> listaItens = new ArrayList<>();
            if (p.getItens() != null) {
                for (ItemPedido item : p.getItens()) {
                    ItemResponse itemDto = new ItemResponse();
                    itemDto.quantidade = item.getQuantidade();
                    itemDto.subtotal = item.getSubtotal();
                    try {
                        itemDto.nome = gerenciador.consultarProduto(item.getProdutoId()).getNome();
                    } catch (Exception e) {
                        itemDto.nome = "Produto #" + item.getProdutoId();
                    }
                    listaItens.add(itemDto);
                }
            }
            dto.itens = listaItens;

            resposta.add(dto);
        }
        return resposta;
    }

    @PostMapping("/pedidos/{id}/avancar")
    public ResponseEntity<String> avancarStatusPedido(@PathVariable("id") int id) {
        try {
            gerenciador.avancarStatus(id);
            return ResponseEntity.ok("Status atualizado!");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao avançar status";
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @PostMapping("/pedidos/{id}/excluir")
    public ResponseEntity<String> excluirPedido(@PathVariable("id") int id) {
        try {
            gerenciador.excluirPedido(id);
            return ResponseEntity.ok("Pedido excluído com sucesso!");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao excluir pedido";
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @PostMapping
    public ResponseEntity<String> registrarPedido(@RequestBody PedidoRequest req) {
        try {
            int clienteId = 0;
            if (req.cliente != null && !req.cliente.trim().isEmpty()) {
                List<Cliente> existentes = gerenciador.buscarClientesPorNome(req.cliente.trim());
                if (!existentes.isEmpty()) {
                    clienteId = existentes.get(0).getId();
                } else {
                    String tel = (req.telefone != null && !req.telefone.isEmpty()) ? req.telefone : "000000000";
                    String end = (req.endereco != null && !req.endereco.isEmpty()) ? req.endereco : "Balcão";
                    Cliente novoCliente = gerenciador.incluirCliente(req.cliente.trim(), tel, end);
                    clienteId = novoCliente.getId();
                }
            }

            Pedido pedido;
            if ("delivery".equalsIgnoreCase(req.tipoPedido)) {
                String end = (req.endereco != null && !req.endereco.isEmpty()) ? req.endereco : "Endereço não informado";
                pedido = gerenciador.criarPedidoEntrega(clienteId, 5.00, end);
            } else if ("retirar".equalsIgnoreCase(req.tipoPedido)) {
                pedido = gerenciador.criarPedidoRetirada(clienteId, LocalDateTime.now().plusMinutes(30));
            } else {
                pedido = gerenciador.criarPedidoConsumoLocal(clienteId, 1, 0.0);
            }

            if (req.itens != null) {
                for (ItemRequest item : req.itens) {
                    Produto prodNoGerenciador = null;

                    if (item.id > 0) {
                        try {
                            prodNoGerenciador = gerenciador.consultarProduto(item.id);
                        } catch (Exception ignored) {
                        }
                    }

                    if (prodNoGerenciador == null) {
                        for (Produto p : gerenciador.listarProdutos()) {
                            if (p.getNome().equalsIgnoreCase(item.nome)) {
                                prodNoGerenciador = p;
                                break;
                            }
                        }
                    }

                    if (prodNoGerenciador == null) {
                        String cat = (item.categoria != null && !item.categoria.isEmpty()) ? item.categoria : "Geral";
                        prodNoGerenciador = gerenciador.incluirProduto(item.nome, item.preco, cat, true);
                    }

                    gerenciador.adicionarItem(pedido.getId(), prodNoGerenciador.getId(), 1);
                }
            }

            String identComanda = (req.cliente != null && !req.cliente.isEmpty()) ? req.cliente : "Pedido #" + pedido.getId();
            Comanda comanda = gerenciador.abrirComanda(identComanda);
            gerenciador.adicionarPedidoNaComanda(comanda.getId(), pedido.getId());

            return ResponseEntity.ok("Pedido registrado com sucesso!");

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao processar pedido";
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @GetMapping("/clientes")
    public List<Cliente> listarClientes() {
        return gerenciador.listarClientes();
    }

    @PostMapping("/clientes")
    public ResponseEntity<String> cadastrarCliente(@RequestBody ClienteRequest req) {
        try {
            if (req.nome == null || req.nome.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("O nome do cliente é obrigatório.");
            }
            String tel = (req.telefone != null && !req.telefone.trim().isEmpty()) ? req.telefone.trim() : "Sem telefone";
            String end = (req.endereco != null && !req.endereco.trim().isEmpty()) ? req.endereco.trim() : "Retira no Balcão (Sem endereço)";

            gerenciador.incluirCliente(req.nome.trim(), tel, end);
            return ResponseEntity.ok("Cliente cadastrado com sucesso!");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao cadastrar cliente";
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @PostMapping("/clientes/{id}/excluir")
    public ResponseEntity<String> excluirCliente(@PathVariable("id") int id) {
        try {
            gerenciador.excluirCliente(id);
            return ResponseEntity.ok("Cliente excluído com sucesso!");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao excluir cliente";
            return ResponseEntity.badRequest().body(msg);
        }
    }
}