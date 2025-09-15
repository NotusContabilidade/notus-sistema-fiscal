package com.notus.contabil.sistema_fiscal.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.ParametrosSNRepository;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ParametrosSNRepository parametrosSNRepository;
    
    public record ClienteDashboardDTO(Cliente cliente, ParametrosSN parametros) {}
    public record NovoClienteDTO(String cnpj, String razaoSocial, double rbt12, double folha12m) {}
    public record ParametrosUpdateDTO(double rbt12, double folha12m) {}
    public record ClienteListaDTO(Long id, String cnpj, String razaoSocial) {}

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<?> buscarClientePorCnpj(@RequestParam String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        Optional<Cliente> clienteOpt = clienteRepository.findByCnpj(cnpjLimpo);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Cliente cliente = clienteOpt.get();
        Optional<ParametrosSN> parametrosOpt = parametrosSNRepository.findByClienteId(cliente.getId());
        return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.orElse(null)));
    }
    
    @Transactional(readOnly = true)
    @GetMapping("/id/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<List<ClienteListaDTO>> buscarPorRazaoSocial(@RequestParam("razaoSocial") String razaoSocial) {
        List<ClienteListaDTO> lista = clienteRepository.findByRazaoSocialContainingIgnoreCase(razaoSocial)
            .stream()
            .map(c -> new ClienteListaDTO(c.getId(), c.getCnpj(), c.getRazaoSocial()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> salvarCliente(@RequestBody NovoClienteDTO novoClienteDTO) {
        String cnpjLimpo = novoClienteDTO.cnpj().replaceAll("[^0-9]", "");
        Cliente clienteParaSalvar = clienteRepository.findByCnpj(cnpjLimpo)
            .orElseGet(() -> {
                Cliente novoCliente = new Cliente();
                novoCliente.setCnpj(cnpjLimpo);
                novoCliente.setRazaoSocial(novoClienteDTO.razaoSocial());
                return clienteRepository.save(novoCliente);
            });
        if (clienteParaSalvar.getId() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        ParametrosSN parametrosParaSalvar = new ParametrosSN();
        parametrosParaSalvar.setCliente(clienteParaSalvar);
        parametrosParaSalvar.setRbt12Atual(novoClienteDTO.rbt12());
        parametrosParaSalvar.setFolhaPagamento12mAtual(novoClienteDTO.folha12m());
        parametrosSNRepository.save(parametrosParaSalvar);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteParaSalvar);
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @RequestBody NovoClienteDTO dadosCliente) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Cliente cliente = clienteOpt.get();
        String cnpjLimpo = dadosCliente.cnpj().replaceAll("[^0-9]", "");
        cliente.setCnpj(cnpjLimpo);
        cliente.setRazaoSocial(dadosCliente.razaoSocial());
        clienteRepository.save(cliente);
        Optional<ParametrosSN> parametrosOpt = parametrosSNRepository.findByClienteId(cliente.getId());
        ParametrosSN parametros = parametrosOpt.orElseGet(() -> {
            ParametrosSN novoParametros = new ParametrosSN();
            novoParametros.setCliente(cliente);
            return novoParametros;
        });
        parametros.setRbt12Atual(dadosCliente.rbt12());
parametros.setFolhaPagamento12mAtual(dadosCliente.folha12m());
        parametrosSNRepository.save(parametros);
        return ResponseEntity.ok(cliente);
    }
}