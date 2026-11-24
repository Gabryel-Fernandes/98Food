package br.edu.api98food;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProdutoController {

    private final GerenciadorPedidos gerenciador;
    private final Map<Integer, String> descricoes = new HashMap<>();

    public ProdutoController(GerenciadorPedidos gerenciador) {
        this.gerenciador = gerenciador;
    }

    public static class ProdutoRequest {
        public String nome;
        public String descricao;
        public double preco;
        public String categoria;
    }

    public static class ProdutoResponse {
        public int id;
        public String nome;
        public String descricao;
        public double preco;
        public String categoria;
    }

    @GetMapping
    public List<ProdutoResponse> listarProdutos() {
        List<ProdutoResponse> listaFormatada = new ArrayList<>();

        for (Produto p : gerenciador.listarProdutos()) {
            ProdutoResponse res = new ProdutoResponse();
            res.id = p.getId();
            res.nome = p.getNome();
            res.categoria = p.getCategoria();
            res.preco = p.getPrecoBase();
            res.descricao = descricoes.getOrDefault(p.getId(), "Categoria: " + p.getCategoria());
            listaFormatada.add(res);
        }

        return listaFormatada;
    }

    @PostMapping
    public ResponseEntity<?> adicionarProduto(@RequestBody ProdutoRequest request) {
        try {
            Produto novo = gerenciador.incluirProduto(
                    request.nome,
                    request.preco,
                    request.categoria,
                    true
            );

            String desc = (request.descricao != null && !request.descricao.isBlank())
                    ? request.descricao
                    : "Sem descrição";
            descricoes.put(novo.getId(), desc);

            ProdutoResponse res = new ProdutoResponse();
            res.id = novo.getId();
            res.nome = novo.getNome();
            res.categoria = novo.getCategoria();
            res.preco = novo.getPrecoBase();
            res.descricao = desc;

            return ResponseEntity.ok(res);
        } catch (GerenciadorException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerProduto(@PathVariable int id) {
        try {
            gerenciador.excluirProduto(id);
            descricoes.remove(id);
            return ResponseEntity.ok().build();
        } catch (GerenciadorException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}