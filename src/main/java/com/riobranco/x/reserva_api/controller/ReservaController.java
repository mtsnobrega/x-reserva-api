package com.riobranco.x.reserva_api.controller;

import com.riobranco.x.reserva_api.model.Reserva;
import com.riobranco.x.reserva_api.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservar")
public class ReservaController {
    
    @Autowired
    private ReservaRepository repository;

    // Listar todas as reservas
    @GetMapping
    public List<Reserva> listar() {
        return repository.findAll();
    }

    // Buscar reserva por ID
    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    // Cadastrar uma nova reserva
    @PostMapping
    public Reserva cadastrar(@RequestBody Reserva nova) {
        return repository.save(nova);
    }

    // Atualizar uma reserva
    @PutMapping("/{id}")
    public Reserva atualizar(
            @PathVariable Long id,
            @RequestBody Reserva atualizada) {

        atualizada.setId(id);
        return repository.save(atualizada);
    }

    // Remover uma reserva
    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        repository.deleteById(id);
    }

    /* 
    // Buscar reservas por status
    @GetMapping("/status/{status}")
    public List<Reserva> buscarPorStatus(@PathVariable String status) {
        return repository.findByStatus(status);
    }

    
    // Buscar reservas por usuário
    @GetMapping("/usuario/{usuarioId}")
    public List<Reserva> buscarPorUsuario(@PathVariable Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    // Buscar reservas por vaga
    @GetMapping("/vaga/{vagaId}")
    public List<Reserva> buscarPorVaga(@PathVariable Long vagaId) {
        return repository.findByVagaId(vagaId);
    }
    */
    
}
