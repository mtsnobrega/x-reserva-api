/*
 * REPOSITORY - Camada de Acesso a Dados
 * Lida diretamente com o banco de dados, intermedia a relação banco\aplicação
 * Usa apenas métodos para buscar, salvar, atualizar e deletar registros
 *
*/

package com.riobranco.x.reserva_api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; // fornece vários métodos prontos para trabalhar com o banco.
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.riobranco.x.reserva_api.model.Reserva;

// Interface específica para a entidade Reserva e o Spring Data JPA deve fornecer operações de banco para a entidade:
// O JPA ja possui metodos prontos para CRUD
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
   
    // Metodos criados para permitir operações especificas 
    List<Reserva> BuscarPorStatus(String status);
    List<Reserva> BuscarPorUsuario(Long usuarioId);
    List<Reserva> BuscarPorVaga(Long vagaId);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
           "WHERE r.vagaId = :vagaId " +
           "AND (:id IS NULL OR r.id <> :id) " +
           "AND r.status <> 'CANCELADA' " +
           "AND r.dataInicio < :dataFim " +
           "AND r.dataFim > :dataInicio")
    boolean existeConflitoDeHorario(@Param("vagaId") Long vagaId,
                                    @Param("dataInicio") LocalDateTime dataInicio,
                                    @Param("dataFim") LocalDateTime dataFim,
                                    @Param("id") Long id);

}
