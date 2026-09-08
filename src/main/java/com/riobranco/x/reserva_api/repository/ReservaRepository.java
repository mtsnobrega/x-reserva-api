package com.riobranco.x.reserva_api.repository;

import com.riobranco.x.reserva_api.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReservaRepository extends JpaRepository<Reserva, Long> {

}
