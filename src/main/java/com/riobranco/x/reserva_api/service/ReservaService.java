/*
 * SERVICE - Camada de Regras de Negócio
 * É o coração e cérebro da aplicação. Onde ficam as validações, regras, cálculos e decisões deve fazer sistema
 * O service não acessa o banco diretamente, ele usa o repository, através da injeção de dependencia
 */
package com.riobranco.x.reserva_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.riobranco.x.reserva_api.model.Reserva;
import com.riobranco.x.reserva_api.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository repository;

    // Através do construtor, o Spring consegue automaticamente fornecer essa dependência para o Service.
    public ReservaService(ReservaRepository repository) {
        this.repository = repository;
    }

    // ----------------------------------------
    // Metodos e validações usadas pelo service
    // ----------------------------------------


    // Validações do Negócio
    private void validarReserva(Reserva reserva) {
        if (reserva.getDataInicio() == null || reserva.getDataFim() == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }

        if (reserva.getDataInicio().isAfter(reserva.getDataFim()) || reserva.getDataInicio().isEqual(reserva.getDataFim())) {
            throw new IllegalArgumentException("A data inicial deve ser menor que a data final.");
        }

        boolean conflito = repository.existeConflitoDeHorario(
            reserva.getVagaId(),
            reserva.getDataInicio(),
            reserva.getDataFim(),
            reserva.getId()
        );

        if (conflito) {
            throw new IllegalStateException("A vaga já possui uma reserva confirmada para o horário selecionado.");
        }
    }


    // CREATE 
    public Reserva salvar(Reserva reserva) {
        validarReserva(reserva);

        if (reserva.getCreatedAt() == null) {
            reserva.setCreatedAt(LocalDateTime.now());
        }
        if (reserva.getStatus() == null) {
            reserva.setStatus("RESERVADO: PENDENTE");
        }
        return repository.save(reserva);
    }

    // READ
    public List<Reserva> listarTodas() {
        return repository.findAll();
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // UPDATE
    public Reserva atualizar(Long id, Reserva dados) {
        Reserva reserva = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Reserva não encontrada com o ID: " + id));

        if (reserva.getStatus().equals("FINALIZADA")) {
            throw new IllegalStateException(
                    "Não é possível alterar uma reserva finalizada.");
        }

        if (reserva.getStatus().equals("CANCELADA")) {
            throw new IllegalStateException(
                    "Não é possível alterar uma reserva cancelada.");
        }

        dados.setId(id);

        validarReserva(dados);

        reserva.setVagaId(dados.getVagaId());
        reserva.setDataInicio(dados.getDataInicio());
        reserva.setDataFim(dados.getDataFim());

        return repository.save(reserva);
    }

    public Reserva realizarCheckin(Long id) {

        Reserva reserva = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Reserva não encontrada com o ID: " + id));

        if (reserva.getStatus().equals("CANCELADA")) {
            throw new IllegalStateException(
                    "Não é possível realizar check-in de uma reserva cancelada.");
        }

        if (reserva.getCheckinAt() != null) {
            throw new IllegalStateException(
                    "O check-in dessa reserva já foi realizado.");
        }

        reserva.setCheckinAt(LocalDateTime.now());
        reserva.setStatus("EM_USO");

        return repository.save(reserva);
    }

    public Reserva realizarCheckout(Long id) {

        Reserva reserva = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Reserva não encontrada com o ID: " + id));

        if (reserva.getStatus().equals("CANCELADA")) {
            throw new IllegalStateException(
                    "Não é possível realizar checkout de uma reserva cancelada.");
        }

        if (reserva.getCheckinAt() == null) {
            throw new IllegalStateException(
                    "Não é possível realizar checkout sem realizar check-in.");
        }

        if (reserva.getCheckoutAt() != null) {
            throw new IllegalStateException(
                    "O checkout dessa reserva já foi realizado.");
        }

        reserva.setCheckoutAt(LocalDateTime.now());
        reserva.setStatus("FINALIZADA");

        return repository.save(reserva);
    }

    // DELETE 
    public void deletar(Long id) {

    Reserva reserva = repository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Reserva não encontrada com o ID: " + id));

    if (reserva.getStatus().equals("PENDENTE")) {
        throw new IllegalStateException(
                "Não é possível deletar uma reserva pendente.");
    }

    if (reserva.getStatus().equals("EM_USO")) {
        throw new IllegalStateException(
                "Não é possível deletar uma reserva em uso.");
    }

    repository.deleteById(id);
}

    // METODOS ESPESCIFICOS DE BUSCA
    public List<Reserva> buscarPorStatus(String status) {
        return repository.BuscarPorStatus(status);
    }

    public List<Reserva> buscarPorUsuario(Long usuarioId) {
        return repository.BuscarPorUsuario(usuarioId);
    }

    public List<Reserva> buscarPorVaga(Long vagaId) {
        return repository.BuscarPorVaga(vagaId);
    }
}