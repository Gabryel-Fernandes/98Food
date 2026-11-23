"use client";
import { useState, useEffect } from 'react';
import "./produtos.css";
import Link from 'next/link';


interface Produto {
    id: number;
    categoria: string;
    nome: string;
    descricao: string;
    preco: number;
}

export default function Produtos() {
    const [produtosLista, setProdutosLista] = useState<Produto[]>([]);

    const [categoria, setCategoria] = useState('');
    const [nome, setNome] = useState('');
    const [descricao, setDescricao] = useState('');
    const [preco, setPreco] = useState('');

    const carregarProdutos = () => {
        fetch("http://localhost:8080/api/produtos")
            .then(res => res.json())
            .then(data => setProdutosLista(data))
            .catch(err => console.error("Erro ao carregar produtos:", err));
    };

    useEffect(() => {
        carregarProdutos();
    }, []);

    const salvarProduto = async (e: any) => {
        e.preventDefault();

        const novoProduto = {
            categoria,
            nome,
            descricao,
            preco: parseFloat(preco)
        };

        try {
            const resposta = await fetch("http://localhost:8080/api/produtos", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(novoProduto)
            });

            if (resposta.ok) {
                alert("Produto adicionado com sucesso! 🍔");
                // Limpa os campos
                setCategoria('');
                setNome('');
                setDescricao('');
                setPreco('');
                // Atualiza a lista na hora
                carregarProdutos();
            } else {
                alert("Erro ao adicionar produto.");
            }
        } catch (erro) {
            console.error("Erro de conexão:", erro);
        }
    };


    const removerProduto = async (id: number) => {
        if (!confirm("Tem a certeza que deseja remover este produto do cardápio?")) return;

        try {
            const resposta = await fetch(`http://localhost:8080/api/produtos/${id}`, {
                method: "DELETE"
            });

            if (resposta.ok) {

                carregarProdutos();
            } else {
                alert("Erro ao remover produto.");
            }
        } catch (erro) {
            console.error("Erro ao remover:", erro);
        }
    };

    return (
        <div className="container">
            <header className="header">
                <h1 className="title-logo">98Food</h1>
                <ul className="lista">
                    <li className="item"><Link href="/Home">Home</Link></li>
                    <li className="item"><Link href="/GestaoDePedidos">Gestão de Pedidos</Link></li>
                    <li className="item"><Link href="/GestaoDeClientes">Gestão de Clientes</Link></li>
                    <li className="item"><Link href="/GestaoDeProdutos">Gestão de Produtos</Link></li>
                </ul>
            </header>

            <main className="main-content">
                <div className="layout-duplo">

                    <section className="painel painel-lista">
                        <h2>Produtos no Cardápio</h2>
                        <div className="lista-produtos-gerenciar">
                            {produtosLista.length === 0 ? (
                                <p>Nenhum produto cadastrado no momento.</p>
                            ) : (
                                produtosLista.map((prod) => (
                                    <div key={prod.id} className="produto-item-lista">
                                        <div className="info-prod">
                                            <strong>{prod.nome}</strong>
                                            <span className="badge-cat">{prod.categoria}</span>
                                            <p>R$ {Number(prod.preco).toFixed(2)}</p>
                                        </div>
                                        <button className="btn-remover" onClick={() => removerProduto(prod.id)}>
                                            🗑️ Remover
                                        </button>
                                    </div>
                                ))
                            )}
                        </div>
                    </section>

                    <section className="painel painel-form">
                        <h2>Adicionar Novo Produto</h2>
                        <form onSubmit={salvarProduto}>
                            <div className="grupo-input">
                                <label htmlFor="categoria">Categoria</label>
                                <select id="categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)} required>
                                    <option value="">Selecione...</option>
                                    <option value="Lanches">🍔 Lanches</option>
                                    <option value="Bebidas">🥤 Bebidas</option>
                                    <option value="Sobremesas">🍰 Sobremesas</option>
                                    <option value="Porções">🍟 Porções</option>
                                </select>
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="nome">Nome do Produto</label>
                                <input type="text" id="nome" placeholder="Ex: Hambúrguer Duplo" value={nome} onChange={(e) => setNome(e.target.value)} required />
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="descricao">Descrição</label>
                                <textarea id="descricao" rows={3} placeholder="Ingredientes..." value={descricao} onChange={(e) => setDescricao(e.target.value)} required></textarea>
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="preco">Preço (R$)</label>
                                <input type="number" id="preco" step="0.01" placeholder="0.00" value={preco} onChange={(e) => setPreco(e.target.value)} required />
                            </div>

                            <button type="submit" className="btn-submit">Lançar no Sistema</button>
                        </form>
                    </section>

                </div>
            </main>

            <footer className="footer">
                <p>© 2026 Sistema 98Food</p>
            </footer>
        </div>
    );
}