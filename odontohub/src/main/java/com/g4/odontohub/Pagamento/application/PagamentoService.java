package com.g4.odontohub.Pagamento.application;

import com.g4.odontohub.Pagamento.domain.Pagamento;
import com.g4.odontohub.Pagamento.domain.PagamentoRepository;

public class PagamentoService {

    private final PagamentoRepository repository;

    public PagamentoService(PagamentoRepository repository) {
        this.repository = repository;
    }

    // Usado pelo 'Quando' do Cucumber
    public void registrar(String pacienteId, double valor) {
        Pagamento pagamento = Pagamento.criar(pacienteId, valor);
        repository.salvar(pagamento);
    }

    // Usado pelo 'Então' do Cucumber para evitar acesso direto ao repositório
    public boolean verificarSeExistePagamento(String pacienteId) {
        return repository.existePorPacienteId(pacienteId);
    }
}