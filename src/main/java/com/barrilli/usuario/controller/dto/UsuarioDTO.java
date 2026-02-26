package com.barrilli.usuario.controller.dto;

import lombok.Builder;

import java.util.List;

public record UsuarioDTO(String nome,
                         String email,
                         String senha,
                         List<EnderecoDTO> enderecos,
                         List<TelefoneDTO> telefones) {

    @Builder
    public UsuarioDTO {}
}
