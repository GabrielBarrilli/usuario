package com.barrilli.usuario.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

public record UsuarioDTO(String nome,
                         String email,
                         @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha,
                         List<EnderecoDTO> enderecos,
                         List<TelefoneDTO> telefones) {

    @Builder
    public UsuarioDTO {
    }
}
