package com.barrilli.usuario.service;

import com.barrilli.usuario.controller.dto.response.ViaCepResponse;
import com.barrilli.usuario.infra.clients.ViaCepClient;
import com.barrilli.usuario.infra.exceptions.IllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViaCepServiceTest {

    @Mock
    ViaCepClient viaCepClient;

    @InjectMocks
    ViaCepService viaCepService;

    @Test
    void removeHifenEEspacosAntesDeConsultar() {
        var resposta = new ViaCepResponse("50000-000", "Rua A", null, null, "Centro", "Recife", "PE", null, null, null, null, "81", null);
        when(viaCepClient.buscaDadosEndereco("50000000")).thenReturn(resposta);

        assertThat(viaCepService.buscaDadosEndereco(" 50000-000 ")).isEqualTo(resposta);
    }

    @Test
    void cepComMenosDeOitoDigitosEInvalidoENaoConsulta() {
        // Precisa ser a IllegalArgumentException do projeto: é ela que o GlobalExceptionHandler traduz para 400
        assertThatThrownBy(() -> viaCepService.buscaDadosEndereco("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP inválido");
        verify(viaCepClient, never()).buscaDadosEndereco(anyString());
    }

    @Test
    void cepComLetrasEInvalido() {
        assertThatThrownBy(() -> viaCepService.buscaDadosEndereco("5000a000"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void falhaDoViaCepViraErroInesperado() {
        when(viaCepClient.buscaDadosEndereco("50000000")).thenThrow(new IllegalStateException("timeout"));

        assertThatThrownBy(() -> viaCepService.buscaDadosEndereco("50000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro inesperado")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
