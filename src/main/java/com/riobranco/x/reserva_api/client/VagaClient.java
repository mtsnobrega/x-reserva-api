package com.riobranco.x.reserva_api.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VagaClient {

    private final RestClient restClient;

    public VagaClient(RestClient vagaRestClient) {
        this.restClient = vagaRestClient;
    }

    public VagaResponse buscarVaga(Long id) {
        try {
            return restClient.get()
                    .uri("/vagas/listar-vagas/{id}", id)
                    .retrieve()
                    .body(VagaResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Vaga não encontrada com o ID: " + id);
        } catch (ResourceAccessException e) {
            throw indisponivel();
        }
    }

    public void ocupar(Long id) {
        alterarEstado(id, "ocupar",
                "A vaga não está disponível: ela já está ocupada ou foi desativada.");
    }

    public void liberar(Long id) {
        alterarEstado(id, "liberar", "A vaga não está ocupada.");
    }

    private void alterarEstado(Long id, String acao, String mensagemConflito) {
        try {
            restClient.patch()
                    .uri("/vagas/{id}/{acao}", id, acao)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalStateException(mensagemConflito);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalStateException("A vaga desta reserva não existe mais.");
        } catch (ResourceAccessException e) {
            throw indisponivel();
        }
    }

    private ResponseStatusException indisponivel() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço de vagas indisponível. Tente novamente mais tarde.");
    }
}