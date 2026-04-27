package com.g4.odontohub.inadimplencia.application;

import com.g4.odontohub.inadimplencia.domain.*;
import com.g4.odontohub.shared.exception.DomainException;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service de aplicação que orquestra o controle de inadimplência (F08).
 * Sem @Service ou @Component — DDD estrito.
 */
public class InadimplenciaService {

    private final PagamentoParceladoRepository pagamentoRepository;
    private final InadimplenciaRepository inadimplenciaRepository;
    private final AtomicLong idSequence = new AtomicLong(1);

    public InadimplenciaService(PagamentoParceladoRepository pagamentoRepository,
                                 InadimplenciaRepository inadimplenciaRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.inadimplenciaRepository = inadimplenciaRepository;
    }

    /**
     * Processa todas as parcelas vencidas até a data informada,
     * marcando-as como INADIMPLENTE e criando registros de inadimplência.
     */
    public void processarParcelasVencidas(LocalDate dataProcessamento) {
        for (PagamentoParcelado pp : pagamentoRepository.listarTodos()) {
            for (ParcelaInadimplencia parcela : pp.getParcelas()) {
                if (parcela.getStatus() == StatusParcela.PENDENTE
                        && parcela.getDataVencimento().isBefore(dataProcessamento)) {
                    parcela.marcarInadimplente();
                }
            }
            if (pp.possuiInadimplencia()) {
                boolean jaExiste = inadimplenciaRepository
                        .buscarPorPaciente(pp.getNomePaciente()).isPresent();
                if (!jaExiste) {
                    RegistroInadimplencia reg = RegistroInadimplencia.criar(
                            idSequence.getAndIncrement(), pp.getNomePaciente());
                    inadimplenciaRepository.salvar(reg);
                }
            }
            pagamentoRepository.salvar(pp);
        }
    }

    /**
     * Verifica se o paciente tem inadimplência ativa e lança exceção se sim.
     */
    public void verificarBloqueioAgendamento(String nomePaciente) {
        inadimplenciaRepository.buscarPorPaciente(nomePaciente)
                .filter(RegistroInadimplencia::isBloqueado)
                .ifPresent(r -> {
                    throw new DomainException(
                            "Paciente com inadimplência ativa. Regularize antes de agendar.");
                });
    }

    /**
     * Quita a parcela informada. Se não houver mais parcelas inadimplentes,
     * remove o bloqueio de agendamento automaticamente.
     *
     * @return true se o bloqueio foi removido, false se ainda há inadimplência
     */
    public boolean quitarParcela(String nomePaciente, int numeroParcela, LocalDate dataPagamento) {
        PagamentoParcelado pp = pagamentoRepository.buscarPorPaciente(nomePaciente)
                .orElseThrow(() -> new DomainException("Pagamento não encontrado para: " + nomePaciente));

        pp.getPorNumero(numeroParcela).liquidar(dataPagamento);
        pagamentoRepository.salvar(pp);

        boolean aindaInadimplente = pp.possuiInadimplencia();

        inadimplenciaRepository.buscarPorPaciente(nomePaciente).ifPresent(reg -> {
            if (!aindaInadimplente) {
                reg.removerBloqueio();
            }
            inadimplenciaRepository.salvar(reg);
        });

        return !aindaInadimplente; // true = bloqueio removido
    }

    /**
     * Registra uma tentativa de cobrança no histórico do registro de inadimplência.
     */
    public void registrarCobranca(String nomePaciente, String responsavel,
                                   LocalDate data, String resultado) {
        RegistroInadimplencia reg = inadimplenciaRepository.buscarPorPaciente(nomePaciente)
                .orElseThrow(() -> new DomainException("Inadimplência não encontrada para: " + nomePaciente));
        reg.registrarCobranca(data, responsavel, resultado);
        inadimplenciaRepository.salvar(reg);
    }

    /**
     * Dentista autoriza o agendamento de paciente inadimplente.
     * Registra o nome do dentista no RegistroInadimplencia.
     */
    public void autorizarAgendamento(String nomePaciente, String nomeDentista) {
        RegistroInadimplencia reg = inadimplenciaRepository.buscarPorPaciente(nomePaciente)
                .orElseThrow(() -> new DomainException("Inadimplência não encontrada para: " + nomePaciente));
        reg.autorizarAgendamento(nomeDentista);
        inadimplenciaRepository.salvar(reg);
    }

    /**
     * Cria um registro de inadimplência manual (para uso nos steps de setup).
     */
    public RegistroInadimplencia criarRegistroInadimplencia(String nomePaciente) {
        RegistroInadimplencia reg = RegistroInadimplencia.criar(
                idSequence.getAndIncrement(), nomePaciente);
        inadimplenciaRepository.salvar(reg);
        return reg;
    }

    public java.util.Optional<RegistroInadimplencia> buscarInadimplencia(String nomePaciente) {
        return inadimplenciaRepository.buscarPorPaciente(nomePaciente);
    }

    public java.util.Optional<PagamentoParcelado> buscarPagamento(String nomePaciente) {
        return pagamentoRepository.buscarPorPaciente(nomePaciente);
    }
}
