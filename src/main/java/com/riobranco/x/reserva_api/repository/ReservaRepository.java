package com.riobranco.x.reserva_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.riobranco.x.reserva_api.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

}
