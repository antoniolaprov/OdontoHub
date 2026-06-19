package com.g4.odontohub.medicamento.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Medicamento {

    public record AlteracaoMedicamento(String usuario, String campo, String valorAnterior, String valorAtualizado) {}

    private final MedicamentoId id;
    private final String nomeComercial;
    private final String principioAtivo;
    private String categoriaTerapeutica;
    private String classeFarmacologica;
    private String apresentacao;
    private String viaAdministracao;
    private StatusMedicamento status;
    private final List<String> posologiasPadrao = new ArrayList<>();
    private final List<String> contraindicacoes = new ArrayList<>();
    private final List<String> interacoes = new ArrayList<>();
    private final List<AlteracaoMedicamento> historicoAlteracoes = new ArrayList<>();

    public Medicamento(MedicamentoId id, String nomeComercial, String principioAtivo,
                       String categoriaTerapeutica, String classeFarmacologica) {
        this(id, nomeComercial, principioAtivo, categoriaTerapeutica, classeFarmacologica, null, null);
    }

    public Medicamento(MedicamentoId id, String nomeComercial, String principioAtivo,
                       String categoriaTerapeutica, String classeFarmacologica,
                       String apresentacao, String viaAdministracao) {
        this.id = id;
        this.nomeComercial = nomeComercial;
        this.principioAtivo = principioAtivo;
        this.categoriaTerapeutica = categoriaTerapeutica;
        this.classeFarmacologica = classeFarmacologica;
        this.apresentacao = apresentacao;
        this.viaAdministracao = viaAdministracao;
        this.status = StatusMedicamento.ATIVO;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Medicamento reconstituir(MedicamentoId id, String nomeComercial, String principioAtivo,
                                           String categoriaTerapeutica, String classeFarmacologica,
                                           StatusMedicamento status, java.util.List<String> posologias,
                                           java.util.List<String> contraindicacoes, java.util.List<String> interacoes,
                                           String apresentacao, String viaAdministracao,
                                           java.util.List<AlteracaoMedicamento> historicoAlteracoes) {
        Medicamento m = new Medicamento(id, nomeComercial, principioAtivo, categoriaTerapeutica,
                classeFarmacologica, apresentacao, viaAdministracao);
        m.status = status;
        if (posologias != null) {
            posologias.forEach(m::adicionarPosologiaPadrao);
        }
        if (contraindicacoes != null) {
            contraindicacoes.forEach(m::adicionarContraindicacao);
        }
        if (interacoes != null) {
            interacoes.forEach(m::adicionarInteracao);
        }
        if (historicoAlteracoes != null) {
            m.historicoAlteracoes.addAll(historicoAlteracoes);
        }
        return m;
    }

    public void inativar() {
        this.status = StatusMedicamento.INATIVO;
    }

    public boolean estaAtivo() {
        return status == StatusMedicamento.ATIVO;
    }

    public void adicionarPosologiaPadrao(String posologia) {
        this.posologiasPadrao.add(posologia);
    }

    public void adicionarContraindicacao(String contraindicacao) {
        this.contraindicacoes.add(contraindicacao);
    }

    public void adicionarInteracao(String interacao) {
        this.interacoes.add(interacao);
    }

    public void alterarCategoriaTerapeutica(String categoriaTerapeutica, String usuario) {
        String anterior = this.categoriaTerapeutica;
        this.categoriaTerapeutica = categoriaTerapeutica;
        historicoAlteracoes.add(new AlteracaoMedicamento(usuario, "categoriaTerapeutica", anterior, categoriaTerapeutica));
    }

    public void alterarClasseFarmacologica(String classeFarmacologica, String usuario) {
        String anterior = this.classeFarmacologica;
        this.classeFarmacologica = classeFarmacologica;
        historicoAlteracoes.add(new AlteracaoMedicamento(usuario, "classeFarmacologica", anterior, classeFarmacologica));
    }

    public MedicamentoId getId() { return id; }
    public String getNomeComercial() { return nomeComercial; }
    public String getPrincipioAtivo() { return principioAtivo; }
    public String getCategoriaTerapeutica() { return categoriaTerapeutica; }
    public String getClasseFarmacologica() { return classeFarmacologica; }
    public String getApresentacao() { return apresentacao; }
    public String getViaAdministracao() { return viaAdministracao; }
    public StatusMedicamento getStatus() { return status; }
    public List<String> getPosologiasPadrao() { return Collections.unmodifiableList(posologiasPadrao); }
    public List<String> getContraindicacoes() { return Collections.unmodifiableList(contraindicacoes); }
    public List<String> getInteracoes() { return Collections.unmodifiableList(interacoes); }
    public List<AlteracaoMedicamento> getHistoricoAlteracoes() { return Collections.unmodifiableList(historicoAlteracoes); }
}
