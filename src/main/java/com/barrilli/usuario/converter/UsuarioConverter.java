package com.barrilli.usuario.converter;

import com.barrilli.usuario.controller.dto.EnderecoDTO;
import com.barrilli.usuario.controller.dto.TelefoneDTO;
import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.infra.model.Endereco;
import com.barrilli.usuario.infra.model.Telefone;
import com.barrilli.usuario.infra.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioConverter {

    public Usuario paraUsuario(UsuarioDTO usuarioDTO) {
        return Usuario.builder()
                .nome(usuarioDTO.nome())
                .email(usuarioDTO.email())
                .senha(usuarioDTO.senha())
                .enderecos(usuarioDTO.enderecos() != null ?
                        paraListaEndereco(usuarioDTO.enderecos()) : null)
                .telefones(usuarioDTO.telefones() != null ?
                        paraListaTelefone(usuarioDTO.telefones()) : null)
                .build();
    }

    public List<Endereco> paraListaEndereco(List<EnderecoDTO> enderecoDTOS) {
        return enderecoDTOS.stream().map(this::paraEndereco).toList();
    }

    public Endereco paraEndereco(EnderecoDTO enderecoDTO) {
        return Endereco.builder()
                .id(enderecoDTO.id())
                .rua(enderecoDTO.rua())
                .cep(enderecoDTO.cep())
                .complemento(enderecoDTO.complemento())
                .cidade(enderecoDTO.cidade())
                .estado(enderecoDTO.estado())
                .numero(enderecoDTO.numero())
                .build();
    }

    private List<Telefone> paraListaTelefone(List<TelefoneDTO> telefonesDtos) {
        return telefonesDtos.stream().map(this::paraTelefone).toList();
    }

    private Telefone paraTelefone(TelefoneDTO telefoneDTO) {
        return Telefone.builder()
                .id(telefoneDTO.id())
                .ddd(telefoneDTO.ddd())
                .numero(telefoneDTO.numero())
                .build();
    }

    public UsuarioDTO paraUsuarioDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .senha(null)
                .enderecos(usuario.getEnderecos() != null ?
                        paraListaEnderecoDTO(usuario.getEnderecos()) : null)
                .telefones(usuario.getTelefones() != null ?
                        paraListaTelefoneDTO(usuario.getTelefones()) : null)
                .build();
    }

    public List<EnderecoDTO> paraListaEnderecoDTO(List<Endereco> endereco) {
        return endereco.stream().map(this::paraEnderecoDTO).toList();
    }

    public EnderecoDTO paraEnderecoDTO(Endereco endereco) {
        return EnderecoDTO.builder()
                .id(endereco.getId())
                .rua(endereco.getRua())
                .cep(endereco.getCep())
                .complemento(endereco.getComplemento())
                .cidade(endereco.getCidade())
                .estado(endereco.getEstado())
                .numero(endereco.getNumero())
                .build();
    }

    private List<TelefoneDTO> paraListaTelefoneDTO(List<Telefone> telefones) {
        return telefones.stream().map(this::paraTelefoneDTO).toList();
    }

    public TelefoneDTO paraTelefoneDTO(Telefone telefone) {
        return TelefoneDTO.builder()
                .id(telefone.getId())
                .ddd(telefone.getDdd())
                .numero(telefone.getNumero())
                .build();
    }

    public List<UsuarioDTO> paraTodosUsuariosDTO(List<Usuario> usuario) {
        return usuario.stream().map(this::paraUsuarioDTO).toList();
    }

    public Usuario atualizaUsuario(UsuarioDTO usuarioDTO, Usuario usuario) {
        return Usuario.builder()
                .nome(usuarioDTO.nome() != null ? usuarioDTO.nome() : usuario.getNome())
                .id(usuario.getId())
                .senha(usuarioDTO.senha() != null ? usuarioDTO.senha() : usuario.getSenha())
                .email(usuarioDTO.email() != null ? usuarioDTO.email() : usuario.getEmail())
                .enderecos(usuario.getEnderecos())
                .telefones(usuario.getTelefones())
                .build();
    }

    public Endereco atualizarEndereco(EnderecoDTO enderecoDTO, Endereco endereco) {
        return Endereco.builder()
                .id(endereco.getId())
                .rua(enderecoDTO.rua() != null ? enderecoDTO.rua() : endereco.getRua())
                .numero(enderecoDTO.numero() != null ? enderecoDTO.numero() : endereco.getNumero())
                .cidade(enderecoDTO.cidade() != null ? enderecoDTO.cidade() : endereco.getCidade())
                .cep(enderecoDTO.cep() != null ? enderecoDTO.cep() : endereco.getCep())
                .complemento(enderecoDTO.complemento() != null ? enderecoDTO.complemento() : endereco.getComplemento())
                .estado(enderecoDTO.estado() != null ? enderecoDTO.estado() : endereco.getEstado())
                .usuarioId(endereco.getUsuarioId())
                .build();
    }

    public Telefone atualizarTelefone(TelefoneDTO telefoneDTO, Telefone telefone) {
        return Telefone.builder()
                .id(telefone.getId())
                .ddd(telefoneDTO.ddd() != null ? telefoneDTO.ddd() : telefone.getDdd())
                .numero(telefoneDTO.numero() != null ? telefoneDTO.numero() : telefone.getNumero())
                .usuarioId(telefone.getUsuarioId())
                .build();
    }

    public Endereco paraEnderecoEntity(EnderecoDTO enderecoDTO, Long usuarioId) {
        return Endereco.builder()
                .rua(enderecoDTO.rua())
                .cep(enderecoDTO.cep())
                .complemento(enderecoDTO.complemento())
                .cidade(enderecoDTO.cidade())
                .estado(enderecoDTO.estado())
                .numero(enderecoDTO.numero())
                .usuarioId(usuarioId)
                .build();
    }

    public Telefone paraTelefoneEntity(TelefoneDTO telefoneDTO, Long usuarioId) {
        return Telefone.builder()
                .numero(telefoneDTO.numero())
                .usuarioId(usuarioId)
                .ddd(telefoneDTO.ddd())
                .build();
    }
}
