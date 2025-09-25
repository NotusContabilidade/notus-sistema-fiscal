package com.notus.contabil.sistema_fiscal.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.ParametrosSN;
import com.notus.contabil.sistema_fiscal.repository.CalculoRepository;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.DocumentoRepository;
import com.notus.contabil.sistema_fiscal.repository.ParametrosSNRepository;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ParametrosSNRepository parametrosSNRepository;
    // Repositórios injetados para permitir a exclusão em cascata
    @Autowired private CalculoRepository calculoRepository;
    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private TaskRepository taskRepository;

    // --- DTOs para comunicação clara e segura ---
    public record ClienteDashboardDTO(Cliente cliente, ParametrosSN parametros) {}
    public record NovoClienteDTO(String cnpj, String razaoSocial, double rbt12, double folha12m, String email, String regimeTributario) {}
    public record ParametrosUpdateDTO(double rbt12, double folha12m) {}
    public record ClienteListaDTO(Long id, String cnpj, String razaoSocial) {}
    // NOVO DTO para atualização de dados cadastrais
    public record ClienteUpdateDTO(String razaoSocial, String cnpj, String email, String regimeTributario) {}


    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<?> buscarClientePorCnpj(@RequestParam String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        Optional<Cliente> clienteOpt = clienteRepository.findByCnpj(cnpjLimpo);
        if (clienteOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Cliente cliente = clienteOpt.get();
        Optional<ParametrosSN> parametrosOpt = parametrosSNRepository.findByClienteId(cliente.getId());
        return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.orElse(null)));
    }

    @Transactional(readOnly = true)
    @GetMapping("/id/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) return ResponseEntity.notFound().build();

        Cliente cliente = clienteOpt.get();
        Optional<ParametrosSN> parametrosOpt = parametrosSNRepository.findByClienteId(id);
        return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.orElse(null)));
    }
    
    @GetMapping("/todos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ClienteListaDTO>> getAllClientes() {
        List<ClienteListaDTO> listaClientes = clienteRepository.findAll().stream()
                .map(cliente -> new ClienteListaDTO(cliente.getId(), cliente.getCnpj(), cliente.getRazaoSocial()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(listaClientes);
    }

    @GetMapping("/busca")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ClienteListaDTO>> buscarClientes(@RequestParam("q") String query) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        String cnpjQuery = query.replaceAll("[^\\d]", "");

        // Busca por Razão Social
        List<Cliente> porRazaoSocial = clienteRepository.findByRazaoSocialContainingIgnoreCase(query);

        // Busca por CNPJ se a query contiver números
        List<Cliente> porCnpj = cnpjQuery.isEmpty() ? List.of() : clienteRepository.findByCnpjContaining(cnpjQuery);

        // Combina as listas, remove duplicatas e mapeia para o DTO
        List<ClienteListaDTO> resultado = Stream.concat(porRazaoSocial.stream(), porCnpj.stream())
                .distinct()
                .map(c -> new ClienteListaDTO(c.getId(), c.getCnpj(), c.getRazaoSocial()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @Transactional
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<?> salvarCliente(@RequestBody NovoClienteDTO novoClienteDTO) {
        String cnpjLimpo = novoClienteDTO.cnpj().replaceAll("[^0-9]", "");
        if ("SIMPLES_NACIONAL".equalsIgnoreCase(novoClienteDTO.regimeTributario()) && novoClienteDTO.rbt12() > 4_800_000.00) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Não é permitido cadastrar clientes no Simples Nacional com RBT12 acima de R$ 4.800.000,00."));
        }
        if (clienteRepository.findByCnpj(cnpjLimpo).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Já existe um cliente com esse CNPJ."));
        }
        Cliente novoCliente = new Cliente();
        novoCliente.setCnpj(cnpjLimpo);
        novoCliente.setRazaoSocial(novoClienteDTO.razaoSocial());
        novoCliente.setEmail(novoClienteDTO.email());
        novoCliente.setRegimeTributario(novoClienteDTO.regimeTributario());
        Cliente clienteSalvo = clienteRepository.save(novoCliente);

        ParametrosSN parametrosParaSalvar = new ParametrosSN();
        parametrosParaSalvar.setCliente(clienteSalvo);
        parametrosParaSalvar.setRbt12Atual(novoClienteDTO.rbt12());
        parametrosParaSalvar.setFolhaPagamento12mAtual(novoClienteDTO.folha12m());
        parametrosSNRepository.save(parametrosParaSalvar);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ClienteDashboardDTO(clienteSalvo, parametrosParaSalvar));
    }

    // --- 1. CORREÇÃO: ENDPOINT PARA ATUALIZAR PARÂMETROS ---
    @Transactional
    @PutMapping("/{id}/parametros")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<?> atualizarParametros(@PathVariable Long id, @RequestBody ParametrosUpdateDTO dto) {
        ParametrosSN parametros = parametrosSNRepository.findByClienteId(id)
            .orElseThrow(() -> new RuntimeException("Parâmetros não encontrados para o cliente ID: " + id));
        
        parametros.setRbt12Atual(dto.rbt12());
        parametros.setFolhaPagamento12mAtual(dto.folha12m());
        
        ParametrosSN parametrosSalvos = parametrosSNRepository.save(parametros);
        return ResponseEntity.ok(parametrosSalvos);
    }

    // --- 2. NOVO: ENDPOINT PARA ATUALIZAR DADOS DO CLIENTE (SÓ ADMIN) ---
    @Transactional
    @PutMapping("/{id}/dados-gerais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarDadosCliente(@PathVariable Long id, @RequestBody ClienteUpdateDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + id));

        cliente.setRazaoSocial(dto.razaoSocial());
        cliente.setCnpj(dto.cnpj().replaceAll("[^0-9]", ""));
        cliente.setEmail(dto.email());
        cliente.setRegimeTributario(dto.regimeTributario());

        Cliente clienteSalvo = clienteRepository.save(cliente);
        return ResponseEntity.ok(clienteSalvo);
    }
    
    // --- 3. NOVO: ENDPOINT PARA DELETAR CLIENTE (SÓ ADMIN) ---
    @Transactional
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Lógica para deletar dados dependentes ANTES de deletar o cliente
        // para evitar erros de chave estrangeira (ForeignKeyViolationException)
        calculoRepository.deleteAll(calculoRepository.findAllByClienteIdOrderByAnoReferenciaDescMesReferenciaDesc(id));
        documentoRepository.deleteAll(documentoRepository.findByClienteId(id));
        taskRepository.deleteAll(taskRepository.findByClienteId(id));
        parametrosSNRepository.findByClienteId(id).ifPresent(parametrosSNRepository::delete);
        
        // Finalmente, deleta o cliente
        clienteRepository.deleteById(id);
        
        return ResponseEntity.noContent().build(); // Retorna 204 No Content, indicando sucesso na exclusão
    }
}