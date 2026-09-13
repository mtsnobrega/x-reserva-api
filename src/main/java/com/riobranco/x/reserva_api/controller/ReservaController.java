/*
 * CONTROLLER - Camada de Exposição de Endpoints/Entrada HTTP
 * É a porta de entrada da API. Ele recebe a requisição de fora (cliente web ou app),
 * lê os dados em JSON, passa o trabalho para o Service e devolve a resposta HTTP adequada (200 OK, 201 Created, 400 Bad Request)
*/
package com.riobranco.x.reserva_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riobranco.x.reserva_api.model.Reserva;
import com.riobranco.x.reserva_api.service.ReservaService;

@RestController // define a classe como tipo Rest 
@RequestMapping("/reservas") // caminho base para os endpoints do controller
// @GetMapping ---> GET /reservar
// @PostMapping --> Post/reservar

public class ReservaController {

    private final ReservaService service;
    // Recebe o service por injeção de dependência
    public ReservaController(ReservaService service) {
        this.service = service;
    }

    // CREATE ROUTE
    @PostMapping("/nova-reserva")
    public ResponseEntity<?> cadastrar(@RequestBody Reserva nova) {
        try {
            Reserva salva = service.salvar(nova);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // READ ROUTE
    @GetMapping("/listar-reservas")
    public ResponseEntity<List<Reserva>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/listar-reservas/{id}")
    public ResponseEntity<Reserva> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // UPDATE ROUTE
     @PutMapping("/atualizar-reservas/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Reserva atualizada) {
        try {
            Reserva reservaSalva = service.atualizar(id, atualizada);
            return ResponseEntity.ok(reservaSalva);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/checkin")
    public ResponseEntity<?> checkin(@PathVariable Long id) {
        try {
            Reserva reserva = service.realizarCheckin(id);
            return ResponseEntity.ok(reserva);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Long id) {
        try {
            Reserva reserva = service.realizarCheckout(id);
            return ResponseEntity.ok(reserva);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // DELETE ROUTE
    @DeleteMapping("/deletar-reservas/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        try {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    
    //ROTAS PERSONALIZADAS

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Reserva>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.buscarPorStatus(status));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Reserva>> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/vaga/{vagaId}")
    public ResponseEntity<List<Reserva>> buscarPorVaga(@PathVariable Long vagaId) {
        return ResponseEntity.ok(service.buscarPorVaga(vagaId));
    }
}