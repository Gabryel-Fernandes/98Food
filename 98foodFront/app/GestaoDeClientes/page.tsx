"use client";
import { useState, useEffect } from 'react';
import "./clientes.css";
import Link from 'next/link';

interface Cliente {
    id: number;
    nome: string;
    telefone: string;
    endereco: string;
}

export default function Clientes() {
    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [nome, setNome] = useState('');
    const [telefone, setTelefone] = useState('');
    const [endereco, setEndereco] = useState('');

    const carregarClientes = () => {
        fetch("http://localhost:8080/api/comandas/clientes")
            .then(res => res.json())
            .then(data => setClientes(data))
            .catch(err => console.error("Erro ao carregar clientes:", err));
    };

    useEffect(() => {
        carregarClientes();
    }, []);

    const salvarCliente = async () => {
        if (!nome.trim()) {
            alert("Por favor, informe o nome do cliente.");
            return;
        }

        const novoCliente = {
            nome: nome,
            telefone: telefone,
            endereco: endereco
        };

        try {
            const response = await fetch("http://localhost:8080/api/comandas/clientes", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(novoCliente)
            });

            if (response.ok) {
                alert("Cliente salvo com sucesso!");
                setNome('');
                setTelefone('');
                setEndereco('');
                carregarClientes();
            } else {
                const erroMsg = await response.text();
                alert("Erro ao salvar cliente: " + erroMsg);
            }
        } catch (error) {
            console.error("Erro de conexão ao salvar cliente:", error);
            alert("Erro de conexão. O servidor Java está ligado?");
        }
    };

    const excluirCliente = async (id: number, nomeCliente: string) => {
        if (!confirm(`Tem certeza que deseja excluir o cliente "${nomeCliente}"?`)) {
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/comandas/clientes/${id}/excluir`, {
                method: "POST"
            });

            if (response.ok) {
                alert("Cliente excluído com sucesso!");
                carregarClientes();
            } else {
                const erroMsg = await response.text();
                alert("Não foi possível excluir: " + erroMsg);
            }
        } catch (error) {
            console.error("Erro ao excluir cliente:", error);
            alert("Erro de conexão ao excluir cliente.");
        }
    };

    return (
        <div className="container">
            <header className="header">
                <h1 title="title-logo">98Food</h1>

                <ul className="lista">
                    <li className="item">
                        <Link href="/Home">Home</Link>
                    </li>
                    <li className="item">
                        <Link href="/GestaoDePedidos">Gestão de Pedidos</Link>
                    </li>
                    <li className="item">
                        <Link href="/GestaoDeClientes">Gestão de Clientes</Link>
                    </li>
                    <li className="item">
                        <Link href="/GestaoDeProdutos">Gestão de Produtos</Link>
                    </li>
                </ul>
            </header>

            <main className="main-content">
                <div className="layout-duplo">


                    <section className="painel painel-lista">
                        <h2>Clientes Salvos</h2>

                        <div className="lista-clientes">
                            {clientes.length === 0 ? (
                                <p>Nenhum cliente cadastrado ainda.</p>
                            ) : (
                                clientes.map((cliente) => (
                                    <div key={cliente.id} className="cliente-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <div>
                                            <strong>{cliente.nome}</strong>
                                            <p>📞 {cliente.telefone || "Sem telefone"}</p>
                                            <p>📍 {cliente.endereco || "Retira no Balcão (Sem endereço)"}</p>
                                        </div>
                                        <button
                                            onClick={() => excluirCliente(cliente.id, cliente.nome)}
                                            style={{
                                                backgroundColor: '#e74c3c',
                                                color: 'white',
                                                border: 'none',
                                                padding: '8px 12px',
                                                borderRadius: '6px',
                                                cursor: 'pointer',
                                                fontWeight: 'bold'
                                            }}
                                            title="Excluir Cliente"
                                        >
                                            Excluir
                                        </button>
                                    </div>
                                ))
                            )}
                        </div>
                    </section>


                    <section className="painel painel-form">
                        <h2>Adicionar Novo Cliente</h2>

                        <form className="form-container" onSubmit={(e) => e.preventDefault()}>
                            <div className="grupo-input">
                                <label htmlFor="nome">Nome Completo</label>
                                <input
                                    type="text"
                                    id="nome"
                                    placeholder="Ex: João da Silva"
                                    value={nome}
                                    onChange={(e) => setNome(e.target.value)}
                                />
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="telefone">Telefone</label>
                                <input
                                    type="tel"
                                    id="telefone"
                                    placeholder="(00) 00000-0000"
                                    value={telefone}
                                    onChange={(e) => setTelefone(e.target.value)}
                                />
                            </div>

                            <div className="grupo-input">
                                <label htmlFor="endereco">Endereço (Para Entrega)</label>
                                <input
                                    type="text"
                                    id="endereco"
                                    placeholder="Rua, Número, Bairro, Referência"
                                    value={endereco}
                                    onChange={(e) => setEndereco(e.target.value)}
                                />
                            </div>

                            <button
                                type="button"
                                className="btn-submit"
                                onClick={salvarCliente}
                            >
                                Salvar Cliente
                            </button>
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