package com.barrilli.usuario.service;

import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.converter.UsuarioConverter;
import com.barrilli.usuario.infra.model.Usuario;
import com.barrilli.usuario.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;

    public UsuarioDTO salvar(UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public List<UsuarioDTO> buscarTodosUsuarios() {
        return usuarioConverter.paraTodosUsuariosDTO(usuarioRepository.findAll());
    }

    public UsuarioDTO deleteById(Long id) {
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.deleteUsuarioById(id));
    }
}
