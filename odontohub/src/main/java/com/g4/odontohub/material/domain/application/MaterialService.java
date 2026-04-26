package com.g4.odontohub.material.domain.application;

import com.g4.odontohub.material.domain.Material;
import com.g4.odontohub.material.domain.MaterialRepository;
import com.g4.odontohub.shared.exception.DomainException;

public class MaterialService {

    private final MaterialRepository repository;

    public MaterialService(MaterialRepository repository) {
        this.repository = repository;
    }

    // Usado pela aplicação e pelo 'Dado' do Cucumber
    public String cadastrar(String nome, int quantidadeInicial) {
        Material material = Material.criar(nome, quantidadeInicial);
        repository.salvar(material);
        return material.getId();
    }

    // Usado pelo 'Quando' do Cucumber
    public void repor(String materialId, int quantidadeReposicao) {
        Material material = repository.buscarPorId(materialId);
        if (material == null) {
            throw new DomainException("Material não encontrado.");
        }
        
        material.repor(quantidadeReposicao);
        repository.salvar(material);
    }

    // Usado pelo 'Então' do Cucumber para evitar acesso direto ao repositório
    public Material buscar(String materialId) {
        return repository.buscarPorId(materialId);
    }
}