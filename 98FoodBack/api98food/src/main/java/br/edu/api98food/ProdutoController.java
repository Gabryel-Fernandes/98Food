package br.edu.api98food;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProdutoController {


    private List<Produto> produtos = new ArrayList<>();
    private List<String> descricoes = new ArrayList<>();


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

        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            ProdutoResponse res = new ProdutoResponse();
            res.id = p.getId();
            res.nome = p.getNome();
            res.categoria = p.getCategoria();
            res.preco = p.getPrecoBase();
            res.descricao = descricoes.get(i);
            listaFormatada.add(res);
        }

        return listaFormatada;
    }


    @PostMapping
    public ProdutoResponse adicionarProduto(@RequestBody ProdutoRequest request) throws DadoInvalidoException {


        Produto novo = new Produto(request.nome, request.preco, request.categoria, true);
        produtos.add(novo);


        String desc = (request.descricao != null && !request.descricao.isEmpty()) ? request.descricao : "Sem descrição";
        descricoes.add(desc);


        ProdutoResponse res = new ProdutoResponse();
        res.id = novo.getId();
        res.nome = novo.getNome();
        res.categoria = novo.getCategoria();
        res.preco = novo.getPrecoBase();
        res.descricao = desc;

        return res;
    }


    @DeleteMapping("/{id}")
    public void removerProduto(@PathVariable int id) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getId() == id) {
                produtos.remove(i);
                descricoes.remove(i);
                break;
            }
        }
    }
}