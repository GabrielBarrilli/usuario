package com.barrilli.usuario.converter;

import com.barrilli.usuario.controller.dto.EnderecoDTO;
import com.barrilli.usuario.controller.dto.TelefoneDTO;
import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.infra.model.Endereco;
import com.barrilli.usuario.infra.model.Telefone;
import com.barrilli.usuario.infra.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioConverterTest {

    private final UsuarioConverter converter = new UsuarioConverter();

    private final Endereco endereco = Endereco.builder().id(1L).rua("Rua A").numero(10L).complemento("ap 1")
            .cidade("Recife").estado("PE").cep("50000000").usuarioId(9L).build();
    private final Telefone telefone = Telefone.builder().id(2L).ddd("81").numero("99999999").usuarioId(9L).build();

    @Test
    void dtoParaEntidadeEDeVolta() {
        var dto = UsuarioDTO.builder().nome("Ana").email("ana@t.com").senha("hash")
                .enderecos(List.of(EnderecoDTO.builder().rua("Rua A").cidade("Recife").build()))
                .telefones(List.of(TelefoneDTO.builder().ddd("81").numero("999").build()))
                .build();

        var usuario = converter.paraUsuario(dto);
        var deVolta = converter.paraUsuarioDTO(usuario);

        assertThat(usuario.getEnderecos()).singleElement().extracting(Endereco::getCidade).isEqualTo("Recife");
        assertThat(usuario.getTelefones()).singleElement().extracting(Telefone::getDdd).isEqualTo("81");
        assertThat(deVolta).isEqualTo(dto);
    }

    @Test
    void listasNulasContinuamNulas() {
        var usuario = converter.paraUsuario(UsuarioDTO.builder().email("ana@t.com").build());

        assertThat(usuario.getEnderecos()).isNull();
        assertThat(usuario.getTelefones()).isNull();
        assertThat(converter.paraUsuarioDTO(usuario).enderecos()).isNull();
    }

    @Test
    void atualizaUsuarioSoNosCamposEnviados() {
        var atual = Usuario.builder().id(9L).nome("Ana").email("ana@t.com").senha("hash")
                .enderecos(List.of(endereco)).telefones(List.of(telefone)).build();

        var atualizado = converter.atualizaUsuario(UsuarioDTO.builder().nome("Ana B").build(), atual);

        assertThat(atualizado.getId()).isEqualTo(9L);
        assertThat(atualizado.getNome()).isEqualTo("Ana B");
        assertThat(atualizado.getEmail()).isEqualTo("ana@t.com");
        assertThat(atualizado.getSenha()).isEqualTo("hash");
        assertThat(atualizado.getEnderecos()).containsExactly(endereco);
    }

    @Test
    void atualizaEnderecoSoNosCamposEnviadosEMantemODono() {
        var atualizado = converter.atualizarEndereco(EnderecoDTO.builder().cidade("Olinda").build(), endereco);

        assertThat(atualizado.getId()).isEqualTo(1L);
        assertThat(atualizado.getCidade()).isEqualTo("Olinda");
        assertThat(atualizado.getRua()).isEqualTo("Rua A");
        assertThat(atualizado.getCep()).isEqualTo("50000000");
        assertThat(atualizado.getUsuarioId()).isEqualTo(9L);
    }

    @Test
    void atualizaTelefoneSoNosCamposEnviadosEMantemODono() {
        var atualizado = converter.atualizarTelefone(TelefoneDTO.builder().numero("88888888").build(), telefone);

        assertThat(atualizado.getId()).isEqualTo(2L);
        assertThat(atualizado.getNumero()).isEqualTo("88888888");
        assertThat(atualizado.getDdd()).isEqualTo("81");
        assertThat(atualizado.getUsuarioId()).isEqualTo(9L);
    }

    @Test
    void novoEnderecoETelefoneRecebemODono() {
        var novoEndereco = converter.paraEnderecoEntity(EnderecoDTO.builder().rua("Rua B").build(), 9L);
        var novoTelefone = converter.paraTelefoneEntity(TelefoneDTO.builder().numero("777").build(), 9L);

        assertThat(novoEndereco.getUsuarioId()).isEqualTo(9L);
        assertThat(novoEndereco.getId()).isNull();
        assertThat(novoTelefone.getUsuarioId()).isEqualTo(9L);
    }

    @Test
    void entidadeParaDto() {
        assertThat(converter.paraEnderecoDTO(endereco).cidade()).isEqualTo("Recife");
        assertThat(converter.paraTelefoneDTO(telefone).numero()).isEqualTo("99999999");
        assertThat(converter.paraListaEndereco(List.of(EnderecoDTO.builder().id(3L).build())))
                .singleElement().extracting(Endereco::getId).isEqualTo(3L);
    }
}
