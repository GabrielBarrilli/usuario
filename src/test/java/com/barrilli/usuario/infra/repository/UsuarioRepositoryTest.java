package com.barrilli.usuario.infra.repository;

import com.barrilli.usuario.infra.model.Endereco;
import com.barrilli.usuario.infra.model.Usuario;
import com.barrilli.usuario.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Roda o schema.sql de verdade num PostgreSQL, então também valida o DDL
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(UserDetailsServiceImpl.class)
class UsuarioRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    EnderecoRepository enderecoRepository;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    TestEntityManager entityManager;

    private Usuario salva(String email) {
        return usuarioRepository.save(Usuario.builder().nome("Ana").email(email).senha("$2a$hash").build());
    }

    @Test
    void buscaEConfereEmail() {
        salva("ana@t.com");

        assertThat(usuarioRepository.existsByEmail("ana@t.com")).isTrue();
        assertThat(usuarioRepository.existsByEmail("bob@t.com")).isFalse();
        assertThat(usuarioRepository.findByEmail("ana@t.com")).get().extracting(Usuario::getNome).isEqualTo("Ana");
    }

    @Test
    void salvaUsuarioComEnderecoEmCascata() {
        var usuario = Usuario.builder().nome("Ana").email("ana@t.com").senha("x")
                .enderecos(List.of(Endereco.builder().rua("Rua A").cidade("Recife").estado("PE").cep("50000000").build()))
                .build();

        var salvo = usuarioRepository.saveAndFlush(usuario);
        entityManager.clear(); // relê do banco: o usuario_id é gravado por um UPDATE do @JoinColumn

        assertThat(enderecoRepository.findAll()).singleElement()
                .extracting(Endereco::getUsuarioId).isEqualTo(salvo.getId());
    }

    @Test
    void deletaPorEmailEPorId() {
        salva("ana@t.com");
        var bob = salva("bob@t.com");

        usuarioRepository.deleteByEmail("ana@t.com");
        var deletado = usuarioRepository.deleteUsuarioById(bob.getId());

        assertThat(deletado.getEmail()).isEqualTo("bob@t.com");
        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void userDetailsUsaEmailESenhaDoBanco() {
        salva("ana@t.com");

        var userDetails = userDetailsService.loadUserByUsername("ana@t.com");

        assertThat(userDetails.getUsername()).isEqualTo("ana@t.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$hash");
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("x@t.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
