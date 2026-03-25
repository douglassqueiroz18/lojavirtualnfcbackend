package com.virtualnfc.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.virtualnfc.backend.entity.Produto;
import com.virtualnfc.backend.repository.ProdutoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class ProdutoService {
    private final ProdutoRepository repository;
    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;
    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    @Transactional
public void deletar(Long id) {
    Produto produto = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

    if (produto.getImagemUrl() != null && !produto.getImagemUrl().isEmpty()) {
        try {
            String url = produto.getImagemUrl();
            String key = url.substring(url.lastIndexOf(".com/") + 5);
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível deletar no DigitalOcean: " + e.getMessage());
        }
    }

    repository.delete(produto);
}
    public Produto atualizar(Long id, Produto produtoAtualizado) {
    return repository.findById(id)
        .map(produto -> {
            produto.setNome(produtoAtualizado.getNome());
            produto.setPreco(produtoAtualizado.getPreco());
            return repository.save(produto);
        })
        .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }
}
