import { useState, useMemo, useEffect } from "react";
import { useNavigate } from "react-router";
import {
  AlertTriangle,
  Search,
  CheckCircle,
  XCircle,
  RefreshCw,
  FileText,
  DollarSign,
  Clock,
  Ban,
  Phone,
  History,
  ExternalLink,
  MessageSquare,
} from "lucide-react";

// ─── Types ────────────────────────────────────────────────────────────────────

type StatusParcela =
  | "PENDENTE"
  | "PAGA"
  | "PARCIALMENTE_PAGA"
  | "VENCIDA"
  | "SUBSTITUIDA"
  | "ACORDO_INADIMPLIDO";

type StatusAcordo = "ATIVO" | "QUITADO" | "INADIMPLIDO" | "CANCELADO";

type CanalCobranca = "WHATSAPP" | "TELEFONE" | "EMAIL" | "PRESENCIAL" | "CARTA";
type ResultadoCobranca =
  | "SEM_RESPOSTA"
  | "PROMETEU_PAGAR"
  | "NEGOU_DIVIDA"
  | "RETORNAR_CONTATO"
  | "ACORDO_FECHADO";

interface TentativaCobranca {
  id: number;
  data: string;        // DD/MM/AAAA HH:MM
  responsavel: string;
  canal: CanalCobranca;
  resultado: ResultadoCobranca;
  observacao?: string;
}

interface HistoricoNegociacao {
  id: number;
  data: string;        // DD/MM/AAAA HH:MM
  tipo: "ACORDO_CRIADO" | "ACORDO_QUITADO" | "ACORDO_INADIMPLIDO" | "ACORDO_CANCELADO" | "COBRANCA" | "STATUS_ALTERADO";
  descricao: string;
  responsavel: string;
}

interface Parcela {
  id: number;
  numero: number;
  total: number;
  vencimento: string;
  status: StatusParcela;
  valorOriginal: number;
  multa: number;
  juros: number;
  diasAtraso: number;
  dataPagamento?: string;
  acordoId?: number;
}

interface Acordo {
  id: number;
  pacienteId: number;
  parcelasOriginaisIds: number[];
  novasParcelas: Parcela[];
  valorTotalDivida: number;
  valorTotalComJuros: number;
  dataCriacao: string;
  status: StatusAcordo;
  responsavel: string;
}

interface Paciente {
  id: number;
  nome: string;
  cpf: string;
  telefone: string;
  status: "ATIVO" | "RESTRITO" | "INATIVO";
  diasMaiorAtraso: number;
  parcelas: Parcela[];
  acordos: Acordo[];
  tentativasCobranca: TentativaCobranca[];
  historicoNegociacao: HistoricoNegociacao[];
}

// ─── Configuração global do sistema ──────────────────────────────────────────

const CONFIG = {
  multaPercentual: 2,   // 2% sobre o valor da parcela
  jurosDiario: 0.033,   // ~1% ao mês em taxa diária
  diasBloqueio: 30,     // dias de atraso para marcar como RESTRITO
};

// ─── Mock de dados ────────────────────────────────────────────────────────────

const MOCK_PACIENTES: Paciente[] = [
  {
    id: 1, nome: "Maria Santos", cpf: "123.456.789-00", telefone: "(11) 98888-1111",
    status: "RESTRITO", diasMaiorAtraso: 45,
    parcelas: [
      { id: 101, numero: 1, total: 600, vencimento: "10/04/2026", status: "VENCIDA", valorOriginal: 600, multa: 12, juros: 8.91, diasAtraso: 45 },
      { id: 102, numero: 2, total: 600, vencimento: "10/05/2026", status: "VENCIDA", valorOriginal: 600, multa: 12, juros: 3.96, diasAtraso: 15 },
      { id: 103, numero: 3, total: 600, vencimento: "10/06/2026", status: "PENDENTE", valorOriginal: 600, multa: 0, juros: 0, diasAtraso: 0 },
    ],
    acordos: [],
    tentativasCobranca: [
      { id: 1, data: "05/05/2026 09:15", responsavel: "Recepcionista Ana", canal: "WHATSAPP", resultado: "SEM_RESPOSTA", observacao: "Mensagem enviada sem retorno." },
      { id: 2, data: "12/05/2026 14:30", responsavel: "Recepcionista Ana", canal: "TELEFONE", resultado: "PROMETEU_PAGAR", observacao: "Paciente disse que pagaria até sexta." },
    ],
    historicoNegociacao: [
      { id: 1, data: "05/05/2026 09:15", tipo: "COBRANCA", descricao: "Cobrança via WhatsApp — Sem resposta", responsavel: "Recepcionista Ana" },
      { id: 2, data: "12/05/2026 14:30", tipo: "COBRANCA", descricao: "Cobrança por Telefone — Prometeu pagar", responsavel: "Recepcionista Ana" },
    ],
  },
  {
    id: 2, nome: "José Ferreira", cpf: "234.567.890-11", telefone: "(11) 97777-2222",
    status: "RESTRITO", diasMaiorAtraso: 38,
    parcelas: [
      { id: 201, numero: 1, total: 850, vencimento: "01/05/2026", status: "VENCIDA", valorOriginal: 850, multa: 17, juros: 10.54, diasAtraso: 38 },
      { id: 202, numero: 2, total: 850, vencimento: "01/06/2026", status: "VENCIDA", valorOriginal: 850, multa: 17, juros: 2.81, diasAtraso: 8 },
    ],
    acordos: [],
    tentativasCobranca: [],
    historicoNegociacao: [
      { id: 1, data: "01/06/2026 10:00", tipo: "STATUS_ALTERADO", descricao: "Status alterado para RESTRITO (atraso > 30 dias)", responsavel: "Sistema" },
    ],
  },
  {
    id: 3, nome: "Cláudia Dias", cpf: "345.678.901-22", telefone: "(11) 96666-3333",
    status: "ATIVO", diasMaiorAtraso: 12,
    parcelas: [
      { id: 301, numero: 1, total: 400, vencimento: "28/05/2026", status: "VENCIDA", valorOriginal: 400, multa: 8, juros: 1.58, diasAtraso: 12 },
    ],
    acordos: [],
    tentativasCobranca: [],
    historicoNegociacao: [],
  },
  {
    id: 4, nome: "Roberto Lima", cpf: "456.789.012-33", telefone: "(11) 95555-4444",
    status: "RESTRITO", diasMaiorAtraso: 62,
    parcelas: [
      { id: 401, numero: 1, total: 1200, vencimento: "08/04/2026", status: "SUBSTITUIDA", valorOriginal: 1200, multa: 24, juros: 24.55, diasAtraso: 62, acordoId: 1 },
      { id: 402, numero: 2, total: 1200, vencimento: "08/05/2026", status: "SUBSTITUIDA", valorOriginal: 1200, multa: 24, juros: 12.87, diasAtraso: 31, acordoId: 1 },
      { id: 501, numero: 1, total: 842.14, vencimento: "09/06/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0, acordoId: 1 },
      { id: 502, numero: 2, total: 842.14, vencimento: "09/07/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0, acordoId: 1 },
      { id: 503, numero: 3, total: 842.14, vencimento: "09/08/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0, acordoId: 1 },
    ],
    acordos: [{
      id: 1, pacienteId: 4, parcelasOriginaisIds: [401, 402],
      novasParcelas: [
        { id: 501, numero: 1, total: 842.14, vencimento: "09/06/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0 },
        { id: 502, numero: 2, total: 842.14, vencimento: "09/07/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0 },
        { id: 503, numero: 3, total: 842.14, vencimento: "09/08/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0 },
      ],
      valorTotalDivida: 2400, valorTotalComJuros: 2526.42,
      dataCriacao: "09/06/2026", status: "ATIVO", responsavel: "Recepcionista Ana",
    }],
    tentativasCobranca: [
      { id: 1, data: "15/05/2026 11:00", responsavel: "Recepcionista Ana", canal: "TELEFONE", resultado: "ACORDO_FECHADO", observacao: "Paciente concordou com parcelamento em 3x." },
    ],
    historicoNegociacao: [
      { id: 1, data: "15/05/2026 11:00", tipo: "COBRANCA", descricao: "Cobrança por Telefone — Acordo fechado", responsavel: "Recepcionista Ana" },
      { id: 2, data: "09/06/2026 10:30", tipo: "ACORDO_CRIADO", descricao: "Acordo #1 criado: 3x R$ 842,14 (total R$ 2.526,42)", responsavel: "Recepcionista Ana" },
    ],
  },
];


const PACIENTES_STORAGE_KEY = "odontohub_financeiro_pacientes";

function normalizarPacientes(pacientes: Paciente[]): Paciente[] {
  return pacientes.map((paciente) => {
    const base = MOCK_PACIENTES.find((p) => p.id === paciente.id);
    return {
      ...(base ?? paciente),
      ...paciente,
      telefone: paciente.telefone ?? base?.telefone ?? "",
      diasMaiorAtraso: paciente.diasMaiorAtraso ?? base?.diasMaiorAtraso ?? 0,
      tentativasCobranca: paciente.tentativasCobranca ?? base?.tentativasCobranca ?? [],
      historicoNegociacao: paciente.historicoNegociacao ?? base?.historicoNegociacao ?? [],
    };
  });
}

function carregarPacientes(): Paciente[] {
  if (typeof window === "undefined") return MOCK_PACIENTES;
  try {
    const raw = window.localStorage.getItem(PACIENTES_STORAGE_KEY);
    return raw ? normalizarPacientes(JSON.parse(raw) as Paciente[]) : MOCK_PACIENTES;
  } catch {
    return MOCK_PACIENTES;
  }
}

function salvarPacientesStorage(pacientes: Paciente[]) {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(PACIENTES_STORAGE_KEY, JSON.stringify(pacientes));
}

function parseDataBR(data: string): Date | null {
  const partes = data.split("/");
  if (partes.length !== 3) return null;
  const [dia, mes, ano] = partes.map(Number);
  if (!dia || !mes || !ano) return null;
  const dt = new Date(ano, mes - 1, dia);
  dt.setHours(0, 0, 0, 0);
  return dt;
}

function isoParaBR(iso: string): string {
  if (!iso) return "";
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

function gerarVencimentoAPartirDaPrimeira(dataPrimeiraParcela: string, indice: number): string {
  const base = new Date(dataPrimeiraParcela + "T00:00:00");
  base.setMonth(base.getMonth() + indice);
  return base.toLocaleDateString("pt-BR");
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function calcularJuros(valorOriginal: number, diasAtraso: number): number {
  const multa = (CONFIG.multaPercentual / 100) * valorOriginal;
  const juros = valorOriginal * (CONFIG.jurosDiario / 100) * diasAtraso;
  return parseFloat((multa + juros).toFixed(2));
}

function formatarMoeda(valor: number): string {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function agora(): string {
  return new Date().toLocaleDateString("pt-BR") + " " +
    new Date().toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

const CANAL_LABEL: Record<CanalCobranca, string> = {
  WHATSAPP: "WhatsApp", TELEFONE: "Telefone", EMAIL: "E-mail", PRESENCIAL: "Presencial", CARTA: "Carta",
};
const RESULTADO_LABEL: Record<ResultadoCobranca, string> = {
  SEM_RESPOSTA: "Sem resposta", PROMETEU_PAGAR: "Prometeu pagar",
  NEGOU_DIVIDA: "Negou dívida", RETORNAR_CONTATO: "Retornar contato", ACORDO_FECHADO: "Acordo fechado",
};
const COR_RESULTADO: Record<ResultadoCobranca, string> = {
  SEM_RESPOSTA: "bg-gray-100 border-gray-400 text-gray-600",
  PROMETEU_PAGAR: "bg-blue-100 border-blue-400 text-blue-700",
  NEGOU_DIVIDA: "bg-red-100 border-red-400 text-red-700",
  RETORNAR_CONTATO: "bg-yellow-100 border-yellow-400 text-yellow-700",
  ACORDO_FECHADO: "bg-green-100 border-green-400 text-green-700",
};
const COR_HISTORICO: Record<HistoricoNegociacao["tipo"], string> = {
  ACORDO_CRIADO: "bg-blue-500", ACORDO_QUITADO: "bg-green-500",
  ACORDO_INADIMPLIDO: "bg-red-500", ACORDO_CANCELADO: "bg-gray-500",
  COBRANCA: "bg-orange-400", STATUS_ALTERADO: "bg-purple-400",
};

// ─── Badges ──────────────────────────────────────────────────────────────────

function StatusParcelaBadge({ status }: { status: StatusParcela }) {
  const map: Record<StatusParcela, { label: string; cls: string }> = {
    PENDENTE: { label: "Pendente", cls: "bg-yellow-100 border-yellow-500 text-yellow-700" },
    PAGA: { label: "Paga", cls: "bg-green-100 border-green-500 text-green-700" },
    PARCIALMENTE_PAGA: { label: "Parcial", cls: "bg-blue-100 border-blue-500 text-blue-700" },
    VENCIDA: { label: "Vencida", cls: "bg-red-100 border-red-500 text-red-700" },
    SUBSTITUIDA: { label: "Substituída", cls: "bg-gray-100 border-gray-400 text-gray-500" },
    ACORDO_INADIMPLIDO: { label: "Acordo Inadimplido", cls: "bg-red-200 border-red-600 text-red-800" },
  };
  const { label, cls } = map[status];
  return <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${cls}`}>{label}</span>;
}

function StatusAcordoBadge({ status }: { status: StatusAcordo }) {
  const map: Record<StatusAcordo, { label: string; cls: string }> = {
    ATIVO: { label: "Ativo", cls: "bg-blue-100 border-blue-500 text-blue-700" },
    QUITADO: { label: "Quitado", cls: "bg-green-100 border-green-500 text-green-700" },
    INADIMPLIDO: { label: "Inadimplido", cls: "bg-red-100 border-red-500 text-red-700" },
    CANCELADO: { label: "Cancelado", cls: "bg-gray-100 border-gray-400 text-gray-500" },
  };
  const { label, cls } = map[status];
  return <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${cls}`}>{label}</span>;
}

function StatusPacienteBadge({ status }: { status: Paciente["status"] }) {
  const map = {
    ATIVO: "bg-green-100 border-green-500 text-green-700",
    RESTRITO: "bg-red-100 border-red-500 text-red-700",
    INATIVO: "bg-gray-100 border-gray-400 text-gray-500",
  };
  return <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${map[status]}`}>{status}</span>;
}

// ─── Componente Principal ─────────────────────────────────────────────────────

export default function RecepcionistaAcordos() {
  const navigate = useNavigate();
  const [pacientes, setPacientes] = useState<Paciente[]>(carregarPacientes);
  const [busca, setBusca] = useState("");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");
  const [toast, setToast] = useState<{ msg: string; tipo: "sucesso" | "erro" | "aviso" } | null>(null);

  // ─ Modais ─────────────────────────────────────────────────────────────────
  const [modalDetalhe, setModalDetalhe] = useState<{ aberto: boolean; paciente?: Paciente }>({ aberto: false });
  const [abaDetalhe, setAbaDetalhe] = useState<"parcelas" | "acordos" | "cobrancas" | "historico">("parcelas");

  const [modalAcordo, setModalAcordo] = useState<{ aberto: boolean; paciente?: Paciente }>({ aberto: false });
  const [parcelasSelecionadas, setParcelasSelecionadas] = useState<number[]>([]);
  const [numeroParcelas, setNumeroParcelas] = useState(3);
  const [erroAcordo, setErroAcordo] = useState("");
  const [justificativaAcordo, setJustificativaAcordo] = useState("");
  const [dataPrimeiraParcela, setDataPrimeiraParcela] = useState(new Date().toISOString().slice(0, 10));

  const [modalInadimplencia, setModalInadimplencia] = useState<{ aberto: boolean; paciente?: Paciente; acordoId?: number }>({ aberto: false });
  const [justInadimplencia, setJustInadimplencia] = useState("");

  const [modalCancelarAcordo, setModalCancelarAcordo] = useState<{ aberto: boolean; paciente?: Paciente; acordoId?: number }>({ aberto: false });
  const [justCancelamentoAcordo, setJustCancelamentoAcordo] = useState("");

  // Modal: Registrar Cobrança
  const [modalCobranca, setModalCobranca] = useState<{ aberto: boolean; paciente?: Paciente }>({ aberto: false });
  const [cobData, setCobData] = useState("");
  const [cobResponsavel, setCobResponsavel] = useState("Recepcionista Ana");
  const [cobCanal, setCobCanal] = useState<CanalCobranca | "">("");
  const [cobResultado, setCobResultado] = useState<ResultadoCobranca | "">("");
  const [cobObs, setCobObs] = useState("");
  const [erroCob, setErroCob] = useState<Record<string, string>>({});

  function showToast(msg: string, tipo: "sucesso" | "erro" | "aviso") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 4000);
  }


  useEffect(() => {
    salvarPacientesStorage(pacientes);
  }, [pacientes]);

  useEffect(() => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    let alterou = false;

    const atualizados = pacientes.map((p) => {
      const acordosAtualizados = p.acordos.map((acordo) => {
        if (acordo.status !== "ATIVO") return acordo;
        const temParcelaVencida = acordo.novasParcelas.some((parcela) => {
          const vencimento = parseDataBR(parcela.vencimento);
          return parcela.status === "PENDENTE" && vencimento !== null && vencimento < hoje;
        });
        if (!temParcelaVencida) return acordo;
        alterou = true;
        return { ...acordo, status: "INADIMPLIDO" as StatusAcordo };
      });

      const idsAcordosInadimplidos = acordosAtualizados
        .filter((a) => a.status === "INADIMPLIDO")
        .map((a) => a.id);

      if (idsAcordosInadimplidos.length === 0) return { ...p, acordos: acordosAtualizados };

      const parcelasAtualizadas = p.parcelas.map((parcela) => {
        const vencimento = parseDataBR(parcela.vencimento);
        const estaVencida = vencimento !== null && vencimento < hoje;
        if (parcela.acordoId && idsAcordosInadimplidos.includes(parcela.acordoId) && parcela.status === "PENDENTE" && estaVencida) {
          return { ...parcela, status: "ACORDO_INADIMPLIDO" as StatusParcela, diasAtraso: Math.max(1, Math.floor((hoje.getTime() - vencimento.getTime()) / (1000 * 60 * 60 * 24))) };
        }
        return parcela;
      });

      const jaTemHistoricoAutomatico = p.historicoNegociacao.some((h) => h.descricao.includes("automaticamente como inadimplido"));
      const historicoAutomatico = jaTemHistoricoAutomatico
        ? p.historicoNegociacao
        : [
            ...p.historicoNegociacao,
            {
              id: Date.now() + p.id,
              data: agora(),
              tipo: "ACORDO_INADIMPLIDO" as HistoricoNegociacao["tipo"],
              descricao: `Acordo marcado automaticamente como inadimplido por parcela vencida.`,
              responsavel: "Sistema",
            },
          ];

      return { ...p, status: "RESTRITO" as Paciente["status"], acordos: acordosAtualizados, parcelas: parcelasAtualizadas, historicoNegociacao: historicoAutomatico };
    });

    if (alterou) setPacientes(atualizados);
  }, [pacientes]);

  // ─ Helpers internos ───────────────────────────────────────────────────────

  function adicionarHistorico(pacienteId: number, tipo: HistoricoNegociacao["tipo"], descricao: string, responsavel: string) {
    setPacientes((prev) => prev.map((p) =>
      p.id !== pacienteId ? p : {
        ...p,
        historicoNegociacao: [...p.historicoNegociacao, {
          id: Date.now() + Math.random(),
          data: agora(),
          tipo,
          descricao,
          responsavel,
        }],
      }
    ));
  }

  // Navega para F17 passando contexto de paciente e parcela via state do router
  // A F17 pode lê-lo com useLocation().state para pré-selecionar o formulário
  function irParaPagamento(pacienteId: number, parcelaId?: number) {
    navigate("/recepcionista/pagamentos", {
      state: { pacienteId, parcelaId },
    });
  }

  // ─ Derived ──────────────────────────────────────────────────────────────

  const pacientesFiltrados = useMemo(() => {
    const q = busca.toLowerCase();
    return pacientes.filter((p) => {
      const temDivida = p.parcelas.some(
        (parc) => parc.status === "VENCIDA" || parc.status === "ACORDO_INADIMPLIDO"
      ) || p.acordos.some((a) => a.status === "ATIVO");
      if (!temDivida) return false;
      const matchBusca = busca === "" || p.nome.toLowerCase().includes(q) || p.cpf.includes(q);
      const matchStatus = filtroStatus === "TODOS" || p.status === filtroStatus;
      return matchBusca && matchStatus;
    });
  }, [pacientes, busca, filtroStatus]);

  const totais = useMemo(() => {
    let totalDividas = 0;
    let totalEmAcordo = 0;
    let pacientesRestrito = 0;
    let acordosAtivos = 0;
    let totalCobranças = 0;
    pacientes.forEach((p) => {
      if (p.status === "RESTRITO") pacientesRestrito++;
      totalCobranças += p.tentativasCobranca.length;
      p.parcelas.forEach((parc) => {
        if (parc.status === "VENCIDA") {
          totalDividas += parc.valorOriginal + calcularJuros(parc.valorOriginal, parc.diasAtraso);
        }
      });
      p.acordos.forEach((a) => {
        if (a.status === "ATIVO") {
          acordosAtivos++;
          totalEmAcordo += a.valorTotalComJuros;
        }
      });
    });
    return { totalDividas, totalEmAcordo, pacientesRestrito, acordosAtivos, totalCobranças };
  }, [pacientes]);

  // ─ Accord handlers ───────────────────────────────────────────────────────

  function abrirModalAcordo(pac: Paciente) {
    const parcelasVencidas = pac.parcelas.filter(
      (p) => p.status === "VENCIDA" || p.status === "ACORDO_INADIMPLIDO"
    );
    if (parcelasVencidas.length === 0) {
      showToast("Este paciente não possui parcelas vencidas para negociar.", "aviso");
      return;
    }
    setParcelasSelecionadas(parcelasVencidas.map((p) => p.id));
    setNumeroParcelas(3);
    setJustificativaAcordo("");
    setDataPrimeiraParcela(new Date().toISOString().slice(0, 10));
    setErroAcordo("");
    setModalAcordo({ aberto: true, paciente: pac });
  }

  function calcularResumoAcordo() {
    if (!modalAcordo.paciente) return { totalOriginal: 0, totalEncargos: 0, totalComEncargos: 0, valorParcela: 0 };
    const selecionadas = modalAcordo.paciente.parcelas.filter((p) => parcelasSelecionadas.includes(p.id));
    const totalOriginal = selecionadas.reduce((acc, p) => acc + p.valorOriginal, 0);
    const totalEncargos = selecionadas.reduce((acc, p) => acc + calcularJuros(p.valorOriginal, p.diasAtraso), 0);
    const totalComEncargos = parseFloat((totalOriginal + totalEncargos).toFixed(2));
    const valorParcela = numeroParcelas > 0 ? parseFloat((totalComEncargos / numeroParcelas).toFixed(2)) : 0;
    return { totalOriginal, totalEncargos, totalComEncargos, valorParcela };
  }


  function confirmarAcordo() {
    if (parcelasSelecionadas.length === 0) { setErroAcordo("Selecione ao menos uma parcela para o acordo."); return; }
    if (numeroParcelas < 1 || numeroParcelas > 24) { setErroAcordo("O número de parcelas deve ser entre 1 e 24."); return; }
    if (!dataPrimeiraParcela) { setErroAcordo("Informe a data da primeira parcela do acordo."); return; }
    if (!justificativaAcordo.trim()) { setErroAcordo("Informe a justificativa obrigatória da negociação."); return; }

    const pac = modalAcordo.paciente!;
    const { totalComEncargos, valorParcela } = calcularResumoAcordo();
    const novoAcordoId = Math.max(0, ...pac.acordos.map((a) => a.id)) + 1;

    const novasParcelas: Parcela[] = Array.from({ length: numeroParcelas }, (_, i) => ({
      id: Date.now() + i,
      numero: i + 1,
      total: valorParcela,
      vencimento: gerarVencimentoAPartirDaPrimeira(dataPrimeiraParcela, i),
      status: "PENDENTE" as StatusParcela,
      valorOriginal: valorParcela,
      multa: 0,
      juros: 0,
      diasAtraso: 0,
      acordoId: novoAcordoId,
    }));

    const valorTotalOriginal = parcelasSelecionadas.reduce((acc, id) => {
      const p = pac.parcelas.find((x) => x.id === id);
      return acc + (p?.valorOriginal ?? 0);
    }, 0);

    const novoAcordo: Acordo = {
      id: novoAcordoId,
      pacienteId: pac.id,
      parcelasOriginaisIds: [...parcelasSelecionadas],
      novasParcelas,
      valorTotalDivida: valorTotalOriginal,
      valorTotalComJuros: totalComEncargos,
      dataCriacao: new Date().toLocaleDateString("pt-BR"),
      status: "ATIVO",
      responsavel: "Recepcionista Ana",
    };

    // Toda criação de acordo deve gerar registro no histórico
    const entradaHistorico: HistoricoNegociacao = {
      id: Date.now() + 999,
      data: agora(),
      tipo: "ACORDO_CRIADO",
      descricao: `Acordo #${novoAcordoId} criado: ${numeroParcelas}x ${formatarMoeda(valorParcela)} (total ${formatarMoeda(totalComEncargos)}). Justificativa: ${justificativaAcordo.trim()}`,
      responsavel: "Recepcionista Ana",
    };

    setPacientes((prev) =>
      prev.map((p) =>
        p.id === pac.id
          ? {
              ...p,
              parcelas: [
                ...p.parcelas.map((parc) =>
                  parcelasSelecionadas.includes(parc.id)
                    ? { ...parc, status: "SUBSTITUIDA" as StatusParcela, acordoId: novoAcordoId }
                    : parc
                ),
                ...novasParcelas,
              ],
              acordos: [...p.acordos, novoAcordo],
              historicoNegociacao: [...p.historicoNegociacao, entradaHistorico],
            }
          : p
      )
    );

    showToast(`Acordo gerado com sucesso! ${numeroParcelas}x de ${formatarMoeda(valorParcela)}.`, "sucesso");
    setModalAcordo({ aberto: false });
    setJustificativaAcordo("");
  }

  function registrarInadimplencia() {
    if (!justInadimplencia.trim()) return;
    const pac = modalInadimplencia.paciente!;
    const acordoId = modalInadimplencia.acordoId!;

    // Toda alteração de acordo deve gerar registro no histórico
    const entradaHistorico: HistoricoNegociacao = {
      id: Date.now(),
      data: agora(),
      tipo: "ACORDO_INADIMPLIDO",
      descricao: `Acordo #${acordoId} registrado como inadimplido. Justificativa: ${justInadimplencia}`,
      responsavel: "Recepcionista Ana",
    };

    setPacientes((prev) =>
      prev.map((p) => {
        if (p.id !== pac.id) return p;
        return {
          ...p,
          status: "RESTRITO",
          acordos: p.acordos.map((a) =>
            a.id === acordoId ? { ...a, status: "INADIMPLIDO" as StatusAcordo } : a
          ),
          parcelas: p.parcelas.map((parc) =>
            parc.acordoId === acordoId
              ? { ...parc, status: "ACORDO_INADIMPLIDO" as StatusParcela }
              : parc
          ),
          historicoNegociacao: [...p.historicoNegociacao, entradaHistorico],
        };
      })
    );

    showToast("Inadimplência registrada. Parcelas originais restauradas com multas retroativas.", "aviso");
    setModalInadimplencia({ aberto: false });
    setJustInadimplencia("");
  }


  function abrirModalCancelarAcordo(pac: Paciente, acordoId: number) {
    setModalCancelarAcordo({ aberto: true, paciente: pac, acordoId });
    setJustCancelamentoAcordo("");
  }

  function confirmarCancelamentoAcordo() {
    if (!justCancelamentoAcordo.trim()) return;
    const pac = modalCancelarAcordo.paciente!;
    const acordoId = modalCancelarAcordo.acordoId!;

    const entradaHistorico: HistoricoNegociacao = {
      id: Date.now(),
      data: agora(),
      tipo: "ACORDO_CANCELADO",
      descricao: `Acordo #${acordoId} cancelado. Justificativa: ${justCancelamentoAcordo.trim()}`,
      responsavel: "Recepcionista Ana",
    };

    setPacientes((prev) =>
      prev.map((p) =>
        p.id !== pac.id
          ? p
          : {
              ...p,
              acordos: p.acordos.map((a) => a.id === acordoId ? { ...a, status: "CANCELADO" as StatusAcordo } : a),
              parcelas: p.parcelas.map((parcela) => parcela.acordoId === acordoId && parcela.status === "PENDENTE" ? { ...parcela, status: "SUBSTITUIDA" as StatusParcela } : parcela),
              historicoNegociacao: [...p.historicoNegociacao, entradaHistorico],
            }
      )
    );

    showToast("Acordo cancelado com justificativa registrada.", "aviso");
    setModalCancelarAcordo({ aberto: false });
    setJustCancelamentoAcordo("");
  }

  // ─ Cobrança handlers ──────────────────────────────────────────────────────

  function abrirModalCobranca(pac: Paciente) {
    setCobData(new Date().toISOString().slice(0, 10));
    setCobResponsavel("Recepcionista Ana");
    setCobCanal("");
    setCobResultado("");
    setCobObs("");
    setErroCob({});
    setModalCobranca({ aberto: true, paciente: pac });
  }

  function confirmarCobranca() {
    const erros: Record<string, string> = {};
    if (!cobData) erros.data = "Informe a data da tentativa.";
    if (!cobResponsavel.trim()) erros.responsavel = "Informe o responsável.";
    if (!cobCanal) erros.canal = "Selecione o canal de contato.";
    if (!cobResultado) erros.resultado = "Selecione o resultado.";
    setErroCob(erros);
    if (Object.keys(erros).length > 0) return;

    const pac = modalCobranca.paciente!;
    const dataBR = cobData.split("-").reverse().join("/");
    const dataBRComHora = dataBR + " " + new Date().toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });

    const novaTentativa: TentativaCobranca = {
      id: Date.now(),
      data: dataBRComHora,
      responsavel: cobResponsavel,
      canal: cobCanal as CanalCobranca,
      resultado: cobResultado as ResultadoCobranca,
      observacao: cobObs || undefined,
    };

    const entradaHistorico: HistoricoNegociacao = {
      id: Date.now() + 1,
      data: dataBRComHora,
      tipo: "COBRANCA",
      descricao: `Cobrança via ${CANAL_LABEL[cobCanal as CanalCobranca]} — ${RESULTADO_LABEL[cobResultado as ResultadoCobranca]}${cobObs ? `. ${cobObs}` : ""}`,
      responsavel: cobResponsavel,
    };

    setPacientes((prev) => prev.map((p) =>
      p.id !== pac.id ? p : {
        ...p,
        tentativasCobranca: [...p.tentativasCobranca, novaTentativa],
        historicoNegociacao: [...p.historicoNegociacao, entradaHistorico],
      }
    ));

    showToast("Tentativa de cobrança registrada com sucesso.", "sucesso");
    setModalCobranca({ aberto: false });
  }

  const resumoAcordo = calcularResumoAcordo();

  // ─ Render ────────────────────────────────────────────────────────────────

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      {/* Toast */}
      {toast && (
        <div className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold text-sm shadow-lg max-w-sm ${toast.tipo === "sucesso" ? "bg-green-50 border-green-500 text-green-700" : toast.tipo === "aviso" ? "bg-yellow-50 border-yellow-500 text-yellow-700" : "bg-red-50 border-red-500 text-red-700"}`}>
          {toast.tipo === "sucesso" ? "✓ " : toast.tipo === "aviso" ? "⚠ " : "✕ "}{toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="mb-6">
        <h1 className="text-xl font-bold text-gray-700 flex items-center gap-2">
          <DollarSign className="w-6 h-6" />
          Inadimplência e Acordos
        </h1>
        <p className="text-xs text-gray-500 mt-1">Gerencie dívidas em atraso, registre cobranças, negocie acordos e redirecione para pagamento na F17.</p>
      </div>

      {/* Política de juros */}
      <div className="bg-blue-50 border-2 border-blue-300 p-3 mb-6 flex items-start gap-3 text-sm">
        <FileText className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
        <span className="text-blue-700">
          <strong>Política vigente:</strong> Multa de {CONFIG.multaPercentual}% + Juros de {CONFIG.jurosDiario}% ao dia (≈ 1% a.m.) — calculados automaticamente, sem edição manual.
          Pacientes com &gt;{CONFIG.diasBloqueio} dias de atraso recebem status <strong>RESTRITO</strong> e ficam bloqueados para novos agendamentos não emergenciais.
          <strong> Esta tela não registra pagamentos — use o botão "Ir para Pagamento" para acessar a F17.</strong>
        </span>
      </div>

      {/* Cards de resumo financeiro */}
      <div className="grid grid-cols-5 gap-3 mb-6">
        <div className="border-2 border-red-500 bg-red-50 p-4">
          <div className="text-xl font-bold text-red-700">{formatarMoeda(totais.totalDividas)}</div>
          <div className="text-xs text-gray-500 mt-1">Total em Dívida (com encargos)</div>
        </div>
        <div className="border-2 border-blue-500 bg-blue-50 p-4">
          <div className="text-xl font-bold text-blue-700">{formatarMoeda(totais.totalEmAcordo)}</div>
          <div className="text-xs text-gray-500 mt-1">Em Acordo Ativo</div>
        </div>
        <div className="border-2 border-orange-500 bg-orange-50 p-4">
          <div className="text-2xl font-bold text-orange-700">{totais.pacientesRestrito}</div>
          <div className="text-xs text-gray-500 mt-1">Pacientes Restritos</div>
        </div>
        <div className="border-2 border-purple-500 bg-purple-50 p-4">
          <div className="text-2xl font-bold text-purple-700">{totais.acordosAtivos}</div>
          <div className="text-xs text-gray-500 mt-1">Acordos Ativos</div>
        </div>
        <div className="border-2 border-yellow-500 bg-yellow-50 p-4">
          <div className="text-2xl font-bold text-yellow-700">{totais.totalCobranças}</div>
          <div className="text-xs text-gray-500 mt-1">Cobranças Registradas</div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white border-2 border-gray-400 p-4 mb-4">
        <div className="grid grid-cols-4 gap-3 items-end">
          <div className="col-span-2">
            <label className="text-sm font-bold mb-2 block">Buscar paciente</label>
            <div className="relative">
              <Search className="absolute left-2 top-2.5 w-4 h-4 text-gray-400" />
              <input type="text" className="w-full border-2 border-gray-300 p-2 pl-8 bg-white rounded focus:border-blue-500 focus:outline-none" placeholder="Nome ou CPF" value={busca} onChange={(e) => setBusca(e.target.value)} />
            </div>
          </div>
          <div>
            <label className="text-sm font-bold mb-2 block">Status do paciente</label>
            <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value)}>
              <option value="TODOS">Todos</option>
              <option value="RESTRITO">Restrito</option>
              <option value="ATIVO">Ativo</option>
            </select>
          </div>
          <div className="text-xs text-gray-500 self-end pb-2 text-right">{pacientesFiltrados.length} paciente(s) com pendências</div>
        </div>
      </div>

      {/* Tabela de inadimplentes */}
      <div className="border-2 border-gray-400 bg-white overflow-x-auto">
        <div className="min-w-[1100px]">
          <div className="grid grid-cols-8 bg-gray-100 border-b-2 border-gray-400 text-sm font-bold">
            <div className="p-3 border-r-2 border-gray-400 col-span-2">Paciente</div>
            <div className="p-3 border-r-2 border-gray-400">Status</div>
            <div className="p-3 border-r-2 border-gray-400">Parc. Vencidas</div>
            <div className="p-3 border-r-2 border-gray-400">Dias de Atraso</div>
            <div className="p-3 border-r-2 border-gray-400">Valor Atualizado</div>
            <div className="p-3 border-r-2 border-gray-400">Cobranças</div>
            <div className="p-3 text-center">Ações</div>
          </div>

          {pacientesFiltrados.length === 0 && (
            <div className="p-12 text-center text-gray-500">Nenhum paciente com pendências encontrado.</div>
          )}

          {pacientesFiltrados.map((pac) => {
            const parcelasVencidas = pac.parcelas.filter((p) => p.status === "VENCIDA" || p.status === "ACORDO_INADIMPLIDO");
            const totalAtualizado = parcelasVencidas.reduce(
              (acc, p) => acc + p.valorOriginal + calcularJuros(p.valorOriginal, p.diasAtraso), 0
            );
            const temAcordoAtivo = pac.acordos.some((a) => a.status === "ATIVO");
            const primeiraVencida = parcelasVencidas[0];

            return (
              <div key={pac.id} className="grid grid-cols-8 border-b-2 border-gray-400 items-center text-sm hover:bg-gray-50">
                {/* Paciente */}
                <div className="p-3 border-r-2 border-gray-400 col-span-2">
                  <div className="font-bold text-gray-800">{pac.nome}</div>
                  <div className="text-xs text-gray-500">{pac.cpf}</div>
                  <div className="text-xs text-gray-400 flex items-center gap-1 mt-0.5"><Phone className="w-3 h-3" />{pac.telefone}</div>
                </div>
                {/* Status */}
                <div className="p-3 border-r-2 border-gray-400">
                  <StatusPacienteBadge status={pac.status} />
                  {pac.status === "RESTRITO" && (
                    <div className="text-xs text-red-600 mt-1 flex items-center gap-1"><Ban className="w-3 h-3" /> Agend. bloqueado</div>
                  )}
                  {temAcordoAtivo && (
                    <div className="text-xs text-blue-600 mt-1 font-bold">Acordo ativo</div>
                  )}
                </div>
                {/* Parcelas vencidas */}
                <div className="p-3 border-r-2 border-gray-400 text-center">
                  <span className={`font-bold text-xl ${parcelasVencidas.length > 0 ? "text-red-600" : "text-gray-400"}`}>{parcelasVencidas.length}</span>
                </div>
                {/* Dias de atraso */}
                <div className="p-3 border-r-2 border-gray-400">
                  <span className={`font-bold text-base ${pac.diasMaiorAtraso >= CONFIG.diasBloqueio ? "text-red-600" : "text-yellow-600"}`}>
                    {pac.diasMaiorAtraso} dias
                  </span>
                  {pac.diasMaiorAtraso >= CONFIG.diasBloqueio && (
                    <div className="text-xs text-red-500 flex items-center gap-1 mt-0.5">
                      <AlertTriangle className="w-3 h-3" /> Bloqueio ativo
                    </div>
                  )}
                </div>
                {/* Valor atualizado com encargos */}
                <div className="p-3 border-r-2 border-gray-400">
                  <div className="font-bold text-red-700">{formatarMoeda(totalAtualizado)}</div>
                  <div className="text-xs text-gray-400">c/ multa e juros</div>
                </div>
                {/* Cobranças */}
                <div className="p-3 border-r-2 border-gray-400 text-center">
                  <span className="font-bold text-gray-700">{pac.tentativasCobranca.length}</span>
                  {pac.tentativasCobranca.length > 0 && (
                    <div className="text-xs text-gray-400">tentativa(s)</div>
                  )}
                </div>
                {/* Ações */}
                <div className="p-3 flex flex-col gap-1 items-center">
                  {/* Ver detalhes */}
                  <button
                    onClick={() => { setModalDetalhe({ aberto: true, paciente: pac }); setAbaDetalhe("parcelas"); }}
                    className="w-full px-2 py-1 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-50 flex items-center justify-center gap-1"
                  >
                    <FileText className="w-3 h-3" /> Ver
                  </button>
                  {/* Registrar cobrança */}
                  <button
                    onClick={() => abrirModalCobranca(pac)}
                    className="w-full px-2 py-1 border-2 border-orange-400 bg-white text-orange-700 text-xs font-bold hover:bg-orange-50 flex items-center justify-center gap-1"
                  >
                    <MessageSquare className="w-3 h-3" /> Cobrança
                  </button>
                  {/* Criar acordo */}
                  {!temAcordoAtivo && parcelasVencidas.length > 0 && (
                    <button
                      onClick={() => abrirModalAcordo(pac)}
                      className="w-full px-2 py-1 border-2 border-blue-500 bg-blue-500 text-white text-xs font-bold hover:bg-blue-600 flex items-center justify-center gap-1"
                    >
                      <RefreshCw className="w-3 h-3" /> Criar Acordo
                    </button>
                  )}
                  {/* Inadimpliu o acordo */}
                  {temAcordoAtivo && (
                    <>
                      <button
                        onClick={() => {
                          const acordo = pac.acordos.find((a) => a.status === "ATIVO")!;
                          setModalInadimplencia({ aberto: true, paciente: pac, acordoId: acordo.id });
                          setJustInadimplencia("");
                        }}
                        className="w-full px-2 py-1 border-2 border-red-400 bg-white text-red-600 text-xs font-bold hover:bg-red-50"
                      >
                        Inadimpliu
                      </button>
                      <button
                        onClick={() => {
                          const acordo = pac.acordos.find((a) => a.status === "ATIVO")!;
                          abrirModalCancelarAcordo(pac, acordo.id);
                        }}
                        className="w-full px-2 py-1 border-2 border-gray-400 bg-white text-gray-700 text-xs font-bold hover:bg-gray-50"
                      >
                        Cancelar Acordo
                      </button>
                    </>
                  )}
                  {/* Ir para Pagamento (F17) */}
                  {(parcelasVencidas.length > 0 || temAcordoAtivo) && (
                    <button
                      onClick={() => irParaPagamento(pac.id, primeiraVencida?.id)}
                      className="w-full px-2 py-1 border-2 border-green-500 bg-green-500 text-white text-xs font-bold hover:bg-green-600 flex items-center justify-center gap-1"
                    >
                      <ExternalLink className="w-3 h-3" /> Ir para Pagamento
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>


      {/* ══ Modal: Detalhe do Paciente ══════════════════════════════════════════ */}
      {modalDetalhe.aberto && modalDetalhe.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-blue-500 w-[760px] max-h-[90vh] flex flex-col">
            {/* Header do modal */}
            <div className="bg-blue-50 p-4 border-b-2 border-gray-400 flex items-start justify-between flex-shrink-0">
              <div>
                <div className="font-bold text-lg text-gray-800">{modalDetalhe.paciente.nome}</div>
                <div className="text-sm text-gray-500 flex items-center gap-3">
                  {modalDetalhe.paciente.cpf}
                  <StatusPacienteBadge status={modalDetalhe.paciente.status} />
                  {modalDetalhe.paciente.status === "RESTRITO" && (
                    <span className="text-xs text-red-600 flex items-center gap-1"><Ban className="w-3 h-3" /> Bloqueado p/ agendamentos</span>
                  )}
                </div>
              </div>
              <button onClick={() => setModalDetalhe({ aberto: false })} className="text-xl text-gray-400 hover:text-gray-700">✕</button>
            </div>

            {/* Abas */}
            <div className="flex border-b-2 border-gray-300 flex-shrink-0 overflow-x-auto">
              {(["parcelas", "acordos", "cobrancas", "historico"] as const).map((aba) => {
                const labels: Record<string, string> = {
                  parcelas: `Parcelas (${modalDetalhe.paciente!.parcelas.length})`,
                  acordos: `Acordos (${modalDetalhe.paciente!.acordos.length})`,
                  cobrancas: `Cobranças (${modalDetalhe.paciente!.tentativasCobranca.length})`,
                  historico: `Histórico (${modalDetalhe.paciente!.historicoNegociacao.length})`,
                };
                return (
                  <button
                    key={aba}
                    onClick={() => setAbaDetalhe(aba)}
                    className={`px-5 py-3 text-sm font-bold border-b-2 whitespace-nowrap ${abaDetalhe === aba ? "border-blue-500 text-blue-600" : "border-transparent text-gray-500 hover:text-gray-700"}`}
                  >
                    {labels[aba]}
                  </button>
                );
              })}
            </div>

            <div className="overflow-y-auto flex-1">
              {/* Aba: Parcelas */}
              {abaDetalhe === "parcelas" && (
                <div className="p-6 space-y-3">
                  {modalDetalhe.paciente.parcelas.map((parc) => {
                    const encargos = calcularJuros(parc.valorOriginal, parc.diasAtraso);
                    const totalComEncargos = parc.valorOriginal + encargos;
                    const isVencida = parc.status === "VENCIDA" || parc.status === "ACORDO_INADIMPLIDO";
                    return (
                      <div key={parc.id} className={`border-2 p-4 rounded ${isVencida ? "border-red-300 bg-red-50" : parc.status === "SUBSTITUIDA" ? "border-gray-200 bg-gray-50 opacity-60" : parc.status === "PAGA" ? "border-green-300 bg-green-50" : "border-gray-200 bg-white"}`}>
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-gray-700">Parcela {parc.numero}</span>
                            <StatusParcelaBadge status={parc.status} />
                            {parc.acordoId && <span className="text-xs text-gray-400">Acordo #{parc.acordoId}</span>}
                          </div>
                          <div className="text-sm text-gray-500 flex items-center gap-1"><Clock className="w-3 h-3" /> Vence: {parc.vencimento}</div>
                        </div>
                        <div className="grid grid-cols-4 gap-3 text-sm">
                          <div><div className="text-xs text-gray-500">Valor original</div><div className="font-bold">{formatarMoeda(parc.valorOriginal)}</div></div>
                          {isVencida && (
                            <>
                              <div><div className="text-xs text-gray-500">Multa + Juros</div><div className="font-bold text-red-600">+{formatarMoeda(encargos)}</div></div>
                              <div><div className="text-xs text-gray-500">Total atualizado</div><div className="font-bold text-red-700">{formatarMoeda(totalComEncargos)}</div></div>
                              <div><div className="text-xs text-gray-500">Dias em atraso</div><div className="font-bold text-red-600">{parc.diasAtraso} dias</div></div>
                            </>
                          )}
                          {parc.status === "PAGA" && (
                            <div><div className="text-xs text-gray-500">Pago em</div><div className="font-bold text-green-700">{parc.dataPagamento}</div></div>
                          )}
                        </div>
                        {/* F9 não registra pagamento — redireciona para F17 */}
                        {isVencida && (
                          <div className="mt-3 flex justify-end">
                            <button
                              onClick={() => { irParaPagamento(modalDetalhe.paciente!.id, parc.id); setModalDetalhe({ aberto: false }); }}
                              className="px-3 py-1.5 border-2 border-green-500 bg-green-500 text-white text-xs font-bold hover:bg-green-600 flex items-center gap-1"
                            >
                              <ExternalLink className="w-3 h-3" /> Ir para Pagamento (F17)
                            </button>
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}

              {/* Aba: Acordos */}
              {abaDetalhe === "acordos" && (
                <div className="p-6">
                  {modalDetalhe.paciente.acordos.length === 0 ? (
                    <div className="text-center text-gray-400 py-8">Nenhum acordo registrado para este paciente.</div>
                  ) : (
                    <div className="space-y-4">
                      {modalDetalhe.paciente.acordos.map((acordo) => (
                        <div key={acordo.id} className="border-2 border-gray-300 rounded p-4">
                          <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center gap-2">
                              <span className="font-bold text-gray-700">Acordo #{acordo.id}</span>
                              <StatusAcordoBadge status={acordo.status} />
                            </div>
                            <div className="text-xs text-gray-500">Criado em {acordo.dataCriacao} por {acordo.responsavel}</div>
                          </div>
                          <div className="grid grid-cols-3 gap-3 text-sm mb-3">
                            <div className="border border-gray-200 p-2 rounded"><div className="text-xs text-gray-500">Dívida original</div><div className="font-bold">{formatarMoeda(acordo.valorTotalDivida)}</div></div>
                            <div className="border border-gray-200 p-2 rounded"><div className="text-xs text-gray-500">Total com encargos</div><div className="font-bold text-red-600">{formatarMoeda(acordo.valorTotalComJuros)}</div></div>
                            <div className="border border-gray-200 p-2 rounded"><div className="text-xs text-gray-500">Parcelas do acordo</div><div className="font-bold">{acordo.novasParcelas.length}x de {formatarMoeda(acordo.valorTotalComJuros / acordo.novasParcelas.length)}</div></div>
                          </div>
                          <div className="text-xs font-bold text-gray-500 uppercase mb-2">Parcelas do Acordo</div>
                          <div className="space-y-1">
                            {acordo.novasParcelas.map((np) => {
                              const parcAtual = modalDetalhe.paciente!.parcelas.find((x) => x.id === np.id) ?? np;
                              return (
                                <div key={np.id} className={`flex items-center justify-between p-2 rounded border text-sm ${parcAtual.status === "PAGA" ? "bg-green-50 border-green-200" : "bg-white border-gray-200"}`}>
                                  <span>Parcela {np.numero} — Vence {np.vencimento}</span>
                                  <div className="flex items-center gap-2">
                                    <span className="font-bold">{formatarMoeda(np.total)}</span>
                                    <StatusParcelaBadge status={parcAtual.status} />
                                    {/* Pagamento sempre via F17 */}
                                    {(parcAtual.status === "PENDENTE" || parcAtual.status === "VENCIDA") && (
                                      <button
                                        onClick={() => { irParaPagamento(modalDetalhe.paciente!.id, parcAtual.id); setModalDetalhe({ aberto: false }); }}
                                        className="px-2 py-0.5 border border-green-500 text-green-700 bg-white text-xs font-bold hover:bg-green-50 rounded flex items-center gap-1"
                                      >
                                        <ExternalLink className="w-3 h-3" /> Pagar (F17)
                                      </button>
                                    )}
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Aba: Cobranças */}
              {abaDetalhe === "cobrancas" && (
                <div className="p-6">
                  <div className="flex justify-end mb-4">
                    <button
                      onClick={() => { setModalDetalhe({ aberto: false }); abrirModalCobranca(modalDetalhe.paciente!); }}
                      className="px-4 py-2 border-2 border-orange-400 bg-orange-400 text-white font-bold hover:bg-orange-500 text-sm flex items-center gap-2"
                    >
                      <MessageSquare className="w-4 h-4" /> Registrar Nova Cobrança
                    </button>
                  </div>
                  {modalDetalhe.paciente.tentativasCobranca.length === 0 ? (
                    <div className="text-center text-gray-400 py-8">Nenhuma tentativa de cobrança registrada.</div>
                  ) : (
                    <div className="space-y-3">
                      {[...modalDetalhe.paciente.tentativasCobranca].reverse().map((tc) => (
                        <div key={tc.id} className="border-2 border-gray-200 bg-white p-4 rounded">
                          <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                              <span className="text-xs font-bold text-gray-500">{tc.data}</span>
                              <span className="inline-block px-2 py-0.5 border text-xs font-bold rounded bg-gray-100 border-gray-300">{CANAL_LABEL[tc.canal]}</span>
                              <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${COR_RESULTADO[tc.resultado]}`}>{RESULTADO_LABEL[tc.resultado]}</span>
                            </div>
                            <span className="text-xs text-gray-400">por {tc.responsavel}</span>
                          </div>
                          {tc.observacao && <p className="text-sm text-gray-600 bg-gray-50 p-2 rounded">{tc.observacao}</p>}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Aba: Histórico de Negociação */}
              {abaDetalhe === "historico" && (
                <div className="p-6">
                  {modalDetalhe.paciente.historicoNegociacao.length === 0 ? (
                    <div className="text-center text-gray-400 py-8">Nenhum evento registrado no histórico.</div>
                  ) : (
                    <div className="relative">
                      {/* Linha vertical da timeline */}
                      <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200" />
                      <div className="space-y-4 pl-10">
                        {[...modalDetalhe.paciente.historicoNegociacao].reverse().map((h) => (
                          <div key={h.id} className="relative">
                            {/* Ponto da timeline */}
                            <div className={`absolute -left-[26px] w-3.5 h-3.5 rounded-full border-2 border-white ${COR_HISTORICO[h.tipo]}`} />
                            <div className="border-2 border-gray-200 bg-white p-3 rounded">
                              <div className="flex items-center justify-between mb-1">
                                <span className="text-xs font-bold text-gray-500">{h.data}</span>
                                <span className="text-xs text-gray-400">por {h.responsavel}</span>
                              </div>
                              <p className="text-sm text-gray-700">{h.descricao}</p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Rodapé do modal */}
            <div className="border-t-2 border-gray-400 p-4 flex gap-3 justify-between flex-shrink-0">
              <div className="flex gap-2">
                <button
                  onClick={() => { abrirModalAcordo(modalDetalhe.paciente!); setModalDetalhe({ aberto: false }); }}
                  className="px-4 py-2 border-2 border-blue-500 bg-white text-blue-600 font-bold hover:bg-blue-50 flex items-center gap-2 text-sm"
                >
                  <RefreshCw className="w-4 h-4" /> Criar Acordo
                </button>
                <button
                  onClick={() => { abrirModalCobranca(modalDetalhe.paciente!); setModalDetalhe({ aberto: false }); }}
                  className="px-4 py-2 border-2 border-orange-400 bg-white text-orange-700 font-bold hover:bg-orange-50 flex items-center gap-2 text-sm"
                >
                  <MessageSquare className="w-4 h-4" /> Registrar Cobrança
                </button>
                <button
                  onClick={() => { irParaPagamento(modalDetalhe.paciente!.id); setModalDetalhe({ aberto: false }); }}
                  className="px-4 py-2 border-2 border-green-500 bg-green-500 text-white font-bold hover:bg-green-600 flex items-center gap-2 text-sm"
                >
                  <ExternalLink className="w-4 h-4" /> Ir para Pagamento
                </button>
              </div>
              <button onClick={() => setModalDetalhe({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50 text-sm">Fechar</button>
            </div>
          </div>
        </div>
      )}


      {/* ══ Modal: Registrar Cobrança ════════════════════════════════════════════ */}
      {modalCobranca.aberto && modalCobranca.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[520px] max-h-[90vh] overflow-y-auto">
            <div className="bg-orange-50 p-4 border-b-2 border-gray-400 flex justify-between items-center">
              <div>
                <h2 className="text-lg font-bold text-orange-800 flex items-center gap-2">
                  <MessageSquare className="w-5 h-5" /> Registrar Tentativa de Cobrança
                </h2>
                <p className="text-sm text-gray-600">{modalCobranca.paciente.nome} — {modalCobranca.paciente.telefone}</p>
              </div>
              <button onClick={() => setModalCobranca({ aberto: false })} className="text-gray-400 hover:text-gray-700 text-xl">✕</button>
            </div>
            <div className="p-6 space-y-4">
              {/* Data */}
              <div>
                <label className="text-sm font-bold mb-2 block">Data da tentativa <span className="text-red-500">*</span></label>
                <input
                  type="date"
                  max={new Date().toISOString().slice(0, 10)}
                  className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${erroCob.data ? "border-red-500" : "border-gray-300 focus:border-orange-400"}`}
                  value={cobData}
                  onChange={(e) => setCobData(e.target.value)}
                />
                {erroCob.data && <p className="text-xs text-red-500 mt-1">{erroCob.data}</p>}
              </div>
              {/* Responsável */}
              <div>
                <label className="text-sm font-bold mb-2 block">Responsável <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${erroCob.responsavel ? "border-red-500" : "border-gray-300 focus:border-orange-400"}`}
                  value={cobResponsavel}
                  onChange={(e) => setCobResponsavel(e.target.value)}
                />
                {erroCob.responsavel && <p className="text-xs text-red-500 mt-1">{erroCob.responsavel}</p>}
              </div>
              {/* Canal de contato */}
              <div>
                <label className="text-sm font-bold mb-2 block">Canal de contato <span className="text-red-500">*</span></label>
                <select
                  className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${erroCob.canal ? "border-red-500" : "border-gray-300 focus:border-orange-400"}`}
                  value={cobCanal}
                  onChange={(e) => setCobCanal(e.target.value as CanalCobranca)}
                >
                  <option value="">Selecione</option>
                  {Object.entries(CANAL_LABEL).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                </select>
                {erroCob.canal && <p className="text-xs text-red-500 mt-1">{erroCob.canal}</p>}
              </div>
              {/* Resultado */}
              <div>
                <label className="text-sm font-bold mb-2 block">Resultado <span className="text-red-500">*</span></label>
                <select
                  className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${erroCob.resultado ? "border-red-500" : "border-gray-300 focus:border-orange-400"}`}
                  value={cobResultado}
                  onChange={(e) => setCobResultado(e.target.value as ResultadoCobranca)}
                >
                  <option value="">Selecione</option>
                  {Object.entries(RESULTADO_LABEL).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                </select>
                {erroCob.resultado && <p className="text-xs text-red-500 mt-1">{erroCob.resultado}</p>}
              </div>
              {/* Observação */}
              <div>
                <label className="text-sm font-bold mb-2 block">Observação (opcional)</label>
                <textarea
                  rows={2}
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-orange-400 focus:outline-none resize-none"
                  placeholder="Ex.: paciente disse que ligaria de volta na segunda-feira..."
                  value={cobObs}
                  onChange={(e) => setCobObs(e.target.value)}
                />
              </div>
            </div>
            <div className="border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalCobranca({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Cancelar</button>
              <button onClick={confirmarCobranca} className="px-4 py-2 border-2 border-orange-500 bg-orange-500 text-white font-bold hover:bg-orange-600">
                Registrar Cobrança
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ══ Modal: Gerar Acordo ══════════════════════════════════════════════════ */}
      {modalAcordo.aberto && modalAcordo.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-blue-500 w-[600px] max-h-[90vh] overflow-y-auto">
            <div className="bg-blue-50 p-4 border-b-2 border-gray-400 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-bold text-blue-800">Criar Acordo Financeiro</h2>
                <p className="text-sm text-gray-600">{modalAcordo.paciente.nome}</p>
              </div>
              <button onClick={() => setModalAcordo({ aberto: false })} className="text-xl text-gray-400 hover:text-gray-700">✕</button>
            </div>
            <div className="p-6 space-y-5">
              <div className="bg-yellow-50 border-2 border-yellow-400 p-3 rounded flex items-start gap-2">
                <AlertTriangle className="w-4 h-4 text-yellow-600 flex-shrink-0 mt-0.5" />
                <p className="text-sm text-yellow-700">
                  <strong>Juros e multas calculados automaticamente pelo sistema.</strong> A recepcionista não pode editar esses valores.
                  Taxa: {CONFIG.multaPercentual}% multa + {CONFIG.jurosDiario}% ao dia.
                </p>
              </div>

              {/* Seleção de parcelas */}
              <div>
                <div className="text-xs font-bold text-gray-500 uppercase mb-3 border-b border-gray-200 pb-1">Parcelas a Incluir no Acordo</div>
                <div className="space-y-2">
                  {modalAcordo.paciente.parcelas.filter((p) => p.status === "VENCIDA" || p.status === "ACORDO_INADIMPLIDO").map((parc) => {
                    const encargos = calcularJuros(parc.valorOriginal, parc.diasAtraso);
                    const isSelecionada = parcelasSelecionadas.includes(parc.id);
                    return (
                      <label key={parc.id} className={`flex items-center justify-between p-3 border-2 rounded cursor-pointer ${isSelecionada ? "border-blue-500 bg-blue-50" : "border-gray-200 bg-white hover:bg-gray-50"}`}>
                        <div className="flex items-center gap-3">
                          <input type="checkbox" checked={isSelecionada}
                            onChange={(e) => { setParcelasSelecionadas((prev) => e.target.checked ? [...prev, parc.id] : prev.filter((id) => id !== parc.id)); setErroAcordo(""); }}
                            className="w-4 h-4"
                          />
                          <div>
                            <div className="font-bold text-sm">Parcela {parc.numero} — Venc. {parc.vencimento}</div>
                            <div className="text-xs text-gray-500">{parc.diasAtraso} dias em atraso</div>
                          </div>
                        </div>
                        <div className="text-right text-sm">
                          <div className="text-gray-600">{formatarMoeda(parc.valorOriginal)}</div>
                          <div className="text-red-600 font-bold">+{formatarMoeda(encargos)} encargos</div>
                        </div>
                      </label>
                    );
                  })}
                </div>
              </div>

              {/* Nº de parcelas */}
              <div>
                <label className="text-sm font-bold mb-2 block">Número de Parcelas do Acordo</label>
                <div className="flex items-center gap-3">
                  <input type="number" min={1} max={24}
                    className="w-24 border-2 border-gray-300 p-2 rounded text-center font-bold focus:border-blue-500 focus:outline-none"
                    value={numeroParcelas}
                    onChange={(e) => { setNumeroParcelas(Number(e.target.value)); setErroAcordo(""); }}
                  />
                  <span className="text-sm text-gray-500">parcelas (máx. 24)</span>
                </div>
              </div>


              {/* Data da primeira parcela */}
              <div>
                <label className="text-sm font-bold mb-2 block">Data da Primeira Parcela <span className="text-red-500">*</span></label>
                <input
                  type="date"
                  className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none"
                  value={dataPrimeiraParcela}
                  onChange={(e) => { setDataPrimeiraParcela(e.target.value); setErroAcordo(""); }}
                />
                <p className="text-xs text-gray-500 mt-1">As demais parcelas serão calculadas mensalmente a partir dessa data.</p>
              </div>

              {/* Justificativa */}
              <div>
                <label className="text-sm font-bold mb-2 block">Justificativa da Negociação <span className="text-red-500">*</span></label>
                <textarea
                  rows={3}
                  className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none resize-none"
                  placeholder="Ex.: paciente solicitou renegociação por dificuldade financeira temporária..."
                  value={justificativaAcordo}
                  onChange={(e) => { setJustificativaAcordo(e.target.value); setErroAcordo(""); }}
                />
              </div>

              {/* Resumo calculado */}
              {parcelasSelecionadas.length > 0 && (
                <div className="bg-gray-50 border-2 border-gray-300 p-4 rounded">
                  <div className="text-xs font-bold text-gray-500 uppercase mb-3">Resumo do Acordo</div>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between"><span className="text-gray-600">Valor original das parcelas</span><span className="font-bold">{formatarMoeda(resumoAcordo.totalOriginal)}</span></div>
                    <div className="flex justify-between"><span className="text-red-600">Multa + Juros acumulados</span><span className="font-bold text-red-600">+{formatarMoeda(resumoAcordo.totalEncargos)}</span></div>
                    <div className="flex justify-between border-t border-gray-300 pt-2"><span className="font-bold">Total a negociar</span><span className="font-bold text-lg">{formatarMoeda(resumoAcordo.totalComEncargos)}</span></div>
                    <div className="flex justify-between border-t border-gray-300 pt-2"><span className="font-bold text-blue-700">{numeroParcelas}x de</span><span className="font-bold text-blue-700 text-lg">{formatarMoeda(resumoAcordo.valorParcela)}</span></div>
                  </div>
                </div>
              )}

              <div className="bg-orange-50 border border-orange-300 p-3 rounded text-xs text-orange-700">
                <strong>Importante:</strong> Ao confirmar, as parcelas originais serão marcadas como "Substituídas". Caso o paciente não cumpra o acordo, as multas retroativas originais voltarão a valer. Para receber pagamento, use o botão "Ir para Pagamento" na F17.
              </div>

              {erroAcordo && <div className="border-2 border-red-500 bg-red-50 p-3 text-red-700 text-sm font-bold">{erroAcordo}</div>}
            </div>
            <div className="sticky bottom-0 bg-white border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalAcordo({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Cancelar</button>
              <button onClick={confirmarAcordo} className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600">Confirmar Acordo</button>
            </div>
          </div>
        </div>
      )}



      {/* ══ Modal: Cancelar Acordo ══════════════════════════════════════════════ */}
      {modalCancelarAcordo.aberto && modalCancelarAcordo.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-gray-500 w-[500px]">
            <div className="bg-gray-50 p-4 border-b-2 border-gray-400 flex items-center gap-2">
              <Ban className="w-5 h-5 text-gray-700" />
              <h2 className="text-lg font-bold text-gray-800">Cancelar Acordo</h2>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-sm text-gray-600">
                Informe a justificativa obrigatória para cancelar o acordo ativo de <strong>{modalCancelarAcordo.paciente.nome}</strong>.
              </p>
              <textarea
                rows={4}
                className="w-full border-2 border-gray-300 p-3 rounded resize-none focus:border-gray-500 focus:outline-none"
                placeholder="Ex.: paciente desistiu da negociação e solicitou nova análise..."
                value={justCancelamentoAcordo}
                onChange={(e) => setJustCancelamentoAcordo(e.target.value)}
              />
              {!justCancelamentoAcordo.trim() && (
                <p className="text-xs text-red-500 font-bold">A justificativa é obrigatória para cancelar o acordo.</p>
              )}
            </div>
            <div className="border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalCancelarAcordo({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Voltar</button>
              <button
                onClick={confirmarCancelamentoAcordo}
                disabled={!justCancelamentoAcordo.trim()}
                className="px-4 py-2 border-2 border-gray-600 bg-gray-600 text-white font-bold hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Confirmar Cancelamento
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Registrar Inadimplência do Acordo ════════════════════════════ */}
      {modalInadimplencia.aberto && modalInadimplencia.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-red-500 w-[500px]">
            <div className="bg-red-50 p-4 border-b-2 border-gray-400 flex items-center gap-2">
              <XCircle className="w-5 h-5 text-red-600" />
              <h2 className="text-lg font-bold text-red-800">Registrar Inadimplência do Acordo</h2>
            </div>
            <div className="p-6 space-y-4">
              <div className="bg-red-50 border-2 border-red-300 p-3 rounded text-sm text-red-800">
                <strong>Atenção:</strong> Ao registrar a inadimplência do acordo de <strong>{modalInadimplencia.paciente.nome}</strong>, as parcelas originais (com multas retroativas) voltarão a valer e o paciente permanecerá com status Restrito.
              </div>
              <div>
                <label className="text-sm font-bold mb-2 block">Justificativa <span className="text-red-500">*</span></label>
                <textarea
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-red-500 focus:outline-none resize-none"
                  rows={3}
                  placeholder="Descreva o motivo do registro de inadimplência..."
                  value={justInadimplencia}
                  onChange={(e) => setJustInadimplencia(e.target.value)}
                />
              </div>
              <div className="flex gap-3">
                <button onClick={() => setModalInadimplencia({ aberto: false })} className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold">Cancelar</button>
                <button
                  onClick={registrarInadimplencia}
                  disabled={!justInadimplencia.trim()}
                  className="flex-1 px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Confirmar Inadimplência
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
