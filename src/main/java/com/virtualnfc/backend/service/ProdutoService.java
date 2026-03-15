package com.virtualnfc.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.virtualnfc.backend.entity.Produto;
import com.virtualnfc.backend.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {
    private final ProdutoRepository repository;

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
