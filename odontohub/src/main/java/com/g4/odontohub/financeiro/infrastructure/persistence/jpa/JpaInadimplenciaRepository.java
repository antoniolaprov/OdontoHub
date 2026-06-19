package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ContatoCobranca;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link InadimplenciaRepository}. */
@Repository
public class JpaInadimplenciaRepository implements InadimplenciaRepository {

    private final SpringDataVencidaParcelaRepository vencidaRepository;
    private final SpringDataAcordoRepository acordoRepository;

    public JpaInadimplenciaRepository(SpringDataVencidaParcelaRepository vencidaRepository,
                                      SpringDataAcordoRepository acordoRepository) {
        this.vencidaRepository = vencidaRepository;
        this.acordoRepository = acordoRepository;
    }

    @Override
    public void registrarPaciente(String paciente) {
        // O adapter JPA reconstitui pacientes a partir das parcelas/acordos persistidos.
    }

    @Override
    public Set<String> pacientes() {
        Set<String> pacientes = new HashSet<>();
        vencidaRepository.findAll().forEach(v -> pacientes.add(v.getPaciente()));
        return pacientes;
    }

    @Override
    public void salvarVencida(String paciente, ParcelaCobranca parcela) {
        vencidaRepository.save(new VencidaParcelaJpaEntity(paciente, parcela));
    }

    @Override
    public List<ParcelaCobranca> vencidasDe(String paciente) {
        return vencidaRepository.findByPaciente(paciente).stream()
                .map(VencidaParcelaJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void removerVencidas(String paciente) {
        vencidaRepository.deleteAll(vencidaRepository.findByPaciente(paciente));
    }

    @Override
    public void salvarAcordo(String paciente, Acordo acordo) {
        acordoRepository.save(AcordoJpaEntity.fromDomain(paciente, acordo));
    }

    @Override
    public Acordo acordoDe(String paciente) {
        AcordoJpaEntity e = acordoRepository.findFirstByPacienteOrderByIdDesc(paciente);
        return e == null ? null : e.toDomain();
    }

    @Override
    public void salvarContato(String paciente, ContatoCobranca contato) {
        // Contatos de cobranca da F09 sao mantidos em memoria nos testes BDD.
    }

    @Override
    public List<ContatoCobranca> contatosDe(String paciente) {
        return Collections.emptyList();
    }

    @Override
    public long proximoAcordoId() {
        return acordoRepository.proximoId();
    }
}
