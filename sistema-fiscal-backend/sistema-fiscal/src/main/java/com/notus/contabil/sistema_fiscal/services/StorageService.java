package com.notus.contabil.sistema_fiscal.services;

import org.springframework.web.multipart.MultipartFile;
import java.net.URL;

public interface StorageService {

    String salvar(MultipartFile arquivo, String pathPrefix);

    URL gerarUrlParaDownload(String storageKey);

    void deletar(String storageKey);
}