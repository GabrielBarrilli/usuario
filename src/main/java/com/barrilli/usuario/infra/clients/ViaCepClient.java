package com.barrilli.usuario.infra.clients;

import com.barrilli.usuario.controller.dto.response.ViaCepResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "via-cep", url = "${via-cep.url}")
public interface ViaCepClient {

    @GetMapping("/ws/{cep}/json/")
    ViaCepResponse buscaDadosEndereco(@PathVariable("cep") String cep);
}
