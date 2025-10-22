package com.barrilli.usuario.controller;

import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioDTO> salvaUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.salvar(usuarioDTO));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> buscarTodosUsuarios() {
        return ResponseEntity.ok(usuarioService.buscarTodosUsuarios());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioDTO> deleteUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.deleteById(id));
    }
}
