package com.barrilli.usuario.controller.dto;

import lombok.Builder;

public record EnderecoDTO(Long id,
                          String rua,
                          Long numero,
                          String complemento,
                          String cidade,
                          String estado,
                          String cep) {

    @Builder
    public EnderecoDTO {
    }
}
