/*
 * MODEL - Entidade\Objeto do domínio
 * Representa um objeto do mundo real, sua estrutura de dados ou uma tabela do banco de dados
*/
package com.riobranco.x.reserva_api.model;

// Os imports trazem funcionalidades que serão utilizadas pela classe.
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// São responsáveis pelo mapeamento objeto-relacional. Permitem que a classe seja relacionada a uma tabela.

@Entity // @Entity informa ao JPA que a classe Reserva é uma entidade persistente.
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "placa_veiculo", nullable = false)
    private String placaVeiculo;

    @Column(name = "vaga_id", nullable = false)
    private Long vagaId;

    @Column(name = "nome_vaga", nullable = false)
    private String nomeVaga;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(nullable = false)
    private String status;

    @Column(name = "checkin_at")
    private LocalDateTime checkinAt;

    @Column(name = "checkout_at")
    private LocalDateTime checkoutAt;

    @Column(name = "preco_vaga")
    private BigDecimal precoVaga;

    @Column(name = "preco_reserva")
    private BigDecimal precoReserva;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Construtor vazio, requisito para JPA
    // O JPA precisa conseguir criar objetos da classe Reserva sem necessariamente receber parâmetros no construtor.
    public Reserva() {}

    // Getters e Setters
    public Long getId() { return id; } // O getter serve para obter o valor
    public void setId(Long id) { this.id = id; } // O setter serve para alterar o valor:

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getPlacaVeiculo() { return placaVeiculo; }
    public void setPlacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }

    public Long getVagaId() { return vagaId; }
    public void setVagaId(Long vagaId) { this.vagaId = vagaId; }

    public String getNomeVaga() { return nomeVaga; }
    public void setNomeVaga(String nomeVaga) { this.nomeVaga = nomeVaga; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCheckinAt() { return checkinAt; }
    public void setCheckinAt(LocalDateTime checkinAt) { this.checkinAt = checkinAt; }

    public LocalDateTime getCheckoutAt() { return checkoutAt; }
    public void setCheckoutAt(LocalDateTime checkoutAt) { this.checkoutAt = checkoutAt;}

    public BigDecimal getPrecoVaga() { return precoVaga; }
    public void setPrecoVaga(BigDecimal precoVaga) { this.precoVaga = precoVaga; }

    public BigDecimal getPrecoReserva() { return precoReserva; }
    public void setPrecoReserva(BigDecimal precoReserva) { this.precoReserva = precoReserva; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
