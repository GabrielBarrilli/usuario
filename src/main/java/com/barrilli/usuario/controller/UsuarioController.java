package com.barrilli.usuario.controller;

import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.infra.model.Usuario;
import com.barrilli.usuario.infra.security.JwtUtil;
import com.barrilli.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String login(@RequestBody UsuarioDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
        );
        return "Bearer " + jwtUtil.generateToken(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> salvaUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.salvar(usuarioDTO));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> buscarTodosUsuarios() {
        return ResponseEntity.ok(usuarioService.buscarTodosUsuarios());
    }

    @GetMapping
    public ResponseEntity<Usuario> buscaUsuarioPorEmail (@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.findUsuarioByEmail(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioDTO> deleteUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.deleteById(id));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUsuarioPorEmail(@PathVariable String email) {
        usuarioService.deleteByEmail(email);
        return ResponseEntity.ok().build();
    }
}
