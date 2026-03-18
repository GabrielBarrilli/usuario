package com.barrilli.usuario.service;

import com.barrilli.usuario.controller.dto.response.ViaCepResponse;
import com.barrilli.usuario.infra.clients.ViaCepClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    private final ViaCepClient viaCepClient;

    public ViaCepResponse buscaDadosEndereco(String cep) {
        try {
            return viaCepClient.buscaDadosEndereco(formataCep(cep));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro ao buscar dados do endereço: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao buscar dados do endereço: " + e.getMessage(), e);
        }
    }

    private String formataCep(String cep) {
        String cepFormatado = cep.replace(" ", "").
                replace("-", "");

        if (!cepFormatado.matches("\\d+") || !Objects.equals(cepFormatado.length(), 8)) {
            throw new IllegalArgumentException("CEP inválido. O CEP deve conter exatamente 8 dígitos numéricos.");
        }

        return cepFormatado;
    }
}
