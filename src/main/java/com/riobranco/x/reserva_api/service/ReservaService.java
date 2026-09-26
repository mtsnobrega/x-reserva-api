/*
 * SERVICE - Camada de Regras de Negócio
 * É o coração e cérebro da aplicação. Onde ficam as validações, regras, cálculos e decisões deve fazer sistema
 * O service não acessa o banco diretamente, ele usa o repository, através da injeção de dependencia
 */

package com.riobranco.x.reserva_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.riobranco.x.reserva_api.model.Reserva;
import com.riobranco.x.reserva_api.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository repository;

    public ReservaService(ReservaRepository repository) {
        this.repository = repository;
    }

    // ----------------------------------------
    // Validações e regras de negócio
    // ----------------------------------------

    private void validarReserva(Reserva reserva) {

        if (reserva.getUsuarioId() == null) {
            throw new IllegalArgumentException(
                    "O usuário é obrigatório.");
        }
        if (reserva.getPlacaVeiculo() == null) {
            throw new IllegalArgumentException(
                    "A placa do veiculo é obrigatório.");
        }

        if (reserva.getVagaId() == null) {
            throw new IllegalArgumentException(
                    "O ID da vaga é obrigatória.");
        }

        if (reserva.getNomeVaga() == null) {
            throw new IllegalArgumentException(
                    "O Nome da vaga é obrigatória.");
        }

        if (reserva.getDataInicio() == null || reserva.getDataFim() == null) {
            throw new IllegalArgumentException(
                    "As datas de início e fim são obrigatórias.");
        }

         LocalDateTime agora = LocalDateTime.now();

        if (reserva.getDataInicio().isBefore(agora)) {
            throw new IllegalArgumentException(
                    "Não é possível criar uma reserva com data de início no passado.");
        }


        if (!reserva.getDataInicio().isBefore(reserva.getDataFim())) {
            throw new IllegalArgumentException(
                    "A data inicial deve ser menor que a data final.");
        }

        if (reserva.getPrecoVaga() == null) {
            throw new IllegalArgumentException(
                    "O preço da vaga é obrigatório.");
        }

        if (reserva.getPrecoVaga().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O preço da vaga não pode ser negativo.");
        }

        boolean conflito = repository.existeConflitoDeHorario(
                reserva.getVagaId(),
                reserva.getDataInicio(),
                reserva.getDataFim(),
                reserva.getId()
        );

        if (conflito) {
            throw new IllegalStateException(
                    "A vaga já possui uma reserva confirmada para o horário selecionado.");
        }
    }

    /**
     * Calcula o valor total da reserva.
     *
     * Regra:
     * Cada hora iniciada é cobrada integralmente.
     *
     * Exemplo:
     * 1h00 = 1 hora
     * 1h30 = 2 horas
     * 2h00 = 2 horas
     */
    private BigDecimal calcularPrecoReserva(Reserva reserva) {

        Duration duracao = Duration.between(
                reserva.getDataInicio(),
                reserva.getDataFim()
        );

        long minutos = duracao.toMinutes();

        long horasCobradas = (long) Math.ceil(minutos / 60.0);

        return reserva.getPrecoVaga()
                .multiply(BigDecimal.valueOf(horasCobradas))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ----------------------------------------
    // CREATE
    // ----------------------------------------

    public Reserva salvar(Reserva reserva) {

        validarReserva(reserva);

        // O valor total é sempre calculado pelo backend.
        reserva.setPrecoReserva(
                calcularPrecoReserva(reserva)
        );

        if (reserva.getCreatedAt() == null) {
            reserva.setCreatedAt(LocalDateTime.now());
        }

        if (reserva.getStatus() == null) {
            reserva.setStatus("PENDENTE");
        }

        return repository.save(reserva);
    }

    // ----------------------------------------
    // READ
    // ----------------------------------------

    public List<Reserva> listarTodas() {
        return repository.findAll();
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // ----------------------------------------
    // UPDATE
    // ----------------------------------------

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

        // Atualiza somente os dados que podem ser alterados.
        reserva.setVagaId(dados.getVagaId());
        reserva.setDataInicio(dados.getDataInicio());
        reserva.setDataFim(dados.getDataFim());

        // Valida a nova configuração da reserva.
        validarReserva(reserva);

        // Mantém o preço da vaga controlado pelo sistema.
        // O preço da reserva é recalculado com base nos novos dados.
        reserva.setPrecoReserva(
            calcularPrecoReserva(reserva)
        );

        return repository.save(reserva);
    }

    // ----------------------------------------
    // CHECK-IN
    // ----------------------------------------

    public Reserva realizarCheckin(Long id) {

        Reserva reserva = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Reserva não encontrada com o ID: " + id));

        if (reserva.getStatus().equals("CANCELADA")) {
            throw new IllegalStateException(
                    "Não é possível realizar check-in de uma reserva cancelada.");
        }

        if (reserva.getStatus().equals("FINALIZADA")) {
            throw new IllegalStateException(
                    "Não é possível realizar check-in de uma reserva finalizada.");
        }

        if (reserva.getCheckinAt() != null) {
            throw new IllegalStateException(
                    "O check-in dessa reserva já foi realizado.");
        }

        reserva.setCheckinAt(LocalDateTime.now());
        reserva.setStatus("EM_USO");

        return repository.save(reserva);
    }

    // ----------------------------------------
    // CHECK-OUT
    // ----------------------------------------

    public Reserva realizarCheckout(Long id) {

        Reserva reserva = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Reserva não encontrada com o ID: " + id));

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

    // ----------------------------------------
    // DELETE
    // ----------------------------------------

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

    // ----------------------------------------
    // MÉTODOS ESPECÍFICOS DE BUSCA
    // ----------------------------------------

    public List<Reserva> buscarPorStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<Reserva> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public List<Reserva> buscarPorVaga(Long vagaId) {
        return repository.findByVagaId(vagaId);
    }
}


/*
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
        return repository.findByStatus(status);
    }

    public List<Reserva> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public List<Reserva> buscarPorVaga(Long vagaId) {
        return repository.findByVagaId(vagaId);
    }
}
    */