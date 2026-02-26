package com.barrilli.usuario.infra.exceptions.handler;

import java.time.Instant;

/**
 * Payload padronizado para respostas de erro da API.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
