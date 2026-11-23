"use client";
import { useEffect, useState } from 'react';

import Home from './Home/page';

export default function Page() {
    const [comandas, setComandas] = useState([]);

    useEffect(() => {
        fetch('http://localhost:8080/api/comandas')
            .then((resposta) => resposta.json())
            .then((dados) => setComandas(dados))
            .catch((erro) => console.error("Erro ao ligar à API:", erro));
    }, []);

    return (
        <Home />
    );
}