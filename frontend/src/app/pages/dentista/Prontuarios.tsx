import { useState, useMemo, useRef, useEffect, useCallback } from "react";
import {
  FileText, Lock, ShieldAlert, FileClock, Pill, AlertTriangle, RotateCcw, Search, X,
  Plus, Loader2, Trash2, XCircle, ChevronDown, ChevronUp,
} from "lucide-react";
import { api } from "../../api/client";

// ─── Catálogo de medicamentos (espelha a F17 — fora de escopo, mantido como mock) ──
interface MedicamentoCatalogo {
  id: number;
  nomeComercial: string;
  principioAtivo: string;
  categoria: string;
  classeFarmacologica: string;
  contraindicacoes: string;
  interacoes: string;
  posologiasPadrao: string[];
  apresentacoes: string[];
}

const CATALOGO_MEDICAMENTOS: MedicamentoCatalogo[] = [
  { id: 1, nomeComercial: "Amoxil", principioAtivo: "Amoxicilina", categoria: "Antibiótico", classeFarmacologica: "Beta-lactâmicos", contraindicacoes: "Alergia a penicilinas ou cefalosporinas, mononucleose infecciosa", interacoes: "Anticoagulantes orais (warfarina), metotrexato", posologiasPadrao: ["1 cáps. a cada 8h por 7 dias", "1 cáps. a cada 12h por 5 dias"], apresentacoes: ["Cáps. 500mg", "Susp. 250mg/5ml"] },
  { id: 2, nomeComercial: "Ibupril", principioAtivo: "Ibuprofeno", categoria: "Anti-inflamatório", classeFarmacologica: "AINEs", contraindicacoes: "Úlcera péptica ativa, insuficiência renal grave, gestação (3º trimestre)", interacoes: "Anticoagulantes, diuréticos, anti-hipertensivos", posologiasPadrao: ["1 comp. de 600mg a cada 8h por 3 dias"], apresentacoes: ["Comp. 600mg", "Comp. 400mg"] },
  { id: 3, nomeComercial: "Nimesil", principioAtivo: "Nimesulida", categoria: "Anti-inflamatório", classeFarmacologica: "AINEs", contraindicacoes: "Hepatopatia, insuficiência renal, gestação", interacoes: "Anticoagulantes, lítio, metotrexato", posologiasPadrao: ["1 comp. a cada 12h por 2 dias"], apresentacoes: ["Comp. 100mg", "Granulado 100mg/sachê"] },
  { id: 4, nomeComercial: "Dipirax", principioAtivo: "Dipirona", categoria: "Analgésico", classeFarmacologica: "Pirazolonas", contraindicacoes: "Hipersensibilidade à dipirona, porfiria aguda", interacoes: "Ciclosporina, cloranfenicol", posologiasPadrao: ["1 comp. a cada 6h se dor"], apresentacoes: ["Comp. 500mg", "Gotas 500mg/ml"] },
  { id: 5, nomeComercial: "Paracetol", principioAtivo: "Paracetamol", categoria: "Analgésico", classeFarmacologica: "Paraaminofenóis", contraindicacoes: "Hepatopatia grave, alcoolismo crônico", interacoes: "Warfarina (uso prolongado), isoniazida", posologiasPadrao: ["1 comp. de 750mg a cada 6h se dor"], apresentacoes: ["Comp. 750mg", "Susp. 200mg/ml"] },
  { id: 6, nomeComercial: "Clindoxyl", principioAtivo: "Clindamicina", categoria: "Antibiótico", classeFarmacologica: "Lincosamidas", contraindicacoes: "Colite ulcerativa, hipersensibilidade", interacoes: "Bloqueadores neuromusculares", posologiasPadrao: ["1 cáps. a cada 6h por 7 dias"], apresentacoes: ["Cáps. 300mg"] },
];

// Alergias usadas só pela aba Prescrições (F8, fora de escopo) — mock mantido como estava.
const ALERGIAS_PACIENTE = ["Penicilina", "Dipirona"];

const CLASSE_ALERGICA: Record<string, string> = {
  amoxicilina: "Penicilina",
  ampicilina: "Penicilina",
  cloxacilina: "Penicilina",
};

function verificarContraindicacao(medicamento: string): string | null {
  const medLower = medicamento.toLowerCase().trim();
  if (!medLower) return null;

  for (const alergia of ALERGIAS_PACIENTE) {
    if (medLower.includes(alergia.toLowerCase())) return alergia;
  }
  for (const [nomeMed, classe] of Object.entries(CLASSE_ALERGICA)) {
    if (medLower.includes(nomeMed) && ALERGIAS_PACIENTE.includes(classe)) {
      return classe;
    }
  }
  return null;
}

function parseDateBR(ddmmyyyy: string): Date | null {
  const parts = ddmmyyyy.split("/");
  if (parts.length !== 3) return null;
  return new Date(parseInt(parts[2]), parseInt(parts[1]) - 1, parseInt(parts[0]));
}

// ─── Tipos (Anamnese/Plano — F02/F03, ligados ao backend) ──────────────────────

interface PacienteResumo {
  id: number;
  nome: string;
  cpf: string | null;
  dataNascimento: string | null;
  telefone: string | null;
}

interface VersaoAnamneseUI {
  versao: number;
  alergias: string;
  contraindicacoes: string;
  condicoesSistemicas: string;
  dataAlteracao: string | null;
  responsavel: string;
}

interface AnamneseUI {
  alergias: string;
  contraindicacoes: string;
  condicoesSistemicas: string;
  versaoAtual: number;
  dataRegistro: string | null;
  dataUltimaAtualizacao: string | null;
  responsavelCadastro: string;
  historicoVersoes: VersaoAnamneseUI[];
}

type StatusProcedimentoUI = "PENDENTE" | "REALIZADO" | "CANCELADO";

interface ProcedimentoUI {
  id: number;
  nome: string;
  tipoProcedimento: string;
  status: StatusProcedimentoUI;
  evolucao: { descricaoTecnica: string; executor: string; dataRegistro: string | null } | null;
  dataRealizacao: string | null;
  executor: string | null;
  justificativaCancelamento: string | null;
  dentroDaJanelaDeCorrecao: boolean;
}

type StatusPlanoUI = "EM_ANDAMENTO" | "CONCLUIDO" | "ENCERRADO";

interface PlanoUI {
  id: number;
  status: StatusPlanoUI;
  versao: number;
  dataCriacao: string | null;
  justificativaEncerramento: string | null;
  procedimentos: ProcedimentoUI[];
}

// ─── Adaptadores backend → UI ───────────────────────────────────────────────────

function adaptarPaciente(b: any): PacienteResumo {
  return {
    id: b.id?.id ?? b.id,
    nome: b.nomeCompleto ?? "—",
    cpf: b.cpf ?? null,
    dataNascimento: b.dataNascimento ?? null,
    telefone: b.telefone ?? null,
  };
}

function adaptarAnamnese(b: any): AnamneseUI {
  return {
    alergias: b.alergias ?? "",
    contraindicacoes: b.contraindicacoes ?? "",
    condicoesSistemicas: b.condicoesSistemicas ?? "",
    versaoAtual: b.versaoAtual ?? 1,
    dataRegistro: b.dataRegistro ?? null,
    dataUltimaAtualizacao: b.dataUltimaAtualizacao ?? null,
    responsavelCadastro: b.responsavelCadastro ?? "—",
    historicoVersoes: (b.historicoVersoes ?? []).map((v: any) => ({
      versao: v.versao,
      alergias: v.alergias ?? "",
      contraindicacoes: v.contraindicacoes ?? "",
      condicoesSistemicas: v.condicoesSistemicas ?? "",
      dataAlteracao: v.dataAlteracao ?? null,
      responsavel: v.responsavel ?? "—",
    })),
  };
}

function adaptarProcedimento(b: any): ProcedimentoUI {
  return {
    id: b.id?.id ?? b.id,
    nome: b.nome ?? "—",
    tipoProcedimento: b.tipoProcedimento ?? "—",
    status: (b.status ?? "PENDENTE") as StatusProcedimentoUI,
    evolucao: b.evolucao
      ? { descricaoTecnica: b.evolucao.descricaoTecnica ?? "", executor: b.evolucao.executor ?? "", dataRegistro: b.evolucao.dataRegistro ?? null }
      : null,
    dataRealizacao: b.dataRealizacao ?? null,
    executor: b.executor ?? null,
    justificativaCancelamento: b.justificativaCancelamento ?? null,
    dentroDaJanelaDeCorrecao: b.dentroDaJanelaDeCorrecao ?? true,
  };
}

function adaptarPlano(b: any): PlanoUI {
  return {
    id: b.id?.id ?? b.id,
    status: (b.status ?? "EM_ANDAMENTO") as StatusPlanoUI,
    versao: b.versao ?? 1,
    dataCriacao: b.dataCriacao ?? null,
    justificativaEncerramento: b.justificativaEncerramento ?? null,
    procedimentos: (b.procedimentos ?? []).map(adaptarProcedimento),
  };
}

function formatarData(valor: string | number | null | undefined): string {
  if (!valor) return "—";
  const d = new Date(valor);
  return isNaN(d.getTime()) ? "—" : d.toLocaleDateString("pt-BR");
}

const STATUS_PROCEDIMENTO_LABEL: Record<StatusProcedimentoUI, string> = {
  PENDENTE: "Pendente",
  REALIZADO: "Realizado",
  CANCELADO: "Cancelado",
};

const STATUS_PROCEDIMENTO_CLASS: Record<StatusProcedimentoUI, string> = {
  PENDENTE: "bg-blue-100 text-blue-800 border-blue-500",
  REALIZADO: "bg-green-100 text-green-800 border-green-500",
  CANCELADO: "bg-red-100 text-red-800 border-red-500",
};

const STATUS_PLANO_LABEL: Record<StatusPlanoUI, string> = {
  EM_ANDAMENTO: "Em Andamento",
  CONCLUIDO: "Concluído",
  ENCERRADO: "Encerrado",
};

// Não há sessão de colaborador logado nesta tela ainda (login hoje é só por clínica,
// F18) — usamos um id fixo só para armazenar junto do agregado, sem regra de negócio
// dependendo dele.
const DENTISTA_ID_DEMO = 1;

interface ColaboradorResumo { nome: string; funcao: string; }
interface AgendamentoResumo { id: number; dentista: string; label: string; }
interface MaterialResumo { nome: string; unidadeMedida: string; quantidadeEmEstoque: number; }
interface MaterialUsado { material: string; quantidade: string; }

export default function DentistaProntuarios() {
  const [abaAtiva, setAbaAtiva] = useState("anamnese");
  const [alertaAberto, setAlertaAberto] = useState(true);

  // ── Pacientes ────────────────────────────────────────────────────────────
  // Cadastro de paciente é função da Recepcionista (tela própria) — o dentista só
  // seleciona, dentre os já cadastrados, o prontuário que vai atender.
  const [pacientes, setPacientes] = useState<PacienteResumo[]>([]);
  const [pacienteSelecionadoId, setPacienteSelecionadoId] = useState<number | null>(null);
  const [carregandoPacientes, setCarregandoPacientes] = useState(true);
  const [erroPacientes, setErroPacientes] = useState<string | null>(null);

  const pacienteSelecionado = pacientes.find((p) => p.id === pacienteSelecionadoId) ?? null;

  // Busca com autocomplete: lista horizontal de todos os pacientes não escala
  // visualmente com muitos cadastros — agora pesquisa por nome (sem distinção
  // de maiúsculas/minúsculas) e sugere os pacientes que combinam.
  const [buscaPacienteInput, setBuscaPacienteInput] = useState("");
  const [sugestoesPacienteAbertas, setSugestoesPacienteAbertas] = useState(false);
  const buscaPacienteRef = useRef<HTMLDivElement>(null);

  const pacientesFiltrados = useMemo(() => {
    const q = buscaPacienteInput.trim().toLowerCase();
    if (!q) return pacientes;
    return pacientes.filter((p) => p.nome.toLowerCase().includes(q));
  }, [buscaPacienteInput, pacientes]);

  useEffect(() => {
    function handleClickForaBuscaPaciente(e: MouseEvent) {
      if (buscaPacienteRef.current && !buscaPacienteRef.current.contains(e.target as Node)) {
        setSugestoesPacienteAbertas(false);
      }
    }
    document.addEventListener("mousedown", handleClickForaBuscaPaciente);
    return () => document.removeEventListener("mousedown", handleClickForaBuscaPaciente);
  }, []);

  // ── Colaboradores (F12) — usado nos selects de "responsável"/"executor" ─
  const [colaboradores, setColaboradores] = useState<ColaboradorResumo[]>([]);
  const cirurgioesDentistas = useMemo(
    () => colaboradores.filter((c) => c.funcao === "ESPECIALISTA"),
    [colaboradores],
  );
  const [responsavelSelecionado, setResponsavelSelecionado] = useState("");

  // ── Agendamentos (F01) — usado pra vincular a evolução clínica a uma consulta real ─
  const [agendamentosPaciente, setAgendamentosPaciente] = useState<AgendamentoResumo[]>([]);

  // ── Materiais (F05) — consumo real de estoque ao realizar um procedimento ─
  const [materiaisDisponiveis, setMateriaisDisponiveis] = useState<MaterialResumo[]>([]);

  // ── Anamnese (F02) ───────────────────────────────────────────────────────
  const [anamnese, setAnamnese] = useState<AnamneseUI | null>(null);
  const [carregandoAnamnese, setCarregandoAnamnese] = useState(false);
  const [mostrarHistorico, setMostrarHistorico] = useState(false);
  const [formRegistro, setFormRegistro] = useState({
    alergias: "", contraindicacoes: "", condicoesSistemicas: "",
  });
  const [salvandoAnamnese, setSalvandoAnamnese] = useState(false);
  const [novaAlergiaInput, setNovaAlergiaInput] = useState("");
  const [condicoesEditando, setCondicoesEditando] = useState("");

  // anamnesePreenchida agora reflete dado real (também usado pelo gate da aba Prescrições, fora de escopo)
  const anamnesePreenchida = anamnese !== null;

  // ── Plano de Tratamento (F03) ────────────────────────────────────────────
  const [plano, setPlano] = useState<PlanoUI | null>(null);
  const [carregandoPlano, setCarregandoPlano] = useState(false);

  const [modalNovoProcedimento, setModalNovoProcedimento] = useState(false);
  const [novoProcNome, setNovoProcNome] = useState("");
  const [novoProcTipo, setNovoProcTipo] = useState("");

  const [modalRealizar, setModalRealizar] = useState<{ aberto: boolean; procedimento?: ProcedimentoUI }>({ aberto: false });
  const [realizarDescricao, setRealizarDescricao] = useState("");
  const [realizarAgendamentoId, setRealizarAgendamentoId] = useState("");
  const [materiaisUsados, setMateriaisUsados] = useState<MaterialUsado[]>([]);

  // O agendamento já indica o cirurgião-dentista responsável pela consulta —
  // por isso o executor não é mais escolhido à parte, é derivado do agendamento.
  const executorDoAgendamentoSelecionado = useMemo(
    () => agendamentosPaciente.find((a) => a.id === Number(realizarAgendamentoId))?.dentista ?? "",
    [agendamentosPaciente, realizarAgendamentoId],
  );
  const [erroMateriais, setErroMateriais] = useState<string | null>(null);

  const [modalJustificativa, setModalJustificativa] = useState<{
    aberto: boolean;
    acao?: "encerrar-plano" | "excluir-plano" | "cancelar-procedimento" | "excluir-procedimento";
    procedimentoId?: number;
  }>({ aberto: false });
  const [justificativaInput, setJustificativaInput] = useState("");
  const [responsavelAcaoInput, setResponsavelAcaoInput] = useState("");

  const [erroAcao, setErroAcao] = useState<string | null>(null);
  const [salvandoAcao, setSalvandoAcao] = useState(false);

  // ── Prescrições (F08 — fora de escopo, mantido como mock) ───────────────
  const [modalNovaPrescricao, setModalNovaPrescricao] = useState(false);
  const [modalAlertaAlergia, setModalAlertaAlergia] = useState(false);
  const [modalAlertaRecente, setModalAlertaRecente] = useState(false);
  const [modalRepetirPrescricao, setModalRepetirPrescricao] = useState<{aberto: boolean, prescricao?: any}>({aberto: false});
  const [modalExcluirPrescricao, setModalExcluirPrescricao] = useState<{aberto: boolean, prescricao?: any}>({aberto: false});

  const [buscaMedicamento, setBuscaMedicamento] = useState("");
  const [medicamentoSelecionado, setMedicamentoSelecionado] = useState<MedicamentoCatalogo | null>(null);
  const [dropdownAberto, setDropdownAberto] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const medicamentosFiltrados = useMemo(() => {
    if (!buscaMedicamento.trim()) return CATALOGO_MEDICAMENTOS;
    const q = buscaMedicamento.toLowerCase();
    return CATALOGO_MEDICAMENTOS.filter(
      (m) => m.nomeComercial.toLowerCase().includes(q) || m.principioAtivo.toLowerCase().includes(q)
    );
  }, [buscaMedicamento]);

  useEffect(() => {
    function handleClickFora(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownAberto(false);
      }
    }
    document.addEventListener("mousedown", handleClickFora);
    return () => document.removeEventListener("mousedown", handleClickFora);
  }, []);

  function selecionarMedicamento(med: MedicamentoCatalogo) {
    setMedicamentoSelecionado(med);
    setBuscaMedicamento(med.nomeComercial);
    setDropdownAberto(false);
  }

  function limparMedicamento() {
    setMedicamentoSelecionado(null);
    setBuscaMedicamento("");
    setDropdownAberto(false);
  }

  const [medicamentoInput, setMedicamentoInput] = useState("");
  const [dosagemInput, setDosagemInput] = useState("");
  const [posologiaInput, setPosologiaInput] = useState("");
  const [periodoInput, setPeriodoInput] = useState("");
  const [agendamentoInput, setAgendamentoInput] = useState("");
  const [observacoesInput, setObservacoesInput] = useState("");

  const [contraindicacaoDetectada, setContraindicacaoDetectada] = useState<string | null>(null);
  const [prescricaoRecenteDetectada, setPrescricaoRecenteDetectada] = useState<any | null>(null);

  const prescricoes = [
    { id: 1, data: "15/04/2026", medicamento: "Ibuprofeno", dosagem: "600mg", posologia: "1 comp. a cada 8h", periodo: "3 dias", agendamento: "AGE-0412", responsavel: "Dr. Felipe", editavel: false },
    { id: 2, data: "20/04/2026", medicamento: "Amoxicilina", dosagem: "500mg", posologia: "1 cáps. a cada 8h", periodo: "7 dias", agendamento: "AGE-0419", responsavel: "Dr. Felipe", editavel: false },
    { id: 3, data: "08/06/2026", medicamento: "Nimesulida", dosagem: "100mg", posologia: "1 comp. a cada 12h", periodo: "2 dias", agendamento: "AGE-0607", responsavel: "Dr. Felipe", editavel: true },
  ];

  function verificarPrescricaoRecente(medicamento: string): typeof prescricoes[0] | null {
    const medLower = medicamento.toLowerCase().trim();
    if (!medLower) return null;

    const agora = new Date();
    const trintaDiasAtras = new Date(agora.getTime() - 30 * 24 * 60 * 60 * 1000);

    for (const p of prescricoes) {
      const dataPrescricao = parseDateBR(p.data);
      if (!dataPrescricao) continue;
      if (
        dataPrescricao >= trintaDiasAtras &&
        (p.medicamento.toLowerCase().includes(medLower) || medLower.includes(p.medicamento.toLowerCase()))
      ) {
        return p;
      }
    }
    return null;
  }

  function salvarPrescricaoFinal() {
    limparFormulario();
    setModalNovaPrescricao(false);
    setModalAlertaAlergia(false);
    setModalAlertaRecente(false);
  }

  function limparFormulario() {
    setMedicamentoInput("");
    limparMedicamento();
    setDosagemInput("");
    setPosologiaInput("");
    setPeriodoInput("");
    setAgendamentoInput("");
    setObservacoesInput("");
    setContraindicacaoDetectada(null);
    setPrescricaoRecenteDetectada(null);
  }

  function handleSalvarPrescricao() {
    const nomeMed = medicamentoSelecionado ? medicamentoSelecionado.principioAtivo : medicamentoInput;
    const contraindicacaoPorClasse = medicamentoSelecionado
      ? ALERGIAS_PACIENTE.includes(medicamentoSelecionado.classeFarmacologica.replace("Beta-lactâmicos", "Penicilina"))
        ? medicamentoSelecionado.classeFarmacologica
        : verificarContraindicacao(medicamentoSelecionado.principioAtivo)
      : null;
    const contraindicacao = contraindicacaoPorClasse || verificarContraindicacao(nomeMed);

    if (contraindicacao) {
      setContraindicacaoDetectada(contraindicacao);
      setModalNovaPrescricao(false);
      setModalAlertaAlergia(true);
      return;
    }

    const recente = verificarPrescricaoRecente(medicamentoInput);
    if (recente) {
      setPrescricaoRecenteDetectada(recente);
      setModalNovaPrescricao(false);
      setModalAlertaRecente(true);
      return;
    }

    salvarPrescricaoFinal();
  }

  function handleConfirmarCienciaRisco() {
    console.log(
      "[AUDITORIA] Dentista confirmou ciência do risco de contraindicação. Medicamento:",
      medicamentoInput,
      "| Alergia identificada:",
      contraindicacaoDetectada,
      "| Data/hora:",
      new Date().toISOString()
    );

    setModalAlertaAlergia(false);

    const recente = verificarPrescricaoRecente(medicamentoInput);
    if (recente) {
      setPrescricaoRecenteDetectada(recente);
      setModalAlertaRecente(true);
    } else {
      salvarPrescricaoFinal();
    }
  }

  function handleConfirmarRepeticao() {
    const med = modalRepetirPrescricao.prescricao?.medicamento || "";
    const contraindicacao = verificarContraindicacao(med);

    if (contraindicacao) {
      setMedicamentoInput(med);
      setContraindicacaoDetectada(contraindicacao);
      setModalRepetirPrescricao({aberto: false});
      setModalAlertaAlergia(true);
      return;
    }

    const recente = verificarPrescricaoRecente(med);
    if (recente) {
      setPrescricaoRecenteDetectada(recente);
      setModalRepetirPrescricao({aberto: false});
      setModalAlertaRecente(true);
      return;
    }

    setModalRepetirPrescricao({aberto: false});
  }

  // ── Carregamento: pacientes ──────────────────────────────────────────────
  useEffect(() => {
    let ativo = true;
    setCarregandoPacientes(true);
    api.get<any[]>("/pacientes")
      .then((lista) => {
        if (!ativo) return;
        const adaptados = (lista ?? []).map(adaptarPaciente);
        setPacientes(adaptados);
        setErroPacientes(null);
        if (adaptados.length > 0) {
          setPacienteSelecionadoId((atual) => atual ?? adaptados[0].id);
        }
      })
      .catch((e) => {
        if (!ativo) return;
        console.warn("Falha ao carregar pacientes:", e);
        setErroPacientes("Não foi possível carregar a lista de pacientes.");
      })
      .finally(() => { if (ativo) setCarregandoPacientes(false); });
    return () => { ativo = false; };
  }, []);

  // ── Carregamento: colaboradores (selects de responsável/executor) ───────
  useEffect(() => {
    api.get<any[]>("/colaboradores")
      .then((lista) => {
        const adaptados = (lista ?? []).map((c: any) => ({ nome: c.nomeCompleto ?? "—", funcao: c.funcao ?? "" }));
        setColaboradores(adaptados);
        const primeiro = adaptados.find((c) => c.funcao === "ESPECIALISTA");
        if (primeiro) {
          setResponsavelSelecionado((atual) => atual || primeiro.nome);
          setResponsavelAcaoInput((atual) => atual || primeiro.nome);
        }
      })
      .catch((e) => console.warn("Falha ao carregar colaboradores:", e));
  }, []);

  // ── Carregamento: materiais disponíveis (consumo de estoque ao realizar) ─
  const carregarMateriais = useCallback(() => {
    return api.get<any[]>("/materiais")
      .then((lista) => setMateriaisDisponiveis((lista ?? []).map((m: any) => ({
        nome: m.nome ?? "—", unidadeMedida: m.unidadeMedida ?? "", quantidadeEmEstoque: m.quantidadeEmEstoque ?? 0,
      }))))
      .catch((e) => console.warn("Falha ao carregar materiais:", e));
  }, []);
  useEffect(() => { carregarMateriais(); }, [carregarMateriais]);

  // ── Carregamento: agendamentos do paciente selecionado (vínculo da evolução) ─
  useEffect(() => {
    if (!pacienteSelecionado) { setAgendamentosPaciente([]); return; }
    api.get<any[]>("/agendamentos")
      .then((lista) => {
        const doPaciente = (lista ?? [])
          .filter((a: any) => a.paciente === pacienteSelecionado.nome)
          .map((a: any) => {
            const dataHora = a.dataHora ? new Date(a.dataHora).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" }) : "—";
            return { id: a.id, dentista: a.dentista ?? "—", label: `${dataHora} — ${a.dentista ?? "—"} (${a.status ?? "—"})` };
          });
        setAgendamentosPaciente(doPaciente);
      })
      .catch((e) => console.warn("Falha ao carregar agendamentos do paciente:", e));
  }, [pacienteSelecionado]);

  // ── Carregamento: anamnese + plano do paciente selecionado ──────────────
  const recarregarProntuario = useCallback(async (pacienteId: number) => {
    setCarregandoAnamnese(true);
    try {
      const a = await api.get<any>(`/prontuarios/anamnese/${pacienteId}`)
        .catch((e) => { console.warn("Anamnese ainda não registrada:", e); return null; });
      const adaptada = a ? adaptarAnamnese(a) : null;
      setAnamnese(adaptada);
      setCondicoesEditando(adaptada?.condicoesSistemicas ?? "");
    } finally {
      setCarregandoAnamnese(false);
    }

    setCarregandoPlano(true);
    try {
      const p = await api.get<any>(`/prontuarios/planos/paciente/${pacienteId}`)
        .catch((e) => { console.warn("Plano ainda não criado:", e); return null; });
      setPlano(p ? adaptarPlano(p) : null);
    } finally {
      setCarregandoPlano(false);
    }
  }, []);

  useEffect(() => {
    setErroAcao(null);
    setMostrarHistorico(false);
    if (pacienteSelecionadoId == null) {
      setAnamnese(null);
      setPlano(null);
      return;
    }
    recarregarProntuario(pacienteSelecionadoId);
  }, [pacienteSelecionadoId, recarregarProntuario]);

  // ── Ações: anamnese ──────────────────────────────────────────────────────
  async function handleRegistrarAnamnese(e: React.FormEvent) {
    e.preventDefault();
    if (pacienteSelecionadoId == null || !responsavelSelecionado) return;
    setSalvandoAnamnese(true);
    setErroAcao(null);
    try {
      await api.post("/prontuarios/anamnese", {
        pacienteId: pacienteSelecionadoId,
        alergias: formRegistro.alergias,
        contraindicacoes: formRegistro.contraindicacoes,
        condicoesSistemicas: formRegistro.condicoesSistemicas,
        responsavel: responsavelSelecionado,
      });
      setFormRegistro({ alergias: "", contraindicacoes: "", condicoesSistemicas: "" });
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao registrar anamnese.");
    } finally {
      setSalvandoAnamnese(false);
    }
  }

  async function handleAdicionarAlergia() {
    if (pacienteSelecionadoId == null || !novaAlergiaInput.trim() || !responsavelSelecionado) return;
    setErroAcao(null);
    try {
      await api.post(`/prontuarios/anamnese/${pacienteSelecionadoId}/alergias`, {
        alergia: novaAlergiaInput.trim(), responsavel: responsavelSelecionado,
      });
      setNovaAlergiaInput("");
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao adicionar alergia.");
    }
  }

  async function handleSalvarCondicoes() {
    if (pacienteSelecionadoId == null || !responsavelSelecionado) return;
    setErroAcao(null);
    try {
      await api.put(`/prontuarios/anamnese/${pacienteSelecionadoId}/condicoes-sistemicas`, {
        condicoes: condicoesEditando, responsavel: responsavelSelecionado,
      });
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao atualizar condições sistêmicas.");
    }
  }

  // ── Ações: plano de tratamento ───────────────────────────────────────────
  async function handleCriarPlano() {
    if (pacienteSelecionadoId == null) return;
    setSalvandoAcao(true);
    setErroAcao(null);
    try {
      await api.post("/prontuarios/planos", { pacienteId: pacienteSelecionadoId, dentistaId: DENTISTA_ID_DEMO });
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao criar plano de tratamento.");
    } finally {
      setSalvandoAcao(false);
    }
  }

  async function handleAdicionarProcedimento(e: React.FormEvent) {
    e.preventDefault();
    if (!plano || pacienteSelecionadoId == null || !novoProcNome.trim() || !novoProcTipo.trim()) return;
    setErroAcao(null);
    try {
      await api.post(`/prontuarios/planos/${plano.id}/procedimentos`, { nome: novoProcNome, tipoProcedimento: novoProcTipo });
      setModalNovoProcedimento(false);
      setNovoProcNome("");
      setNovoProcTipo("");
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao adicionar procedimento.");
    }
  }

  async function handleRealizarProcedimento(e: React.FormEvent) {
    e.preventDefault();
    if (!plano || !modalRealizar.procedimento || pacienteSelecionadoId == null) return;
    if (!realizarAgendamentoId) {
      setErroAcao("Selecione o agendamento vinculado a este procedimento.");
      return;
    }
    if (!executorDoAgendamentoSelecionado) {
      setErroAcao("O agendamento selecionado não tem um cirurgião-dentista associado.");
      return;
    }
    const itensValidos = materiaisUsados.filter((m) => m.material && Number(m.quantidade) > 0);
    setErroAcao(null);
    setErroMateriais(null);
    try {
      const procedimentoId = modalRealizar.procedimento.id;
      await api.put(`/prontuarios/planos/${plano.id}/procedimentos/${procedimentoId}/realizar`, {
        descricaoEvolucao: realizarDescricao,
        agendamentoId: Number(realizarAgendamentoId),
        executor: executorDoAgendamentoSelecionado,
      });

      // Consome o estoque dos materiais usados — falha aqui não desfaz o procedimento
      // (já realizado), só é reportada pra o dentista ajustar o estoque manualmente.
      const falhasConsumo: string[] = [];
      for (const item of itensValidos) {
        try {
          await api.post(`/materiais/${encodeURIComponent(item.material)}/consumo`, {
            quantidade: Number(item.quantidade), procedimentoId,
          });
        } catch (err: any) {
          falhasConsumo.push(`${item.material}: ${err.message ?? "falha desconhecida"}`);
        }
      }
      if (falhasConsumo.length > 0) {
        setErroMateriais(`Procedimento realizado, mas houve falha ao baixar estoque: ${falhasConsumo.join("; ")}`);
      }
      await carregarMateriais();

      setModalRealizar({ aberto: false });
      setRealizarDescricao("");
      setRealizarAgendamentoId("");
      setMateriaisUsados([]);
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao realizar procedimento.");
    }
  }

  async function confirmarJustificativa() {
    if (!plano || pacienteSelecionadoId == null || !modalJustificativa.acao) return;
    if (!justificativaInput.trim()) return;
    setSalvandoAcao(true);
    setErroAcao(null);
    try {
      const acao = modalJustificativa.acao;
      if (acao === "encerrar-plano") {
        await api.put(`/prontuarios/planos/${plano.id}/encerrar`, { justificativa: justificativaInput });
      } else if (acao === "excluir-plano") {
        await api.del(`/prontuarios/planos/${plano.id}`);
      } else if (acao === "cancelar-procedimento" && modalJustificativa.procedimentoId) {
        await api.put(`/prontuarios/planos/${plano.id}/procedimentos/${modalJustificativa.procedimentoId}/cancelar`, {
          justificativa: justificativaInput,
        });
      } else if (acao === "excluir-procedimento" && modalJustificativa.procedimentoId) {
        const qs = `justificativa=${encodeURIComponent(justificativaInput)}&responsavel=${encodeURIComponent(responsavelAcaoInput)}`;
        await api.del(`/prontuarios/planos/${plano.id}/procedimentos/${modalJustificativa.procedimentoId}?${qs}`);
      }
      setModalJustificativa({ aberto: false });
      setJustificativaInput("");
      await recarregarProntuario(pacienteSelecionadoId);
    } catch (err: any) {
      setErroAcao(err.message ?? "Falha ao concluir a ação.");
    } finally {
      setSalvandoAcao(false);
    }
  }

  return (
    <div className="p-6">
      {anamnesePreenchida && anamnese!.alergias.trim() && (
        <div className="mb-6 border-4 border-red-500 bg-red-50 p-4">
          <div className="flex items-center gap-3">
            <span className="text-2xl">⚠️</span>
            <div>
              <div className="font-bold text-red-700">ALERTA: ALERGIA</div>
              <div className="text-sm text-red-600">
                Paciente alérgico a: {anamnese!.alergias}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── Seleção de paciente ─────────────────────────────────────────── */}
      <div className="mb-6 border-2 border-gray-400 bg-gray-50 p-4">
        {carregandoPacientes ? (
          <div className="flex items-center gap-2 text-gray-500 text-sm">
            <Loader2 className="w-4 h-4 animate-spin" /> Carregando pacientes...
          </div>
        ) : pacienteSelecionado ? (
          <div className="grid grid-cols-3 gap-4">
            <div>
              <div className="text-sm text-gray-500">Paciente</div>
              <div className="font-bold">{pacienteSelecionado.nome}</div>
            </div>
            <div>
              <div className="text-sm text-gray-500">CPF</div>
              <div className="font-bold">{pacienteSelecionado.cpf || "—"}</div>
            </div>
            <div>
              <div className="text-sm text-gray-500">Data Nascimento</div>
              <div className="font-bold">{pacienteSelecionado.dataNascimento || "—"}</div>
            </div>
          </div>
        ) : (
          <div className="text-sm text-gray-500">Nenhum paciente cadastrado ainda.</div>
        )}

        {erroPacientes && <div className="mt-2 text-sm text-red-600">{erroPacientes}</div>}

        {pacientes.length > 0 && (
          <div className="mt-4 border-t border-gray-300 pt-3" ref={buscaPacienteRef}>
            <label className="text-xs font-bold text-gray-500 mb-2 block">Buscar paciente</label>
            <div className="relative max-w-sm">
              <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={buscaPacienteInput}
                onChange={(e) => { setBuscaPacienteInput(e.target.value); setSugestoesPacienteAbertas(true); }}
                onFocus={() => setSugestoesPacienteAbertas(true)}
                placeholder="Digite o nome do paciente..."
                className="w-full border-2 border-gray-300 rounded pl-9 pr-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
              {sugestoesPacienteAbertas && (
                <div className="absolute z-10 mt-1 w-full bg-white border-2 border-gray-300 rounded shadow-lg max-h-56 overflow-auto">
                  {pacientesFiltrados.length === 0 ? (
                    <div className="px-3 py-2 text-sm text-gray-400">Nenhum paciente encontrado.</div>
                  ) : (
                    pacientesFiltrados.map((p) => (
                      <button
                        key={p.id}
                        type="button"
                        onClick={() => {
                          setPacienteSelecionadoId(p.id);
                          setBuscaPacienteInput("");
                          setSugestoesPacienteAbertas(false);
                        }}
                        className={`w-full text-left px-3 py-2 text-sm hover:bg-blue-50 ${
                          p.id === pacienteSelecionadoId ? "bg-blue-50 font-bold text-blue-700" : "text-gray-700"
                        }`}
                      >
                        {p.nome}
                      </button>
                    ))
                  )}
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      <div className="border-2 border-gray-400 mb-4">
        <div className="flex border-b-2 border-gray-400">
          <button
            onClick={() => setAbaAtiva("anamnese")}
            className={`flex-1 p-3 border-r-2 border-gray-400 font-bold ${
              abaAtiva === "anamnese" ? "bg-blue-500 text-white" : "bg-gray-100"
            }`}
          >
            Anamnese
          </button>
          <button
            onClick={() => setAbaAtiva("plano")}
            className={`flex-1 p-3 border-r-2 border-gray-400 font-bold ${
              abaAtiva === "plano" ? "bg-blue-500 text-white" : "bg-gray-100"
            }`}
          >
            Plano de Tratamento
          </button>
          <button
            onClick={() => setAbaAtiva("prescricoes")}
            className={`flex-1 p-3 font-bold ${
              abaAtiva === "prescricoes" ? "bg-blue-500 text-white" : "bg-gray-100"
            }`}
          >
            Prescrições
          </button>
        </div>

        <div className="p-6 bg-white">
          {!pacienteSelecionado ? (
            <div className="text-center text-gray-500 py-12">
              Cadastre um paciente para começar.
            </div>
          ) : (
            <>
              {erroAcao && (
                <div className="mb-4 border-2 border-red-400 bg-red-50 text-red-700 text-sm p-3 rounded flex justify-between items-start gap-3">
                  <span>{erroAcao}</span>
                  <button onClick={() => setErroAcao(null)}><X className="w-4 h-4" /></button>
                </div>
              )}

              {abaAtiva === "anamnese" && (
                <div className="space-y-4">
                  {carregandoAnamnese ? (
                    <div className="flex items-center gap-2 text-gray-500 text-sm py-8 justify-center">
                      <Loader2 className="w-4 h-4 animate-spin" /> Carregando anamnese...
                    </div>
                  ) : !anamnese ? (
                    <form onSubmit={handleRegistrarAnamnese} className="space-y-4">
                      <h3 className="font-bold text-lg">Registrar Anamnese</h3>
                      <p className="text-sm text-gray-500">
                        Nenhuma anamnese registrada para este paciente ainda.
                      </p>
                      <div>
                        <label className="text-sm font-bold mb-1 block">Alergias</label>
                        <input
                          className="w-full border-2 border-gray-300 p-2 rounded"
                          placeholder="Ex: Penicilina, Dipirona"
                          value={formRegistro.alergias}
                          onChange={(e) => setFormRegistro({ ...formRegistro, alergias: e.target.value })}
                        />
                      </div>
                      <div>
                        <label className="text-sm font-bold mb-1 block">Contraindicações</label>
                        <input
                          className="w-full border-2 border-gray-300 p-2 rounded"
                          value={formRegistro.contraindicacoes}
                          onChange={(e) => setFormRegistro({ ...formRegistro, contraindicacoes: e.target.value })}
                        />
                      </div>
                      <div>
                        <label className="text-sm font-bold mb-1 block">Condições Sistêmicas</label>
                        <textarea
                          className="w-full border-2 border-gray-300 p-2 rounded resize-none"
                          rows={3}
                          placeholder="Ex: Hipertensão, Diabetes..."
                          value={formRegistro.condicoesSistemicas}
                          onChange={(e) => setFormRegistro({ ...formRegistro, condicoesSistemicas: e.target.value })}
                        />
                      </div>
                      <div>
                        <label className="text-sm font-bold mb-1 block">Responsável <span className="text-red-500">*</span></label>
                        <select
                          className="w-full border-2 border-gray-300 p-2 rounded bg-white"
                          value={responsavelSelecionado}
                          onChange={(e) => setResponsavelSelecionado(e.target.value)}
                          required
                        >
                          <option value="">Selecione o cirurgião-dentista</option>
                          {cirurgioesDentistas.map((c) => (
                            <option key={c.nome} value={c.nome}>{c.nome}</option>
                          ))}
                        </select>
                        {cirurgioesDentistas.length === 0 && (
                          <p className="text-xs text-red-500 mt-1">
                            Nenhum cirurgião-dentista cadastrado na Equipe ainda.
                          </p>
                        )}
                      </div>
                      <button
                        type="submit"
                        disabled={salvandoAnamnese}
                        className="px-4 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
                      >
                        <FileText className="w-4 h-4" /> {salvandoAnamnese ? "Salvando..." : "Registrar Anamnese"}
                      </button>
                    </form>
                  ) : (
                    <>
                      <div className="flex justify-between items-center mb-4">
                        <h3 className="font-bold text-lg">Dados Clínicos</h3>
                        <span className="text-xs text-gray-500">Versão {anamnese.versaoAtual}</span>
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div className="border-2 border-gray-300 p-3 rounded">
                          <div className="text-sm font-bold mb-1">Alergias</div>
                          <div className={anamnese.alergias ? "text-red-600 font-bold" : "text-gray-600"}>
                            {anamnese.alergias || "Nenhuma registrada"}
                          </div>
                          <div className="mt-3 flex gap-2">
                            <input
                              className="flex-1 border-2 border-gray-300 p-1.5 rounded text-sm"
                              placeholder="Nova alergia"
                              value={novaAlergiaInput}
                              onChange={(e) => setNovaAlergiaInput(e.target.value)}
                            />
                            <button
                              onClick={handleAdicionarAlergia}
                              className="px-3 py-1.5 bg-blue-600 text-white text-sm font-bold rounded hover:bg-blue-700"
                            >
                              + Adicionar
                            </button>
                          </div>
                        </div>
                        <div className="border-2 border-gray-300 p-3 rounded">
                          <div className="text-sm font-bold mb-1">Contraindicações</div>
                          <div className="text-gray-600">{anamnese.contraindicacoes || "—"}</div>
                        </div>
                        <div className="border-2 border-gray-300 p-3 rounded col-span-2">
                          <div className="text-sm font-bold mb-1">Condições Sistêmicas</div>
                          <textarea
                            className="w-full border border-gray-300 p-2 rounded resize-none text-sm"
                            rows={2}
                            value={condicoesEditando}
                            onChange={(e) => setCondicoesEditando(e.target.value)}
                          />
                          <button
                            onClick={handleSalvarCondicoes}
                            disabled={condicoesEditando === anamnese.condicoesSistemicas}
                            className="mt-2 px-3 py-1.5 bg-blue-600 text-white text-sm font-bold rounded hover:bg-blue-700 disabled:opacity-50"
                          >
                            Salvar condições
                          </button>
                        </div>
                      </div>

                      <div className="mt-6 pt-4 border-t border-gray-200 flex items-center gap-4">
                        <span className="text-xs text-gray-500">
                          Última atualização: {formatarData(anamnese.dataUltimaAtualizacao)} por {anamnese.responsavelCadastro}
                        </span>
                        <button
                          onClick={() => setMostrarHistorico((v) => !v)}
                          className="text-xs text-blue-600 hover:text-blue-800 underline flex items-center gap-1"
                        >
                          <FileClock className="w-3 h-3" />
                          Ver histórico de alterações
                          {mostrarHistorico ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                        </button>
                      </div>

                      {mostrarHistorico && (
                        <div className="mt-2 border-2 border-gray-300 rounded divide-y">
                          {anamnese.historicoVersoes.length === 0 ? (
                            <div className="p-3 text-sm text-gray-400 italic">Sem versões anteriores.</div>
                          ) : (
                            anamnese.historicoVersoes.map((v, i) => (
                              <div key={i} className="p-3 text-sm">
                                <div className="font-bold mb-1">Versão {v.versao} — {formatarData(v.dataAlteracao)} ({v.responsavel})</div>
                                <div className="text-gray-600">Alergias: {v.alergias || "—"}</div>
                                <div className="text-gray-600">Condições sistêmicas: {v.condicoesSistemicas || "—"}</div>
                              </div>
                            ))
                          )}
                        </div>
                      )}
                    </>
                  )}
                </div>
              )}

              {abaAtiva === "plano" && (
                <div>
                  {!anamnesePreenchida ? (
                    <div className="flex flex-col items-center justify-center py-16 px-4 text-center border-2 border-dashed border-gray-300 bg-gray-50 rounded">
                      <Lock className="w-16 h-16 text-gray-400 mb-4" />
                      <h3 className="text-lg font-bold text-gray-700 mb-2">Ação bloqueada</h3>
                      <p className="text-gray-500 mb-6 max-w-md">
                        É obrigatório o preenchimento da Anamnese antes de criar um plano.
                      </p>
                      <button
                        onClick={() => setAbaAtiva("anamnese")}
                        className="px-6 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 transition-colors"
                      >
                        Preencher Anamnese
                      </button>
                    </div>
                  ) : carregandoPlano ? (
                    <div className="flex items-center gap-2 text-gray-500 text-sm py-8 justify-center">
                      <Loader2 className="w-4 h-4 animate-spin" /> Carregando plano...
                    </div>
                  ) : !plano ? (
                    <div className="flex flex-col items-center justify-center py-16 px-4 text-center border-2 border-dashed border-gray-300 bg-gray-50 rounded">
                      <FileText className="w-16 h-16 text-gray-400 mb-4" />
                      <h3 className="text-lg font-bold text-gray-700 mb-2">Nenhum plano de tratamento</h3>
                      <button
                        onClick={handleCriarPlano}
                        disabled={salvandoAcao}
                        className="px-6 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 disabled:opacity-50"
                      >
                        Criar Plano de Tratamento
                      </button>
                    </div>
                  ) : (
                    <div>
                      <div className="flex justify-between items-center mb-4">
                        <h3 className="font-bold text-lg">
                          Plano Atual (Versão {plano.versao}) — {STATUS_PLANO_LABEL[plano.status]}
                        </h3>
                        <div className="flex gap-2">
                          <button
                            onClick={() => setModalNovoProcedimento(true)}
                            className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold rounded text-sm hover:bg-blue-600 flex items-center gap-1"
                          >
                            <Plus className="w-4 h-4" /> Procedimento
                          </button>
                          {plano.status === "EM_ANDAMENTO" && (
                            <button
                              onClick={() => setModalJustificativa({aberto: true, acao: 'encerrar-plano'})}
                              className="px-4 py-2 border border-gray-400 bg-white hover:bg-gray-50 text-gray-700 font-bold rounded text-sm transition-colors"
                            >
                              Encerrar Plano
                            </button>
                          )}
                          <button
                            onClick={() => setModalJustificativa({aberto: true, acao: 'excluir-plano'})}
                            className="px-4 py-2 border border-red-500 bg-white hover:bg-red-50 text-red-600 font-bold rounded text-sm transition-colors"
                          >
                            Excluir Plano
                          </button>
                        </div>
                      </div>

                      {plano.justificativaEncerramento && (
                        <div className="mb-3 text-sm text-orange-700 bg-orange-50 border border-orange-300 p-2 rounded">
                          Encerrado: {plano.justificativaEncerramento}
                        </div>
                      )}

                      <div className="border-2 border-gray-400 bg-white overflow-x-auto">
                        <div className="min-w-[900px]">
                          <div className="grid grid-cols-6 bg-gray-100 border-b-2 border-gray-400 text-sm">
                            <div className="p-3 border-r-2 border-gray-400 font-bold">Procedimento</div>
                            <div className="p-3 border-r-2 border-gray-400 font-bold">Tipo</div>
                            <div className="p-3 border-r-2 border-gray-400 font-bold">Executor / Data</div>
                            <div className="p-3 border-r-2 border-gray-400 font-bold">Status</div>
                            <div className="p-3 border-r-2 border-gray-400 font-bold">Evolução</div>
                            <div className="p-3 font-bold text-center">Ações</div>
                          </div>

                          {plano.procedimentos.length === 0 ? (
                            <div className="p-6 text-center text-gray-400 italic">Nenhum procedimento ainda.</div>
                          ) : (
                            plano.procedimentos.map((proc) => (
                              <div key={proc.id} className="grid grid-cols-6 border-b-2 border-gray-400 items-center text-sm">
                                <div className="p-3 border-r-2 border-gray-400">{proc.nome}</div>
                                <div className="p-3 border-r-2 border-gray-400">{proc.tipoProcedimento}</div>
                                <div className="p-3 border-r-2 border-gray-400">
                                  {proc.executor ? `${proc.executor} — ${formatarData(proc.dataRealizacao)}` : "—"}
                                </div>
                                <div className="p-3 border-r-2 border-gray-400">
                                  <span className={`border px-2 py-1 rounded text-xs font-bold ${STATUS_PROCEDIMENTO_CLASS[proc.status]}`}>
                                    {STATUS_PROCEDIMENTO_LABEL[proc.status]}
                                  </span>
                                </div>
                                <div className="p-3 border-r-2 border-gray-400 text-xs text-gray-600">
                                  {proc.evolucao?.descricaoTecnica || (proc.justificativaCancelamento ? `Justificativa: ${proc.justificativaCancelamento}` : "—")}
                                </div>
                                <div className="p-3 flex items-center justify-center gap-2">
                                  {proc.status === "PENDENTE" && (
                                    <>
                                      <button
                                        onClick={() => {
                                          setMateriaisUsados([]);
                                          setErroMateriais(null);
                                          setRealizarAgendamentoId("");
                                          setModalRealizar({aberto: true, procedimento: proc});
                                        }}
                                        className="px-2 py-1 border-2 border-green-500 text-green-700 bg-white text-xs font-bold rounded hover:bg-green-50"
                                      >
                                        Realizar
                                      </button>
                                      <button
                                        onClick={() => setModalJustificativa({aberto: true, acao: 'cancelar-procedimento', procedimentoId: proc.id})}
                                        className="px-2 py-1 border-2 border-gray-400 text-gray-600 bg-white text-xs font-bold rounded hover:bg-gray-50"
                                      >
                                        Cancelar
                                      </button>
                                    </>
                                  )}
                                  {proc.status === "REALIZADO" && (
                                    proc.dentroDaJanelaDeCorrecao ? (
                                      <button
                                        onClick={() => setModalJustificativa({aberto: true, acao: 'excluir-procedimento', procedimentoId: proc.id})}
                                        className="px-2 py-1 border-2 border-red-500 text-red-600 bg-white text-xs font-bold rounded hover:bg-red-50 flex items-center gap-1"
                                      >
                                        <Trash2 className="w-3 h-3" /> Excluir
                                      </button>
                                    ) : (
                                      <div className="flex items-center gap-1 text-gray-400" title="Registro imutável após 24h">
                                        <Lock className="w-4 h-4" />
                                        <span className="text-xs">&gt;24h</span>
                                      </div>
                                    )
                                  )}
                                  {proc.status === "CANCELADO" && <XCircle className="w-4 h-4 text-gray-400" />}
                                </div>
                              </div>
                            ))
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {abaAtiva === "prescricoes" && (
            <div>
              {!anamnesePreenchida ? (
                <div className="flex flex-col items-center justify-center py-16 px-4 text-center border-2 border-dashed border-gray-300 bg-gray-50 rounded">
                  <Lock className="w-16 h-16 text-gray-400 mb-4" />
                  <h3 className="text-lg font-bold text-gray-700 mb-2">Ação bloqueada</h3>
                  <p className="text-gray-500 mb-6 max-w-md">
                    É obrigatório o preenchimento da Anamnese antes de criar prescrições.
                  </p>
                  <button
                    onClick={() => setAbaAtiva("anamnese")}
                    className="px-6 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 transition-colors"
                  >
                    Preencher Anamnese
                  </button>
                </div>
              ) : (
                <div>
                  <div className="flex justify-between items-center mb-4">
                    <h3 className="font-bold text-lg">Prescrições</h3>
                    <button
                      onClick={() => setModalNovaPrescricao(true)}
                      className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold rounded hover:bg-blue-600 transition-colors flex items-center gap-2"
                    >
                      <Pill className="w-4 h-4" />
                      + Nova Prescrição
                    </button>
                  </div>

                  <div className="border-2 border-gray-400 bg-white overflow-x-auto">
                    <div className="min-w-[1000px]">
                      <div className="grid grid-cols-8 bg-gray-100 border-b-2 border-gray-400 text-sm">
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Data</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Medicamento</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Dosagem</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Posologia</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Período</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Agendamento</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Responsável</div>
                        <div className="p-3 font-bold text-center">Ações</div>
                      </div>

                      {prescricoes.map((prescricao) => (
                        <div key={prescricao.id} className="grid grid-cols-8 border-b-2 border-gray-400 items-center text-sm">
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.data}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.medicamento}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.dosagem}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.posologia}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.periodo}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.agendamento}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.responsavel}</div>
                          <div className="p-3 flex items-center justify-center gap-2">
                            <button
                              onClick={() => setModalRepetirPrescricao({aberto: true, prescricao})}
                              className="px-3 py-1 border-2 border-blue-500 bg-white text-blue-600 text-xs font-bold rounded hover:bg-blue-50 transition-colors flex items-center gap-1"
                              title="Repetir prescrição"
                            >
                              <RotateCcw className="w-3 h-3" />
                              Repetir
                            </button>
                            {prescricao.editavel ? (
                              <>
                                <button
                                  onClick={() => setModalExcluirPrescricao({aberto: true, prescricao})}
                                  className="px-3 py-1 border-2 border-red-500 bg-white text-red-600 text-xs font-bold rounded hover:bg-red-50 transition-colors"
                                >
                                  Excluir
                                </button>
                                <span className="bg-yellow-100 text-yellow-800 border border-yellow-500 px-2 py-1 rounded text-xs font-bold">
                                  Editável
                                </span>
                              </>
                            ) : (
                              <div className="flex items-center gap-2 text-gray-400" title="Registro imutável após 24h">
                                <Lock className="w-4 h-4" />
                                <span className="text-xs">&gt;24h</span>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
              )}
            </>
          )}
        </div>
      </div>

      {alertaAberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-red-500 w-[500px] p-6">
            <div className="flex items-start gap-3 mb-4">
              <span className="text-4xl">🚨</span>
              <div>
                <h2 className="text-lg font-bold text-red-700">
                  Alerta de Complicação Pós-Operatória
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  Paciente Marcos Pereira reportou sintomas pós-cirúrgicos
                </p>
              </div>
            </div>

            <div className="border-2 border-gray-300 bg-gray-50 p-4 mb-4">
              <div className="text-sm space-y-2">
                <div>
                  <span className="font-bold">Procedimento:</span> Extração do dente 38
                </div>
                <div>
                  <span className="font-bold">Data:</span> 26/04/2026
                </div>
                <div>
                  <span className="font-bold">Sintomas reportados:</span>
                  <ul className="list-disc ml-5 mt-1">
                    <li>Dor intensa</li>
                    <li>Sangramento excessivo</li>
                  </ul>
                </div>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setAlertaAberto(false)}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white"
              >
                Dispensar
              </button>
              <button onClick={() => setAlertaAberto(false)} className="flex-1 px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold">
                Abrir Prontuário
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Adicionar Procedimento */}
      {modalNovoProcedimento && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <form onSubmit={handleAdicionarProcedimento} className="bg-white w-full max-w-md rounded shadow-xl p-6">
            <h2 className="text-lg font-bold text-gray-800 mb-4">Adicionar Procedimento</h2>
            <div className="space-y-3">
              <div>
                <label className="text-sm font-bold mb-1 block">Procedimento <span className="text-red-500">*</span></label>
                <input
                  className="w-full border-2 border-gray-300 p-2 rounded"
                  placeholder="Ex: Restauração dente 16"
                  value={novoProcNome}
                  onChange={(e) => setNovoProcNome(e.target.value)}
                  required
                />
              </div>
              <div>
                <label className="text-sm font-bold mb-1 block">Tipo <span className="text-red-500">*</span></label>
                <input
                  className="w-full border-2 border-gray-300 p-2 rounded"
                  placeholder="Ex: Restauradora, Cirúrgica, Preventiva..."
                  value={novoProcTipo}
                  onChange={(e) => setNovoProcTipo(e.target.value)}
                  required
                />
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button type="button" onClick={() => setModalNovoProcedimento(false)} className="px-4 py-2 border-2 border-gray-300 text-gray-700 font-bold rounded hover:bg-gray-50">
                Cancelar
              </button>
              <button type="submit" className="px-4 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700">
                Adicionar
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Modal: Realizar Procedimento */}
      {modalRealizar.aberto && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <form onSubmit={handleRealizarProcedimento} className="bg-white w-full max-w-lg rounded shadow-xl">
            <div className="p-4 border-b border-gray-200 bg-gray-50 rounded-t">
              <h2 className="text-lg font-bold text-gray-800">Confirmar Procedimento</h2>
              <p className="text-sm text-gray-500">{modalRealizar.procedimento?.nome}</p>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">
                  Descreva a Evolução Clínica <span className="text-red-500">*</span>
                </label>
                <textarea
                  className="w-full border-2 border-gray-300 p-3 rounded h-28 focus:border-blue-500 focus:outline-none resize-none"
                  placeholder="Descreva os detalhes do procedimento realizado, materiais utilizados, reações do paciente..."
                  value={realizarDescricao}
                  onChange={(e) => setRealizarDescricao(e.target.value)}
                  required
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-2">
                    Agendamento vinculado <span className="text-red-500">*</span>
                  </label>
                  <select
                    className="w-full border-2 border-gray-300 p-2 rounded bg-white disabled:bg-gray-100"
                    value={realizarAgendamentoId}
                    onChange={(e) => setRealizarAgendamentoId(e.target.value)}
                    disabled={agendamentosPaciente.length === 0}
                    required
                  >
                    <option value="">
                      {agendamentosPaciente.length === 0 ? "Nenhum agendamento encontrado" : "Selecione o agendamento"}
                    </option>
                    {agendamentosPaciente.map((a) => (
                      <option key={a.id} value={a.id}>{a.label}</option>
                    ))}
                  </select>
                  <p className="text-xs text-gray-400 mt-1">
                    {agendamentosPaciente.length === 0
                      ? "Este paciente não possui agendamentos para vincular — crie um agendamento antes de realizar o procedimento."
                      : "Vincula a evolução ao agendamento real em que o procedimento ocorreu (rastreio do prontuário). Obrigatório."}
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-2">Executor</label>
                  <div className="w-full border-2 border-gray-200 bg-gray-100 text-gray-700 p-2 rounded min-h-[2.5rem] flex items-center">
                    {executorDoAgendamentoSelecionado || "—"}
                  </div>
                  <p className="text-xs text-gray-400 mt-1">
                    Definido automaticamente pelo cirurgião-dentista do agendamento selecionado.
                  </p>
                </div>
              </div>

              <div>
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-sm font-bold text-gray-700">Materiais usados</label>
                  <button
                    type="button"
                    onClick={() => setMateriaisUsados((atual) => [...atual, { material: "", quantidade: "1" }])}
                    className="text-xs text-blue-600 hover:text-blue-800 font-bold flex items-center gap-1"
                  >
                    <Plus className="w-3 h-3" /> Adicionar material
                  </button>
                </div>
                {materiaisUsados.length === 0 ? (
                  <p className="text-xs text-gray-400">Nenhum material informado.</p>
                ) : (
                  <div className="space-y-2">
                    {materiaisUsados.map((item, i) => {
                      const materialInfo = materiaisDisponiveis.find((m) => m.nome === item.material);
                      return (
                        <div key={i} className="flex gap-2 items-start">
                          <select
                            className="flex-1 border-2 border-gray-300 p-1.5 rounded bg-white text-sm"
                            value={item.material}
                            onChange={(e) => setMateriaisUsados((atual) =>
                              atual.map((m, j) => (j === i ? { ...m, material: e.target.value } : m)))}
                          >
                            <option value="">Selecione o material</option>
                            {materiaisDisponiveis.map((m) => (
                              <option key={m.nome} value={m.nome}>
                                {m.nome} ({m.quantidadeEmEstoque} {m.unidadeMedida} em estoque)
                              </option>
                            ))}
                          </select>
                          <input
                            type="number"
                            min={1}
                            max={materialInfo?.quantidadeEmEstoque ?? undefined}
                            className="w-24 border-2 border-gray-300 p-1.5 rounded text-sm"
                            placeholder="Qtd."
                            value={item.quantidade}
                            onChange={(e) => setMateriaisUsados((atual) =>
                              atual.map((m, j) => (j === i ? { ...m, quantidade: e.target.value } : m)))}
                          />
                          <button
                            type="button"
                            onClick={() => setMateriaisUsados((atual) => atual.filter((_, j) => j !== i))}
                            className="text-gray-400 hover:text-red-500 p-1.5"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      );
                    })}
                  </div>
                )}
                {erroMateriais && (
                  <div className="mt-2 text-xs text-red-600 border border-red-300 bg-red-50 p-2 rounded">{erroMateriais}</div>
                )}
              </div>

              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setModalRealizar({ aberto: false })}
                  className="px-4 py-2 border-2 border-gray-300 text-gray-700 font-bold rounded hover:bg-gray-50"
                >
                  Cancelar
                </button>
                <button type="submit" className="px-4 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700">
                  Salvar
                </button>
              </div>
            </div>
          </form>
        </div>
      )}

      {/* Modal de Justificativa (plano e procedimentos) */}
      {modalJustificativa.aberto && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white w-full max-w-md rounded shadow-xl border-t-4 border-orange-500">
            <div className="p-4 border-b border-gray-200 bg-orange-50 rounded-t flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-orange-600" />
              <h2 className="text-lg font-bold text-orange-800">Justificativa Obrigatória</h2>
            </div>
            <div className="p-6">
              <p className="text-sm text-gray-600 mb-4">
                {modalJustificativa.acao === 'excluir-plano' && 'Você está prestes a excluir este plano de tratamento.'}
                {modalJustificativa.acao === 'encerrar-plano' && 'Você está prestes a encerrar este plano de tratamento.'}
                {modalJustificativa.acao === 'cancelar-procedimento' && 'Você está prestes a cancelar este procedimento.'}
                {modalJustificativa.acao === 'excluir-procedimento' && 'Você está prestes a excluir este procedimento do histórico.'}
                {' '}Forneça o motivo para registrar no histórico do paciente.
              </p>
              <div className="mb-4">
                <textarea
                  className="w-full border-2 border-gray-300 p-3 rounded h-24 focus:border-orange-500 focus:outline-none resize-none"
                  placeholder="Digite o motivo..."
                  value={justificativaInput}
                  onChange={(e) => setJustificativaInput(e.target.value)}
                  required
                />
              </div>
              {modalJustificativa.acao === 'excluir-procedimento' && (
                <div className="mb-4">
                  <label className="text-sm font-bold mb-1 block">Responsável</label>
                  <select
                    className="w-full border-2 border-gray-300 p-2 rounded bg-white"
                    value={responsavelAcaoInput}
                    onChange={(e) => setResponsavelAcaoInput(e.target.value)}
                  >
                    <option value="">Selecione o cirurgião-dentista</option>
                    {cirurgioesDentistas.map((c) => (
                      <option key={c.nome} value={c.nome}>{c.nome}</option>
                    ))}
                  </select>
                </div>
              )}
              <div className="flex gap-3">
                <button
                  onClick={() => { setModalJustificativa({aberto: false}); setJustificativaInput(""); }}
                  className="flex-1 px-4 py-2 border-2 border-gray-300 text-gray-700 font-bold rounded hover:bg-gray-50 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  onClick={confirmarJustificativa}
                  disabled={salvandoAcao || !justificativaInput.trim()}
                  className="flex-1 px-4 py-2 bg-orange-500 text-white font-bold rounded hover:bg-orange-600 transition-colors disabled:opacity-50"
                >
                  {salvandoAcao ? "Confirmando..." : "Confirmar"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Nova Prescrição */}
      {modalNovaPrescricao && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[500px] p-6">
            <h2 className="text-xl font-bold text-gray-700 mb-4">Nova Prescrição</h2>

            <div className="space-y-4">
              <div>
                <label className="text-sm font-bold mb-2 block">
                  Medicamento <span className="text-red-500">*</span>
                </label>
                <div ref={dropdownRef} className="relative">
                  <div className={`flex items-center border-2 ${dropdownAberto ? "border-blue-500" : "border-gray-300"} bg-white rounded`}>
                    <Search className="w-4 h-4 text-gray-400 ml-2 shrink-0" />
                    <input
                      type="text"
                      className="flex-1 p-2 bg-transparent focus:outline-none text-sm"
                      placeholder="Buscar por nome ou princípio ativo"
                      value={buscaMedicamento}
                      onChange={(e) => { setBuscaMedicamento(e.target.value); setMedicamentoSelecionado(null); setDropdownAberto(true); }}
                      onFocus={() => setDropdownAberto(true)}
                    />
                    {medicamentoSelecionado && (
                      <button onClick={limparMedicamento} className="mr-2 text-gray-400 hover:text-red-500">
                        <X className="w-4 h-4" />
                      </button>
                    )}
                  </div>

                  {dropdownAberto && (
                    <div className="absolute left-0 right-0 top-full mt-1 bg-white border-2 border-gray-400 shadow-lg z-50 max-h-48 overflow-y-auto">
                      {medicamentosFiltrados.length === 0 ? (
                        <div className="p-3 text-sm text-gray-400 italic">Nenhum medicamento encontrado.</div>
                      ) : (
                        medicamentosFiltrados.map((med) => (
                          <button
                            key={med.id}
                            type="button"
                            onClick={() => selecionarMedicamento(med)}
                            className="w-full text-left px-3 py-2 hover:bg-blue-50 border-b border-gray-100 last:border-0"
                          >
                            <span className="font-bold text-sm text-gray-800">{med.nomeComercial}</span>
                            <span className="text-xs text-gray-500 ml-2">{med.principioAtivo}</span>
                          </button>
                        ))
                      )}
                    </div>
                  )}
                </div>

                {medicamentoSelecionado && (
                  <div className="mt-2 bg-blue-50 border border-blue-300 p-3 rounded text-sm space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-bold text-gray-500">Categoria:</span>
                      <span className="bg-blue-100 border border-blue-400 px-2 py-0.5 rounded text-xs font-bold text-blue-700">{medicamentoSelecionado.categoria}</span>
                      <span className="text-xs font-bold text-gray-500 ml-2">Classe:</span>
                      <span className="text-xs text-gray-700">{medicamentoSelecionado.classeFarmacologica}</span>
                    </div>
                    {medicamentoSelecionado.contraindicacoes && (
                      <div className="text-xs text-yellow-800 flex gap-1">
                        <AlertTriangle className="w-3 h-3 mt-0.5 shrink-0" />
                        <span><strong>Contraindicações:</strong> {medicamentoSelecionado.contraindicacoes}</span>
                      </div>
                    )}
                    {medicamentoSelecionado.interacoes && (
                      <div className="text-xs text-orange-700 flex gap-1">
                        <AlertTriangle className="w-3 h-3 mt-0.5 shrink-0" />
                        <span><strong>Interações:</strong> {medicamentoSelecionado.interacoes}</span>
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Dosagem <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  placeholder="Ex: 500mg"
                  value={dosagemInput}
                  onChange={(e) => setDosagemInput(e.target.value)}
                />
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Posologia <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  placeholder="Ex: 1 cáps. a cada 8h"
                  value={posologiaInput}
                  onChange={(e) => setPosologiaInput(e.target.value)}
                />
                {medicamentoSelecionado && medicamentoSelecionado.posologiasPadrao.length > 0 && (
                  <div className="mt-2 space-y-1">
                    <div className="text-xs text-gray-500 font-bold">Posologias padrão do catálogo:</div>
                    {medicamentoSelecionado.posologiasPadrao.map((p, i) => (
                      <button
                        key={i}
                        type="button"
                        onClick={() => setPosologiaInput(p)}
                        className="w-full text-left text-xs px-2 py-1.5 border border-blue-400 bg-blue-50 text-blue-700 rounded hover:bg-blue-100 font-medium"
                      >
                        ↳ {p}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Período de uso <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  placeholder="Ex: 7 dias"
                  value={periodoInput}
                  onChange={(e) => setPeriodoInput(e.target.value)}
                />
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Agendamento vinculado <span className="text-red-500">*</span>
                </label>
                <select
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  value={agendamentoInput}
                  onChange={(e) => setAgendamentoInput(e.target.value)}
                >
                  <option value="">Selecione um agendamento</option>
                  <option value="AGE-0607">AGE-0607 - 08/06/2026 - Restauração</option>
                  <option value="AGE-0419">AGE-0419 - 20/04/2026 - Limpeza</option>
                  <option value="AGE-0412">AGE-0412 - 15/04/2026 - Extração</option>
                </select>
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Observações terapêuticas
                </label>
                <textarea
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none resize-none"
                  rows={3}
                  placeholder="Motivo da prescrição ou recomendações verbais"
                  value={observacoesInput}
                  onChange={(e) => setObservacoesInput(e.target.value)}
                ></textarea>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => {
                  setModalNovaPrescricao(false);
                  limparFormulario();
                }}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
              >
                Cancelar
              </button>
              <button
                onClick={handleSalvarPrescricao}
                className="flex-1 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold"
              >
                Salvar Prescrição
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Alerta de Contraindicação por Alergia */}
      {modalAlertaAlergia && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-red-500 w-[500px] p-6">
            <div className="flex items-start gap-3 mb-4">
              <span className="text-4xl">🚨</span>
              <div>
                <h2 className="text-xl font-bold text-red-700">Alerta de Contraindicação</h2>
              </div>
            </div>

            <p className="text-sm text-gray-700 mb-4">
              O medicamento informado pertence a uma classe à qual o paciente possui alergia registrada na anamnese:{" "}
              <strong>{contraindicacaoDetectada}</strong>.
            </p>

            <div className="bg-red-50 border-2 border-red-300 p-4 mb-4">
              <p className="text-sm text-red-800 font-bold">
                O salvamento está bloqueado. Para prosseguir, o Cirurgião-Dentista deve confirmar ciência do risco. Essa confirmação será registrada em log de auditoria.
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  setModalAlertaAlergia(false);
                  setModalNovaPrescricao(true);
                }}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
              >
                Revisar Medicamento
              </button>
              <button
                onClick={handleConfirmarCienciaRisco}
                className="flex-1 px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold"
              >
                Confirmo Ciência do Risco
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Alerta de Prescrição Recente */}
      {modalAlertaRecente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[500px]">
            <div className="bg-orange-50 p-4 flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <h2 className="text-xl font-bold text-orange-800">Prescrição Recente Identificada</h2>
            </div>

            <div className="p-6">
              <p className="text-sm text-gray-700 mb-4">
                Este medicamento já foi prescrito a este paciente nos últimos 30 dias.
              </p>

              <div className="bg-gray-100 border-2 border-gray-300 p-4 mb-4">
                <div className="text-sm">
                  <div className="font-bold mb-2">Última prescrição:</div>
                  <div><strong>Data:</strong> {prescricaoRecenteDetectada?.data}</div>
                  <div><strong>Medicamento:</strong> {prescricaoRecenteDetectada?.medicamento} {prescricaoRecenteDetectada?.dosagem}</div>
                  <div><strong>Posologia:</strong> {prescricaoRecenteDetectada?.posologia} por {prescricaoRecenteDetectada?.periodo}</div>
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => {
                    setModalAlertaRecente(false);
                    limparFormulario();
                  }}
                  className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
                >
                  Cancelar
                </button>
                <button
                  onClick={salvarPrescricaoFinal}
                  className="flex-1 px-4 py-2 border-2 border-orange-500 bg-orange-500 text-white font-bold"
                >
                  Salvar Mesmo Assim
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Re-prescrição */}
      {modalRepetirPrescricao.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[500px] p-6">
            <h2 className="text-xl font-bold text-gray-700 mb-4">Repetir Prescrição</h2>

            <div className="bg-gray-100 border-2 border-gray-300 p-4 mb-4">
              <div className="text-sm space-y-2">
                <div><strong>Medicamento:</strong> {modalRepetirPrescricao.prescricao?.medicamento}</div>
                <div><strong>Dosagem:</strong> {modalRepetirPrescricao.prescricao?.dosagem}</div>
                <div><strong>Posologia:</strong> {modalRepetirPrescricao.prescricao?.posologia}</div>
                <div><strong>Período:</strong> {modalRepetirPrescricao.prescricao?.periodo}</div>
              </div>
            </div>

            <div className="bg-yellow-50 border-2 border-yellow-300 p-3 mb-4">
              <p className="text-xs text-yellow-800">
                <AlertTriangle className="w-4 h-4 inline mr-1" />
                O sistema irá revalidar automaticamente as alergias da anamnese atualizada do paciente antes de confirmar.
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setModalRepetirPrescricao({aberto: false})}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
              >
                Cancelar
              </button>
              <button
                onClick={handleConfirmarRepeticao}
                className="flex-1 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold"
              >
                Confirmar Repetição
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Justificativa de Exclusão (prescrição) */}
      {modalExcluirPrescricao.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[500px]">
            <div className="bg-orange-50 p-4 flex items-center gap-3">
              <ShieldAlert className="w-6 h-6 text-orange-600" />
              <h2 className="text-xl font-bold text-orange-800">Exclusão de Prescrição</h2>
            </div>

            <div className="p-6">
              <p className="text-sm text-gray-700 mb-4">
                Esta exclusão será registrada em log de auditoria. Forneça o motivo obrigatoriamente.
              </p>

              <div className="mb-4">
                <label className="text-sm font-bold mb-2 block">
                  Justificativa <span className="text-red-500">*</span>
                </label>
                <textarea
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-orange-500 focus:outline-none resize-none"
                  rows={4}
                  placeholder="Digite o motivo da exclusão..."
                ></textarea>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setModalExcluirPrescricao({aberto: false})}
                  className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
                >
                  Cancelar
                </button>
                <button
                  onClick={() => setModalExcluirPrescricao({aberto: false})}
                  className="flex-1 px-4 py-2 border-2 border-orange-500 bg-orange-500 text-white font-bold"
                >
                  Confirmar Exclusão
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
