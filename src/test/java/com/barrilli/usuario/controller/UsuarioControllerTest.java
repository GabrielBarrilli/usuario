package com.barrilli.usuario.controller;

import com.barrilli.usuario.controller.dto.UsuarioDTO;
import com.barrilli.usuario.controller.dto.response.ViaCepResponse;
import com.barrilli.usuario.infra.exceptions.ConflictException;
import com.barrilli.usuario.infra.exceptions.ResourceNotFoundException;
import com.barrilli.usuario.infra.exceptions.UnauthorizedException;
import com.barrilli.usuario.infra.security.JwtUtil;
import com.barrilli.usuario.infra.security.SecurityConfig;
import com.barrilli.usuario.service.UsuarioService;
import com.barrilli.usuario.service.ViaCepService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    private static final String AUTH = "Bearer tok";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioService usuarioService;

    @MockitoBean
    ViaCepService viaCepService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    UserDetailsService userDetailsService;

    @BeforeEach
    void tokenValido() {
        when(jwtUtil.extrairEmailToken("tok")).thenReturn("ana@t.com");
        when(jwtUtil.validateToken("tok", "ana@t.com")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("ana@t.com"))
                .thenReturn(User.withUsername("ana@t.com").password("hash").build());
    }

    @Test
    void loginSemTokenDevolveOJwt() throws Exception {
        when(usuarioService.autenticarUsuario(any())).thenReturn("Bearer jwt");

        mockMvc.perform(post("/v1/usuario/login").contentType(JSON).content("{\"email\":\"ana@t.com\",\"senha\":\"s3nha\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bearer jwt"));
    }

    @Test
    void loginComSenhaErradaDevolve401() throws Exception {
        when(usuarioService.autenticarUsuario(any())).thenThrow(new UnauthorizedException("Usuário ou senha inválidos."));

        mockMvc.perform(post("/v1/usuario/login").contentType(JSON).content("{\"email\":\"ana@t.com\",\"senha\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos."));
    }

    @Test
    void cadastroEPublicoENaoDevolveASenha() throws Exception {
        when(usuarioService.salvar(any()))
                .thenReturn(UsuarioDTO.builder().nome("Ana").email("ana@t.com").senha("$2a$hash").build());

        mockMvc.perform(post("/v1/usuario").contentType(JSON).content("{\"nome\":\"Ana\",\"email\":\"ana@t.com\",\"senha\":\"s3nha\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@t.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void cadastroDuplicadoDevolve409() throws Exception {
        when(usuarioService.salvar(any())).thenThrow(new ConflictException("Email já cadastrado."));

        mockMvc.perform(post("/v1/usuario").contentType(JSON).content("{\"email\":\"ana@t.com\",\"senha\":\"s3nha\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rotaProtegidaSemTokenDevolve401() throws Exception {
        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void tokenExpiradoDevolve401() throws Exception {
        when(jwtUtil.extrairEmailToken("expirado")).thenThrow(new ExpiredJwtException(null, null, "expirado"));

        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com").header("Authorization", "Bearer expirado"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void tokenComAssinaturaFalsaDevolve401() throws Exception {
        when(jwtUtil.extrairEmailToken("forjado")).thenThrow(new SignatureException("assinatura inválida"));

        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com").header("Authorization", "Bearer forjado"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void tokenMalformadoDevolve401() throws Exception {
        when(jwtUtil.extrairEmailToken("lixo")).thenThrow(new MalformedJwtException("malformado"));

        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com").header("Authorization", "Bearer lixo"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void tokenVazioDevolve401() throws Exception {
        // É o que o JwtUtil real lança para "" (ver JwtUtilTest.tokenVazioLancaIllegalArgument)
        when(jwtUtil.extrairEmailToken("")).thenThrow(new IllegalArgumentException("JWT String argument cannot be null or empty."));

        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com").header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void tokenDeUsuarioApagadoDevolve401() throws Exception {
        when(jwtUtil.extrairEmailToken("apagado")).thenReturn("apagado@t.com");
        when(userDetailsService.loadUserByUsername("apagado@t.com")).thenThrow(new UsernameNotFoundException("apagado@t.com"));

        mockMvc.perform(get("/v1/usuario/email").param("email", "apagado@t.com").header("Authorization", "Bearer apagado"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void rotasForaDaListaPublicaExigemToken() throws Exception {
        // Sem token, o que não é cadastro, login ou CEP dá 401 (inclusive GET /v1/auth, regra morta removida).
        // Os públicos passando sem token estão em loginSemTokenDevolveOJwt, cadastroEPublicoENaoDevolveASenha e buscaCepEPublica
        mockMvc.perform(get("/v1/auth")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/v1/usuario").contentType(JSON).content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/v1/usuario/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/usuario/endereco").contentType(JSON).content("{}")).andExpect(status().isUnauthorized());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void buscaPorEmailComTokenNaoDevolveASenha() throws Exception {
        when(usuarioService.findUsuarioByEmail("ana@t.com"))
                .thenReturn(UsuarioDTO.builder().nome("Ana").email("ana@t.com").senha("$2a$hash").build());

        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void usuarioInexistenteDevolve404() throws Exception {
        when(usuarioService.findUsuarioByEmail("x@t.com")).thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        mockMvc.perform(get("/v1/usuario/email").param("email", "x@t.com").header("Authorization", AUTH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/v1/usuario/email"));
    }

    @Test
    void deletaPorIdEPorEmail() throws Exception {
        mockMvc.perform(delete("/v1/usuario/7").header("Authorization", AUTH)).andExpect(status().isOk());
        mockMvc.perform(delete("/v1/usuario/email/ana@t.com").header("Authorization", AUTH)).andExpect(status().isOk());

        verify(usuarioService).deleteById(7L);
        verify(usuarioService).deleteByEmail("ana@t.com");
    }

    @Test
    void cadastraEnderecoETelefoneComToken() throws Exception {
        mockMvc.perform(post("/v1/usuario/endereco").header("Authorization", AUTH).contentType(JSON).content("{\"rua\":\"A\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/v1/usuario/telefone").header("Authorization", AUTH).contentType(JSON).content("{\"numero\":\"999\"}"))
                .andExpect(status().isOk());

        verify(usuarioService).cadastraEndereco(eq(AUTH), any());
        verify(usuarioService).cadastraTelefone(eq(AUTH), any());
    }

    @Test
    void atualizaUsuarioEnderecoETelefone() throws Exception {
        mockMvc.perform(put("/v1/usuario").header("Authorization", AUTH).contentType(JSON).content("{\"nome\":\"Ana B\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/v1/usuario/endereco").param("id", "1").header("Authorization", AUTH).contentType(JSON).content("{\"cidade\":\"Recife\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/v1/usuario/telefone").param("telefoneId", "2").header("Authorization", AUTH).contentType(JSON).content("{\"ddd\":\"81\"}"))
                .andExpect(status().isOk());

        verify(usuarioService).atualizarUsuario(eq(AUTH), any());
        verify(usuarioService).atualizarEndereco(eq(1L), any());
        verify(usuarioService).atualizarTelefone(eq(2L), any());
    }

    @Test
    void buscaCepEPublica() throws Exception {
        when(viaCepService.buscaDadosEndereco("50000000"))
                .thenReturn(new ViaCepResponse("50000-000", "Rua A", null, null, "Centro", "Recife", "PE", null, null, null, null, "81", null));

        mockMvc.perform(get("/v1/usuario/endereco/50000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localidade").value("Recife"));
    }

    @Test
    void cepInvalidoDevolve400() throws Exception {
        when(viaCepService.buscaDadosEndereco("123"))
                .thenThrow(new com.barrilli.usuario.infra.exceptions.IllegalArgumentException("CEP inválido."));

        mockMvc.perform(get("/v1/usuario/endereco/123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void erroInesperadoDevolve500Generico() throws Exception {
        when(usuarioService.findUsuarioByEmail("ana@t.com")).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/v1/usuario/email").param("email", "ana@t.com").header("Authorization", AUTH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno. Tente novamente mais tarde."));
    }
}
