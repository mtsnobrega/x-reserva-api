package com.riobranco.x.reserva_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservaApiApplication.class, args);
		// Fluxo Geral: Model(Entidade) -> Repository(Acesso aos Dados) -> Service(Regras de Negócio) -> Controller(Entrada HTTP)
	}

}
