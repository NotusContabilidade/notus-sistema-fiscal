package com.notus.contabil.sistema_fiscal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;


// ✅ SEM IMPORT do AbstractIntegrationTest, pois está no mesmo pacote
@AutoConfigureMockMvc
public class ClienteControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deveSalvarNovoClienteEParametrosComSucesso() throws Exception {
        ClienteController.NovoClienteDTO novoClienteDTO = new ClienteController.NovoClienteDTO(
                "53.624.573/0001-57",
                "EMPRESA DE TESTE LTDA",
                150000.00,
                20000.00
        );

        ResultActions resultActions = mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoClienteDTO)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.cnpj").value("53624573000157"))
                .andExpect(jsonPath("$.cliente.razaoSocial").value("EMPRESA DE TESTE LTDA"))
                .andExpect(jsonPath("$.parametros.rbt12Atual").value(150000.00));
    }
}