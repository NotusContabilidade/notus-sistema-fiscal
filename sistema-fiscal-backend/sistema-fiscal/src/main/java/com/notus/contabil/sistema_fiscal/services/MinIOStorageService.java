package com.notus.contabil.sistema_fiscal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
public class MinIOStorageService implements StorageService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String salvar(MultipartFile arquivo, String pathPrefix) {
        try {
            String nomeArquivoOriginal = arquivo.getOriginalFilename();
            String extensao = nomeArquivoOriginal.substring(nomeArquivoOriginal.lastIndexOf("."));
            String nomeArquivoUnico = UUID.randomUUID().toString() + extensao;
            String storageKey = pathPrefix + nomeArquivoUnico;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(arquivo.getInputStream(), arquivo.getSize()));

            return storageKey;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar arquivo no storage", e);
        }
    }

    @Override
    public URL gerarUrlParaDownload(String storageKey) {
         try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10)) // Link válido por 10 minutos
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar URL de download", e);
        }
    }

    @Override
    public void deletar(String storageKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar arquivo do storage", e);
        }
    }
}