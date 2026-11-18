package br.edu.api98food;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/comandas")
@CrossOrigin(origins = "*")
public class ComandaController {

    private GerenciadorPedidos gerenciador;

    public ComandaController() {
        try {
            // Usa o seu GerenciadorPedidos existente (certifique-se de que a pasta 'dados' exista na raiz do projeto)
            this.gerenciador = new GerenciadorPedidos("dados");
        } catch (Exception e) {
            System.out.println("Erro ao iniciar o gerenciador: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Comanda> listarTodas() {
        if (gerenciador != null) {
            return gerenciador.listarComandas();
        }
        return new ArrayList<>(); // Retorna lista vazia se der erro no gerenciador
    }
}