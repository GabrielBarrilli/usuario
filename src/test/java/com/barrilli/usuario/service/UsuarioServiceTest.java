package com.barrilli.usuario.service;

import com.barrilli.usuario.controller.dto.EnderecoDTO;
import com.barrilli.usuario.controller.dto.TelefoneDTO;
import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.converter.UsuarioConverter;
import com.barrilli.usuario.infra.exceptions.ConflictException;
import com.barrilli.usuario.infra.exceptions.ResourceNotFoundException;
import com.barrilli.usuario.infra.model.Endereco;
import com.barrilli.usuario.infra.model.Telefone;
import com.barrilli.usuario.infra.model.Usuario;
import com.barrilli.usuario.infra.repository.EnderecoRepository;
import com.barrilli.usuario.infra.repository.TelefoneRepository;
import com.barrilli.usuario.infra.repository.UsuarioRepository;
import com.barrilli.usuario.infra.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioConverter usuarioConverter;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private TelefoneRepository telefoneRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void salvar_devePersistir_quandoEmailNaoExiste() {
        UsuarioDTO entrada = UsuarioDTO.builder()
                .nome("Fulano")
                .email("fulano@mail.com")
                .senha("123")
                .build();

        Usuario entity = Usuario.builder().nome("Fulano").email("fulano@mail.com").senha("ENC").build();
        UsuarioDTO respostaEsperada = UsuarioDTO.builder()
                .nome("Fulano")
                .email("fulano@mail.com")
                .senha("ENC")
                .build();

        when(usuarioRepository.existsByEmail("fulano@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("ENC");
        when(usuarioConverter.paraUsuario(any(UsuarioDTO.class))).thenReturn(entity);
        when(usuarioRepository.save(entity)).thenReturn(entity);
        when(usuarioConverter.paraUsuarioDTO(entity)).thenReturn(respostaEsperada);

        UsuarioDTO resposta = usuarioService.salvar(entrada);

        assertThat(resposta).isEqualTo(respostaEsperada);
    }

    @Test
    void salvar_deveLancarConflict_quandoEmailJaExiste() {
        when(usuarioRepository.existsByEmail("existe@mail.com")).thenReturn(true);

        UsuarioDTO entrada = UsuarioDTO.builder()
                .email("existe@mail.com")
                .senha("pwd")
                .build();

        assertThatThrownBy(() -> usuarioService.salvar(entrada))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void atualizarUsuario_deveAtualizarDados() {
        String token = "Bearer abc";
        UsuarioDTO entrada = UsuarioDTO.builder()
                .nome("Novo Nome")
                .email("novo@mail.com")
                .senha("pwd")
                .build();

        Usuario existente = Usuario.builder().id(1L).nome("Antigo").email("antigo@mail.com").senha("old").build();
        Usuario atualizado = Usuario.builder().id(1L).nome("Novo Nome").email("novo@mail.com").senha("ENC").build();
        UsuarioDTO esperado = UsuarioDTO.builder()
                .nome("Novo Nome")
                .email("novo@mail.com")
                .senha("ENC")
                .build();

        when(jwtUtil.extrairEmailToken("abc")).thenReturn("antigo@mail.com");
        when(usuarioRepository.findByEmail("antigo@mail.com")).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("pwd")).thenReturn("ENC");
        when(usuarioConverter.atualizaUsuario(any(UsuarioDTO.class), eq(existente))).thenReturn(atualizado);
        when(usuarioRepository.save(atualizado)).thenReturn(atualizado);
        when(usuarioConverter.paraUsuarioDTO(atualizado)).thenReturn(esperado);

        UsuarioDTO resultado = usuarioService.atualizarUsuario(token, entrada);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void atualizarUsuario_deveLancarResourceNotFound_quandoEmailNaoEncontrado() {
        when(jwtUtil.extrairEmailToken("abc")).thenReturn("sem@mail.com");
        when(usuarioRepository.findByEmail("sem@mail.com")).thenReturn(Optional.empty());

        UsuarioDTO entrada = UsuarioDTO.builder().email("qualquer@mail.com").build();

        assertThatThrownBy(() -> usuarioService.atualizarUsuario("Bearer abc", entrada))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void atualizarEndereco_devePersistirEnderecoAtualizado() {
        EnderecoDTO entrada = EnderecoDTO.builder().rua("Nova").numero(123L).build();
        Endereco antigo = Endereco.builder().id(1L).rua("Antiga").numero(1L).build();
        Endereco atualizado = Endereco.builder().id(1L).rua("Nova").numero(123L).build();
        EnderecoDTO esperado = EnderecoDTO.builder().id(1L).rua("Nova").numero(123L).build();

        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(antigo));
        when(usuarioConverter.atualizarEndereco(entrada, antigo)).thenReturn(atualizado);
        when(enderecoRepository.save(atualizado)).thenReturn(atualizado);
        when(usuarioConverter.paraEnderecoDTO(atualizado)).thenReturn(esperado);

        EnderecoDTO resultado = usuarioService.atualizarEndereco(1L, entrada);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void atualizarTelefone_devePersistirTelefoneAtualizado() {
        TelefoneDTO entrada = TelefoneDTO.builder().numero("9999").ddd("11").build();
        Telefone antigo = Telefone.builder().id(1L).numero("8888").ddd("11").build();
        Telefone atualizado = Telefone.builder().id(1L).numero("9999").ddd("11").build();
        TelefoneDTO esperado = TelefoneDTO.builder().id(1L).numero("9999").ddd("11").build();

        when(telefoneRepository.findById(1L)).thenReturn(Optional.of(antigo));
        when(usuarioConverter.atualizarTelefone(entrada, antigo)).thenReturn(atualizado);
        when(telefoneRepository.save(atualizado)).thenReturn(atualizado);
        when(usuarioConverter.paraTelefoneDTO(atualizado)).thenReturn(esperado);

        TelefoneDTO resultado = usuarioService.atualizarTelefone(1L, entrada);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void cadastraEndereco_deveSalvarParaUsuario() {
        String token = "Bearer token";
        EnderecoDTO entrada = EnderecoDTO.builder().rua("Rua A").numero(10L).build();
        Usuario usuario = Usuario.builder().id(9L).email("u@mail.com").build();
        Endereco endereco = Endereco.builder().rua("Rua A").numero(10L).usuarioId(9L).build();
        Endereco salvo = Endereco.builder().id(3L).rua("Rua A").numero(10L).usuarioId(9L).build();
        EnderecoDTO esperado = EnderecoDTO.builder().id(3L).rua("Rua A").numero(10L).build();

        when(jwtUtil.extrairEmailToken("token")).thenReturn("u@mail.com");
        when(usuarioRepository.findByEmail("u@mail.com")).thenReturn(Optional.of(usuario));
        when(usuarioConverter.paraEnderecoEntity(entrada, 9L)).thenReturn(endereco);
        when(enderecoRepository.save(endereco)).thenReturn(salvo);
        when(usuarioConverter.paraEnderecoDTO(salvo)).thenReturn(esperado);

        EnderecoDTO resultado = usuarioService.cadastraEndereco(token, entrada);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void cadastraTelefone_deveSalvarParaUsuario() {
        String token = "Bearer token";
        TelefoneDTO entrada = TelefoneDTO.builder().numero("1234").ddd("31").build();
        Usuario usuario = Usuario.builder().id(9L).email("u@mail.com").build();
        Telefone telefone = Telefone.builder().numero("1234").ddd("31").usuarioId(9L).build();
        Telefone salvo = Telefone.builder().id(4L).numero("1234").ddd("31").usuarioId(9L).build();
        TelefoneDTO esperado = TelefoneDTO.builder().id(4L).numero("1234").ddd("31").build();

        when(jwtUtil.extrairEmailToken("token")).thenReturn("u@mail.com");
        when(usuarioRepository.findByEmail("u@mail.com")).thenReturn(Optional.of(usuario));
        when(usuarioConverter.paraTelefoneEntity(entrada, 9L)).thenReturn(telefone);
        when(telefoneRepository.save(telefone)).thenReturn(salvo);
        when(usuarioConverter.paraTelefoneDTO(salvo)).thenReturn(esperado);

        TelefoneDTO resultado = usuarioService.cadastraTelefone(token, entrada);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void deleteById_deveRetornarDTODeletado() {
        Usuario deletado = Usuario.builder().id(1L).email("a@mail.com").build();
        UsuarioDTO esperado = UsuarioDTO.builder().email("a@mail.com").build();

        when(usuarioRepository.deleteUsuarioById(1L)).thenReturn(deletado);
        when(usuarioConverter.paraUsuarioDTO(deletado)).thenReturn(esperado);

        UsuarioDTO resultado = usuarioService.deleteById(1L);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void buscarTodosUsuarios_deveRetornarListaConvertida() {
        List<Usuario> usuarios = List.of(Usuario.builder().id(1L).build());
        List<UsuarioDTO> esperado = List.of(UsuarioDTO.builder().nome("n").build());

        when(usuarioRepository.findAll()).thenReturn(usuarios);
        when(usuarioConverter.paraTodosUsuariosDTO(usuarios)).thenReturn(esperado);

        List<UsuarioDTO> resultado = usuarioService.buscarTodosUsuarios();

        assertThat(resultado).isEqualTo(esperado);
    }
}
