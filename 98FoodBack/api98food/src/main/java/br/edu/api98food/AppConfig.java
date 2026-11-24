package br.edu.api98food;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public GerenciadorPedidos gerenciadorPedidos() throws GerenciadorException {
        return new GerenciadorPedidos("dados");
    }
}