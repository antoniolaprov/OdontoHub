package com.g4.odontohub.manutencao.application;

import com.g4.odontohub.manutencao.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoService {

    private static final int DIAS_ANTECEDENCIA_ALERTA = 7;

    private final EquipamentoRepository equipamentoRepository;
    private final RegistroManutencaoRepository manutencaoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository,
                               RegistroManutencaoRepository manutencaoRepository) {
        this.equipamentoRepository = equipamentoRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    public void cadastrarEquipamento(String nome, StatusEquipamento status, Integer periodicidadeDias) {
        Equipamento equipamento = Equipamento.criar(nome, status, periodicidadeDias);
        equipamentoRepository.salvar(equipamento);
    }

    public void iniciarManutencaoCorretiva(String nomeEquipamento) {
        Equipamento equipamento = equipamentoRepository.buscarPorNome(nomeEquipamento);
        equipamento.iniciarManutencao();
        equipamentoRepository.salvar(equipamento);
    }

    public void vincularEquipamentoAProcedimento(String nomeEquipamento) {
        Equipamento equipamento = equipamentoRepository.buscarPorNome(nomeEquipamento);
        equipamento.verificarDisponibilidadeParaProcedimento();
    }

    public void concluirManutencao(String nomeEquipamento, LocalDate dataConclusao) {
        Equipamento equipamento = equipamentoRepository.buscarPorNome(nomeEquipamento);
        equipamento.concluirManutencao(dataConclusao);
        equipamentoRepository.salvar(equipamento);
    }

    public void registrarManutencao(String nomeEquipamento, TipoManutencao tipo,
                                     LocalDate dataInicio, String responsavelTecnico,
                                     String descricao, BigDecimal custo) {
        Equipamento equipamento = equipamentoRepository.buscarPorNome(nomeEquipamento);
        RegistroManutencao registro = RegistroManutencao.criar(
                equipamento.getId(), tipo, dataInicio, responsavelTecnico, descricao, custo);
        manutencaoRepository.salvar(registro);
    }

    public List<AlertaManutencao> verificarAlertasManutencao(String nomeEquipamento, LocalDate dataVerificacao) {
        Equipamento equipamento = equipamentoRepository.buscarPorNome(nomeEquipamento);
        List<AlertaManutencao> alertas = new ArrayList<>();
        if (equipamento.alertaManutencaoProxima(dataVerificacao, DIAS_ANTECEDENCIA_ALERTA)) {
            long dias = equipamento.diasRestantesManutencao(dataVerificacao);
            alertas.add(AlertaManutencao.criar(equipamento.getNome(), dias));
        }
        return alertas;
    }

    public void definirPeriodicidade(String nomeEquipamento, Integer dias) {
        Equipamento equipamento = equipamentoRepository.buscarPorNome(nomeEquipamento);
        equipamento.definirPeriodicidade(dias);
        equipamentoRepository.salvar(equipamento);
    }
}