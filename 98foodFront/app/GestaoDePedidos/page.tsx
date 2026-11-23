"use client";
import { useState, useEffect } from 'react';
import "./pedidos.css";
import Link from 'next/link';

interface ItemComanda {
    nome: string;
    quantidade: number;
    subtotal: number;
}

interface Pedido {
    id: number;
    cliente: string;
    tipo: string;
    status: string;
    total: number;
    detalhes: string;
    itens: ItemComanda[];
}

export default function Pedidos() {
    const [pedidos, setPedidos] = useState<Pedido[]>([]);
    const [pedidoSelecionado, setPedidoSelecionado] = useState<Pedido | null>(null);

    const carregarPedidos = () => {
        fetch("http://localhost:8080/api/comandas/pedidos")
            .then(res => res.json())
            .then(data => setPedidos(data))
            .catch(err => console.error("Erro ao carregar pedidos:", err));
    };

    useEffect(() => {
        carregarPedidos();
    }, []);

    const avancarStatus = async (id: number) => {
        try {
            const response = await fetch(`http://localhost:8080/api/comandas/pedidos/${id}/avancar`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (response.ok) {
                carregarPedidos();
            } else {
                const erro = await response.text();
                alert("Não foi possível avançar: " + erro);
            }
        } catch (error) {
            console.error("Erro ao atualizar status:", error);
            alert("Erro de conexão ao atualizar status.");
        }
    };

    const excluirPedido = async (id: number) => {
        if (!confirm(`Tem certeza que deseja excluir o Pedido #${String(id).padStart(3, '0')}?`)) {
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/comandas/pedidos/${id}/excluir`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (response.ok) {
                alert("Pedido excluído com sucesso!");
                carregarPedidos();
            } else {
                const erro = await response.text();
                alert("Não foi possível excluir: " + erro);
            }
        } catch (error) {
            console.error("Erro ao excluir pedido:", error);
            alert("Erro de conexão ao excluir pedido.");
        }
    };

    const formatarTipo = (tipo: string) => {
        const t = tipo.toLowerCase();
        if (t.includes("entrega")) return "🛵 Entrega";
        if (t.includes("retirada")) return "🥡 Retirada";
        return "🍽️ Comer no Local";
    };

    const isEncerrado = (status: string) => {
        const s = status.toUpperCase();
        return s === "FINALIZADO" || s === "CANCELADO";
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
                <div className="pedidos-grid">
                    {pedidos.length === 0 ? (
                        <p>Nenhum pedido registrado ainda.</p>
                    ) : (
                        pedidos.map((pedido) => (
                            <div key={pedido.id} className="pedido-card">
                                <div className="pedido-header">
                                    <span className="numero-pedido">#{String(pedido.id).padStart(3, '0')}</span>
                                    <span className={`status ${isEncerrado(pedido.status) ? 'badge-finalizado' : 'badge-aberto'}`}>
                                        {pedido.status}
                                    </span>
                                </div>
                                <h3>{pedido.cliente}</h3>
                                <p className="tipo-pedido">{formatarTipo(pedido.tipo)}</p>

                                <div className="acoes-pedido">
                                    <button
                                        className="btn-comanda"
                                        onClick={() => setPedidoSelecionado(pedido)}
                                    >
                                        Ver Comanda
                                    </button>

                                    {!isEncerrado(pedido.status) ? (
                                        <button
                                            className="btn-acao btn-finalizar"
                                            onClick={() => avancarStatus(pedido.id)}
                                        >
                                            Avançar Status
                                        </button>
                                    ) : (
                                        <button
                                            className="btn-acao"
                                            style={{ backgroundColor: '#e74c3c', color: 'white' }}
                                            onClick={() => excluirPedido(pedido.id)}
                                        >
                                            Excluir
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </main>

            {pedidoSelecionado && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2>Comanda #{String(pedidoSelecionado.id).padStart(3, '0')}</h2>
                            <button className="btn-fechar" onClick={() => setPedidoSelecionado(null)}>X</button>
                        </div>

                        <div className="modal-body">
                            <p><strong>Cliente:</strong> {pedidoSelecionado.cliente}</p>
                            <p><strong>Tipo:</strong> {formatarTipo(pedidoSelecionado.tipo)}</p>
                            <p><strong>Detalhes:</strong> {pedidoSelecionado.detalhes}</p>

                            <hr className="divisor" />

                            <ul className="lista-itens-comanda">
                                {pedidoSelecionado.itens && pedidoSelecionado.itens.length > 0 ? (
                                    pedidoSelecionado.itens.map((item, idx) => (
                                        <li key={idx}>
                                            <span>{item.quantidade}x {item.nome}</span>
                                            <span>R$ {Number(item.subtotal).toFixed(2)}</span>
                                        </li>
                                    ))
                                ) : (
                                    <li>Nenhum item encontrado.</li>
                                )}
                            </ul>

                            <hr className="divisor" />

                            <div className="modal-footer">
                                <h3>Total:</h3>
                                <h3>R$ {Number(pedidoSelecionado.total).toFixed(2)}</h3>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <footer className="footer">
                <p>© 2026 Sistema 98Food</p>
            </footer>
        </div>
    );
}