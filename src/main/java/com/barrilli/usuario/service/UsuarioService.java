package com.barrilli.usuario.service;

import com.barrilli.usuario.controller.dto.EnderecoDTO;
import com.barrilli.usuario.controller.dto.TelefoneDTO;
import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.converter.UsuarioConverter;
import com.barrilli.usuario.infra.exceptions.ConflictException;
import com.barrilli.usuario.infra.exceptions.ResourceNotFoundException;
import com.barrilli.usuario.infra.model.Usuario;
import com.barrilli.usuario.infra.repository.EnderecoRepository;
import com.barrilli.usuario.infra.repository.TelefoneRepository;
import com.barrilli.usuario.infra.repository.UsuarioRepository;
import com.barrilli.usuario.infra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvar(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    private void emailExiste(String email) {
        try {
            if (verificaEmailExiste(email)) {
                throw new ConflictException("Já existe um usuário com esse email!");
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já cadastrado. " + e.getCause());
        }
    }

    private boolean verificaEmailExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public List<UsuarioDTO> buscarTodosUsuarios() {
        return usuarioConverter.paraTodosUsuariosDTO(usuarioRepository.findAll());
    }

    public UsuarioDTO deleteById(Long id) {
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.deleteUsuarioById(id));
    }

    public void deleteByEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(
                        () -> new ResourceNotFoundException("Usuário com email : '" + email + "' não encontrado")
                );
    }

    public UsuarioDTO atualizarUsuario(String token, UsuarioDTO usuarioDTO) {
        var email = jwtUtil.extrairEmailToken(token.substring(7));

        usuarioDTO.setSenha(usuarioDTO.getSenha() != null ? passwordEncoder.encode(usuarioDTO.getSenha()) : null);

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Email não localizado ")
                );

        usuario = usuarioConverter.atualizaUsuario(usuarioDTO, usuario);

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizarEndereco(Long enderecoId, EnderecoDTO enderecoDTO) {
        var enderecoAntigo = enderecoRepository.findById(enderecoId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Não foi encontrado endereço com id " + enderecoId)
                );

        var endereco = usuarioConverter.atualizarEndereco(enderecoDTO, enderecoAntigo);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizarTelefone(Long telefoneId, TelefoneDTO telefoneDTO) {
        var telefoneAntigo = telefoneRepository.findById(telefoneId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Não foi encontrado telefone com id " + telefoneId)
                );

        var telefone = usuarioConverter.atualizarTelefone(telefoneDTO, telefoneAntigo);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO enderecoDTO) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Email não localizado " + email)
                );

        var endereco = usuarioConverter.paraEnderecoEntity(enderecoDTO, usuario.getId());

        var enderecoEntity = enderecoRepository.save(endereco);

        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO telefoneDTO) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Email não localizado " + email)
                );

        var telefone = usuarioConverter.paraTelefoneEntity(telefoneDTO, usuario.getId());

        var telefoneEntity = telefoneRepository.save(telefone);

        return usuarioConverter.paraTelefoneDTO(telefoneEntity);
    }
}
