import { useState, useMemo, useEffect, useCallback } from "react";
import { Link } from "react-router";
import { api } from "../../api/client";

// ─── Types ────────────────────────────────────────────────────────────────────

type TipoAgendamento = "CONSULTA" | "RETORNO";
type StatusAgendamento =
  | "AGENDADO"
  | "CONFIRMADO"
  | "CANCELADO"
  | "REMARCADO";

interface Agendamento {
  id: number;
  paciente: string;
  dentista: string;
  data: string; // DD/MM/YYYY
  hora: string; // HH:MM
  tipo: TipoAgendamento;
  status: StatusAgendamento;
  responsavel: string;
  ultimaAlteracao: string;
  motivoCancelamento?: string;
  inadimplente: boolean;
  historico: {
    data: string;
    acao: string;
    responsavel: string;
  }[];
}

interface Paciente {
  nome: string;
  telefone: string;
  temPlano: boolean;
  inadimplente: boolean;
  restrito: boolean;
}

interface FollowupCard {
  paciente: string;
  cirurgia: string;
  data: string;
}

interface FollowupFlags {
  dorIntensa: boolean;
  sangramentoExcessivo: boolean;
  febre: boolean;
}

const FOLLOWUP_CARDS: FollowupCard[] = [
  { paciente: "Marcos Pereira", cirurgia: "Extração", data: "26/04/2026" },
  { paciente: "Paula Mendes", cirurgia: "Implante", data: "27/04/2026" },
];

const DENTISTAS = [
  "Dra. Sofia Martins",
  "Dr. Ricardo Alves",
  "Dra. Camila Torres",
];
const HORARIOS = [
  "08:00",
  "09:00",
  "10:00",
  "11:00",
  "14:00",
  "15:00",
  "16:00",
  "17:00",
];
const DIAS_SEMANA_LABELS = ["SEG", "TER", "QUA", "QUI", "SEX"];

// ─── Helpers ──────────────────────────────────────────────────────────────────

function getMondayOf(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay();
  d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day));
  d.setHours(0, 0, 0, 0);
  return d;
}

function addDays(date: Date, days: number): Date {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function dateToBR(d: Date): string {
  return d.toLocaleDateString("pt-BR");
}

function inputToDate(s: string): Date {
  const [y, m, day] = s.split("-").map(Number);
  return new Date(y, m - 1, day);
}

function hoje(): string {
  return dateToBR(new Date());
}

function isToday(d: Date): boolean {
  const t = new Date();
  return (
    d.getDate() === t.getDate() &&
    d.getMonth() === t.getMonth() &&
    d.getFullYear() === t.getFullYear()
  );
}

// ─── Sub-components ───────────────────────────────────────────────────────────

function StatusBadge({
  status,
}: {
  status: StatusAgendamento;
}) {
  const cls: Record<StatusAgendamento, string> = {
    AGENDADO: "bg-blue-100 border-blue-400 text-blue-700",
    CONFIRMADO: "bg-green-100 border-green-500 text-green-700",
    CANCELADO: "bg-red-100 border-red-500 text-red-700",
    REMARCADO:
      "bg-yellow-100 border-yellow-500 text-yellow-700",
  };
  return (
    <span
      className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${cls[status]}`}
    >
      {status}
    </span>
  );
}

function TipoBadge({ tipo }: { tipo: TipoAgendamento }) {
  return (
    <span
      className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${
        tipo === "RETORNO"
          ? "bg-purple-100 border-purple-400 text-purple-700"
          : "bg-gray-100 border-gray-400 text-gray-700"
      }`}
    >
      {tipo}
    </span>
  );
}

// ─── Integração com o backend (GET /api/agendamentos, GET /api/pacientes) ─────
// tipo (CONSULTA/RETORNO) e status (AGENDADO/CONFIRMADO/CANCELADO/REMARCADO) do
// backend já batem com a UI. paciente/dentista já vêm resolvidos pelo backend
// (o agregado só guarda os ids; o controller resolve os nomes via ACL local).
// inadimplente por agendamento não existe via API real — default false.

function splitISODateTime(iso: string | null | undefined): { data: string; hora: string } {
  if (!iso) return { data: "—", hora: "—" };
  const [datePart, timePart = ""] = iso.split("T");
  const [y, m, d] = datePart.split("-");
  return { data: `${d}/${m}/${y}`, hora: timePart.slice(0, 5) };
}

function adaptarAgendamento(b: any, indice: number): Agendamento {
  const { data, hora } = splitISODateTime(b.dataHora);
  return {
    id: b.id ?? indice + 1,
    paciente: b.paciente ?? "—",
    dentista: b.dentista ?? "—",
    data,
    hora,
    tipo: (b.tipo as TipoAgendamento) ?? "CONSULTA",
    status: (b.status as StatusAgendamento) ?? "AGENDADO",
    responsavel: b.responsavelAlteracao ?? "Recepcionista",
    ultimaAlteracao: b.dataUltimaAlteracao ? splitISODateTime(b.dataUltimaAlteracao).data : "—",
    motivoCancelamento: b.motivoCancelamento ?? undefined,
    inadimplente: false,
    historico: (b.historico ?? []).map((h: any) => ({
      data: h.dataRegistro ? splitISODateTime(h.dataRegistro).data : "—",
      acao: h.acao ?? "",
      responsavel: h.responsavel ?? "—",
    })),
  };
}

function adaptarPacienteParaAgenda(b: any): Paciente {
  return {
    nome: b.nomeCompleto ?? "—",
    telefone: b.telefone ?? "—",
    temPlano: false,
    inadimplente: false,
    restrito: b.status === "RESTRITO",
  };
}

function followupFlagsPadrao(): FollowupFlags {
  return {
    dorIntensa: false,
    sangramentoExcessivo: false,
    febre: false,
  };
}

// Mesma regra do backend (Agendamento.validarHorarioComercial): seg-sex, 08:00-17:00.
function foraDoHorarioComercial(data: Date, hora: string): string | null {
  const dia = data.getDay();
  if (dia === 0 || dia === 6) {
    return "Agendamentos só podem ser feitos de segunda a sexta-feira.";
  }
  const [h] = hora.split(":").map(Number);
  if (h < 8 || h > 17) {
    return "Agendamentos só podem ser feitos entre 08:00 e 17:00.";
  }
  return null;
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function RecepcionistaAgenda() {
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [carregandoAgendamentos, setCarregandoAgendamentos] = useState(true);
  const [followupFlags, setFollowupFlags] = useState<Record<string, FollowupFlags>>(() =>
    Object.fromEntries(FOLLOWUP_CARDS.map((card) => [card.paciente, followupFlagsPadrao()])) as Record<string, FollowupFlags>,
  );
  const [followupEnviando, setFollowupEnviando] = useState<Record<string, boolean>>({});

  // Carrega os agendamentos reais do backend — sem fallback: vazio/erro mostra a tela vazia.
  const carregarAgendamentos = useCallback(() => {
    return api.get<any[]>("/agendamentos")
      .then((lista) => setAgendamentos(Array.isArray(lista) ? lista.map(adaptarAgendamento) : []))
      .catch((e) => {
        console.warn("Falha ao carregar agendamentos do backend:", e);
        setAgendamentos([]);
      })
      .finally(() => setCarregandoAgendamentos(false));
  }, []);

  useEffect(() => { carregarAgendamentos(); }, [carregarAgendamentos]);

  useEffect(() => {
    api.get<any[]>("/pacientes")
      .then((lista) => setPacientes(Array.isArray(lista) ? lista.map(adaptarPacienteParaAgenda) : []))
      .catch((e) => {
        console.warn("Falha ao carregar pacientes do backend:", e);
        setPacientes([]);
      });
  }, []);

  const [semanaBase, setSemanaBase] = useState<Date>(() =>
    getMondayOf(new Date(2026, 5, 3)),
  );
  const [view, setView] = useState<"calendario" | "lista">(
    "calendario",
  );

  // Filters
  const [filtroDentista, setFiltroDentista] = useState("TODOS");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");
  const [filtroTipo, setFiltroTipo] = useState("TODOS");
  const [busca, setBusca] = useState("");

  // Toast
  const [toast, setToast] = useState<{
    msg: string;
    tipo: "sucesso" | "erro";
  } | null>(null);

  // Modals
  const [modalCriar, setModalCriar] = useState(false);
  const [modalDetalhes, setModalDetalhes] = useState<{
    aberto: boolean;
    item?: Agendamento;
  }>({ aberto: false });
  const [modalConfirmar, setModalConfirmar] = useState<{
    aberto: boolean;
    item?: Agendamento;
  }>({ aberto: false });
  const [modalCancelar, setModalCancelar] = useState<{
    aberto: boolean;
    item?: Agendamento;
  }>({ aberto: false });
  const [modalRemarcar, setModalRemarcar] = useState<{
    aberto: boolean;
    item?: Agendamento;
  }>({ aberto: false });

  // Form: criar
  const [formCriar, setFormCriar] = useState({
    paciente: "",
    dentista: "",
    data: "",
    hora: "",
  });
  const [erroCriar, setErroCriar] = useState("");
  const [erroInadimplente, setErroInadimplente] =
    useState(false);
  const [erroRestrito, setErroRestrito] = useState(false);

  // Form: cancelar
  const [motivoCancelar, setMotivoCancelar] = useState("");
  const [erroCancelar, setErroCancelar] = useState("");

  // Form: remarcar
  const [formRemarcar, setFormRemarcar] = useState({
    data: "",
    hora: "",
  });
  const [erroRemarcar, setErroRemarcar] = useState("");

  // ─ Derived ────────────────────────────────────────────────────────────────

  const diasDaSemana = useMemo(
    () =>
      Array.from({ length: 5 }, (_, i) =>
        addDays(semanaBase, i),
      ),
    [semanaBase],
  );

  const pacienteSelecionado = formCriar.paciente;

  const tipoAutomatico = useMemo((): TipoAgendamento => {
    const pac = pacientes.find(
      (p) => p.nome === formCriar.paciente,
    );
    return pac?.temPlano ? "RETORNO" : "CONSULTA";
  }, [pacientes, formCriar.paciente]);

  const filtrados = useMemo(() => {
    return agendamentos.filter((a) => {
      const matchD =
        filtroDentista === "TODOS" ||
        a.dentista === filtroDentista;
      const matchS =
        filtroStatus === "TODOS" || a.status === filtroStatus;
      const matchT =
        filtroTipo === "TODOS" || a.tipo === filtroTipo;
      const matchB =
        busca === "" ||
        a.paciente
          .toLowerCase()
          .includes(busca.toLowerCase()) ||
        a.hora.includes(busca);
      return matchD && matchS && matchT && matchB;
    });
  }, [
    agendamentos,
    filtroDentista,
    filtroStatus,
    filtroTipo,
    busca,
  ]);

  const totais = useMemo(
    () => ({
      agendados: agendamentos.filter(
        (a) => a.status === "AGENDADO",
      ).length,
      confirmados: agendamentos.filter(
        (a) => a.status === "CONFIRMADO",
      ).length,
      cancelados: agendamentos.filter(
        (a) => a.status === "CANCELADO",
      ).length,
      remarcados: agendamentos.filter(
        (a) => a.status === "REMARCADO",
      ).length,
      consultas: agendamentos.filter(
        (a) => a.tipo === "CONSULTA",
      ).length,
      retornos: agendamentos.filter((a) => a.tipo === "RETORNO")
        .length,
    }),
    [agendamentos],
  );

  // Returns filtered items for a specific calendar cell
  function getCelula(dia: Date, hora: string): Agendamento[] {
    const str = dateToBR(dia);
    return filtrados.filter(
      (a) => a.data === str && a.hora === hora,
    );
  }

  // ─ Helpers ────────────────────────────────────────────────────────────────

  function showToast(msg: string, tipo: "sucesso" | "erro") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 3000);
  }

  function limparFiltros() {
    setFiltroDentista("TODOS");
    setFiltroStatus("TODOS");
    setFiltroTipo("TODOS");
    setBusca("");
  }

  function abrirCriar(pacientePre = "") {
    setFormCriar({
      paciente: pacientePre,
      dentista: "",
      data: "",
      hora: "",
    });
    setErroCriar("");
    setErroInadimplente(false);
    setErroRestrito(false);
    setModalCriar(true);
  }

  function corCelula(status: StatusAgendamento): string {
    return {
      AGENDADO: "border-blue-400 bg-blue-50",
      CONFIRMADO: "border-green-500 bg-green-50",
      CANCELADO: "border-red-400 bg-red-50",
      REMARCADO: "border-yellow-400 bg-yellow-50",
    }[status];
  }

  function abrirRemarcar(item: Agendamento) {
    setFormRemarcar({ data: "", hora: "" });
    setErroRemarcar("");
    setModalRemarcar({ aberto: true, item });
  }

  function abrirCancelar(item: Agendamento) {
    setMotivoCancelar("");
    setErroCancelar("");
    setModalCancelar({ aberto: true, item });
  }

  // ─ Action handlers ────────────────────────────────────────────────────────

  const [salvandoCriar, setSalvandoCriar] = useState(false);
  const [salvandoConfirmar, setSalvandoConfirmar] = useState(false);
  const [salvandoCancelar, setSalvandoCancelar] = useState(false);
  const [salvandoRemarcar, setSalvandoRemarcar] = useState(false);

  async function handleCriar() {
    if (!formCriar.paciente) {
      setErroCriar("Selecione um paciente cadastrado.");
      return;
    }
    if (
      !formCriar.dentista ||
      !formCriar.data ||
      !formCriar.hora
    ) {
      setErroCriar("Preencha todos os campos obrigatórios.");
      return;
    }
    const dataSel = inputToDate(formCriar.data);
    dataSel.setHours(0, 0, 0, 0);
    const agora = new Date();
    agora.setHours(0, 0, 0, 0);
    if (dataSel < agora) {
      setErroCriar(
        "Não é possível agendar para datas passadas.",
      );
      return;
    }
    const erroHorario = foraDoHorarioComercial(dataSel, formCriar.hora);
    if (erroHorario) {
      setErroCriar(erroHorario);
      return;
    }
    const pac = pacientes.find(
      (p) => p.nome === formCriar.paciente,
    );
    if (pac?.inadimplente) {
      setErroInadimplente(true);
      return;
    }
    if (pac?.restrito) {
      setErroRestrito(true);
      return;
    }

    const dataStr = dateToBR(dataSel);
    const conflito = agendamentos.find(
      (a) =>
        a.dentista === formCriar.dentista &&
        a.data === dataStr &&
        a.hora === formCriar.hora &&
        a.status !== "CANCELADO",
    );
    if (conflito) {
      setErroCriar(
        `Conflito de horário: ${formCriar.dentista} já possui agendamento às ${formCriar.hora} neste dia.`,
      );
      return;
    }

    setSalvandoCriar(true);
    setErroCriar("");
    try {
      await api.post("/agendamentos", {
        paciente: formCriar.paciente,
        dentista: formCriar.dentista,
        dataHora: `${formCriar.data}T${formCriar.hora}:00`,
      });
      await carregarAgendamentos();
      setModalCriar(false);
      showToast("Agendamento criado com sucesso!", "sucesso");
    } catch (e: any) {
      setErroCriar(e.message ?? "Falha ao criar agendamento.");
    } finally {
      setSalvandoCriar(false);
    }
  }

  async function handleConfirmar(id: number) {
    setSalvandoConfirmar(true);
    try {
      await api.post(`/agendamentos/${id}/confirmar?responsavel=${encodeURIComponent("Recepcionista")}`);
      await carregarAgendamentos();
      setModalConfirmar({ aberto: false });
      showToast("Agendamento confirmado!", "sucesso");
    } catch (e: any) {
      showToast(e.message ?? "Falha ao confirmar agendamento.", "erro");
    } finally {
      setSalvandoConfirmar(false);
    }
  }

  async function handleCancelar(id: number) {
    if (!motivoCancelar.trim()) {
      setErroCancelar("Informe o motivo do cancelamento.");
      return;
    }
    setSalvandoCancelar(true);
    setErroCancelar("");
    try {
      await api.post(
        `/agendamentos/${id}/cancelar?motivo=${encodeURIComponent(motivoCancelar.trim())}&responsavel=${encodeURIComponent("Recepcionista")}`,
      );
      await carregarAgendamentos();
      setModalCancelar({ aberto: false });
      setMotivoCancelar("");
      showToast("Agendamento cancelado.", "sucesso");
    } catch (e: any) {
      setErroCancelar(e.message ?? "Falha ao cancelar agendamento.");
    } finally {
      setSalvandoCancelar(false);
    }
  }

  async function handleRemarcar(id: number) {
    if (!formRemarcar.data || !formRemarcar.hora) {
      setErroRemarcar("Informe nova data e horário.");
      return;
    }
    const dataSel = inputToDate(formRemarcar.data);
    dataSel.setHours(0, 0, 0, 0);
    const agora = new Date();
    agora.setHours(0, 0, 0, 0);
    if (dataSel < agora) {
      setErroRemarcar(
        "Não é possível remarcar para datas passadas.",
      );
      return;
    }
    const erroHorario = foraDoHorarioComercial(dataSel, formRemarcar.hora);
    if (erroHorario) {
      setErroRemarcar(erroHorario);
      return;
    }
    const dataStr = dateToBR(dataSel);
    const item = agendamentos.find((a) => a.id === id);
    const conflito = agendamentos.find(
      (a) =>
        a.id !== id &&
        a.dentista === item?.dentista &&
        a.data === dataStr &&
        a.hora === formRemarcar.hora &&
        a.status !== "CANCELADO",
    );
    if (conflito) {
      setErroRemarcar(
        `Conflito de horário às ${formRemarcar.hora} neste dia.`,
      );
      return;
    }

    setSalvandoRemarcar(true);
    setErroRemarcar("");
    try {
      await api.post(
        `/agendamentos/${id}/remarcar?novaDataHora=${encodeURIComponent(`${formRemarcar.data}T${formRemarcar.hora}:00`)}&responsavel=${encodeURIComponent("Recepcionista")}`,
      );
      await carregarAgendamentos();
      setModalRemarcar({ aberto: false });
      setFormRemarcar({ data: "", hora: "" });
      showToast("Agendamento remarcado!", "sucesso");
    } catch (e: any) {
      setErroRemarcar(e.message ?? "Falha ao remarcar agendamento.");
    } finally {
      setSalvandoRemarcar(false);
    }
  }

  async function handleAcionarEmergencia(card: FollowupCard) {
    const flags = followupFlags[card.paciente] ?? followupFlagsPadrao();
    setFollowupEnviando((atual) => ({ ...atual, [card.paciente]: true }));
    try {
      await api.post("/followups/gatilho", {
        paciente: card.paciente,
        tipoProcedimento: "Cirurgia",
      });
      await api.post("/followups/checklist", {
        paciente: card.paciente,
        sangramento: flags.sangramentoExcessivo,
        nivelDor: flags.dorIntensa ? 8 : 3,
        observacoes: flags.febre
          ? "Febre relatada no follow-up pós-cirúrgico."
          : "Checklist pós-cirúrgico registrado pela recepção.",
        responsavel: "Recepcionista",
        dentistaResponsavel: "Dra. Sofia Martins",
      });
      setToast({ msg: `Follow-up de ${card.paciente} registrado com sucesso!`, tipo: "sucesso" });
      setTimeout(() => setToast(null), 3000);
    } catch (e: any) {
      setToast({ msg: e.message ?? "Falha ao registrar follow-up.", tipo: "erro" });
      setTimeout(() => setToast(null), 3000);
    } finally {
      setFollowupEnviando((atual) => ({ ...atual, [card.paciente]: false }));
    }
  }

  // ─ Render ─────────────────────────────────────────────────────────────────

  const hojeStr = hoje();

  return (
    <div className="flex h-full overflow-hidden">
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold shadow-lg ${
            toast.tipo === "sucesso"
              ? "bg-green-100 border-green-500 text-green-800"
              : "bg-red-100 border-red-500 text-red-800"
          }`}
        >
          {toast.tipo === "sucesso" ? "✓" : "✗"} {toast.msg}
        </div>
      )}

      {/* ── Left panel ── */}
      <div className="flex-1 flex flex-col border-r-2 border-gray-300 overflow-auto p-6">
        {/* Header */}
        <div className="mb-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-700">
            Agenda de Consultas
          </h1>
          <button
            onClick={() => abrirCriar()}
            className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600"
          >
            + Novo Agendamento
          </button>
        </div>

        {/* Summary cards */}
        <div className="grid grid-cols-6 gap-2 mb-4">
          {[
            {
              label: "AGENDADOS",
              valor: totais.agendados,
              cor: "border-blue-400 bg-blue-50 text-blue-700",
            },
            {
              label: "CONFIRMADOS",
              valor: totais.confirmados,
              cor: "border-green-500 bg-green-50 text-green-700",
            },
            {
              label: "CANCELADOS",
              valor: totais.cancelados,
              cor: "border-red-400 bg-red-50 text-red-700",
            },
            {
              label: "REMARCADOS",
              valor: totais.remarcados,
              cor: "border-yellow-400 bg-yellow-50 text-yellow-700",
            },
            {
              label: "CONSULTAS",
              valor: totais.consultas,
              cor: "border-gray-400 bg-gray-100 text-gray-700",
            },
            {
              label: "RETORNOS",
              valor: totais.retornos,
              cor: "border-purple-400 bg-purple-50 text-purple-700",
            },
          ].map((c) => (
            <div
              key={c.label}
              className={`border-2 ${c.cor} p-2 text-center`}
            >
              <div className="text-xl font-bold">{c.valor}</div>
              <div className="text-[10px] font-bold mt-0.5">
                {c.label}
              </div>
            </div>
          ))}
        </div>

        {/* Filters */}
        <div className="border-2 border-gray-400 bg-gray-50 p-3 mb-4">
          <div className="grid grid-cols-5 gap-2 mb-2">
            <div className="col-span-2 flex items-center border-2 border-gray-400 bg-white">
              <span className="px-2 text-gray-400 text-sm">
                🔍
              </span>
              <input
                type="text"
                placeholder="Buscar paciente ou horário..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
                className="flex-1 p-1.5 outline-none bg-transparent text-sm"
              />
            </div>
            <select
              value={filtroDentista}
              onChange={(e) =>
                setFiltroDentista(e.target.value)
              }
              className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
            >
              <option value="TODOS">Todos os Dentistas</option>
              {DENTISTAS.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
            <select
              value={filtroStatus}
              onChange={(e) => setFiltroStatus(e.target.value)}
              className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
            >
              <option value="TODOS">Todos os Status</option>
              <option value="AGENDADO">Agendado</option>
              <option value="CONFIRMADO">Confirmado</option>
              <option value="CANCELADO">Cancelado</option>
              <option value="REMARCADO">Remarcado</option>
            </select>
            <select
              value={filtroTipo}
              onChange={(e) => setFiltroTipo(e.target.value)}
              className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
            >
              <option value="TODOS">Todos os Tipos</option>
              <option value="CONSULTA">Consulta</option>
              <option value="RETORNO">Retorno</option>
            </select>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={limparFiltros}
              className="text-sm text-blue-600 underline hover:text-blue-800"
            >
              Limpar filtros
            </button>
            <span className="text-xs text-gray-500">
              {filtrados.length} agendamento(s)
            </span>
          </div>
        </div>

        {/* View tabs */}
        <div className="flex border-2 border-gray-400 mb-4">
          <button
            onClick={() => setView("calendario")}
            className={`flex-1 p-2 font-bold border-r-2 border-gray-400 text-sm ${
              view === "calendario"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 hover:bg-gray-200"
            }`}
          >
            📅 Calendário Semanal
          </button>
          <button
            onClick={() => setView("lista")}
            className={`flex-1 p-2 font-bold text-sm ${
              view === "lista"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 hover:bg-gray-200"
            }`}
          >
            📋 Lista de Agendamentos
          </button>
        </div>

        {/* ── Calendar view ── */}
        {view === "calendario" && (
          <>
            <div className="flex justify-between items-center mb-2">
              <button
                onClick={() =>
                  setSemanaBase((prev) => addDays(prev, -7))
                }
                className="px-3 py-1.5 border-2 border-gray-400 bg-white text-sm hover:bg-gray-100"
              >
                ← Semana Anterior
              </button>
              <span className="text-sm font-bold text-gray-600">
                {dateToBR(semanaBase)} —{" "}
                {dateToBR(addDays(semanaBase, 4))}
              </span>
              <button
                onClick={() =>
                  setSemanaBase((prev) => addDays(prev, 7))
                }
                className="px-3 py-1.5 border-2 border-gray-400 bg-white text-sm hover:bg-gray-100"
              >
                Próxima Semana →
              </button>
            </div>

            <div className="border-2 border-gray-400">
              {/* Day headers */}
              <div className="grid grid-cols-6 border-b-2 border-gray-400">
                <div className="p-2 border-r-2 border-gray-400 bg-gray-100" />
                {diasDaSemana.map((dia, i) => (
                  <div
                    key={i}
                    className={`p-2 text-center border-r-2 border-gray-400 font-bold text-sm ${
                      isToday(dia)
                        ? "bg-blue-500 text-white"
                        : "bg-gray-100"
                    }`}
                  >
                    <div>{DIAS_SEMANA_LABELS[i]}</div>
                    <div className="text-xs">
                      {String(dia.getDate()).padStart(2, "0")}/
                      {String(dia.getMonth() + 1).padStart(
                        2,
                        "0",
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {/* Time rows */}
              {HORARIOS.map((hora) => (
                <div
                  key={hora}
                  className="grid grid-cols-6 border-b-2 border-gray-400"
                >
                  <div className="p-2 border-r-2 border-gray-400 bg-gray-50 font-bold text-center text-sm">
                    {hora}
                  </div>
                  {diasDaSemana.map((dia, dIdx) => {
                    const items = getCelula(dia, hora);
                    return (
                      <div
                        key={dIdx}
                        className="p-1 border-r-2 border-gray-400 min-h-[72px]"
                      >
                        {items.map((item) => (
                          <div
                            key={item.id}
                            onClick={() =>
                              setModalDetalhes({
                                aberto: true,
                                item,
                              })
                            }
                            className={`border-2 p-1.5 mb-1 cursor-pointer hover:opacity-75 ${corCelula(item.status)}`}
                          >
                            <div className="flex justify-between items-start">
                              <span className="text-xs font-bold leading-tight">
                                {item.paciente}
                              </span>
                              {item.inadimplente && (
                                <span className="bg-red-500 text-white text-[9px] px-1 font-bold">
                                  !
                                </span>
                              )}
                            </div>
                            <div className="text-[9px] text-gray-600 mt-0.5">
                              {item.tipo}
                            </div>
                            <div className="text-[9px] font-bold">
                              {item.status}
                            </div>
                          </div>
                        ))}
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>
          </>
        )}

        {/* ── List view ── */}
        {view === "lista" && (
          <div className="border-2 border-gray-400 bg-white">
            <div
              className="grid bg-gray-100 border-b-2 border-gray-400"
              style={{
                gridTemplateColumns:
                  "1.2fr 1.5fr 0.8fr 0.7fr 1fr 1fr 1.8fr",
              }}
            >
              {[
                "Paciente",
                "Dentista",
                "Data",
                "Hora",
                "Tipo",
                "Status",
                "Ações",
              ].map((h) => (
                <div
                  key={h}
                  className="p-2 border-r-2 border-gray-400 font-bold text-sm last:border-r-0"
                >
                  {h}
                </div>
              ))}
            </div>

            {filtrados.length === 0 && (
              <div className="p-8 text-center text-gray-400">
                {carregandoAgendamentos
                  ? "Carregando agendamentos..."
                  : "Nenhum agendamento encontrado com os filtros aplicados."}
              </div>
            )}

            {filtrados.map((item) => (
              <div
                key={item.id}
                className="grid border-b-2 border-gray-400"
                style={{
                  gridTemplateColumns:
                    "1.2fr 1.5fr 0.8fr 0.7fr 1fr 1fr 1.8fr",
                }}
              >
                <div className="p-2 border-r-2 border-gray-400 text-sm">
                  <div>{item.paciente}</div>
                  {item.inadimplente && (
                    <span className="text-[10px] bg-red-500 text-white px-1 font-bold">
                      INADIMPLENTE
                    </span>
                  )}
                </div>
                <div className="p-2 border-r-2 border-gray-400 text-sm">
                  {item.dentista}
                </div>
                <div className="p-2 border-r-2 border-gray-400 text-sm">
                  {item.data}
                </div>
                <div className="p-2 border-r-2 border-gray-400 text-sm font-mono">
                  {item.hora}
                </div>
                <div className="p-2 border-r-2 border-gray-400">
                  <TipoBadge tipo={item.tipo} />
                </div>
                <div className="p-2 border-r-2 border-gray-400">
                  <StatusBadge status={item.status} />
                </div>
                <div className="p-2 flex gap-1 flex-wrap items-center">
                  <button
                    onClick={() =>
                      setModalDetalhes({ aberto: true, item })
                    }
                    className="px-2 py-0.5 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-100"
                  >
                    Ver
                  </button>
                  {item.status !== "CANCELADO" &&
                    item.status !== "CONFIRMADO" && (
                      <button
                        onClick={() =>
                          setModalConfirmar({
                            aberto: true,
                            item,
                          })
                        }
                        className="px-2 py-0.5 border-2 border-green-500 bg-green-50 text-green-700 text-xs font-bold hover:bg-green-500 hover:text-white"
                      >
                        Confirmar
                      </button>
                    )}
                  {item.status !== "CANCELADO" && (
                    <button
                      onClick={() => abrirRemarcar(item)}
                      className="px-2 py-0.5 border-2 border-yellow-500 bg-yellow-50 text-yellow-700 text-xs font-bold hover:bg-yellow-500 hover:text-white"
                    >
                      Remarcar
                    </button>
                  )}
                  {item.status !== "CANCELADO" && (
                    <button
                      onClick={() => abrirCancelar(item)}
                      className="px-2 py-0.5 border-2 border-red-400 bg-red-50 text-red-600 text-xs font-bold hover:bg-red-500 hover:text-white"
                    >
                      Cancelar
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── Right panel ── */}
      <div className="w-80 p-6 bg-gray-50 overflow-auto">
        <h2 className="font-bold text-gray-700 mb-4">
          Tarefas do Dia
        </h2>

        {/* Today's schedule */}
        <div className="mb-5">
          <h3 className="text-sm font-bold text-gray-600 mb-2 flex items-center gap-2">
            <span>📋</span> Agenda de Hoje
          </h3>
          {agendamentos.filter((a) => a.data === hojeStr)
            .length === 0 ? (
            <div className="text-xs text-gray-400 border-2 border-gray-200 p-3 bg-white">
              Nenhum agendamento para hoje.
            </div>
          ) : (
            <div className="space-y-1">
              {agendamentos
                .filter((a) => a.data === hojeStr)
                .sort((a, b) => a.hora.localeCompare(b.hora))
                .map((a) => (
                  <div
                    key={a.id}
                    onClick={() =>
                      setModalDetalhes({
                        aberto: true,
                        item: a,
                      })
                    }
                    className={`border-2 p-2 cursor-pointer hover:opacity-75 ${corCelula(a.status)}`}
                  >
                    <div className="flex justify-between items-center">
                      <span className="text-xs font-bold">
                        {a.hora} — {a.paciente}
                      </span>
                      <StatusBadge status={a.status} />
                    </div>
                    <div className="text-[10px] text-gray-500 mt-0.5">
                      {a.dentista}
                    </div>
                  </div>
                ))}
            </div>
          )}
        </div>

        {/* Recall queue */}
        <div className="mb-5">
          <h3 className="text-sm font-bold text-gray-600 mb-3 flex items-center gap-2">
            <span>📞</span> Fila de Recall
          </h3>
          <div className="space-y-2">
            {[
              {
                nome: "Lucia Oliveira",
                ultimaConsulta: "15/01/2026",
              },
              {
                nome: "Roberto Alves",
                ultimaConsulta: "20/01/2026",
              },
              {
                nome: "Fernanda Ramos",
                ultimaConsulta: "22/01/2026",
              },
            ].map((pac, i) => (
              <div
                key={i}
                className="border-2 border-gray-300 bg-white p-3"
              >
                <div className="font-bold text-sm">
                  {pac.nome}
                </div>
                <div className="text-xs text-gray-500 mb-2">
                  Última: {pac.ultimaConsulta}
                </div>
                <button
                  onClick={() => abrirCriar(pac.nome)}
                  className="w-full px-3 py-1 border-2 border-blue-500 bg-blue-500 text-white text-xs font-bold hover:bg-blue-600"
                >
                  Agendar
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Post-surgical follow-up */}
        <div>
          <h3 className="text-sm font-bold text-gray-600 mb-3 flex items-center gap-2">
            <span>🏥</span> Follow-up Pós-Cirúrgico
          </h3>
          <div className="space-y-2">
            {FOLLOWUP_CARDS.map((pac) => {
              const flags = followupFlags[pac.paciente] ?? followupFlagsPadrao();
              const enviando = Boolean(followupEnviando[pac.paciente]);
              return (
              <div
                key={pac.paciente}
                className="border-2 border-gray-300 bg-white p-3"
              >
                <div className="font-bold text-sm">
                  {pac.paciente}
                </div>
                <div className="text-xs text-gray-500 mb-2">
                  {pac.cirurgia} — {pac.data}
                </div>
                <div className="space-y-1 mb-2">
                  {[
                    {
                      label: "Dor intensa?",
                      checked: flags.dorIntensa,
                      onChange: (checked: boolean) =>
                        setFollowupFlags((atual) => ({
                          ...atual,
                          [pac.paciente]: {
                            ...(atual[pac.paciente] ?? followupFlagsPadrao()),
                            dorIntensa: checked,
                          },
                        })),
                    },
                    {
                      label: "Sangramento excessivo?",
                      checked: flags.sangramentoExcessivo,
                      onChange: (checked: boolean) =>
                        setFollowupFlags((atual) => ({
                          ...atual,
                          [pac.paciente]: {
                            ...(atual[pac.paciente] ?? followupFlagsPadrao()),
                            sangramentoExcessivo: checked,
                          },
                        })),
                    },
                    {
                      label: "Febre?",
                      checked: flags.febre,
                      onChange: (checked: boolean) =>
                        setFollowupFlags((atual) => ({
                          ...atual,
                          [pac.paciente]: {
                            ...(atual[pac.paciente] ?? followupFlagsPadrao()),
                            febre: checked,
                          },
                        })),
                    },
                  ].map((item) => (
                    <label
                      key={item.label}
                      className="flex items-center gap-2 text-xs"
                    >
                      <input
                        type="checkbox"
                        className="border-2 border-gray-400"
                        checked={item.checked}
                        onChange={(e) => item.onChange(e.target.checked)}
                      />
                      {item.label}
                    </label>
                  ))}
                </div>
                <button
                  onClick={() => void handleAcionarEmergencia(pac)}
                  disabled={enviando}
                  className="w-full px-3 py-1 border-2 border-red-500 bg-red-500 text-white text-xs font-bold disabled:opacity-60"
                >
                  {enviando ? "Registrando..." : "🚨 Acionar Emergência"}
                </button>
              </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* ══ Modal: Criar Agendamento ══ */}
      {modalCriar && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-blue-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Novo Agendamento</span>
              <button
                onClick={() => setModalCriar(false)}
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              {erroInadimplente && (
                <div className="border-2 border-red-500 bg-red-50 p-3">
                  <div className="font-bold text-red-700 text-sm">
                    🚫 Paciente Inadimplente
                  </div>
                  <div className="text-xs text-red-600 mt-1">
                    Este paciente possui pendências financeiras
                    em aberto. O agendamento está bloqueado até
                    regularização do débito ou autorização de um
                    responsável.
                  </div>
                </div>
              )}
              {erroRestrito && (
                <div className="border-2 border-red-500 bg-red-50 p-3">
                  <div className="font-bold text-red-700 text-sm">
                    🚫 Paciente Restrito
                  </div>
                  <div className="text-xs text-red-600 mt-1">
                    Este paciente está com status Restrito no
                    cadastro. Altere o status na tela de
                    Pacientes antes de agendar.
                  </div>
                </div>
              )}
              <div>
                <label className="block text-sm font-bold mb-1">
                  Paciente *
                </label>
                <select
                  className="w-full border-2 border-gray-400 p-2 outline-none bg-white"
                  value={formCriar.paciente}
                  onChange={(e) => {
                    setFormCriar((f) => ({
                      ...f,
                      paciente: e.target.value,
                    }));
                    setErroCriar("");
                    setErroInadimplente(false);
                    setErroRestrito(false);
                  }}
                >
                  <option value="">
                    Selecione um paciente
                  </option>
                  {pacientes.map((p) => (
                    <option
                      key={`${p.nome}-${p.telefone}`}
                      value={p.nome}
                    >
                      {p.nome}
                      {p.inadimplente ? " ⚠ Inadimplente" : ""}
                      {p.restrito ? " 🚫 Restrito" : ""}
                    </option>
                  ))}
                </select>
                <div className="mt-1 text-xs text-gray-500">
                  Paciente não está na lista?{" "}
                  <Link
                    to="/recepcionista/pacientes"
                    className="text-blue-600 underline hover:text-blue-800"
                    onClick={() => setModalCriar(false)}
                  >
                    Cadastrar novo paciente →
                  </Link>
                </div>
              </div>

              {pacienteSelecionado && (
                <div className="border-2 border-gray-300 bg-gray-50 p-2 flex gap-4 text-sm flex-wrap">
                  <div className="flex items-center gap-1">
                    Tipo automático:{" "}
                    <TipoBadge tipo={tipoAutomatico} />
                  </div>
                  <div className="flex items-center gap-1">
                    Status inicial:{" "}
                    <StatusBadge status="AGENDADO" />
                  </div>
                </div>
              )}

              <div>
                <label className="block text-sm font-bold mb-1">
                  Dentista *
                </label>
                <select
                  className="w-full border-2 border-gray-400 p-2 outline-none bg-white"
                  value={formCriar.dentista}
                  onChange={(e) => {
                    setFormCriar((f) => ({
                      ...f,
                      dentista: e.target.value,
                    }));
                    setErroCriar("");
                  }}
                >
                  <option value="">
                    Selecione um dentista
                  </option>
                  {DENTISTAS.map((d) => (
                    <option key={d} value={d}>
                      {d}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-bold mb-1">
                    Data *
                  </label>
                  <input
                    type="date"
                    className="w-full border-2 border-gray-400 p-2 outline-none"
                    value={formCriar.data}
                    onChange={(e) => {
                      setFormCriar((f) => ({
                        ...f,
                        data: e.target.value,
                      }));
                      setErroCriar("");
                    }}
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold mb-1">
                    Horário *
                  </label>
                  <select
                    className="w-full border-2 border-gray-400 p-2 outline-none bg-white"
                    value={formCriar.hora}
                    onChange={(e) => {
                      setFormCriar((f) => ({
                        ...f,
                        hora: e.target.value,
                      }));
                      setErroCriar("");
                    }}
                  >
                    <option value="">Selecione</option>
                    {HORARIOS.map((h) => (
                      <option key={h} value={h}>
                        {h}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {erroCriar && (
                <div className="text-red-600 text-sm border-2 border-red-300 bg-red-50 p-2">
                  {erroCriar}
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() => setModalCriar(false)}
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                onClick={handleCriar}
                disabled={salvandoCriar}
                className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600 disabled:opacity-50"
              >
                {salvandoCriar ? "Salvando..." : "Salvar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Detalhes ══ */}
      {modalDetalhes.aberto && modalDetalhes.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-gray-700 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Detalhes do Agendamento</span>
              <button
                onClick={() =>
                  setModalDetalhes({ aberto: false })
                }
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                {(
                  [
                    ["PACIENTE", modalDetalhes.item.paciente],
                    ["DENTISTA", modalDetalhes.item.dentista],
                    ["DATA", modalDetalhes.item.data],
                    ["HORÁRIO", modalDetalhes.item.hora],
                    [
                      "RESPONSÁVEL",
                      modalDetalhes.item.responsavel,
                    ],
                    [
                      "ÚLT. ALTERAÇÃO",
                      modalDetalhes.item.ultimaAlteracao,
                    ],
                  ] as [string, string][]
                ).map(([label, val]) => (
                  <div key={label}>
                    <div className="text-xs text-gray-500 font-bold">
                      {label}
                    </div>
                    <div className="font-medium text-sm">
                      {val}
                    </div>
                  </div>
                ))}
                <div>
                  <div className="text-xs text-gray-500 font-bold">
                    TIPO
                  </div>
                  <TipoBadge tipo={modalDetalhes.item.tipo} />
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold">
                    STATUS
                  </div>
                  <StatusBadge
                    status={modalDetalhes.item.status}
                  />
                </div>
              </div>

              {modalDetalhes.item.inadimplente && (
                <div className="border-2 border-red-400 bg-red-50 p-2 text-xs text-red-700 font-bold">
                  ⚠ Paciente inadimplente
                </div>
              )}

              {modalDetalhes.item.motivoCancelamento && (
                <div className="border-2 border-gray-300 bg-gray-50 p-2">
                  <div className="text-xs font-bold text-gray-500 mb-1">
                    MOTIVO DO CANCELAMENTO
                  </div>
                  <div className="text-sm">
                    {modalDetalhes.item.motivoCancelamento}
                  </div>
                </div>
              )}

              <div className="border-t-2 border-gray-300 pt-3">
                <div className="text-xs text-gray-500 font-bold mb-2">
                  HISTÓRICO
                </div>
                <div className="space-y-1 max-h-28 overflow-y-auto">
                  {modalDetalhes.item.historico.map((h, i) => (
                    <div
                      key={i}
                      className="flex justify-between text-xs border-b border-gray-100 pb-1"
                    >
                      <span className="text-gray-400">
                        {h.data}
                      </span>
                      <span className="flex-1 px-2">
                        {h.acao}
                      </span>
                      <span className="text-gray-500">
                        {h.responsavel}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-2 flex-wrap justify-end">
              {modalDetalhes.item.status !== "CANCELADO" &&
                modalDetalhes.item.status !== "CONFIRMADO" && (
                  <button
                    onClick={() => {
                      setModalConfirmar({
                        aberto: true,
                        item: modalDetalhes.item,
                      });
                      setModalDetalhes({ aberto: false });
                    }}
                    className="px-3 py-1.5 border-2 border-green-500 bg-green-50 text-green-700 text-sm font-bold hover:bg-green-500 hover:text-white"
                  >
                    Confirmar
                  </button>
                )}
              {modalDetalhes.item.status !== "CANCELADO" && (
                <>
                  <button
                    onClick={() => {
                      abrirRemarcar(modalDetalhes.item!);
                      setModalDetalhes({ aberto: false });
                    }}
                    className="px-3 py-1.5 border-2 border-yellow-500 bg-yellow-50 text-yellow-700 text-sm font-bold hover:bg-yellow-500 hover:text-white"
                  >
                    Remarcar
                  </button>
                  <button
                    onClick={() => {
                      abrirCancelar(modalDetalhes.item!);
                      setModalDetalhes({ aberto: false });
                    }}
                    className="px-3 py-1.5 border-2 border-red-400 bg-red-50 text-red-600 text-sm font-bold hover:bg-red-500 hover:text-white"
                  >
                    Cancelar
                  </button>
                </>
              )}
              <button
                onClick={() =>
                  setModalDetalhes({ aberto: false })
                }
                className="px-3 py-1.5 border-2 border-gray-400 bg-white text-sm font-bold hover:bg-gray-100"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Confirmar ══ */}
      {modalConfirmar.aberto && modalConfirmar.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-green-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Confirmar Agendamento</span>
              <button
                onClick={() =>
                  setModalConfirmar({ aberto: false })
                }
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6">
              <p className="text-sm mb-1">
                Paciente:{" "}
                <strong>{modalConfirmar.item.paciente}</strong>
              </p>
              <p className="text-sm mb-3">
                Data:{" "}
                <strong>
                  {modalConfirmar.item.data} às{" "}
                  {modalConfirmar.item.hora}
                </strong>
              </p>
              <p className="text-sm text-gray-600">
                Confirmar a presença do paciente neste
                agendamento?
              </p>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() =>
                  setModalConfirmar({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Voltar
              </button>
              <button
                onClick={() =>
                  handleConfirmar(modalConfirmar.item!.id)
                }
                disabled={salvandoConfirmar}
                className="px-4 py-2 border-2 border-green-500 bg-green-500 text-white font-bold hover:bg-green-600 disabled:opacity-50"
              >
                {salvandoConfirmar ? "Confirmando..." : "Confirmar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Cancelar ══ */}
      {modalCancelar.aberto && modalCancelar.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-red-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Cancelar Agendamento</span>
              <button
                onClick={() =>
                  setModalCancelar({ aberto: false })
                }
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-3">
              <p className="text-sm">
                Paciente:{" "}
                <strong>{modalCancelar.item.paciente}</strong>
              </p>
              <p className="text-sm">
                Data:{" "}
                <strong>
                  {modalCancelar.item.data} às{" "}
                  {modalCancelar.item.hora}
                </strong>
              </p>
              <div>
                <label className="block text-sm font-bold mb-1">
                  Motivo do Cancelamento *
                </label>
                <textarea
                  rows={3}
                  className="w-full border-2 border-gray-400 p-2 outline-none resize-none"
                  placeholder="Descreva o motivo do cancelamento..."
                  value={motivoCancelar}
                  onChange={(e) => {
                    setMotivoCancelar(e.target.value);
                    setErroCancelar("");
                  }}
                />
                {erroCancelar && (
                  <div className="text-red-600 text-sm mt-1">
                    {erroCancelar}
                  </div>
                )}
              </div>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() =>
                  setModalCancelar({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Voltar
              </button>
              <button
                onClick={() =>
                  handleCancelar(modalCancelar.item!.id)
                }
                disabled={salvandoCancelar}
                className="px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600 disabled:opacity-50"
              >
                {salvandoCancelar ? "Cancelando..." : "Cancelar Agendamento"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Remarcar ══ */}
      {modalRemarcar.aberto && modalRemarcar.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-yellow-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Remarcar Agendamento</span>
              <button
                onClick={() =>
                  setModalRemarcar({ aberto: false })
                }
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="border-2 border-gray-300 bg-gray-50 p-2 text-sm">
                <div>
                  Paciente:{" "}
                  <strong>{modalRemarcar.item.paciente}</strong>
                </div>
                <div>
                  Data atual:{" "}
                  <strong>
                    {modalRemarcar.item.data} às{" "}
                    {modalRemarcar.item.hora}
                  </strong>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-bold mb-1">
                    Nova Data *
                  </label>
                  <input
                    type="date"
                    className="w-full border-2 border-gray-400 p-2 outline-none"
                    value={formRemarcar.data}
                    onChange={(e) => {
                      setFormRemarcar((f) => ({
                        ...f,
                        data: e.target.value,
                      }));
                      setErroRemarcar("");
                    }}
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold mb-1">
                    Novo Horário *
                  </label>
                  <select
                    className="w-full border-2 border-gray-400 p-2 outline-none bg-white"
                    value={formRemarcar.hora}
                    onChange={(e) => {
                      setFormRemarcar((f) => ({
                        ...f,
                        hora: e.target.value,
                      }));
                      setErroRemarcar("");
                    }}
                  >
                    <option value="">Selecione</option>
                    {HORARIOS.map((h) => (
                      <option key={h} value={h}>
                        {h}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              {erroRemarcar && (
                <div className="text-red-600 text-sm border-2 border-red-300 bg-red-50 p-2">
                  {erroRemarcar}
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() =>
                  setModalRemarcar({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                onClick={() =>
                  handleRemarcar(modalRemarcar.item!.id)
                }
                disabled={salvandoRemarcar}
                className="px-4 py-2 border-2 border-yellow-500 bg-yellow-500 text-white font-bold hover:bg-yellow-600 disabled:opacity-50"
              >
                {salvandoRemarcar ? "Remarcando..." : "Confirmar Remarcação"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
