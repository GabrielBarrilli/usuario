package com.barrilli.usuario.infra.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100)
    private String nome;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "senha")
    private String senha;

    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    @OneToMany(cascade = CascadeType.ALL)
    private List<Endereco> enderecos;

    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    @OneToMany(cascade = CascadeType.ALL)
    private List<Telefone> telefones;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
