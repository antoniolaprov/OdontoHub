package com.g4.odontohub.steps;

import com.g4.odontohub.comissao_repasse.application.ComissaoService;
import com.g4.odontohub.comissao_repasse.domain.*;
import com.g4.odontohub.infra.*;
import com.g4.odontohub.shared.exception.DomainException;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class F10ComissaoSteps {

    private ComissaoService service;
    private InMemoryRegraComissaoRepository regraRepository;
    private InMemoryComissaoRepository comissaoRepository;
    private InMemoryRepasseRepository repasseRepository;
    private InMemoryLancamentoCaixaRepository lancamentoRepository;

    private Comissao ultimaComissao;
    private Repasse ultimoRepasse;
    private BigDecimal valorProcedimentoContexto;

    @Before
    public void setup() {
        regraRepository = new InMemoryRegraComissaoRepository();
        comissaoRepository = new InMemoryComissaoRepository();
        repasseRepository = new InMemoryRepasseRepository();
        lancamentoRepository = new InMemoryLancamentoCaixaRepository();
        service = new ComissaoService(regraRepository, comissaoRepository, repasseRepository, lancamentoRepository);
    }

    @Dado("que o especialista {string} está cadastrado no sistema")
    public void especialistaCadastrado(String nome) {}

    @Dado("que a regra de comissão de {string} para {string} é de {int}% do valor do procedimento")
    public void regraComissaoPercentual(String especialista, String procedimento, int percentual) {
        service.cadastrarRegraPercentual(especialista, procedimento, BigDecimal.valueOf(percentual));
    }

    @Dado("que a regra de comissão de {string} para {string} é de valor fixo R$ {double}")
    public void regraComissaoValorFixo(String especialista, String procedimento, Double valor) {
        service.cadastrarRegraValorFixo(especialista, procedimento, BigDecimal.valueOf(valor));
    }

    @Dado("que existe um plano de tratamento aprovado com o procedimento {string} no valor de R$ {double}")
    public void planoTratamentoAprovado(String procedimento, Double valor) {
        this.valorProcedimentoContexto = BigDecimal.valueOf(valor);
    }

    @Dado("que a comissão de {string} pelo procedimento {string} tem status {string}")
    public void comissaoPorProcedimentoTemStatus(String especialista, String procedimento, String status) {
        Comissao comissao = comissaoRepository
                .buscarPorEspecialistaEProcedimento(especialista, procedimento)
                .orElseGet(() -> {
                    Comissao c = Comissao.criar(especialista, procedimento, new BigDecimal("600.00"));
                    comissaoRepository.salvar(c);
                    return c;
                });
        if ("LIBERADA".equals(status)) comissao.liberar();
        comissaoRepository.salvar(comissao);
    }

    @Dado("que a comissão de {string} tem status {string}")
    public void comissaoEspecialistaTemStatus(String especialista, String status) {
        Comissao comissao = comissaoRepository
                .buscarPorEspecialista(especialista)
                .orElseGet(() -> {
                    Comissao c = Comissao.criar(especialista, "Implante", new BigDecimal("600.00"));
                    comissaoRepository.salvar(c);
                    return c;
                });
        if ("LIBERADA".equals(status)) comissao.liberar();
        comissaoRepository.salvar(comissao);
    }

    @Dado("que o paciente ainda não liquidou o pagamento correspondente")
    public void pacienteNaoLiquidou() {}

    @Dado("que existe um repasse registrado para {string}")
    public void repasseRegistrado(String especialista) {
        Comissao comissao = comissaoRepository
                .buscarPorEspecialista(especialista)
                .orElseGet(() -> {
                    Comissao c = Comissao.criar(especialista, "Implante", new BigDecimal("600.00"));
                    comissaoRepository.salvar(c);
                    return c;
                });
        if (comissao.getStatus() != StatusComissao.LIBERADA) {
            comissao.liberar();
            comissaoRepository.salvar(comissao);
        }
        ultimoRepasse = service.registrarRepasse(especialista, new BigDecimal("600.00"), LocalDate.now());
    }

    @Quando("o dentista registra o procedimento {string} como realizado por {string}")
    public void registrarProcedimentoPorEspecialista(String procedimento, String especialista) {
        try {
            BigDecimal valor = valorProcedimentoContexto != null
                    ? valorProcedimentoContexto : new BigDecimal("2000.00");
            ultimaComissao = service.registrarProcedimentoRealizadoPorEspecialista(
                    procedimento, especialista, valor);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista tenta registrar o repasse para {string}")
    public void tentaRegistrarRepasse(String especialista) {
        try {
            service.tentarRegistrarRepasse(especialista);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o paciente liquida o pagamento referente ao procedimento {string}")
    public void pacienteLiquidaPagamento(String procedimento) {
        try {
            service.registrarPagamentoPaciente("Dr. Marcos", procedimento);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista registra o repasse de R$ {double} para {string} em {string}")
    public void registrarRepasse(Double valor, String especialista, String data) {
        try {
            ultimoRepasse = service.registrarRepasse(especialista,
                    BigDecimal.valueOf(valor), LocalDate.parse(data));
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("qualquer usuário tenta excluir o repasse")
    public void tentaExcluirRepasse() {
        try {
            service.excluirRepasse("Dr. Marcos");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista estorna o repasse com a justificativa {string}")
    public void estornarRepasse(String justificativa) {
        try {
            service.estornarRepasse("Dr. Marcos", justificativa);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o sistema deve calcular automaticamente a comissão de R$ {double}")
    public void comissaoCalculadaAutomaticamente(Double valor) {
        assertNotNull(ultimaComissao, "Comissão não foi calculada");
        BigDecimal esperado = BigDecimal.valueOf(valor);
        assertEquals(0, esperado.compareTo(ultimaComissao.getValor()),
                "Valor esperado: " + esperado + " | obtido: " + ultimaComissao.getValor());
    }

    @Então("o sistema deve calcular a comissão como R$ {double} independente do valor do procedimento")
    public void comissaoValorFixoCalculada(Double valor) {
        assertNotNull(ultimaComissao, "Comissão não foi calculada");
        BigDecimal esperado = BigDecimal.valueOf(valor);
        assertEquals(0, esperado.compareTo(ultimaComissao.getValor()),
                "Valor fixo esperado: " + esperado + " | obtido: " + ultimaComissao.getValor());
    }

    @Então("o sistema deve bloquear o registro")
    public void sistemaBloqueiaRegistro() {
        assertNotNull(ScenarioContext.get().excecao, "Sistema deveria ter bloqueado o registro");
    }

    @Então("a comissão deve ter status {string}")
    public void comissaoDeveTermStatus(String statusEsperado) {
        assertNotNull(ultimaComissao, "Comissão não encontrada no contexto");
        assertEquals(StatusComissao.valueOf(statusEsperado), ultimaComissao.getStatus());
    }

    @Então("deve estar vinculada a {string} e ao procedimento {string}")
    public void comissaoVinculada(String especialista, String procedimento) {
        assertNotNull(ultimaComissao);
        assertEquals(especialista, ultimaComissao.getNomeEspecialista());
        assertEquals(procedimento, ultimaComissao.getNomeProcedimento());
    }

    @Então("a comissão deve ter status alterado para {string}")
    public void comissaoStatusAlterado(String statusEsperado) {
        Comissao comissao = comissaoRepository.buscarPorEspecialista("Dr. Marcos")
                .orElseThrow(() -> new AssertionError("Comissão não encontrada"));
        assertEquals(StatusComissao.valueOf(statusEsperado), comissao.getStatus());
    }

    @Então("o repasse pode ser registrado")
    public void repassePodeSerRegistrado() {
        Comissao comissao = comissaoRepository.buscarPorEspecialista("Dr. Marcos")
                .orElseThrow(() -> new AssertionError("Comissão não encontrada"));
        assertEquals(StatusComissao.LIBERADA, comissao.getStatus());
    }

    @Então("o repasse deve ser criado com valor R$ {double} e data {string}")
    public void repasseCriadoComValorEData(Double valor, String data) {
        assertNotNull(ultimoRepasse, "Repasse não foi criado");
        assertEquals(0, BigDecimal.valueOf(valor).compareTo(ultimoRepasse.getValor()));
        assertEquals(LocalDate.parse(data), ultimoRepasse.getData());
    }


    @Então("o repasse deve ter o campo {string} marcado como verdadeiro")
    public void repasseEstornado(String campo) {
        Repasse repasse = repasseRepository.buscarPorEspecialista("Dr. Marcos")
                .orElseThrow(() -> new AssertionError("Repasse não encontrado"));
        if ("estornado".equals(campo)) {
            assertTrue(repasse.isEstornado());
        }
    }

    @Então("a justificativa de estorno {string} deve ser salva no repasse")
    public void justificativaRegistrada(String justificativa) {
        Repasse repasse = repasseRepository.buscarPorEspecialista("Dr. Marcos")
                .orElseThrow(() -> new AssertionError("Repasse não encontrado"));
        assertEquals(justificativa, repasse.getJustificativaEstorno());
    }
}