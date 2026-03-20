package com.barrilli.usuario.controller.dto;

import lombok.Builder;

public record TelefoneDTO(Long id,
                          String numero,
                          String ddd) {

    @Builder
    public TelefoneDTO {
    }
}
