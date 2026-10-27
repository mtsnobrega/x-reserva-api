package com.riobranco.x.reserva_api.client;

import java.math.BigDecimal;

public record VagaResponse(Long id, String nomeVaga, BigDecimal precoHora, String status) {}