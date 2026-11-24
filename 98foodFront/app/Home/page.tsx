"use client";
import { useState, useEffect } from 'react';
import "./home.css";
import Link from 'next/link';

interface Produto {
    id: number;
    categoria: string;
    nome: string;
    descricao: string;
    preco: number;
}

export default function Page() {
    const [produtos, setProdutos] = useState<Produto[]>([]);
    const [comanda, setComanda] = useState<Produto[]>([]);

    const [cliente, setCliente] = useState('');
    const [telefone, setTelefone] = useState('');
    const [tipoPedido, setTipoPedido] = useState('local');
    const [endereco, setEndereco] = useState('');

    useEffect(() => {
        fetch("http://localhost:8080/api/produtos")
            .then(res => res.json())
            .then(data => setProdutos(data))
            .catch(err => console.error("Erro ao carregar cardápio:", err));
    }, []);

    const adicionarNaComanda = (produto: Produto) => {
        setComanda([...comanda, produto]);
    };

    const removerDaComanda = (indexParaRemover: number) => {
        const novaComanda = comanda.filter((_, index) => index !== indexParaRemover);
        setComanda(novaComanda);
    };

    const subtotal = comanda.reduce((acumulador, item) => acumulador + item.preco, 0);


    const finalizarPedido = async () => {
        if (!cliente) {
            alert("Por favor, preencha o nome do cliente antes de fazer o pedido.");
            return;
        }

        const dadosDoPedido = {
            cliente: cliente,
            telefone: telefone,
            tipoPedido: tipoPedido,
            endereco: tipoPedido === 'delivery' ? endereco : '',
            itens: comanda,
            total: subtotal
        };

        try {
            const response = await fetch("http://localhost:8080/api/comandas", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dadosDoPedido)
            });

            if (response.ok) {
                alert("Pedido realizado com sucesso! Foi enviado para a Gestão de Pedidos.");
                setComanda([]);
                setCliente('');
                setTelefone('');
                setTipoPedido('local');
                setEndereco('');
            } else {
                const erroMsg = await response.text();
                alert("Erro ao registrar: " + erroMsg);
            }
        } catch (error) {
            console.error("Erro de conexão ao enviar pedido:", error);
            alert("Erro de conexão. O servidor Java está ligado?");
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

            <main className="main">

                <section className="container-cardapio">
                    <h2>Cardápio</h2>
                    <div className="grid-produtos">
                        {produtos.length === 0 ? (
                            <p>Nenhum produto cadastrado ainda.</p>
                        ) : (
                            produtos.map((produto) => (
                                <div key={produto.id} className="card-produto">
                                    <span className="categoria-badge">{produto.categoria}</span>
                                    <h3>{produto.nome}</h3>
                                    <p>{produto.descricao}</p>
                                    <strong>R$ {Number(produto.preco).toFixed(2)}</strong>
                                    <button
                                        className="btn-add-comanda"
                                        onClick={() => adicionarNaComanda(produto)}
                                    >
                                        + Adicionar
                                    </button>
                                </div>
                            ))
                        )}
                    </div>
                </section>

                <section className="dados-compra">
                    <div className="dados">
                        <h2>Seus Dados</h2>
                        <div className="container-dados">

                            <div className="grupo-input">
                                <label htmlFor="cliente">Cliente</label>

                                <input
                                    type="text"
                                    id="cliente"
                                    placeholder="Nome do cliente"
                                    value={cliente}
                                    onChange={(e) => setCliente(e.target.value)}
                                />
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="telefone">Número de telefone</label>
                                <input
                                    type="tel"
                                    id="telefone"
                                    placeholder="(00) 00000-0000"
                                    value={telefone}
                                    onChange={(e) => setTelefone(e.target.value)}
                                />
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="tipo-pedido">Onde vai comer?</label>
                                <select
                                    id="tipo-pedido"
                                    value={tipoPedido}
                                    onChange={(e) => setTipoPedido(e.target.value)}
                                >
                                    <option value="local">Comer no local</option>
                                    <option value="retirar">Retirar no balcão</option>
                                    <option value="delivery">Delivery</option>
                                </select>
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="endereco">Endereço (se for Delivery)</label>
                                <input
                                    type="text"
                                    id="endereco"
                                    placeholder="Rua, Número, Bairro"
                                    value={endereco}
                                    onChange={(e) => setEndereco(e.target.value)}
                                    disabled={tipoPedido !== 'delivery'}
                                />
                            </div>

                            <div className="comanda">
                                <h3>Comanda</h3>

                                <ul className="lista-itens">
                                    {comanda.length === 0 ? (
                                        <p style={{ fontSize: '14px', color: '#666' }}>Sua comanda está vazia.</p>
                                    ) : (
                                        comanda.map((item, index) => (
                                            <li key={index} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                                <span>1x {item.nome} - R$ {Number(item.preco).toFixed(2)}</span>
                                                <button
                                                    onClick={() => removerDaComanda(index)}
                                                    style={{ background: 'none', border: 'none', color: 'red', fontWeight: 'bold', cursor: 'pointer', padding: '0 5px' }}
                                                    title="Remover Item"
                                                >
                                                    X
                                                </button>
                                            </li>
                                        ))
                                    )}
                                </ul>

                                <div className="subtotal">
                                    <strong>Subtotal: R$ {subtotal.toFixed(2)}</strong>
                                </div>
                            </div>
                        </div>
                    </div>

                    <button
                        className="fazer-pedido"
                        disabled={comanda.length === 0}
                        onClick={finalizarPedido}
                    >
                        Faça seu pedido
                    </button>
                </section>

            </main>

            <footer className="footer">
                <p>© 2026 Sistema 98Food</p>
            </footer>
        </div>
    );
}