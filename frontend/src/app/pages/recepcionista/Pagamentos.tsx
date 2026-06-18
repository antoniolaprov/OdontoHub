import { useState, useMemo, useEffect } from "react";
import { useLocation } from "react-router";
import {
  CreditCard, Search, CheckCircle, XCircle, Clock,
  AlertTriangle, DollarSign, FileText, Ban,
} from "lucide-react";

// ─── Types ────────────────────────────────────────────────────────────────────

type StatusPagamento = "CONFIRMADO" | "AGUARDANDO" | "CANCELADO";

type FormaPagamento =
  | "PIX"
  | "CARTAO_CREDITO"
  | "CARTAO_DEBITO"
  | "DINHEIRO"
  | "TRANSFERENCIA";

interface RegistroPagamento {
  id: number;
  pacienteId: number;
  pacienteNome: string;
  parcelaId: number;
  parcelaNumero: number;
  valorPago: number;
  formaPagamento: FormaPagamento;
  dataPagamento: string; // DD/MM/AAAA
  observacao?: string;
  status: StatusPagamento;
  motivoCancelamento?: string;
  registradoPor: string;
  dataRegistro: string; // DD/MM/AAAA — usado para cálculo de alerta >2 dias
}

type StatusParcela = "PENDENTE" | "PAGA" | "PARCIALMENTE_PAGA" | "VENCIDA" | "SUBSTITUIDA" | "ACORDO_INADIMPLIDO";
type StatusAcordo = "ATIVO" | "QUITADO" | "INADIMPLIDO" | "CANCELADO";

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
  novasParcelas: Parcela[];
  status: StatusAcordo;
}

interface Paciente {
  id: number;
  nome: string;
  cpf: string;
  status: "ATIVO" | "RESTRITO" | "INATIVO";
  parcelas: Parcela[];
  acordos: Acordo[];
}

// ─── Mock de dados locais ─────────────────────────────────────────────────────
const CONFIG_JUROS = { multaPercentual: 2, jurosDiario: 0.033 };

const MOCK_PACIENTES_LOCAL: Paciente[] = [
  {
    id: 1, nome: "Maria Santos", cpf: "123.456.789-00", status: "RESTRITO",
    parcelas: [
      { id: 101, numero: 1, total: 600, vencimento: "10/04/2026", status: "VENCIDA", valorOriginal: 600, multa: 12, juros: 8.91, diasAtraso: 45 },
      { id: 102, numero: 2, total: 600, vencimento: "10/05/2026", status: "VENCIDA", valorOriginal: 600, multa: 12, juros: 3.96, diasAtraso: 15 },
      { id: 103, numero: 3, total: 600, vencimento: "10/06/2026", status: "PENDENTE", valorOriginal: 600, multa: 0, juros: 0, diasAtraso: 0 },
    ],
    acordos: [],
  },
  {
    id: 2, nome: "José Ferreira", cpf: "234.567.890-11", status: "RESTRITO",
    parcelas: [
      { id: 201, numero: 1, total: 850, vencimento: "01/05/2026", status: "VENCIDA", valorOriginal: 850, multa: 17, juros: 10.54, diasAtraso: 38 },
      { id: 202, numero: 2, total: 850, vencimento: "01/06/2026", status: "VENCIDA", valorOriginal: 850, multa: 17, juros: 2.81, diasAtraso: 8 },
    ],
    acordos: [],
  },
  {
    id: 3, nome: "Cláudia Dias", cpf: "345.678.901-22", status: "ATIVO",
    parcelas: [
      { id: 301, numero: 1, total: 400, vencimento: "28/05/2026", status: "VENCIDA", valorOriginal: 400, multa: 8, juros: 1.58, diasAtraso: 12 },
    ],
    acordos: [],
  },
  {
    id: 4, nome: "Roberto Lima", cpf: "456.789.012-33", status: "RESTRITO",
    parcelas: [
      { id: 501, numero: 1, total: 842.14, vencimento: "09/06/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0, acordoId: 1 },
      { id: 502, numero: 2, total: 842.14, vencimento: "09/07/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0, acordoId: 1 },
      { id: 503, numero: 3, total: 842.14, vencimento: "09/08/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0, acordoId: 1 },
    ],
    acordos: [{ id: 1, novasParcelas: [{ id: 501, numero: 1, total: 842.14, vencimento: "09/06/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0 }, { id: 502, numero: 2, total: 842.14, vencimento: "09/07/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0 }, { id: 503, numero: 3, total: 842.14, vencimento: "09/08/2026", status: "PENDENTE", valorOriginal: 842.14, multa: 0, juros: 0, diasAtraso: 0 }], status: "ATIVO" }],
  },
];

const MOCK_REGISTROS_INICIAIS: RegistroPagamento[] = [
  {
    id: 1, pacienteId: 1, pacienteNome: "Maria Santos", parcelaId: 101, parcelaNumero: 1,
    valorPago: 620.91, formaPagamento: "DINHEIRO", dataPagamento: "10/06/2026",
    status: "CONFIRMADO", registradoPor: "Recepcionista Ana", dataRegistro: "10/06/2026",
  },
  {
    id: 2, pacienteId: 3, pacienteNome: "Cláudia Dias", parcelaId: 301, parcelaNumero: 1,
    valorPago: 409.58, formaPagamento: "PIX", dataPagamento: "09/06/2026",
    status: "CONFIRMADO", registradoPor: "Recepcionista Ana", dataRegistro: "09/06/2026",
  },
  {
    id: 3, pacienteId: 2, pacienteNome: "José Ferreira", parcelaId: 201, parcelaNumero: 1,
    valorPago: 0, formaPagamento: "TRANSFERENCIA", dataPagamento: "",
    status: "AGUARDANDO", observacao: "Transferência enviada pelo WhatsApp, aguardando confirmação.",
    registradoPor: "Recepcionista Ana", dataRegistro: "07/06/2026",
  },
  {
    id: 4, pacienteId: 4, pacienteNome: "Roberto Lima", parcelaId: 501, parcelaNumero: 1,
    valorPago: 0, formaPagamento: "CARTAO_CREDITO", dataPagamento: "",
    status: "CANCELADO", motivoCancelamento: "Cliente solicitou reagendamento do pagamento.",
    registradoPor: "Recepcionista Ana", dataRegistro: "07/06/2026",
  },
];


const PACIENTES_STORAGE_KEY = "odontohub_financeiro_pacientes";
const REGISTROS_STORAGE_KEY = "odontohub_financeiro_registros";

function carregarPacientes(): Paciente[] {
  if (typeof window === "undefined") return MOCK_PACIENTES_LOCAL;
  try {
    const raw = window.localStorage.getItem(PACIENTES_STORAGE_KEY);
    return raw ? JSON.parse(raw) as Paciente[] : MOCK_PACIENTES_LOCAL;
  } catch {
    return MOCK_PACIENTES_LOCAL;
  }
}

function salvarPacientesStorage(pacientes: Paciente[]) {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(PACIENTES_STORAGE_KEY, JSON.stringify(pacientes));
}

function carregarRegistros(): RegistroPagamento[] {
  if (typeof window === "undefined") return MOCK_REGISTROS_INICIAIS;
  try {
    const raw = window.localStorage.getItem(REGISTROS_STORAGE_KEY);
    return raw ? JSON.parse(raw) as RegistroPagamento[] : MOCK_REGISTROS_INICIAIS;
  } catch {
    return MOCK_REGISTROS_INICIAIS;
  }
}

function salvarRegistrosStorage(registros: RegistroPagamento[]) {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(REGISTROS_STORAGE_KEY, JSON.stringify(registros));
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatarMoeda(v: number): string {
  return v.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function calcularEncargos(valorOriginal: number, diasAtraso: number): number {
  const multa = (CONFIG_JUROS.multaPercentual / 100) * valorOriginal;
  const juros = valorOriginal * (CONFIG_JUROS.jurosDiario / 100) * diasAtraso;
  return parseFloat((multa + juros).toFixed(2));
}

function hojeStr(): string {
  return new Date().toLocaleDateString("pt-BR");
}

// Retorna quantos dias se passaram desde uma data DD/MM/AAAA
function diasDesde(ddmmyyyy: string): number {
  const p = ddmmyyyy.split("/");
  if (p.length !== 3) return 0;
  const d = new Date(parseInt(p[2]), parseInt(p[1]) - 1, parseInt(p[0]));
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  d.setHours(0, 0, 0, 0);
  return Math.max(0, Math.floor((hoje.getTime() - d.getTime()) / (1000 * 60 * 60 * 24)));
}

// Converte date input YYYY-MM-DD para DD/MM/AAAA
function isoParaBR(iso: string): string {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
}

// Converte DD/MM/AAAA para YYYY-MM-DD para input[type=date]
function brParaISO(br: string): string {
  if (!br) return "";
  const [d, m, y] = br.split("/");
  return `${y}-${m}-${d}`;
}

const FORMA_LABEL: Record<FormaPagamento, string> = {
  PIX: "PIX",
  CARTAO_CREDITO: "Cartão de Crédito",
  CARTAO_DEBITO: "Cartão de Débito",
  DINHEIRO: "Dinheiro",
  TRANSFERENCIA: "Transferência",
};

// ─── Sub-componentes de badge ─────────────────────────────────────────────────

function BadgeStatusPagamento({ status }: { status: StatusPagamento }) {
  const map: Record<StatusPagamento, { label: string; cls: string }> = {
    CONFIRMADO: { label: "Confirmado",            cls: "bg-green-100 border-green-500 text-green-700" },
    AGUARDANDO: { label: "Aguardando Comprovante", cls: "bg-yellow-100 border-yellow-500 text-yellow-700" },
    CANCELADO:  { label: "Cancelado",             cls: "bg-gray-100 border-gray-400 text-gray-500" },
  };
  const { label, cls } = map[status];
  return <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${cls}`}>{label}</span>;
}

// ─── Estado inicial de formulário ────────────────────────────────────────────
const FORM_PAG_VAZIO = { valor: "", data: "", forma: "" as FormaPagamento | "", obs: "" };
const FORM_AGU_VAZIO = { forma: "" as FormaPagamento | "", obs: "" };

// ─── Componente Principal ─────────────────────────────────────────────────────

export default function RecepcionistaPagamentos() {
  const location = useLocation();
  const navigationState = location.state as { pacienteId?: number; parcelaId?: number } | null;
  const [registros, setRegistros] = useState<RegistroPagamento[]>(carregarRegistros);
  const [pacientes, setPacientes] = useState<Paciente[]>(carregarPacientes);
  const [stateNavegacaoProcessado, setStateNavegacaoProcessado] = useState(false);

  const [toast, setToast] = useState<{ msg: string; tipo: "sucesso" | "erro" | "aviso" } | null>(null);

  // ─ Filtros ──────────────────────────────────────────────────────────────
  const [busca, setBusca] = useState("");
  const [filtroStatus, setFiltroStatus] = useState("");
  const [filtroForma, setFiltroForma] = useState("");

  // ─ Modal 1: Registrar Pagamento ──────────────────────────────────────────
  const [modalReg, setModalReg] = useState<{ aberto: boolean; pacienteId?: number; parcelaId?: number }>({ aberto: false });
  const [formReg, setFormReg] = useState({ ...FORM_PAG_VAZIO });
  const [errosReg, setErrosReg] = useState<Record<string, string>>({});
  const [pacSel, setPacSel] = useState<number | "">("");
  const [parcSel, setParcSel] = useState<number | "">("");

  // ─ Modal 2: Aguardar Comprovante ─────────────────────────────────────────
  const [modalAgu, setModalAgu] = useState<{ aberto: boolean; pacienteId?: number; parcelaId?: number }>({ aberto: false });
  const [formAgu, setFormAgu] = useState({ ...FORM_AGU_VAZIO });
  const [erroAgu, setErroAgu] = useState("");
  const [pacAguSel, setPacAguSel] = useState<number | "">("");
  const [parcAguSel, setParcAguSel] = useState<number | "">("");

  // ─ Modal 3: Confirmar Comprovante ────────────────────────────────────────
  const [modalConf, setModalConf] = useState<{ aberto: boolean; registro?: RegistroPagamento }>({ aberto: false });
  const [formConf, setFormConf] = useState({ ...FORM_PAG_VAZIO });
  const [errosConf, setErrosConf] = useState<Record<string, string>>({});

  // ─ Modal 4: Cancelar Lançamento ──────────────────────────────────────────
  const [modalCanc, setModalCanc] = useState<{ aberto: boolean; registro?: RegistroPagamento }>({ aberto: false });
  const [motivoCanc, setMotivoCanc] = useState("");

  // ─ Modal 5: Comprovante ──────────────────────────────────────────────────
  const [modalComp, setModalComp] = useState<{ aberto: boolean; registro?: RegistroPagamento }>({ aberto: false });

  // ─ Derived ──────────────────────────────────────────────────────────────

  const hoje = hojeStr();


  useEffect(() => {
    salvarRegistrosStorage(registros);
  }, [registros]);

  useEffect(() => {
    if (stateNavegacaoProcessado) return;
    if (!navigationState?.pacienteId) return;
    const pacienteExiste = pacientes.some((p) => p.id === navigationState.pacienteId);
    if (!pacienteExiste) return;

    const parcelaId = navigationState.parcelaId;
    const parcelaExiste = parcelaId
      ? pacientes.some((p) => p.id === navigationState.pacienteId && p.parcelas.some((parcela) => parcela.id === parcelaId))
      : true;

    setPacSel(navigationState.pacienteId);
    setParcSel(parcelaExiste && parcelaId ? parcelaId : "");
    abrirModalRegistrar(navigationState.pacienteId, parcelaExiste ? parcelaId : undefined);
    setStateNavegacaoProcessado(true);
    showToast("Dados vindos da F9 pré-selecionados para pagamento.", "aviso");
  }, [navigationState, pacientes, stateNavegacaoProcessado]);

  const registrosFiltrados = useMemo(() => {
    const q = busca.toLowerCase();
    return registros.filter((r) => {
      if (busca && !r.pacienteNome.toLowerCase().includes(q)) {
        // também busca por CPF do paciente
        const pac = pacientes.find((p) => p.id === r.pacienteId);
        if (!pac?.cpf.includes(busca)) return false;
      }
      if (filtroStatus && r.status !== filtroStatus) return false;
      if (filtroForma && r.formaPagamento !== filtroForma) return false;
      return true;
    });
  }, [registros, busca, filtroStatus, filtroForma, pacientes]);

  const resumo = useMemo(() => {
    const pagamentosHoje = registros.filter((r) => r.status === "CONFIRMADO" && r.dataPagamento === hoje).length;
    const aguardando = registros.filter((r) => r.status === "AGUARDANDO").length;
    const totalHoje = registros.filter((r) => r.status === "CONFIRMADO" && r.dataPagamento === hoje).reduce((acc, r) => acc + r.valorPago, 0);
    const parcelasEmAberto = pacientes.reduce((acc, p) => acc + p.parcelas.filter((parc) => parc.status === "VENCIDA" || parc.status === "PENDENTE" || parc.status === "PARCIALMENTE_PAGA").length, 0);
    return { pagamentosHoje, aguardando, totalHoje, parcelasEmAberto };
  }, [registros, pacientes, hoje]);

  function showToast(msg: string, tipo: "sucesso" | "erro" | "aviso") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 4000);
  }

  // ─ Helpers de parcela ────────────────────────────────────────────────────

  function getParcela(pacienteId: number, parcelaId: number): Parcela | undefined {
    return pacientes.find((p) => p.id === pacienteId)?.parcelas.find((parc) => parc.id === parcelaId);
  }

  function getValorDevido(parc: Parcela): number {
    const encargos = parc.diasAtraso > 0 ? calcularEncargos(parc.valorOriginal, parc.diasAtraso) : 0;
    return parseFloat((parc.valorOriginal + encargos).toFixed(2));
  }

  function parcelasElegiveis(pacienteId: number): Parcela[] {
    return pacientes.find((p) => p.id === pacienteId)?.parcelas.filter(
      (parc) => parc.status === "VENCIDA" || parc.status === "PENDENTE" || parc.status === "PARCIALMENTE_PAGA"
    ) ?? [];
  }

  // Aplica os efeitos de pagamento no array de pacientes (atualiza parcela, acordo, status do paciente)
  function aplicarPagamento(pacienteId: number, parcelaId: number, valorPago: number, dataPagamento: string) {
    setPacientes((prev) => {
      const atualizados = prev.map((p) => {
        if (p.id !== pacienteId) return p;
        const valorDevido = getValorDevido(p.parcelas.find((x) => x.id === parcelaId)!);
        const novoStatus: StatusParcela = valorPago >= valorDevido ? "PAGA" : "PARCIALMENTE_PAGA";
        const saldoRestante = parseFloat(Math.max(0, valorDevido - valorPago).toFixed(2));

        const novasParcelas = p.parcelas.map((parc) =>
          parc.id === parcelaId
            ? { ...parc, status: novoStatus, dataPagamento, total: saldoRestante, valorOriginal: saldoRestante }
            : parc
        );

        // Atualiza também as parcelas internas do acordo para manter F9 e F17 sincronizadas.
        const acordosAtualizados = p.acordos.map((a) => {
          const novasParcelasAcordo = a.novasParcelas.map((np) => {
            const atualizada = novasParcelas.find((x) => x.id === np.id);
            return atualizada ? { ...np, ...atualizada } : np;
          });
          if (a.status !== "ATIVO") return { ...a, novasParcelas: novasParcelasAcordo };
          const todasPagas = novasParcelasAcordo.every((np) => np.status === "PAGA");
          return todasPagas ? { ...a, novasParcelas: novasParcelasAcordo, status: "QUITADO" as StatusAcordo } : { ...a, novasParcelas: novasParcelasAcordo };
        });

        // Recalcular status do paciente: se não houver mais parcelas vencidas, ATIVO
        const aindaTemVencidas = novasParcelas.some((parc) => parc.status === "VENCIDA" || parc.status === "ACORDO_INADIMPLIDO");
        const novoStatusPaciente = aindaTemVencidas ? p.status : "ATIVO";

        return { ...p, parcelas: novasParcelas, acordos: acordosAtualizados, status: novoStatusPaciente };
      });
      salvarPacientesStorage(atualizados);
      return atualizados;
    });
  }

  // ─ Modal 1: Abrir e confirmar pagamento ──────────────────────────────────

  function abrirModalRegistrar(pacienteId?: number, parcelaId?: number) {
    setPacSel(pacienteId ?? "");
    setParcSel(parcelaId ?? "");
    setFormReg({ ...FORM_PAG_VAZIO, data: new Date().toISOString().slice(0, 10) });
    setErrosReg({});
    setModalReg({ aberto: true, pacienteId, parcelaId });
  }

  function confirmarRegistro() {
    const erros: Record<string, string> = {};
    const pacId = modalReg.pacienteId ?? (pacSel !== "" ? Number(pacSel) : undefined);
    const parcId = modalReg.parcelaId ?? (parcSel !== "" ? Number(parcSel) : undefined);
    if (!pacId) erros.paciente = "Selecione um paciente.";
    if (!parcId) erros.parcela = "Selecione uma parcela.";
    if (!formReg.valor || parseFloat(formReg.valor) <= 0) erros.valor = "Informe um valor maior que zero.";
    if (!formReg.data) erros.data = "Informe a data do pagamento.";
    else {
      const dataInformada = new Date(formReg.data + "T00:00:00");
      if (dataInformada > new Date()) erros.data = "A data não pode ser futura.";
    }
    if (!formReg.forma) erros.forma = "Selecione a forma de pagamento.";
    setErrosReg(erros);
    if (Object.keys(erros).length > 0) return;

    const valorPago = parseFloat(formReg.valor);
    const dataBR = isoParaBR(formReg.data);

    // TODO: integrar com Financeiro.tsx — adicionar entrada no fluxo de caixa
    const novoRegistro: RegistroPagamento = {
      id: Date.now(),
      pacienteId: pacId!,
      pacienteNome: pacientes.find((p) => p.id === pacId)?.nome ?? "",
      parcelaId: parcId!,
      parcelaNumero: getParcela(pacId!, parcId!)?.numero ?? 0,
      valorPago,
      formaPagamento: formReg.forma as FormaPagamento,
      dataPagamento: dataBR,
      observacao: formReg.obs,
      status: "CONFIRMADO",
      registradoPor: "Recepcionista Ana",
      dataRegistro: hoje,
    };
    setRegistros((prev) => [novoRegistro, ...prev]);
    aplicarPagamento(pacId!, parcId!, valorPago, dataBR);
    setModalReg({ aberto: false });
    showToast(`✓ Pagamento de ${formatarMoeda(valorPago)} registrado com sucesso.`, "sucesso");
  }

  // ─ Modal 2: Lançar como aguardando ───────────────────────────────────────

  function abrirModalAguardar(pacienteId?: number, parcelaId?: number) {
    setPacAguSel(pacienteId ?? "");
    setParcAguSel(parcelaId ?? "");
    setFormAgu({ ...FORM_AGU_VAZIO });
    setErroAgu("");
    setModalAgu({ aberto: true, pacienteId, parcelaId });
  }

  function confirmarAguardar() {
    const pacId = modalAgu.pacienteId ?? (pacAguSel !== "" ? Number(pacAguSel) : undefined);
    const parcId = modalAgu.parcelaId ?? (parcAguSel !== "" ? Number(parcAguSel) : undefined);
    if (!pacId || !parcId) { setErroAgu("Selecione paciente e parcela."); return; }
    if (!formAgu.forma) { setErroAgu("Selecione a forma de pagamento esperada."); return; }

    // Validar: não pode existir AGUARDANDO para a mesma parcela
    const jaAguardando = registros.some((r) => r.parcelaId === parcId && r.status === "AGUARDANDO");
    if (jaAguardando) {
      setErroAgu("Já existe um pagamento aguardando confirmação para esta parcela.");
      return;
    }

    const novoRegistro: RegistroPagamento = {
      id: Date.now(),
      pacienteId: pacId,
      pacienteNome: pacientes.find((p) => p.id === pacId)?.nome ?? "",
      parcelaId: parcId,
      parcelaNumero: getParcela(pacId, parcId)?.numero ?? 0,
      valorPago: 0,
      formaPagamento: formAgu.forma as FormaPagamento,
      dataPagamento: "",
      observacao: formAgu.obs,
      status: "AGUARDANDO",
      registradoPor: "Recepcionista Ana",
      dataRegistro: hoje,
    };
    setRegistros((prev) => [novoRegistro, ...prev]);
    setModalAgu({ aberto: false });
    showToast("⚠ Pagamento lançado como Aguardando Comprovante.", "aviso");
  }

  // ─ Modal 3: Confirmar comprovante ────────────────────────────────────────

  function abrirModalConfirmar(registro: RegistroPagamento) {
    setFormConf({ valor: "", data: new Date().toISOString().slice(0, 10), forma: registro.formaPagamento, obs: "" });
    setErrosConf({});
    setModalConf({ aberto: true, registro });
  }

  function confirmarComprovante() {
    const reg = modalConf.registro!;
    const erros: Record<string, string> = {};
    if (!formConf.valor || parseFloat(formConf.valor) <= 0) erros.valor = "Informe um valor maior que zero.";
    if (!formConf.data) erros.data = "Informe a data do pagamento.";
    else if (new Date(formConf.data + "T00:00:00") > new Date()) erros.data = "A data não pode ser futura.";
    setErrosConf(erros);
    if (Object.keys(erros).length > 0) return;

    const valorPago = parseFloat(formConf.valor);
    const dataBR = isoParaBR(formConf.data);

    setRegistros((prev) =>
      prev.map((r) =>
        r.id === reg.id
          ? { ...r, status: "CONFIRMADO", valorPago, dataPagamento: dataBR, observacao: formConf.obs || r.observacao }
          : r
      )
    );
    aplicarPagamento(reg.pacienteId, reg.parcelaId, valorPago, dataBR);
    setModalConf({ aberto: false });
    showToast(`✓ Pagamento de ${formatarMoeda(valorPago)} confirmado com sucesso.`, "sucesso");
  }

  // ─ Modal 4: Cancelar lançamento ──────────────────────────────────────────

  function confirmarCancelamento() {
    if (!motivoCanc.trim()) return;
    const reg = modalCanc.registro!;
    setRegistros((prev) =>
      prev.map((r) =>
        r.id === reg.id ? { ...r, status: "CANCELADO", motivoCancelamento: motivoCanc } : r
      )
    );
    setModalCanc({ aberto: false });
    setMotivoCanc("");
    showToast("✕ Lançamento cancelado.", "erro");
  }

  // ─ Render helpers ────────────────────────────────────────────────────────

  // Bloco de aviso de pagamento parcial (Modais 1 e 3)
  function AvisoParcial({ valorStr, pacienteId, parcelaId }: { valorStr: string; pacienteId?: number; parcelaId?: number }) {
    if (!valorStr || !pacienteId || !parcelaId) return null;
    const valor = parseFloat(valorStr);
    if (isNaN(valor) || valor <= 0) return null;
    const parc = getParcela(pacienteId, parcelaId);
    if (!parc) return null;
    const devido = getValorDevido(parc);
    if (valor >= devido) return null;
    const saldo = parseFloat((devido - valor).toFixed(2));
    return (
      <div className="border-2 border-yellow-400 bg-yellow-50 p-3 text-sm text-yellow-800 flex items-start gap-2">
        <AlertTriangle className="w-4 h-4 flex-shrink-0 mt-0.5" />
        <span>Valor inferior ao devido. A parcela ficará como Parcialmente Paga e o saldo de <strong>{formatarMoeda(saldo)}</strong> permanecerá em aberto.</span>
      </div>
    );
  }

  // ─ Render ────────────────────────────────────────────────────────────────

  return (
    <div className="p-6 bg-gray-50 min-h-screen">

      {/* Toast */}
      {toast && (
        <div className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold text-sm shadow-lg max-w-sm ${toast.tipo === "sucesso" ? "bg-green-50 border-green-500 text-green-700" : toast.tipo === "aviso" ? "bg-yellow-50 border-yellow-500 text-yellow-700" : "bg-red-50 border-red-500 text-red-700"}`}>
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="flex justify-between items-start mb-6">
        <div>
          <h1 className="text-xl font-bold text-gray-700 flex items-center gap-2">
            <CreditCard className="w-6 h-6" />
            Pagamentos
          </h1>
          <p className="text-xs text-gray-500 mt-1">Registre e gerencie os pagamentos de parcelas dos pacientes.</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => abrirModalAguardar()}
            className="px-4 py-2 border-2 border-yellow-400 bg-yellow-400 text-white font-bold hover:bg-yellow-500 text-sm"
          >
            Aguardar Comprovante
          </button>
          <button
            onClick={() => abrirModalRegistrar()}
            className="px-4 py-2 border-2 border-green-500 bg-green-500 text-white font-bold hover:bg-green-600 text-sm"
          >
            + Novo Pagamento
          </button>
        </div>
      </div>

      {/* Cards de Resumo */}
      <div className="grid grid-cols-4 gap-3 mb-6">
        <div className="border-2 border-green-500 bg-green-50 p-4">
          <div className="text-2xl font-bold text-green-700">{resumo.pagamentosHoje}</div>
          <div className="text-xs text-gray-500 mt-1">Pagamentos Hoje</div>
        </div>
        <div className="border-2 border-yellow-500 bg-yellow-50 p-4">
          <div className="text-2xl font-bold text-yellow-700">{resumo.aguardando}</div>
          <div className="text-xs text-gray-500 mt-1">Aguardando Comprovante</div>
        </div>
        <div className="border-2 border-blue-500 bg-blue-50 p-4">
          <div className="text-2xl font-bold text-blue-700">{formatarMoeda(resumo.totalHoje)}</div>
          <div className="text-xs text-gray-500 mt-1">Total Recebido Hoje</div>
        </div>
        <div className="border-2 border-red-500 bg-red-50 p-4">
          <div className="text-2xl font-bold text-red-700">{resumo.parcelasEmAberto}</div>
          <div className="text-xs text-gray-500 mt-1">Parcelas em Aberto</div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white border-2 border-gray-400 p-4 mb-4">
        <div className="grid grid-cols-3 gap-4">
          <div className="relative">
            <label className="text-sm font-bold mb-2 block">Buscar paciente</label>
            <Search className="absolute left-2 top-9 w-4 h-4 text-gray-400" />
            <input
              type="text"
              className="w-full border-2 border-gray-300 p-2 pl-8 bg-white rounded focus:border-blue-500 focus:outline-none"
              placeholder="Nome ou CPF"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm font-bold mb-2 block">Status do lançamento</label>
            <select
              className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
              value={filtroStatus}
              onChange={(e) => setFiltroStatus(e.target.value)}
            >
              <option value="">Todos</option>
              <option value="CONFIRMADO">Confirmado</option>
              <option value="AGUARDANDO">Aguardando Comprovante</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>
          <div>
            <label className="text-sm font-bold mb-2 block">Forma de pagamento</label>
            <select
              className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
              value={filtroForma}
              onChange={(e) => setFiltroForma(e.target.value)}
            >
              <option value="">Todas</option>
              <option value="PIX">PIX</option>
              <option value="CARTAO_CREDITO">Cartão de Crédito</option>
              <option value="CARTAO_DEBITO">Cartão de Débito</option>
              <option value="DINHEIRO">Dinheiro</option>
              <option value="TRANSFERENCIA">Transferência</option>
            </select>
          </div>
        </div>
      </div>

      {/* Tabela de lançamentos */}
      <div className="border-2 border-gray-400 bg-white overflow-x-auto">
        <div className="min-w-[1000px]">
          <div className="grid grid-cols-7 bg-gray-100 border-b-2 border-gray-400 text-sm font-bold">
            <div className="p-3 border-r-2 border-gray-400 col-span-2">Paciente</div>
            <div className="p-3 border-r-2 border-gray-400">Parcela</div>
            <div className="p-3 border-r-2 border-gray-400">Valor Pago</div>
            <div className="p-3 border-r-2 border-gray-400">Forma / Data</div>
            <div className="p-3 border-r-2 border-gray-400">Status</div>
            <div className="p-3 text-center">Ações</div>
          </div>

          {registrosFiltrados.length === 0 && (
            <div className="p-10 text-center text-gray-500">Nenhum lançamento encontrado.</div>
          )}

          {registrosFiltrados.map((reg) => {
            const diasAguardando = reg.status === "AGUARDANDO" ? diasDesde(reg.dataRegistro) : 0;
            const alertaAtraso = diasAguardando > 2;

            return (
              <div key={reg.id} className="grid grid-cols-7 border-b-2 border-gray-400 items-center text-sm hover:bg-gray-50">
                {/* Paciente */}
                <div className="p-3 border-r-2 border-gray-400 col-span-2">
                  <div className="font-bold text-gray-800">{reg.pacienteNome}</div>
                  <div className="text-xs text-gray-400">Registrado em {reg.dataRegistro} por {reg.registradoPor}</div>
                </div>
                {/* Parcela */}
                <div className="p-3 border-r-2 border-gray-400">
                  <div className="font-bold">Parcela {reg.parcelaNumero}</div>
                  <div className="text-xs text-gray-400">ID #{reg.parcelaId}</div>
                </div>
                {/* Valor */}
                <div className="p-3 border-r-2 border-gray-400">
                  {reg.valorPago > 0
                    ? <span className="font-bold text-green-700">{formatarMoeda(reg.valorPago)}</span>
                    : <span className="text-gray-400 text-xs">—</span>
                  }
                </div>
                {/* Forma / Data */}
                <div className="p-3 border-r-2 border-gray-400 text-xs text-gray-600">
                  <div className="font-bold">{FORMA_LABEL[reg.formaPagamento]}</div>
                  <div>{reg.dataPagamento || <span className="text-gray-400">Sem data</span>}</div>
                </div>
                {/* Status */}
                <div className="p-3 border-r-2 border-gray-400">
                  <BadgeStatusPagamento status={reg.status} />
                  {/* Alerta: AGUARDANDO há mais de 2 dias */}
                  {alertaAtraso && (
                    <div className="flex items-center gap-1 mt-1 text-xs text-red-600">
                      <AlertTriangle className="w-3 h-3" /> Aguardando há {diasAguardando} dias
                    </div>
                  )}
                </div>
                {/* Ações */}
                <div className="p-3 flex items-center justify-center gap-2">
                  {reg.status === "AGUARDANDO" && (
                    <>
                      <button
                        onClick={() => abrirModalConfirmar(reg)}
                        className="px-2 py-1 border-2 border-green-500 bg-green-500 text-white text-xs font-bold hover:bg-green-600"
                      >
                        Confirmar
                      </button>
                      <button
                        onClick={() => { setModalCanc({ aberto: true, registro: reg }); setMotivoCanc(""); }}
                        className="px-2 py-1 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-50"
                      >
                        Cancelar
                      </button>
                    </>
                  )}
                  {reg.status === "CONFIRMADO" && (
                    <button
                      onClick={() => setModalComp({ aberto: true, registro: reg })}
                      className="px-2 py-1 border-2 border-blue-400 bg-white text-blue-600 text-xs font-bold hover:bg-blue-50"
                    >
                      Comprovante
                    </button>
                  )}
                  {reg.status === "CANCELADO" && (
                    <span className="text-xs text-gray-400 italic">Cancelado</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="mt-3 text-xs text-gray-500">
        {registrosFiltrados.length} lançamento(s) exibido(s)
      </div>


      {/* ══ Modal 1: Registrar Pagamento ══════════════════════════════════════ */}
      {modalReg.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-green-500 w-[600px] max-h-[90vh] overflow-y-auto">
            <div className="bg-green-50 p-4 border-b-2 border-gray-400 flex justify-between items-center">
              <div>
                <h2 className="text-lg font-bold text-green-800">Registrar Pagamento</h2>
                {modalReg.pacienteId
                  ? <p className="text-sm text-gray-600">{pacientes.find((p) => p.id === modalReg.pacienteId)?.nome}</p>
                  : <p className="text-sm text-gray-500">Selecione o paciente e a parcela</p>
                }
              </div>
              <button onClick={() => setModalReg({ aberto: false })} className="text-gray-400 hover:text-gray-700 text-xl">✕</button>
            </div>

            <div className="p-6 space-y-5">
              {/* Seção 1: Parcela — seleção quando aberto do header, somente leitura quando pré-selecionado */}
              {!modalReg.pacienteId ? (
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-bold mb-2 block">Paciente <span className="text-red-500">*</span></label>
                    <select
                      className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosReg.paciente ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                      value={pacSel}
                      onChange={(e) => { setPacSel(Number(e.target.value)); setParcSel(""); }}
                    >
                      <option value="">Selecione um paciente</option>
                      {pacientes.map((p) => <option key={p.id} value={p.id}>{p.nome} — {p.cpf}</option>)}
                    </select>
                    {errosReg.paciente && <p className="text-xs text-red-500 mt-1">{errosReg.paciente}</p>}
                  </div>
                  {pacSel !== "" && (
                    <div>
                      <label className="text-sm font-bold mb-2 block">Parcela <span className="text-red-500">*</span></label>
                      <select
                        className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosReg.parcela ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        value={parcSel}
                        onChange={(e) => setParcSel(Number(e.target.value))}
                      >
                        <option value="">Selecione uma parcela</option>
                        {parcelasElegiveis(Number(pacSel)).map((parc) => (
                          <option key={parc.id} value={parc.id}>
                            Parcela {parc.numero} — Vence {parc.vencimento} — {formatarMoeda(getValorDevido(parc))} ({parc.status})
                          </option>
                        ))}
                      </select>
                      {errosReg.parcela && <p className="text-xs text-red-500 mt-1">{errosReg.parcela}</p>}
                    </div>
                  )}
                </div>
              ) : (
                // Parcela pré-selecionada: card somente leitura
                (() => {
                  const parc = getParcela(modalReg.pacienteId!, modalReg.parcelaId!);
                  if (!parc) return null;
                  const encargos = parc.diasAtraso > 0 ? calcularEncargos(parc.valorOriginal, parc.diasAtraso) : 0;
                  return (
                    <div className="border-2 border-gray-300 bg-gray-50 p-4">
                      <div className="flex items-center gap-2 mb-2">
                        <span className="font-bold">Parcela {parc.numero}</span>
                        <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${parc.status === "VENCIDA" ? "bg-red-100 border-red-500 text-red-700" : parc.status === "PARCIALMENTE_PAGA" ? "bg-blue-100 border-blue-500 text-blue-700" : "bg-yellow-100 border-yellow-500 text-yellow-700"}`}>{parc.status}</span>
                      </div>
                      <div className="grid grid-cols-3 gap-3 text-sm">
                        <div><div className="text-xs text-gray-500">Vencimento</div><div className="font-bold">{parc.vencimento}</div></div>
                        <div><div className="text-xs text-gray-500">Valor original</div><div className="font-bold">{formatarMoeda(parc.valorOriginal)}</div></div>
                        {encargos > 0 && <div><div className="text-xs text-gray-500">Encargos</div><div className="font-bold text-red-600">+{formatarMoeda(encargos)}</div></div>}
                        <div><div className="text-xs text-gray-500 font-bold">Valor devido</div><div className="font-bold text-red-700 text-base">{formatarMoeda(getValorDevido(parc))}</div></div>
                      </div>
                    </div>
                  );
                })()
              )}

              {/* Seção 2: Dados do Pagamento */}
              <div>
                <div className="text-xs font-bold text-gray-500 uppercase border-b border-gray-200 pb-1 mb-3">Dados do Pagamento</div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-bold mb-2 block">Valor Recebido * <span className="text-xs text-gray-400">(R$)</span></label>
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosReg.valor ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                      placeholder="0,00"
                      value={formReg.valor}
                      onChange={(e) => setFormReg({ ...formReg, valor: e.target.value })}
                    />
                    {errosReg.valor && <p className="text-xs text-red-500 mt-1">{errosReg.valor}</p>}
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block">Data do Pagamento *</label>
                    <input
                      type="date"
                      max={new Date().toISOString().slice(0, 10)}
                      className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosReg.data ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                      value={formReg.data}
                      onChange={(e) => setFormReg({ ...formReg, data: e.target.value })}
                    />
                    {errosReg.data && <p className="text-xs text-red-500 mt-1">{errosReg.data}</p>}
                  </div>
                </div>
                <div className="mt-4">
                  <label className="text-sm font-bold mb-2 block">Forma de Pagamento *</label>
                  <select
                    className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosReg.forma ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                    value={formReg.forma}
                    onChange={(e) => setFormReg({ ...formReg, forma: e.target.value as FormaPagamento })}
                  >
                    <option value="">Selecione</option>
                    {Object.entries(FORMA_LABEL).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                  </select>
                  {errosReg.forma && <p className="text-xs text-red-500 mt-1">{errosReg.forma}</p>}
                </div>
                <div className="mt-4">
                  <label className="text-sm font-bold mb-2 block">Observação (opcional)</label>
                  <textarea
                    rows={2}
                    className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none resize-none"
                    placeholder="Informações adicionais..."
                    value={formReg.obs}
                    onChange={(e) => setFormReg({ ...formReg, obs: e.target.value })}
                  />
                </div>
                {/* Aviso de pagamento parcial */}
                <div className="mt-3">
                  <AvisoParcial
                    valorStr={formReg.valor}
                    pacienteId={modalReg.pacienteId ?? (pacSel !== "" ? Number(pacSel) : undefined)}
                    parcelaId={modalReg.parcelaId ?? (parcSel !== "" ? Number(parcSel) : undefined)}
                  />
                </div>
              </div>
            </div>

            <div className="sticky bottom-0 bg-white border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalReg({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Cancelar</button>
              <button onClick={confirmarRegistro} className="px-4 py-2 border-2 border-green-500 bg-green-500 text-white font-bold hover:bg-green-600">
                Confirmar Pagamento
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ══ Modal 2: Lançar como Aguardando Comprovante ═══════════════════════ */}
      {modalAgu.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-yellow-500 w-[540px] max-h-[90vh] overflow-y-auto">
            <div className="bg-yellow-50 p-4 border-b-2 border-gray-400 flex justify-between items-center">
              <h2 className="text-lg font-bold text-yellow-800">Aguardando Comprovante</h2>
              <button onClick={() => setModalAgu({ aberto: false })} className="text-gray-400 hover:text-gray-700 text-xl">✕</button>
            </div>
            <div className="p-6 space-y-4">
              {/* Seleção de paciente/parcela */}
              {!modalAgu.pacienteId ? (
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-bold mb-2 block">Paciente</label>
                    <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={pacAguSel} onChange={(e) => { setPacAguSel(Number(e.target.value)); setParcAguSel(""); }}>
                      <option value="">Selecione</option>
                      {pacientes.map((p) => <option key={p.id} value={p.id}>{p.nome} — {p.cpf}</option>)}
                    </select>
                  </div>
                  {pacAguSel !== "" && (
                    <div>
                      <label className="text-sm font-bold mb-2 block">Parcela</label>
                      <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={parcAguSel} onChange={(e) => setParcAguSel(Number(e.target.value))}>
                        <option value="">Selecione</option>
                        {parcelasElegiveis(Number(pacAguSel)).map((parc) => (
                          <option key={parc.id} value={parc.id}>Parcela {parc.numero} — {parc.vencimento} — {formatarMoeda(getValorDevido(parc))}</option>
                        ))}
                      </select>
                    </div>
                  )}
                  {/* Valor esperado baseado na parcela selecionada */}
                  {parcAguSel !== "" && (() => {
                    const parc = getParcela(Number(pacAguSel), Number(parcAguSel));
                    if (!parc) return null;
                    return (
                      <div className="border-2 border-gray-200 bg-gray-50 p-3 text-sm">
                        <span className="text-gray-500">Valor esperado:</span> <strong>{formatarMoeda(getValorDevido(parc))}</strong>
                      </div>
                    );
                  })()}
                </div>
              ) : (
                // Pré-selecionado
                (() => {
                  const pac = pacientes.find((p) => p.id === modalAgu.pacienteId);
                  const parc = getParcela(modalAgu.pacienteId!, modalAgu.parcelaId!);
                  return (
                    <div className="border-2 border-gray-300 bg-gray-50 p-3 text-sm space-y-1">
                      <div><span className="text-gray-500">Paciente:</span> <strong>{pac?.nome}</strong></div>
                      <div><span className="text-gray-500">Parcela:</span> <strong>{parc?.numero} — Vence {parc?.vencimento}</strong></div>
                      <div><span className="text-gray-500">Valor esperado:</span> <strong>{parc ? formatarMoeda(getValorDevido(parc)) : "—"}</strong></div>
                    </div>
                  );
                })()
              )}

              <div>
                <label className="text-sm font-bold mb-2 block">Forma de Pagamento Esperada *</label>
                <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={formAgu.forma} onChange={(e) => setFormAgu({ ...formAgu, forma: e.target.value as FormaPagamento })}>
                  <option value="">Selecione</option>
                  {Object.entries(FORMA_LABEL).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                </select>
              </div>
              <div>
                <label className="text-sm font-bold mb-2 block">Observação (opcional)</label>
                <textarea rows={2} className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none resize-none" placeholder='Ex: "PIX enviado pelo WhatsApp, aguardando confirmação"' value={formAgu.obs} onChange={(e) => setFormAgu({ ...formAgu, obs: e.target.value })} />
              </div>

              <div className="border-2 border-blue-200 bg-blue-50 p-3 text-sm text-blue-800 flex items-start gap-2">
                <FileText className="w-4 h-4 flex-shrink-0 mt-0.5" />
                <span>A parcela não será baixada agora. O pagamento ficará como <strong>Aguardando Comprovante</strong> até você confirmar o recebimento.</span>
              </div>

              {erroAgu && <div className="border-2 border-red-500 bg-red-50 p-3 text-red-700 text-sm font-bold">{erroAgu}</div>}
            </div>
            <div className="border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalAgu({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Cancelar</button>
              <button onClick={confirmarAguardar} className="px-4 py-2 border-2 border-yellow-500 bg-yellow-500 text-white font-bold hover:bg-yellow-600">
                Lançar como Aguardando
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ══ Modal 3: Confirmar Comprovante Recebido ═══════════════════════════ */}
      {modalConf.aberto && modalConf.registro && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-green-500 w-[540px] max-h-[90vh] overflow-y-auto">
            <div className="bg-green-50 p-4 border-b-2 border-gray-400 flex justify-between items-center">
              <h2 className="text-lg font-bold text-green-800">Confirmar Recebimento</h2>
              <button onClick={() => setModalConf({ aberto: false })} className="text-gray-400 hover:text-gray-700 text-xl">✕</button>
            </div>
            <div className="p-6 space-y-4">
              {/* Resumo somente leitura */}
              <div className="border-2 border-gray-300 bg-gray-50 p-3 text-sm space-y-1">
                <div><span className="text-gray-500">Paciente:</span> <strong>{modalConf.registro.pacienteNome}</strong></div>
                <div><span className="text-gray-500">Parcela:</span> <strong>{modalConf.registro.parcelaNumero}</strong></div>
                <div><span className="text-gray-500">Forma declarada:</span> <strong>{FORMA_LABEL[modalConf.registro.formaPagamento]}</strong></div>
                {modalConf.registro.observacao && <div><span className="text-gray-500">Obs.:</span> {modalConf.registro.observacao}</div>}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-bold mb-2 block">Valor Recebido * (R$)</label>
                  <input type="number" min="0.01" step="0.01" className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosConf.valor ? "border-red-500" : "border-gray-300 focus:border-green-500"}`} placeholder="0,00" value={formConf.valor} onChange={(e) => setFormConf({ ...formConf, valor: e.target.value })} />
                  {errosConf.valor && <p className="text-xs text-red-500 mt-1">{errosConf.valor}</p>}
                </div>
                <div>
                  <label className="text-sm font-bold mb-2 block">Data do Pagamento *</label>
                  <input type="date" max={new Date().toISOString().slice(0, 10)} className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${errosConf.data ? "border-red-500" : "border-gray-300 focus:border-green-500"}`} value={formConf.data} onChange={(e) => setFormConf({ ...formConf, data: e.target.value })} />
                  {errosConf.data && <p className="text-xs text-red-500 mt-1">{errosConf.data}</p>}
                </div>
              </div>
              <div>
                <label className="text-sm font-bold mb-2 block">Observação (opcional)</label>
                <textarea rows={2} className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-green-500 focus:outline-none resize-none" placeholder="Informações adicionais..." value={formConf.obs} onChange={(e) => setFormConf({ ...formConf, obs: e.target.value })} />
              </div>
              <AvisoParcial valorStr={formConf.valor} pacienteId={modalConf.registro.pacienteId} parcelaId={modalConf.registro.parcelaId} />
            </div>
            <div className="border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalConf({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Cancelar</button>
              <button onClick={confirmarComprovante} className="px-4 py-2 border-2 border-green-500 bg-green-500 text-white font-bold hover:bg-green-600">
                Confirmar Recebimento
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ══ Modal 4: Cancelar Lançamento Aguardando ═══════════════════════════ */}
      {modalCanc.aberto && modalCanc.registro && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-red-500 w-[480px]">
            <div className="bg-red-50 p-4 border-b-2 border-gray-400 flex items-center gap-2">
              <XCircle className="w-5 h-5 text-red-600" />
              <h2 className="text-lg font-bold text-red-800">Cancelar Lançamento</h2>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-sm text-gray-700">Tem certeza que deseja cancelar este lançamento?</p>
              <div className="border-2 border-gray-300 bg-gray-50 p-3 text-sm space-y-1">
                <div><span className="text-gray-500">Paciente:</span> <strong>{modalCanc.registro.pacienteNome}</strong></div>
                <div><span className="text-gray-500">Parcela:</span> <strong>{modalCanc.registro.parcelaNumero}</strong></div>
              </div>
              <div>
                <label className="text-sm font-bold mb-2 block">Motivo do cancelamento <span className="text-red-500">*</span></label>
                <textarea rows={3} className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-red-500 focus:outline-none resize-none" placeholder="Informe o motivo..." value={motivoCanc} onChange={(e) => setMotivoCanc(e.target.value)} />
              </div>
            </div>
            <div className="border-t-2 border-gray-400 p-4 flex gap-3">
              <button onClick={() => setModalCanc({ aberto: false })} className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Voltar</button>
              <button onClick={confirmarCancelamento} disabled={!motivoCanc.trim()} className="flex-1 px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed">
                Confirmar Cancelamento
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ══ Modal 5: Comprovante de Pagamento ════════════════════════════════ */}
      {modalComp.aberto && modalComp.registro && (() => {
        const reg = modalComp.registro;
        const pac = pacientes.find((p) => p.id === reg.pacienteId);
        const cpfMascarado = pac?.cpf.replace(/(\d{3})\.(\d{3})\.(\d{3})-(\d{2})/, "$1.***.**$4") ?? "—";
        const parc = getParcela(reg.pacienteId, reg.parcelaId);
        return (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white border-t-4 border-blue-500 w-[480px]">
              <div className="p-6">
                <div className="text-center border-2 border-gray-300 bg-gray-50 p-6 font-mono text-sm space-y-1">
                  <div className="text-lg font-bold text-gray-800">OdontoHub</div>
                  <div className="text-xs text-gray-500">Clínica Odontológica</div>
                  <div className="border-t border-dashed border-gray-300 my-3" />
                  <div className="font-bold text-base">COMPROVANTE DE PAGAMENTO</div>
                  <div className="text-xs text-gray-500">Nº {reg.id}</div>
                  <div className="border-t border-dashed border-gray-300 my-3" />
                  <div className="text-left space-y-1">
                    <div><span className="text-gray-500">Paciente:    </span><span className="font-bold">{reg.pacienteNome}</span></div>
                    <div><span className="text-gray-500">CPF:         </span>{cpfMascarado}</div>
                    <div className="border-t border-dashed border-gray-300 my-2" />
                    <div><span className="text-gray-500">Parcela:     </span><span className="font-bold">{reg.parcelaNumero}</span></div>
                    {parc && <div><span className="text-gray-500">Vencimento:  </span>{parc.vencimento}</div>}
                    <div><span className="text-gray-500">Valor Pago:  </span><span className="font-bold text-green-700">{formatarMoeda(reg.valorPago)}</span></div>
                    <div><span className="text-gray-500">Forma:       </span>{FORMA_LABEL[reg.formaPagamento]}</div>
                    <div><span className="text-gray-500">Data Pag.:   </span>{reg.dataPagamento}</div>
                    <div className="border-t border-dashed border-gray-300 my-2" />
                    <div><span className="text-gray-500">Registrado por: </span>{reg.registradoPor}</div>
                    <div><span className="text-gray-500">Data do registro: </span>{reg.dataRegistro}</div>
                  </div>
                  <div className="border-t border-dashed border-gray-300 my-3" />
                  <div className="flex items-center justify-center gap-2 text-green-700 font-bold">
                    <CheckCircle className="w-4 h-4" /> Pagamento confirmado
                  </div>
                </div>
              </div>
              <div className="border-t-2 border-gray-400 p-4">
                <button onClick={() => setModalComp({ aberto: false })} className="w-full px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Fechar</button>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}
