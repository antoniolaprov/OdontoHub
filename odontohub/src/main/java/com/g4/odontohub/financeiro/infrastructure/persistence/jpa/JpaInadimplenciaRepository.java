package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public void salvarAcordo(String paciente, Acordo acordo) {
        acordoRepository.save(AcordoJpaEntity.fromDomain(paciente, acordo));
    }

    @Override
    public Acordo acordoDe(String paciente) {
        AcordoJpaEntity e = acordoRepository.findFirstByPacienteOrderByIdDesc(paciente);
        return e == null ? null : e.toDomain();
    }

    @Override
    public long proximoAcordoId() {
        return acordoRepository.proximoId();
    }
}
