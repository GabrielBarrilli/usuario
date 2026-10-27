package com.barrilli.usuario.controller;

import com.barrilli.usuario.controller.dto.response.ViaCepResponse;
import com.barrilli.usuario.infra.clients.ViaCepClient;
import com.barrilli.usuario.infra.security.JwtUtil;
import com.barrilli.usuario.infra.security.SecurityConfig;
import com.barrilli.usuario.service.UsuarioService;
import com.barrilli.usuario.service.ViaCepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ViaCepService real, só o cliente Feign é mock: prova que a exceção que o service lança é a que o handler traduz para 400
@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, ViaCepService.class})
class BuscaCepEndpointTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ViaCepClient viaCepClient;

    @MockitoBean
    UsuarioService usuarioService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    UserDetailsService userDetailsService;

    @Test
    void cepInvalidoResponde400SemConsultarOViaCep() throws Exception {
        mockMvc.perform(get("/v1/usuario/endereco/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("CEP inválido")));
        verify(viaCepClient, never()).buscaDadosEndereco(anyString());
    }

    @Test
    void cepValidoComHifenResponde200() throws Exception {
        when(viaCepClient.buscaDadosEndereco("50000000"))
                .thenReturn(new ViaCepResponse("50000-000", "Rua A", null, null, "Centro", "Recife", "PE", null, null, null, null, "81", null));

        mockMvc.perform(get("/v1/usuario/endereco/50000-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localidade").value("Recife"));
    }
}
