import { useState, useMemo, useEffect, useCallback } from "react";
import { api } from "../../api/client";

// ─── Types ────────────────────────────────────────────────────────────────────

type PrioridadeRecall = "CRITICO" | "ALTO" | "NORMAL" | "BAIXO";
type StatusRecall     = "PENDENTE" | "CONTATADO" | "AGENDADO" | "NAO_RESPONSIVO" | "RECUSOU";
type CanalContato     = "TELEFONE" | "WHATSAPP" | "EMAIL" | "SMS";
type ResultadoContato = "ATENDEU" | "NAO_ATENDEU" | "CAIXA_POSTAL" | "AGENDOU" | "RECUSOU";

interface TentativaContato {
  id: number;
  data: string;
  hora: string;
  canal: CanalContato;
  responsavel: string;
  resultado: ResultadoContato;
  observacao: string;
}

interface RecallItem {
  id: number;
  paciente: string;
  telefone: string;
  dentista: string;
  procedimento: string;
  dataUltimoProcedimento: string;
  diasDesdeUltimo: number;
  intervaloRecomendado: number;
  diasVencidos: number;
  prioridade: PrioridadeRecall;
  status: StatusRecall;
  inadimplente: boolean;
  temAgendamentoFuturo: boolean;
  tratamentoCritico: boolean;
  historicoFaltas: number;
  motivoRecall: string;
  alertaInatividade: boolean;
  conversaoRecall: boolean;
  tentativas: TentativaContato[];
  auditLog: { data: string; acao: string; responsavel: string }[];
}

// ─── Mock data ────────────────────────────────────────────────────────────────
// Ordenado por prioridade: CRITICO → ALTO → NORMAL
// Fernanda Ramos excluída: já possui agendamento futuro

const MOCK_RECALL: RecallItem[] = [
  {
    id: 1,
    paciente: "Marcos Pereira", telefone: "(81) 9 9999-0009", dentista: "Dra. Sofia Martins",
    procedimento: "Cirurgia — Extração", dataUltimoProcedimento: "26/05/2026",
    diasDesdeUltimo: 12, intervaloRecomendado: 1, diasVencidos: 11,
    prioridade: "CRITICO", status: "PENDENTE",
    inadimplente: false, temAgendamentoFuturo: false, tratamentoCritico: true,
    historicoFaltas: 0, motivoRecall: "Follow-up pós-cirúrgico 24h não realizado há 11 dias",
    alertaInatividade: true, conversaoRecall: false,
    tentativas: [],
    auditLog: [
      { data: "26/05/2026", acao: "Recall gerado automaticamente — cirurgia 24h", responsavel: "Sistema" },
    ],
  },
  {
    id: 2,
    paciente: "Lucia Oliveira", telefone: "(81) 9 9999-0006", dentista: "Dra. Camila Torres",
    procedimento: "Cirurgia — Implante Follow-up", dataUltimoProcedimento: "15/05/2026",
    diasDesdeUltimo: 23, intervaloRecomendado: 3, diasVencidos: 20,
    prioridade: "CRITICO", status: "CONTATADO",
    inadimplente: true, temAgendamentoFuturo: false, tratamentoCritico: true,
    historicoFaltas: 2, motivoRecall: "Follow-up 72h pós-cirurgia — 20 dias sem retorno",
    alertaInatividade: true, conversaoRecall: false,
    tentativas: [
      {
        id: 1, data: "20/05/2026", hora: "09:30", canal: "TELEFONE",
        responsavel: "Recepcionista", resultado: "RECUSOU",
        observacao: "Paciente disse que está bem mas recusou comparecer",
      },
    ],
    auditLog: [
      { data: "20/05/2026", acao: "1ª tentativa — paciente recusou retorno", responsavel: "Recepcionista" },
      { data: "15/05/2026", acao: "Recall gerado automaticamente — cirurgia 72h", responsavel: "Sistema" },
    ],
  },
  {
    id: 3,
    paciente: "Ana Lima", telefone: "(81) 9 9999-0004", dentista: "Dra. Camila Torres",
    procedimento: "Implante Osseointegrado", dataUltimoProcedimento: "10/04/2026",
    diasDesdeUltimo: 58, intervaloRecomendado: 45, diasVencidos: 13,
    prioridade: "CRITICO", status: "CONTATADO",
    inadimplente: false, temAgendamentoFuturo: false, tratamentoCritico: true,
    historicoFaltas: 0, motivoRecall: "Revisão de implante — acompanhamento obrigatório",
    alertaInatividade: true, conversaoRecall: false,
    tentativas: [
      {
        id: 1, data: "25/05/2026", hora: "10:00", canal: "WHATSAPP",
        responsavel: "Recepcionista", resultado: "NAO_ATENDEU", observacao: "",
      },
      {
        id: 2, data: "01/06/2026", hora: "14:00", canal: "TELEFONE",
        responsavel: "Recepcionista", resultado: "CAIXA_POSTAL",
        observacao: "Deixou mensagem na caixa postal",
      },
    ],
    auditLog: [
      { data: "01/06/2026", acao: "2ª tentativa — caixa postal", responsavel: "Recepcionista" },
      { data: "25/05/2026", acao: "1ª tentativa — não atendeu", responsavel: "Recepcionista" },
      { data: "24/05/2026", acao: "Recall gerado automaticamente — implante 45d", responsavel: "Sistema" },
    ],
  },
  {
    id: 4,
    paciente: "Carlos Souza", telefone: "(81) 9 9999-0005", dentista: "Dr. Ricardo Alves",
    procedimento: "Profilaxia", dataUltimoProcedimento: "20/10/2025",
    diasDesdeUltimo: 230, intervaloRecomendado: 180, diasVencidos: 50,
    prioridade: "CRITICO", status: "NAO_RESPONSIVO",
    inadimplente: false, temAgendamentoFuturo: false, tratamentoCritico: false,
    historicoFaltas: 3, motivoRecall: "Profilaxia semestral — 50 dias em atraso, 3 tentativas sem resposta",
    alertaInatividade: false, conversaoRecall: false,
    tentativas: [
      { id: 1, data: "10/05/2026", hora: "08:30", canal: "TELEFONE", responsavel: "Recepcionista", resultado: "NAO_ATENDEU", observacao: "" },
      { id: 2, data: "17/05/2026", hora: "15:00", canal: "WHATSAPP", responsavel: "Recepcionista", resultado: "NAO_ATENDEU", observacao: "" },
      { id: 3, data: "25/05/2026", hora: "11:00", canal: "TELEFONE", responsavel: "Recepcionista", resultado: "CAIXA_POSTAL", observacao: "Número ativo mas não atende" },
    ],
    auditLog: [
      { data: "26/05/2026", acao: "Status alterado para Não Responsivo (3 tentativas sem resposta)", responsavel: "Sistema" },
      { data: "25/05/2026", acao: "3ª tentativa — caixa postal", responsavel: "Recepcionista" },
      { data: "17/05/2026", acao: "2ª tentativa — não atendeu", responsavel: "Recepcionista" },
      { data: "10/05/2026", acao: "1ª tentativa — não atendeu", responsavel: "Recepcionista" },
    ],
  },
  {
    id: 5,
    paciente: "João Silva", telefone: "(81) 9 9999-0001", dentista: "Dra. Sofia Martins",
    procedimento: "Profilaxia", dataUltimoProcedimento: "25/11/2025",
    diasDesdeUltimo: 194, intervaloRecomendado: 180, diasVencidos: 14,
    prioridade: "ALTO", status: "CONTATADO",
    inadimplente: false, temAgendamentoFuturo: false, tratamentoCritico: false,
    historicoFaltas: 1, motivoRecall: "Profilaxia semestral — 14 dias em atraso",
    alertaInatividade: false, conversaoRecall: false,
    tentativas: [
      {
        id: 1, data: "02/06/2026", hora: "09:00", canal: "WHATSAPP",
        responsavel: "Recepcionista", resultado: "ATENDEU",
        observacao: "Paciente informou que tentará agendar esta semana",
      },
    ],
    auditLog: [
      { data: "02/06/2026", acao: "1ª tentativa — paciente atendeu, aguardando retorno", responsavel: "Recepcionista" },
      { data: "24/05/2026", acao: "Recall gerado automaticamente — profilaxia 6 meses", responsavel: "Sistema" },
    ],
  },
  {
    id: 6,
    paciente: "Paula Mendes", telefone: "(81) 9 9999-0010", dentista: "Dr. Ricardo Alves",
    procedimento: "Implante — Revisão Periódica", dataUltimoProcedimento: "14/04/2026",
    diasDesdeUltimo: 54, intervaloRecomendado: 45, diasVencidos: 9,
    prioridade: "ALTO", status: "CONTATADO",
    inadimplente: false, temAgendamentoFuturo: false, tratamentoCritico: true,
    historicoFaltas: 0, motivoRecall: "Revisão pós-implante — 9 dias em atraso",
    alertaInatividade: false, conversaoRecall: false,
    tentativas: [
      {
        id: 1, data: "03/06/2026", hora: "16:00", canal: "TELEFONE",
        responsavel: "Recepcionista", resultado: "ATENDEU",
        observacao: "Quer agendar para a próxima semana",
      },
    ],
    auditLog: [
      { data: "03/06/2026", acao: "Paciente contatada — aguardando retorno para agendamento", responsavel: "Recepcionista" },
      { data: "28/05/2026", acao: "Recall gerado automaticamente — implante 45d", responsavel: "Sistema" },
    ],
  },
  {
    id: 7,
    paciente: "Maria Santos", telefone: "(81) 9 9999-0002", dentista: "Dr. Ricardo Alves",
    procedimento: "Ortodontia — Revisão Mensal", dataUltimoProcedimento: "05/05/2026",
    diasDesdeUltimo: 33, intervaloRecomendado: 30, diasVencidos: 3,
    prioridade: "NORMAL", status: "PENDENTE",
    inadimplente: true, temAgendamentoFuturo: false, tratamentoCritico: false,
    historicoFaltas: 1, motivoRecall: "Revisão mensal de ortodontia — 3 dias em atraso",
    alertaInatividade: false, conversaoRecall: false,
    tentativas: [],
    auditLog: [
      { data: "04/06/2026", acao: "Recall gerado automaticamente — ortodontia mensal", responsavel: "Sistema" },
    ],
  },
  {
    id: 8,
    paciente: "Roberto Alves", telefone: "(81) 9 9999-0007", dentista: "Dra. Sofia Martins",
    procedimento: "Profilaxia", dataUltimoProcedimento: "08/12/2025",
    diasDesdeUltimo: 181, intervaloRecomendado: 180, diasVencidos: 1,
    prioridade: "NORMAL", status: "PENDENTE",
    inadimplente: false, temAgendamentoFuturo: false, tratamentoCritico: false,
    historicoFaltas: 0, motivoRecall: "Profilaxia semestral — 1 dia em atraso",
    alertaInatividade: false, conversaoRecall: false,
    tentativas: [],
    auditLog: [
      { data: "06/06/2026", acao: "Recall gerado automaticamente — profilaxia 6 meses", responsavel: "Sistema" },
    ],
  },
];

const CANAIS: CanalContato[]         = ["TELEFONE", "WHATSAPP", "EMAIL", "SMS"];
const RESULTADOS: ResultadoContato[] = ["ATENDEU", "NAO_ATENDEU", "CAIXA_POSTAL", "AGENDOU", "RECUSOU"];

const RESULTADO_LABELS: Record<ResultadoContato, string> = {
  ATENDEU:      "Atendeu",
  NAO_ATENDEU:  "Não atendeu",
  CAIXA_POSTAL: "Caixa postal / sem resposta",
  AGENDOU:      "Agendou na hora",
  RECUSOU:      "Recusou retorno",
};

const ORDEM_PRIORIDADE: Record<PrioridadeRecall, number> = {
  CRITICO: 0, ALTO: 1, NORMAL: 2, BAIXO: 3,
};

// ─── Sub-components ───────────────────────────────────────────────────────────

function PrioridadeBadge({ p }: { p: PrioridadeRecall }) {
  const cls: Record<PrioridadeRecall, string> = {
    CRITICO: "bg-red-500 border-red-600 text-white",
    ALTO:    "bg-orange-400 border-orange-500 text-white",
    NORMAL:  "bg-blue-400 border-blue-500 text-white",
    BAIXO:   "bg-gray-300 border-gray-400 text-gray-700",
  };
  return (
    <span className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${cls[p]}`}>{p}</span>
  );
}

function StatusBadge({ s }: { s: StatusRecall }) {
  const cls: Record<StatusRecall, string> = {
    PENDENTE:       "bg-gray-100 border-gray-400 text-gray-600",
    CONTATADO:      "bg-blue-100 border-blue-400 text-blue-700",
    AGENDADO:       "bg-green-100 border-green-500 text-green-700",
    NAO_RESPONSIVO: "bg-red-100 border-red-500 text-red-700",
    RECUSOU:        "bg-yellow-100 border-yellow-500 text-yellow-700",
  };
  const labels: Record<StatusRecall, string> = {
    PENDENTE:       "PENDENTE",
    CONTATADO:      "CONTATADO",
    AGENDADO:       "AGENDADO",
    NAO_RESPONSIVO: "NÃO RESPONSIVO",
    RECUSOU:        "RECUSOU",
  };
  return (
    <span className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${cls[s]}`}>{labels[s]}</span>
  );
}

function rowBg(prioridade: PrioridadeRecall, status: StatusRecall): string {
  if (status === "AGENDADO")    return "bg-green-50";
  if (status === "NAO_RESPONSIVO") return "bg-gray-100 opacity-80";
  if (prioridade === "CRITICO") return "bg-red-50";
  if (prioridade === "ALTO")    return "bg-orange-50";
  return "";
}

// ─── Integração com o backend (GET /api/recalls) ──────────────────────────────

const PRIORIDADE_BACKEND: Record<string, PrioridadeRecall> = {
  ALTA: "CRITICO", MEDIA: "ALTO", BAIXA: "NORMAL",
};
const STATUS_BACKEND: Record<string, StatusRecall> = {
  NA_FILA: "PENDENTE", AGENDADO: "AGENDADO", CONVERTIDO: "AGENDADO",
  CANCELADO: "RECUSOU", EXCLUIDO: "NAO_RESPONSIVO",
};
const RESULTADO_BACKEND: Record<string, ResultadoContato> = {
  CONTATO_REALIZADO: "ATENDEU", SEM_RESPOSTA: "NAO_ATENDEU", NUMERO_INVALIDO: "CAIXA_POSTAL",
  RETORNO_AGENDADO: "AGENDOU", PACIENTE_RECUSOU: "RECUSOU", SOLICITAR_NOVO_CONTATO: "NAO_ATENDEU",
};
// Inverso do mapeamento acima — usado ao enviar uma tentativa de contato pro backend.
const RESULTADO_PARA_BACKEND: Record<ResultadoContato, string> = {
  ATENDEU: "CONTATO_REALIZADO", NAO_ATENDEU: "SEM_RESPOSTA", CAIXA_POSTAL: "NUMERO_INVALIDO",
  AGENDOU: "RETORNO_AGENDADO", RECUSOU: "PACIENTE_RECUSOU",
};

function adaptarRecall(b: any, indice: number): RecallItem {
  const fatores = b.fatores ?? {};
  const tentativas: TentativaContato[] = (b.historicoTentativas ?? []).map((t: any, i: number) => {
    const [data = "", horaCompleta = ""] = (t.dataHora ?? "").split("T");
    return {
      id: i + 1,
      data,
      hora: horaCompleta.slice(0, 5),
      canal: (t.canal ?? "TELEFONE") as CanalContato,
      responsavel: t.responsavel ?? "—",
      resultado: RESULTADO_BACKEND[t.resultado] ?? "NAO_ATENDEU",
      observacao: t.observacoes ?? "",
    };
  });
  // Não há log de auditoria separado no backend — deriva da própria lista de tentativas (mais recente primeiro).
  const auditLog = [...tentativas].reverse().map((t) => ({
    data: t.data || "—",
    acao: `Tentativa (${t.canal}) — ${RESULTADO_LABELS[t.resultado]}${t.observacao ? ": " + t.observacao : ""}`,
    responsavel: t.responsavel,
  }));
  return {
    id: b.id?.id ?? indice + 1,
    paciente: b.paciente ?? "—",
    telefone: "—",
    dentista: "—",
    procedimento: b.procedimentoGatilho ?? "—",
    dataUltimoProcedimento: b.dataCriacao ?? "—",
    diasDesdeUltimo: 0,
    intervaloRecomendado: b.diasParaRetorno ?? 0,
    diasVencidos: 0,
    prioridade: PRIORIDADE_BACKEND[b.prioridade] ?? "NORMAL",
    status: STATUS_BACKEND[b.status] ?? "PENDENTE",
    inadimplente: !!fatores.inadimplente,
    temAgendamentoFuturo: false,
    tratamentoCritico: !!(fatores.procedimentoCritico || fatores.riscoClinicoAlto),
    historicoFaltas: fatores.historicoFaltas ?? 0,
    motivoRecall: `Recall ${b.categoria ?? "PREVENTIVO"} — ${b.procedimentoGatilho ?? ""}`.trim(),
    alertaInatividade: !!fatores.tratamentoInterrompido,
    conversaoRecall: !!b.flagConversaoRecall,
    tentativas,
    auditLog,
  };
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function RecepcionistaRecall() {
  const [recalls, setRecalls] = useState<RecallItem[]>(MOCK_RECALL);

  // Carrega os recalls reais do backend; mantém o mock como fallback se a API falhar/vier vazia.
  const carregarRecalls = useCallback(() => {
    return api.get<any[]>("/recalls")
      .then((lista) => {
        if (Array.isArray(lista) && lista.length > 0) {
          setRecalls(lista.map(adaptarRecall));
        }
      })
      .catch((e) => console.warn("Falha ao carregar recalls do backend:", e));
  }, []);

  useEffect(() => { carregarRecalls(); }, [carregarRecalls]);

  // Filters
  const [filtroPrioridade, setFiltroPrioridade] = useState("TODAS");
  const [filtroStatus,     setFiltroStatus]     = useState("TODOS");
  const [filtroDentista,   setFiltroDentista]   = useState("TODOS");
  const [busca,            setBusca]            = useState("");

  // Toast
  const [toast, setToast] = useState<{ msg: string; tipo: "sucesso" | "erro" } | null>(null);

  // Modals
  const [modalContato,  setModalContato]  = useState<{ aberto: boolean; item?: RecallItem }>({ aberto: false });
  const [modalDetalhes, setModalDetalhes] = useState<{ aberto: boolean; item?: RecallItem }>({ aberto: false });
  const [modalAgendar,  setModalAgendar]  = useState<{ aberto: boolean; item?: RecallItem }>({ aberto: false });

  // Form: contato
  const [formContato, setFormContato] = useState<{
    canal: CanalContato; resultado: ResultadoContato | ""; observacao: string; duracaoMinutos: string;
  }>({ canal: "TELEFONE", resultado: "", observacao: "", duracaoMinutos: "5" });
  const [erroContato, setErroContato] = useState("");
  const [salvandoContato, setSalvandoContato] = useState(false);
  const [erroAgendar, setErroAgendar] = useState("");
  const [salvandoAgendar, setSalvandoAgendar] = useState(false);

  // ─ Derived ──────────────────────────────────────────────────────────────────

  const dentistasDisponiveis = useMemo(
    () => [...new Set(recalls.map(r => r.dentista))],
    [recalls]
  );

  const filtrados = useMemo(() => {
    return recalls
      .filter(r => {
        const matchP = filtroPrioridade === "TODAS" || r.prioridade === filtroPrioridade;
        const matchS = filtroStatus     === "TODOS" || r.status     === filtroStatus;
        const matchD = filtroDentista   === "TODOS" || r.dentista   === filtroDentista;
        const matchB = busca === "" || r.paciente.toLowerCase().includes(busca.toLowerCase());
        return matchP && matchS && matchD && matchB;
      })
      .sort((a, b) => {
        const pDiff = ORDEM_PRIORIDADE[a.prioridade] - ORDEM_PRIORIDADE[b.prioridade];
        return pDiff !== 0 ? pDiff : b.diasVencidos - a.diasVencidos;
      });
  }, [recalls, filtroPrioridade, filtroStatus, filtroDentista, busca]);

  const metricas = useMemo(() => {
    const convertidos = recalls.filter(r => r.conversaoRecall).length;
    return {
      total:      recalls.length,
      criticos:   recalls.filter(r => r.prioridade === "CRITICO").length,
      naoResp:    recalls.filter(r => r.status === "NAO_RESPONSIVO").length,
      agendados:  recalls.filter(r => r.status === "AGENDADO").length,
      conversao:  recalls.length > 0 ? Math.round((convertidos / recalls.length) * 100) : 0,
      alertas:    recalls.filter(r => r.alertaInatividade).length,
    };
  }, [recalls]);

  const alertasInatividade = useMemo(
    () => recalls.filter(r => r.alertaInatividade && r.status !== "AGENDADO"),
    [recalls]
  );

  // ─ Handlers ─────────────────────────────────────────────────────────────────

  function showToast(msg: string, tipo: "sucesso" | "erro") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 3000);
  }

  function limparFiltros() {
    setFiltroPrioridade("TODAS"); setFiltroStatus("TODOS");
    setFiltroDentista("TODOS");   setBusca("");
  }

  function abrirContato(item: RecallItem) {
    setFormContato({ canal: "TELEFONE", resultado: "", observacao: "", duracaoMinutos: "5" });
    setErroContato("");
    setModalContato({ aberto: true, item });
  }

  async function handleRegistrarContato(id: number) {
    if (!formContato.resultado) { setErroContato("Selecione o resultado do contato."); return; }
    const item = recalls.find(r => r.id === id);
    if (!item) return;

    setSalvandoContato(true);
    setErroContato("");
    try {
      const escalonou = await api.post<boolean>(`/recalls/paciente/${encodeURIComponent(item.paciente)}/tentativa-detalhada`, {
        canal: formContato.canal,
        resultado: RESULTADO_PARA_BACKEND[formContato.resultado as ResultadoContato],
        responsavel: "Recepcionista",
        duracaoMinutos: Number(formContato.duracaoMinutos) || 0,
        observacoes: formContato.observacao.trim(),
      });
      await carregarRecalls();
      setModalContato({ aberto: false });
      showToast(
        escalonou ? "Tentativa registrada — caso escalonado (3+ tentativas sem sucesso)." : "Tentativa de contato registrada!",
        escalonou ? "erro" : "sucesso"
      );
    } catch (e: any) {
      setErroContato(e.message ?? "Falha ao registrar contato.");
    } finally {
      setSalvandoContato(false);
    }
  }

  async function handleMarcarAgendado(id: number) {
    const item = recalls.find(r => r.id === id);
    if (!item) return;

    setSalvandoAgendar(true);
    setErroAgendar("");
    try {
      // Não existe endpoint dedicado de "marcar como agendado": o backend transiciona
      // automaticamente para AGENDADO quando a tentativa tem resultado RETORNO_AGENDADO.
      await api.post(`/recalls/paciente/${encodeURIComponent(item.paciente)}/tentativa-detalhada`, {
        canal: "TELEFONE",
        resultado: "RETORNO_AGENDADO",
        responsavel: "Recepcionista",
        duracaoMinutos: 0,
        observacoes: "Convertido em agendamento via tela de Recall.",
      });
      await carregarRecalls();
      setModalAgendar({ aberto: false });
      showToast("Recall convertido em agendamento!", "sucesso");
    } catch (e: any) {
      setErroAgendar(e.message ?? "Falha ao converter recall.");
    } finally {
      setSalvandoAgendar(false);
    }
  }

  // ─ Render ────────────────────────────────────────────────────────────────────

  return (
    <div className="p-6">

      {/* Toast */}
      {toast && (
        <div className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold shadow-lg ${
          toast.tipo === "sucesso"
            ? "bg-green-100 border-green-500 text-green-800"
            : "bg-red-100 border-red-500 text-red-800"
        }`}>
          {toast.tipo === "sucesso" ? "✓" : "✗"} {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="mb-4">
        <h1 className="text-xl font-bold text-gray-700">Fila de Recall — Retorno Preventivo</h1>
        <p className="text-sm text-gray-500 mt-1">
          Triagem inteligente de pacientes para contato preventivo e continuidade do tratamento
        </p>
      </div>

      {/* Alertas de Inatividade Clínica — notificar dentista */}
      {alertasInatividade.length > 0 && (
        <div className="border-2 border-red-500 bg-red-50 p-4 mb-5">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-red-600 font-bold">⚠️ ALERTAS DE INATIVIDADE — Notificar Cirurgião-Dentista</span>
          </div>
          <div className="space-y-1.5">
            {alertasInatividade.map(a => (
              <div key={a.id} className="flex items-start gap-2 text-sm text-red-700">
                <span className="mt-0.5">•</span>
                <span>
                  <strong>{a.paciente}</strong> — {a.procedimento} —{" "}
                  <strong>{a.diasVencidos} dias</strong> sem retorno (dentista: {a.dentista})
                  {a.inadimplente && (
                    <span className="ml-2 bg-red-500 text-white text-[10px] px-1 font-bold">INADIMPLENTE</span>
                  )}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Métricas */}
      <div className="grid grid-cols-6 gap-3 mb-5">
        {[
          { label: "NA FILA",          valor: metricas.total,           cor: "border-gray-400 bg-gray-100 text-gray-700"      },
          { label: "CRÍTICOS",         valor: metricas.criticos,        cor: "border-red-500 bg-red-50 text-red-700"          },
          { label: "NÃO RESPONSIVOS",  valor: metricas.naoResp,         cor: "border-orange-400 bg-orange-50 text-orange-700" },
          { label: "AGENDADOS",        valor: metricas.agendados,       cor: "border-green-500 bg-green-50 text-green-700"    },
          { label: "TAXA CONVERSÃO",   valor: `${metricas.conversao}%`, cor: "border-blue-400 bg-blue-50 text-blue-700"       },
          { label: "ALERTAS DENTISTA", valor: metricas.alertas,         cor: "border-red-400 bg-red-100 text-red-600"         },
        ].map(c => (
          <div key={c.label} className={`border-2 ${c.cor} p-3 text-center`}>
            <div className="text-xl font-bold">{c.valor}</div>
            <div className="text-[10px] font-bold mt-0.5">{c.label}</div>
          </div>
        ))}
      </div>

      {/* Filtros */}
      <div className="border-2 border-gray-400 bg-gray-50 p-3 mb-4">
        <div className="grid grid-cols-5 gap-2 mb-2">
          <div className="col-span-2 flex items-center border-2 border-gray-400 bg-white">
            <span className="px-2 text-gray-400 text-sm">🔍</span>
            <input
              type="text"
              placeholder="Buscar paciente..."
              value={busca}
              onChange={e => setBusca(e.target.value)}
              className="flex-1 p-1.5 outline-none bg-transparent text-sm"
            />
          </div>
          <select
            value={filtroPrioridade}
            onChange={e => setFiltroPrioridade(e.target.value)}
            className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
          >
            <option value="TODAS">Todas as Prioridades</option>
            <option value="CRITICO">Crítico</option>
            <option value="ALTO">Alto</option>
            <option value="NORMAL">Normal</option>
            <option value="BAIXO">Baixo</option>
          </select>
          <select
            value={filtroStatus}
            onChange={e => setFiltroStatus(e.target.value)}
            className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
          >
            <option value="TODOS">Todos os Status</option>
            <option value="PENDENTE">Pendente</option>
            <option value="CONTATADO">Contatado</option>
            <option value="AGENDADO">Agendado</option>
            <option value="NAO_RESPONSIVO">Não Responsivo</option>
            <option value="RECUSOU">Recusou</option>
          </select>
          <select
            value={filtroDentista}
            onChange={e => setFiltroDentista(e.target.value)}
            className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
          >
            <option value="TODOS">Todos os Dentistas</option>
            {dentistasDisponiveis.map(d => <option key={d} value={d}>{d}</option>)}
          </select>
        </div>
        <div className="flex items-center gap-4">
          <button onClick={limparFiltros} className="text-sm text-blue-600 underline hover:text-blue-800">
            Limpar filtros
          </button>
          <span className="text-xs text-gray-500">{filtrados.length} paciente(s)</span>
        </div>
      </div>

      {/* Tabela */}
      <div className="border-2 border-gray-400 bg-white">
        <div
          className="grid bg-gray-100 border-b-2 border-gray-400"
          style={{ gridTemplateColumns: "1.5fr 1.6fr 1.3fr 0.75fr 0.7fr 0.9fr 1fr 1.8fr" }}
        >
          {["Paciente", "Procedimento", "Dentista", "Vencido", "Tentativas", "Prioridade", "Status", "Ações"].map(h => (
            <div key={h} className="p-2 border-r-2 border-gray-400 font-bold text-sm last:border-r-0">{h}</div>
          ))}
        </div>

        {filtrados.length === 0 && (
          <div className="p-8 text-center text-gray-400">
            Nenhum paciente na fila com os filtros aplicados.
          </div>
        )}

        {filtrados.map(item => (
          <div
            key={item.id}
            className={`grid border-b-2 border-gray-400 ${rowBg(item.prioridade, item.status)}`}
            style={{ gridTemplateColumns: "1.5fr 1.6fr 1.3fr 0.75fr 0.7fr 0.9fr 1fr 1.8fr" }}
          >
            {/* Paciente */}
            <div className="p-2 border-r-2 border-gray-400 text-sm">
              <div className="font-medium">{item.paciente}</div>
              <div className="flex gap-1 mt-0.5 flex-wrap">
                {item.inadimplente && (
                  <span className="text-[10px] bg-red-500 text-white px-1 font-bold">INAD.</span>
                )}
                {item.tratamentoCritico && (
                  <span className="text-[10px] bg-red-100 border border-red-400 text-red-600 px-1 font-bold">CRÍTICO</span>
                )}
                {item.conversaoRecall && (
                  <span className="text-[10px] bg-green-100 border border-green-400 text-green-700 px-1 font-bold">CONVERTIDO</span>
                )}
                {item.historicoFaltas > 0 && (
                  <span className="text-[10px] bg-yellow-100 border border-yellow-400 text-yellow-700 px-1">
                    {item.historicoFaltas} falta(s)
                  </span>
                )}
              </div>
            </div>

            {/* Procedimento */}
            <div className="p-2 border-r-2 border-gray-400 text-sm">
              <div>{item.procedimento}</div>
              <div className="text-[10px] text-gray-400 mt-0.5">Último: {item.dataUltimoProcedimento}</div>
            </div>

            {/* Dentista */}
            <div className="p-2 border-r-2 border-gray-400 text-sm">{item.dentista}</div>

            {/* Vencido */}
            <div className="p-2 border-r-2 border-gray-400 text-sm text-center">
              <span className={`font-bold ${
                item.diasVencidos > 30 ? "text-red-600" :
                item.diasVencidos > 7  ? "text-orange-600" :
                                         "text-yellow-600"
              }`}>
                {item.diasVencidos}d
              </span>
            </div>

            {/* Tentativas */}
            <div className="p-2 border-r-2 border-gray-400 text-sm text-center">
              <span className={`font-bold ${item.tentativas.length >= 3 ? "text-red-600" : "text-gray-700"}`}>
                {item.tentativas.length}
              </span>
              {item.tentativas.length >= 3 && (
                <div className="text-[9px] text-red-500 font-bold leading-tight">escalonado</div>
              )}
            </div>

            {/* Prioridade */}
            <div className="p-2 border-r-2 border-gray-400">
              <PrioridadeBadge p={item.prioridade} />
            </div>

            {/* Status */}
            <div className="p-2 border-r-2 border-gray-400">
              <StatusBadge s={item.status} />
            </div>

            {/* Ações */}
            <div className="p-2 flex gap-1 flex-wrap items-center">
              {item.status !== "AGENDADO" && item.status !== "RECUSOU" && (
                <button
                  onClick={() => abrirContato(item)}
                  className="px-2 py-0.5 border-2 border-blue-500 bg-blue-50 text-blue-700 text-xs font-bold hover:bg-blue-500 hover:text-white"
                >
                  Contato
                </button>
              )}
              {item.status !== "AGENDADO" && (
                <button
                  onClick={() => setModalAgendar({ aberto: true, item })}
                  className="px-2 py-0.5 border-2 border-green-500 bg-green-50 text-green-700 text-xs font-bold hover:bg-green-500 hover:text-white"
                >
                  Agendar
                </button>
              )}
              <button
                onClick={() => setModalDetalhes({ aberto: true, item })}
                className="px-2 py-0.5 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-100"
              >
                Detalhes
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* ══ Modal: Registrar Contato ══ */}
      {modalContato.aberto && modalContato.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-blue-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Registrar Tentativa de Contato</span>
              <button onClick={() => setModalContato({ aberto: false })} className="text-xl leading-none hover:text-gray-200">✕</button>
            </div>
            <div className="p-6 space-y-4">
              <div className="border-2 border-gray-300 bg-gray-50 p-3 text-sm space-y-1">
                <div>Paciente: <strong>{modalContato.item.paciente}</strong></div>
                <div>Telefone: <strong>{modalContato.item.telefone}</strong></div>
                <div>Motivo: <strong>{modalContato.item.motivoRecall}</strong></div>
                <div>Tentativas anteriores: <strong>{modalContato.item.tentativas.length}</strong></div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-bold mb-1">Canal *</label>
                  <select
                    className="w-full border-2 border-gray-400 p-2 outline-none bg-white"
                    value={formContato.canal}
                    onChange={e => setFormContato(f => ({ ...f, canal: e.target.value as CanalContato }))}
                  >
                    {CANAIS.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-bold mb-1">Resultado *</label>
                  <select
                    className="w-full border-2 border-gray-400 p-2 outline-none bg-white"
                    value={formContato.resultado}
                    onChange={e => { setFormContato(f => ({ ...f, resultado: e.target.value as ResultadoContato })); setErroContato(""); }}
                  >
                    <option value="">Selecione</option>
                    {RESULTADOS.map(r => <option key={r} value={r}>{RESULTADO_LABELS[r]}</option>)}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">Duração (min)</label>
                <input
                  type="number"
                  min={0}
                  className="w-full border-2 border-gray-400 p-2 outline-none"
                  value={formContato.duracaoMinutos}
                  onChange={e => setFormContato(f => ({ ...f, duracaoMinutos: e.target.value }))}
                />
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">Observações</label>
                <textarea
                  rows={2}
                  className="w-full border-2 border-gray-400 p-2 outline-none resize-none"
                  placeholder="Detalhes adicionais sobre o contato..."
                  value={formContato.observacao}
                  onChange={e => setFormContato(f => ({ ...f, observacao: e.target.value }))}
                />
              </div>

              {erroContato && (
                <div className="text-red-600 text-sm border-2 border-red-300 bg-red-50 p-2">{erroContato}</div>
              )}

              {/* Aviso de escalonamento */}
              {modalContato.item.tentativas.filter(
                t => t.resultado === "NAO_ATENDEU" || t.resultado === "CAIXA_POSTAL"
              ).length >= 2 && (
                <div className="border-2 border-orange-400 bg-orange-50 p-2 text-xs text-orange-700">
                  ⚠ Mais uma tentativa sem resposta marcará este paciente como <strong>Não Responsivo</strong> automaticamente.
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button onClick={() => setModalContato({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100">Cancelar</button>
              <button
                onClick={() => handleRegistrarContato(modalContato.item!.id)}
                disabled={salvandoContato}
                className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600 disabled:opacity-50"
              >
                {salvandoContato ? "Registrando..." : "Registrar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Detalhes ══ */}
      {modalDetalhes.aberto && modalDetalhes.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-lg">
            <div className="bg-gray-700 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Detalhes — {modalDetalhes.item.paciente}</span>
              <button onClick={() => setModalDetalhes({ aberto: false })} className="text-xl leading-none hover:text-gray-200">✕</button>
            </div>
            <div className="p-6 space-y-4">
              {/* Info */}
              <div className="grid grid-cols-2 gap-3 text-sm">
                {([
                  ["PACIENTE",      modalDetalhes.item.paciente],
                  ["TELEFONE",      modalDetalhes.item.telefone],
                  ["DENTISTA",      modalDetalhes.item.dentista],
                  ["PROCEDIMENTO",  modalDetalhes.item.procedimento],
                  ["ÚLTIMO ATEND.", modalDetalhes.item.dataUltimoProcedimento],
                  ["DIAS VENCIDO",  `${modalDetalhes.item.diasVencidos} dias`],
                  ["HISTÓRICO FALTAS", String(modalDetalhes.item.historicoFaltas)],
                  ["INTERVALO RECOM.", `${modalDetalhes.item.intervaloRecomendado} dias`],
                ] as [string, string][]).map(([l, v]) => (
                  <div key={l}>
                    <div className="text-xs text-gray-500 font-bold">{l}</div>
                    <div className="font-medium">{v}</div>
                  </div>
                ))}
                <div>
                  <div className="text-xs text-gray-500 font-bold">PRIORIDADE</div>
                  <PrioridadeBadge p={modalDetalhes.item.prioridade} />
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold">STATUS</div>
                  <StatusBadge s={modalDetalhes.item.status} />
                </div>
              </div>

              <div className="border-2 border-gray-200 bg-gray-50 p-2 text-sm">
                <div className="text-xs text-gray-500 font-bold mb-1">MOTIVO DO RECALL</div>
                <div>{modalDetalhes.item.motivoRecall}</div>
              </div>

              {/* Tentativas */}
              <div>
                <div className="text-xs text-gray-500 font-bold mb-2">
                  TENTATIVAS DE CONTATO ({modalDetalhes.item.tentativas.length})
                </div>
                {modalDetalhes.item.tentativas.length === 0 ? (
                  <div className="text-sm text-gray-400 border-2 border-gray-200 p-2">
                    Nenhuma tentativa registrada.
                  </div>
                ) : (
                  <div className="space-y-1 max-h-36 overflow-y-auto">
                    {modalDetalhes.item.tentativas.map(t => (
                      <div key={t.id} className="border-2 border-gray-200 p-2 text-xs">
                        <div className="flex justify-between items-center">
                          <span className="font-bold">{t.data} {t.hora} — {t.canal}</span>
                          <span className="text-gray-500">{t.responsavel}</span>
                        </div>
                        <div className="mt-0.5">Resultado: <strong>{RESULTADO_LABELS[t.resultado]}</strong></div>
                        {t.observacao && <div className="text-gray-500 mt-0.5 italic">{t.observacao}</div>}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Auditoria */}
              <div>
                <div className="text-xs text-gray-500 font-bold mb-2">AUDITORIA</div>
                <div className="space-y-0.5 max-h-24 overflow-y-auto">
                  {modalDetalhes.item.auditLog.map((l, i) => (
                    <div key={i} className="flex gap-2 text-xs border-b border-gray-100 pb-0.5">
                      <span className="text-gray-400 shrink-0">{l.data}</span>
                      <span className="flex-1">{l.acao}</span>
                      <span className="text-gray-500 shrink-0">{l.responsavel}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-2 flex-wrap justify-end">
              {modalDetalhes.item.status !== "AGENDADO" && modalDetalhes.item.status !== "RECUSOU" && (
                <button
                  onClick={() => { abrirContato(modalDetalhes.item!); setModalDetalhes({ aberto: false }); }}
                  className="px-3 py-1.5 border-2 border-blue-500 bg-blue-50 text-blue-700 text-sm font-bold hover:bg-blue-500 hover:text-white"
                >
                  Registrar Contato
                </button>
              )}
              {modalDetalhes.item.status !== "AGENDADO" && (
                <button
                  onClick={() => { setModalAgendar({ aberto: true, item: modalDetalhes.item }); setModalDetalhes({ aberto: false }); }}
                  className="px-3 py-1.5 border-2 border-green-500 bg-green-50 text-green-700 text-sm font-bold hover:bg-green-500 hover:text-white"
                >
                  Agendar
                </button>
              )}
              <button
                onClick={() => setModalDetalhes({ aberto: false })}
                className="px-3 py-1.5 border-2 border-gray-400 bg-white text-sm font-bold hover:bg-gray-100"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Converter em Agendamento ══ */}
      {modalAgendar.aberto && modalAgendar.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-green-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Converter em Agendamento</span>
              <button onClick={() => setModalAgendar({ aberto: false })} className="text-xl leading-none hover:text-gray-200">✕</button>
            </div>
            <div className="p-6">
              <p className="text-sm mb-1">Paciente: <strong>{modalAgendar.item.paciente}</strong></p>
              <p className="text-sm mb-1">Procedimento: <strong>{modalAgendar.item.procedimento}</strong></p>
              <p className="text-sm mb-1">Dentista: <strong>{modalAgendar.item.dentista}</strong></p>
              <p className="text-sm text-gray-600 mt-3">
                Este recall será marcado como <strong>AGENDADO</strong> e registrado como conversão.
              </p>
              <div className="border-2 border-green-300 bg-green-50 p-2 mt-3 text-xs text-green-700">
                ✓ Realize o agendamento na tela de Agenda para registrar data e horário.
              </div>
              {erroAgendar && (
                <div className="border-2 border-red-300 bg-red-50 p-2 mt-3 text-xs text-red-700">{erroAgendar}</div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button onClick={() => setModalAgendar({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100">Cancelar</button>
              <button
                onClick={() => handleMarcarAgendado(modalAgendar.item!.id)}
                disabled={salvandoAgendar}
                className="px-4 py-2 border-2 border-green-500 bg-green-500 text-white font-bold hover:bg-green-600 disabled:opacity-50"
              >
                {salvandoAgendar ? "Confirmando..." : "Confirmar"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
