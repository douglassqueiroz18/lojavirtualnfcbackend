package com.virtualnfc.backend.service;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class StorageService {

    private final String bucketName;
    private final String endpoint;
    private final S3Presigner presigner;

    public StorageService(
            @Value("${aws.access.key}") String accessKey,
            @Value("${aws.secret.key}") String secretKey,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.endpoint}") String endpoint,
            @Value("${aws.s3.bucket}") String bucketName) {
        
        this.bucketName = bucketName;
        this.endpoint = endpoint;

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public Map<String, String> gerarDadosUpload(String nomeArquivo, String contentType) {
        String caminhoCompleto = "produtos-loja/" + System.currentTimeMillis() + "_" + nomeArquivo;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(caminhoCompleto)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        String urlAssinada = presigner.presignPutObject(presignRequest).url().toString();

        String urlPublica = endpoint.replace("https://", "https://" + bucketName + ".") + "/" + caminhoCompleto;

        return Map.of(
                "uploadUrl", urlAssinada,
                "publicUrl", urlPublica
        );
    }
}