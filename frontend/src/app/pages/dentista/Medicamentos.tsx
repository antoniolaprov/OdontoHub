import { useState, useMemo, useEffect } from "react";
import { api } from "../../api/client";
import {
  Pill,
  Upload,
  AlertTriangle,
  ShieldAlert,
  Search,
  X,
  BarChart2,
  FileText,
  History,
  Lock,
  ClipboardList,
} from "lucide-react";

// ─── Types ────────────────────────────────────────────────────────────────────

type StatusMedicamento = "ATIVO" | "INATIVO";
type ViaAdministracao = "Oral" | "Injetável" | "Tópica" | "Sublingual";

interface LogAuditoria {
  id: number;
  usuario: string;
  campo: string;
  valorAnterior: string;
  valorAtualizado: string;
  data: string;
  hora: string;
  acao: "CRIACAO" | "EDICAO" | "INATIVACAO" | "REATIVACAO";
}

interface Medicamento {
  id: number;
  nomeComercial: string;
  principioAtivo: string;
  categoria: string;
  classeFarmacologica: string;
  viaAdministracao: ViaAdministracao;
  status: StatusMedicamento;
  apresentacoes: string[];
  posologiasPadrao: string[];
  contraindicacoes: string;
  interacoes: string;
  totalPrescrito: number;
  ultimoUso: string;
  periodoMaiorUso: string; // ex: "Jan–Mar 2026" — mês de maior concentração de prescrições
  dentistas: number;
  pacientesAtendidos: number;
  dataCriacaoPrescricao: string | null; // data da prescrição mais recente para regra dos 90 dias
  logs: LogAuditoria[];
}

// ─── Dados ────────────────────────────────────────────────────────────────────

const CATEGORIAS = [
  "Antibiótico",
  "Anti-inflamatório",
  "Analgésico",
  "Corticosteroide",
  "Ansiolítico",
  "Outro",
];

const CLASSES = [
  "Beta-lactâmicos",
  "AINEs",
  "Opioides",
  "Corticosteroides",
  "Benzodiazepínicos",
  "Pirazolonas",
  "Paraaminofenóis",
  "Lincosamidas",
  "Tetraciclinas",
  "Outro",
];

const VIAS: ViaAdministracao[] = ["Oral", "Injetável", "Tópica", "Sublingual"];

// Anamneses mock para simular alerta de contraindicação cruzada
const ANAMNESES_MOCK: Record<number, { alergias: string[] }> = {
  1: { alergias: ["Penicilina", "Beta-lactâmicos"] },
  2: { alergias: ["AINEs", "Ibuprofeno"] },
  3: { alergias: [] },
  4: { alergias: ["Corticosteroides"] },
};

const PACIENTES_MOCK = [
  { id: 1, nome: "Ana Costa" },
  { id: 2, nome: "Bruno Ferreira" },
  { id: 3, nome: "Carla Mendes" },
  { id: 4, nome: "Diego Alves" },
];

function diasDesdeData(dataStr: string | null): number {
  if (!dataStr) return 9999;
  const [dia, mes, ano] = dataStr.split("/").map(Number);
  const data = new Date(ano, mes - 1, dia);
  const hoje = new Date();
  return Math.floor((hoje.getTime() - data.getTime()) / (1000 * 60 * 60 * 24));
}

function dataHojeFormatada() {
  const hoje = new Date();
  return hoje.toLocaleDateString("pt-BR");
}

function horaAgoraFormatada() {
  const hoje = new Date();
  return hoje.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

const MOCK_INICIAL: Medicamento[] = [
  {
    id: 1,
    nomeComercial: "Amoxil",
    principioAtivo: "Amoxicilina",
    categoria: "Antibiótico",
    classeFarmacologica: "Beta-lactâmicos",
    viaAdministracao: "Oral",
    status: "ATIVO",
    apresentacoes: ["Cáps. 500mg", "Susp. 250mg/5ml"],
    posologiasPadrao: ["1 cáps. a cada 8h por 7 dias", "1 cáps. a cada 12h por 5 dias"],
    contraindicacoes: "Alergia a penicilinas ou cefalosporinas, mononucleose infecciosa",
    interacoes: "Anticoagulantes orais (warfarina), metotrexato",
    totalPrescrito: 23,
    ultimoUso: "08/06/2026",
    periodoMaiorUso: "Jan–Mar 2026",
    dentistas: 2,
    pacientesAtendidos: 18,
    dataCriacaoPrescricao: "08/06/2026",
    logs: [
      { id: 1, usuario: "Dr. Ricardo", campo: "Cadastro", valorAnterior: "—", valorAtualizado: "Medicamento criado", data: "01/01/2026", hora: "09:00", acao: "CRIACAO" },
    ],
  },
  {
    id: 2,
    nomeComercial: "Ibupril",
    principioAtivo: "Ibuprofeno",
    categoria: "Anti-inflamatório",
    classeFarmacologica: "AINEs",
    viaAdministracao: "Oral",
    status: "ATIVO",
    apresentacoes: ["Comp. 600mg", "Comp. 400mg"],
    posologiasPadrao: ["1 comp. de 600mg a cada 8h por 3 dias"],
    contraindicacoes: "Úlcera péptica ativa, insuficiência renal grave, gestação (3º trimestre)",
    interacoes: "Anticoagulantes, diuréticos, anti-hipertensivos",
    totalPrescrito: 41,
    ultimoUso: "07/06/2026",
    periodoMaiorUso: "Fev–Abr 2026",
    dentistas: 3,
    pacientesAtendidos: 35,
    dataCriacaoPrescricao: "07/06/2026",
    logs: [
      { id: 1, usuario: "Dr. Ricardo", campo: "Cadastro", valorAnterior: "—", valorAtualizado: "Medicamento criado", data: "01/01/2026", hora: "09:00", acao: "CRIACAO" },
    ],
  },
  {
    id: 3,
    nomeComercial: "Nimesil",
    principioAtivo: "Nimesulida",
    categoria: "Anti-inflamatório",
    classeFarmacologica: "AINEs",
    viaAdministracao: "Oral",
    status: "ATIVO",
    apresentacoes: ["Comp. 100mg", "Granulado 100mg/sachê"],
    posologiasPadrao: ["1 comp. a cada 12h por 2 dias", "1 sachê a cada 12h por 3 dias"],
    contraindicacoes: "Hepatopatia, insuficiência renal, gestação",
    interacoes: "Anticoagulantes, lítio, metotrexato",
    totalPrescrito: 29,
    ultimoUso: "06/06/2026",
    periodoMaiorUso: "Mar–Mai 2026",
    dentistas: 2,
    pacientesAtendidos: 24,
    dataCriacaoPrescricao: "06/06/2026",
    logs: [],
  },
  {
    id: 4,
    nomeComercial: "Dipirax",
    principioAtivo: "Dipirona",
    categoria: "Analgésico",
    classeFarmacologica: "Pirazolonas",
    viaAdministracao: "Oral",
    status: "ATIVO",
    apresentacoes: ["Comp. 500mg", "Gotas 500mg/ml"],
    posologiasPadrao: ["1 comp. a cada 6h se dor"],
    contraindicacoes: "Hipersensibilidade à dipirona, porfiria aguda",
    interacoes: "Ciclosporina, cloranfenicol",
    totalPrescrito: 15,
    ultimoUso: "05/06/2026",
    periodoMaiorUso: "Abr–Jun 2026",
    dentistas: 2,
    pacientesAtendidos: 13,
    dataCriacaoPrescricao: "05/06/2026",
    logs: [],
  },
  {
    id: 5,
    nomeComercial: "Celestone",
    principioAtivo: "Betametasona",
    categoria: "Corticosteroide",
    classeFarmacologica: "Corticosteroides",
    viaAdministracao: "Injetável",
    status: "ATIVO",
    apresentacoes: ["Inj. 3mg/ml"],
    posologiasPadrao: ["4mg IM dose única pré-operatória"],
    contraindicacoes: "Infecções sistêmicas não tratadas, hipersensibilidade",
    interacoes: "AINEs (risco de úlcera), hipoglicemiantes",
    totalPrescrito: 8,
    ultimoUso: "01/06/2026",
    periodoMaiorUso: "Mai–Jun 2026",
    dentistas: 1,
    pacientesAtendidos: 8,
    dataCriacaoPrescricao: "01/06/2026",
    logs: [],
  },
  {
    id: 6,
    nomeComercial: "Clindoxyl",
    principioAtivo: "Clindamicina",
    categoria: "Antibiótico",
    classeFarmacologica: "Lincosamidas",
    viaAdministracao: "Oral",
    status: "ATIVO",
    apresentacoes: ["Cáps. 300mg"],
    posologiasPadrao: ["1 cáps. a cada 6h por 7 dias"],
    contraindicacoes: "Colite ulcerativa, hipersensibilidade",
    interacoes: "Bloqueadores neuromusculares",
    totalPrescrito: 11,
    ultimoUso: "28/05/2026",
    periodoMaiorUso: "Mar–Mai 2026",
    dentistas: 2,
    pacientesAtendidos: 10,
    dataCriacaoPrescricao: "28/05/2026",
    logs: [],
  },
  {
    id: 7,
    nomeComercial: "Paracetol",
    principioAtivo: "Paracetamol",
    categoria: "Analgésico",
    classeFarmacologica: "Paraaminofenóis",
    viaAdministracao: "Oral",
    status: "ATIVO",
    apresentacoes: ["Comp. 750mg", "Susp. 200mg/ml"],
    posologiasPadrao: ["1 comp. de 750mg a cada 6h se dor"],
    contraindicacoes: "Hepatopatia grave, alcoolismo crônico",
    interacoes: "Warfarina (uso prolongado), isoniazida",
    totalPrescrito: 37,
    ultimoUso: "08/06/2026",
    periodoMaiorUso: "Jan–Jun 2026",
    dentistas: 3,
    pacientesAtendidos: 31,
    dataCriacaoPrescricao: "08/06/2026",
    logs: [],
  },
  {
    id: 8,
    nomeComercial: "Doxitrat",
    principioAtivo: "Doxiciclina",
    categoria: "Antibiótico",
    classeFarmacologica: "Tetraciclinas",
    viaAdministracao: "Oral",
    status: "INATIVO",
    apresentacoes: ["Cáps. 100mg"],
    posologiasPadrao: ["100mg a cada 12h por 7 dias"],
    contraindicacoes: "Gestação, crianças menores de 8 anos, hipersensibilidade",
    interacoes: "Antiácidos, anticoagulantes, isotretinoína",
    totalPrescrito: 4,
    ultimoUso: "10/03/2026",
    periodoMaiorUso: "Jan–Mar 2026",
    dentistas: 1,
    pacientesAtendidos: 4,
    dataCriacaoPrescricao: "10/03/2026",
    logs: [
      { id: 1, usuario: "Dr. Ricardo", campo: "status", valorAnterior: "ATIVO", valorAtualizado: "INATIVO", data: "15/03/2026", hora: "14:30", acao: "INATIVACAO" },
    ],
  },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

function StatusBadge({ status }: { status: StatusMedicamento }) {
  return (
    <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${status === "ATIVO" ? "bg-green-100 border-green-500 text-green-700" : "bg-gray-100 border-gray-400 text-gray-500"}`}>
      {status}
    </span>
  );
}

function CategoriaBadge({ categoria }: { categoria: string }) {
  const cores: Record<string, string> = {
    Antibiótico: "bg-blue-100 border-blue-500 text-blue-700",
    "Anti-inflamatório": "bg-green-100 border-green-500 text-green-700",
    Analgésico: "bg-yellow-100 border-yellow-500 text-yellow-700",
    Corticosteroide: "bg-purple-100 border-purple-500 text-purple-700",
    Ansiolítico: "bg-orange-100 border-orange-500 text-orange-700",
  };
  return (
    <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${cores[categoria] ?? "bg-gray-100 border-gray-400 text-gray-700"}`}>
      {categoria}
    </span>
  );
}

function AcaoBadge({ acao }: { acao: LogAuditoria["acao"] }) {
  const map: Record<LogAuditoria["acao"], { label: string; cls: string }> = {
    CRIACAO: { label: "Criação", cls: "bg-blue-100 border-blue-400 text-blue-700" },
    EDICAO: { label: "Edição", cls: "bg-yellow-100 border-yellow-400 text-yellow-700" },
    INATIVACAO: { label: "Inativação", cls: "bg-red-100 border-red-400 text-red-700" },
    REATIVACAO: { label: "Reativação", cls: "bg-green-100 border-green-400 text-green-700" },
  };
  const { label, cls } = map[acao];
  return <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${cls}`}>{label}</span>;
}

function emptyForm() {
  return {
    nomeComercial: "",
    principioAtivo: "",
    categoria: "",
    classeFarmacologica: "",
    viaAdministracao: "Oral" as ViaAdministracao,
    status: "ATIVO" as StatusMedicamento,
    apresentacoes: [] as string[],
    posologiasPadrao: [] as string[],
    contraindicacoes: "",
    interacoes: "",
    novaApresentacao: "",
    novaPosologia: "",
  };
}

// ─── Componente Principal ─────────────────────────────────────────────────────

// ─── Integração com o backend (GET /api/medicamentos) ─────────────────────────

function adaptarMedicamento(b: any, i: number): Medicamento {
  return {
    id: b.id?.id ?? i + 1,
    nomeComercial: b.nomeComercial ?? "—",
    principioAtivo: b.principioAtivo ?? "—",
    categoria: b.categoriaTerapeutica ?? "—",
    classeFarmacologica: b.classeFarmacologica ?? "—",
    viaAdministracao: "Oral",
    status: (b.status as StatusMedicamento) ?? "ATIVO",
    apresentacoes: [],
    posologiasPadrao: b.posologiasPadrao ?? [],
    contraindicacoes: Array.isArray(b.contraindicacoes) ? b.contraindicacoes.join("; ") : (b.contraindicacoes ?? ""),
    interacoes: "",
    totalPrescrito: 0,
    ultimoUso: "—",
    periodoMaiorUso: "—",
    dentistas: 0,
    pacientesAtendidos: 0,
    dataCriacaoPrescricao: null,
    logs: [],
  };
}

export default function DentistaMedicamentos() {
  const [medicamentos, setMedicamentos] = useState<Medicamento[]>(MOCK_INICIAL);

  // Carrega o catálogo real do backend; mantém o mock como fallback.
  useEffect(() => {
    api.get<any[]>("/medicamentos")
      .then((lista) => {
        if (Array.isArray(lista) && lista.length > 0) {
          setMedicamentos(lista.map(adaptarMedicamento));
        }
      })
      .catch((e) => console.warn("Falha ao carregar medicamentos:", e));
  }, []);

  // Filtros
  const [busca, setBusca] = useState("");
  const [filtroCategoria, setFiltroCategoria] = useState("TODAS");
  const [filtroClasse, setFiltroClasse] = useState("TODAS");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");

  // Toast
  const [toast, setToast] = useState<{ msg: string; tipo: "sucesso" | "erro" | "aviso" } | null>(null);

  // Modais
  const [modalForm, setModalForm] = useState<{ aberto: boolean; medicamento?: Medicamento }>({ aberto: false });
  const [form, setForm] = useState(emptyForm());
  const [erroForm, setErroForm] = useState("");
  const [alertaBloqueioClasse, setAlertaBloqueioClasse] = useState("");

  const [modalVer, setModalVer] = useState<{ aberto: boolean; medicamento?: Medicamento }>({ aberto: false });
  const [abaModalVer, setAbaModalVer] = useState<"ficha" | "auditoria">("ficha");

  const [modalInativar, setModalInativar] = useState<{ aberto: boolean; medicamento?: Medicamento }>({ aberto: false });
  const [justificativaInativar, setJustificativaInativar] = useState("");
  const [erroJustificativa, setErroJustificativa] = useState("");

  const [modalCSV, setModalCSV] = useState(false);
  const [csvCarregado, setCsvCarregado] = useState(false);

  // Modal Contraindicação cruzada
  const [modalContraIndicacao, setModalContraIndicacao] = useState<{
    aberto: boolean;
    medicamento?: Medicamento;
    pacienteId?: number;
    alertas: string[];
  }>({ aberto: false, alertas: [] });
  const [pacienteSelecionado, setPacienteSelecionado] = useState<number | "">("");

  // ─ Derived ──────────────────────────────────────────────────────────────

  const filtrados = useMemo(() => {
    const q = busca.toLowerCase();
    return medicamentos.filter((m) => {
      const matchBusca = busca === "" || m.nomeComercial.toLowerCase().includes(q) || m.principioAtivo.toLowerCase().includes(q);
      const matchCat = filtroCategoria === "TODAS" || m.categoria === filtroCategoria;
      const matchClasse = filtroClasse === "TODAS" || m.classeFarmacologica === filtroClasse;
      const matchStatus = filtroStatus === "TODOS" || m.status === filtroStatus;
      return matchBusca && matchCat && matchClasse && matchStatus;
    });
  }, [medicamentos, busca, filtroCategoria, filtroClasse, filtroStatus]);

  const totais = useMemo(
    () => ({
      total: medicamentos.length,
      ativos: medicamentos.filter((m) => m.status === "ATIVO").length,
      inativos: medicamentos.filter((m) => m.status === "INATIVO").length,
      totalPrescricoes: medicamentos.reduce((acc, m) => acc + m.totalPrescrito, 0),
    }),
    [medicamentos]
  );

  // ─ Helpers internos ─────────────────────────────────────────────────────

  function showToast(msg: string, tipo: "sucesso" | "erro" | "aviso") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 4000);
  }

  function classeProtegida(med: Medicamento): boolean {
    return diasDesdeData(med.dataCriacaoPrescricao) <= 90;
  }

  // ─ Handlers Form ────────────────────────────────────────────────────────

  function abrirNovo() {
    setForm(emptyForm());
    setErroForm("");
    setAlertaBloqueioClasse("");
    setModalForm({ aberto: true });
  }

  function abrirEditar(m: Medicamento) {
    setForm({
      nomeComercial: m.nomeComercial,
      principioAtivo: m.principioAtivo,
      categoria: m.categoria,
      classeFarmacologica: m.classeFarmacologica,
      viaAdministracao: m.viaAdministracao,
      status: m.status,
      apresentacoes: [...m.apresentacoes],
      posologiasPadrao: [...m.posologiasPadrao],
      contraindicacoes: m.contraindicacoes,
      interacoes: m.interacoes,
      novaApresentacao: "",
      novaPosologia: "",
    });
    setErroForm("");
    setAlertaBloqueioClasse(
      classeProtegida(m)
        ? `A classe farmacológica não pode ser alterada pois este medicamento possui prescrições emitidas nos últimos 90 dias (último uso: ${m.ultimoUso}).`
        : ""
    );
    setModalForm({ aberto: true, medicamento: m });
  }

  function adicionarApresentacao() {
    if (!form.novaApresentacao.trim()) return;
    setForm((f) => ({ ...f, apresentacoes: [...f.apresentacoes, f.novaApresentacao.trim()], novaApresentacao: "" }));
  }

  function removerApresentacao(idx: number) {
    setForm((f) => ({ ...f, apresentacoes: f.apresentacoes.filter((_, i) => i !== idx) }));
  }

  function adicionarPosologia() {
    if (!form.novaPosologia.trim()) return;
    setForm((f) => ({ ...f, posologiasPadrao: [...f.posologiasPadrao, f.novaPosologia.trim()], novaPosologia: "" }));
  }

  function removerPosologia(idx: number) {
    setForm((f) => ({ ...f, posologiasPadrao: f.posologiasPadrao.filter((_, i) => i !== idx) }));
  }

  function handleSalvar() {
    if (!form.nomeComercial.trim()) { setErroForm("Nome comercial é obrigatório."); return; }
    if (!form.principioAtivo.trim()) { setErroForm("Princípio ativo é obrigatório."); return; }
    if (!form.categoria) { setErroForm("Categoria terapêutica é obrigatória."); return; }
    if (!form.classeFarmacologica) { setErroForm("Classe farmacológica é obrigatória."); return; }
    if (form.apresentacoes.length === 0) { setErroForm("Adicione ao menos uma apresentação disponível (ex: Cáps. 500mg)."); return; }

    const duplicado = medicamentos.some(
      (m) =>
        m.nomeComercial.toLowerCase() === form.nomeComercial.trim().toLowerCase() &&
        m.principioAtivo.toLowerCase() === form.principioAtivo.trim().toLowerCase() &&
        m.id !== modalForm.medicamento?.id
    );
    if (duplicado) { setErroForm("Já existe um medicamento com este nome comercial e princípio ativo."); return; }

    // Bloqueio alteração de classe nos últimos 90 dias
    if (modalForm.medicamento && classeProtegida(modalForm.medicamento) && form.classeFarmacologica !== modalForm.medicamento.classeFarmacologica) {
      setErroForm("Não é possível alterar a classe farmacológica de um medicamento com prescrições nos últimos 90 dias.");
      return;
    }

    if (modalForm.medicamento) {
      const med = modalForm.medicamento;
      const camposAlterados: Omit<LogAuditoria, "id">[] = [];

      const campos: Array<{ key: keyof typeof form; label: string }> = [
        { key: "nomeComercial", label: "Nome Comercial" },
        { key: "principioAtivo", label: "Princípio Ativo" },
        { key: "categoria", label: "Categoria" },
        { key: "classeFarmacologica", label: "Classe Farmacológica" },
        { key: "viaAdministracao", label: "Via de Administração" },
        { key: "contraindicacoes", label: "Contraindicações" },
        { key: "interacoes", label: "Interações" },
      ];

      campos.forEach(({ key, label }) => {
        const anterior = String((med as any)[key] ?? "");
        const novo = String(form[key] ?? "");
        if (anterior !== novo) {
          camposAlterados.push({
            usuario: "Dr. Ricardo",
            campo: label,
            valorAnterior: anterior,
            valorAtualizado: novo,
            data: dataHojeFormatada(),
            hora: horaAgoraFormatada(),
            acao: "EDICAO",
          });
        }
      });

      setMedicamentos((prev) =>
        prev.map((m) =>
          m.id === med.id
            ? {
                ...m,
                nomeComercial: form.nomeComercial.trim(),
                principioAtivo: form.principioAtivo.trim(),
                categoria: form.categoria,
                classeFarmacologica: form.classeFarmacologica,
                viaAdministracao: form.viaAdministracao,
                status: form.status,
                apresentacoes: form.apresentacoes,
                posologiasPadrao: form.posologiasPadrao,
                contraindicacoes: form.contraindicacoes,
                interacoes: form.interacoes,
                logs: [...m.logs, ...camposAlterados.map((l, i) => ({ ...l, id: m.logs.length + i + 1 }))],
              }
            : m
        )
      );
      showToast("Medicamento atualizado com sucesso.", "sucesso");
    } else {
      const novoId = Math.max(...medicamentos.map((m) => m.id), 0) + 1;
      const novo: Medicamento = {
        id: novoId,
        nomeComercial: form.nomeComercial.trim(),
        principioAtivo: form.principioAtivo.trim(),
        categoria: form.categoria,
        classeFarmacologica: form.classeFarmacologica,
        viaAdministracao: form.viaAdministracao,
        status: form.status,
        apresentacoes: form.apresentacoes,
        posologiasPadrao: form.posologiasPadrao,
        contraindicacoes: form.contraindicacoes,
        interacoes: form.interacoes,
        totalPrescrito: 0,
        ultimoUso: "—",
        periodoMaiorUso: "—",
        dentistas: 0,
        pacientesAtendidos: 0,
        dataCriacaoPrescricao: null,
        logs: [
          { id: 1, usuario: "Dr. Ricardo", campo: "Cadastro", valorAnterior: "—", valorAtualizado: "Medicamento criado", data: dataHojeFormatada(), hora: horaAgoraFormatada(), acao: "CRIACAO" },
        ],
      };
      setMedicamentos((prev) => [...prev, novo]);
      showToast("Medicamento cadastrado com sucesso.", "sucesso");
    }
    setModalForm({ aberto: false });
  }

  function handleInativar() {
    if (!justificativaInativar.trim()) { setErroJustificativa("Justificativa é obrigatória."); return; }
    const med = modalInativar.medicamento!;
    setMedicamentos((prev) =>
      prev.map((m) =>
        m.id === med.id
          ? {
              ...m,
              status: "INATIVO",
              logs: [
                ...m.logs,
                {
                  id: m.logs.length + 1,
                  usuario: "Dr. Ricardo",
                  campo: "status",
                  valorAnterior: "ATIVO",
                  valorAtualizado: `INATIVO — Motivo: ${justificativaInativar.trim()}`,
                  data: dataHojeFormatada(),
                  hora: horaAgoraFormatada(),
                  acao: "INATIVACAO" as const,
                },
              ],
            }
          : m
      )
    );
    showToast("Medicamento inativado.", "sucesso");
    setModalInativar({ aberto: false });
    setJustificativaInativar("");
    setErroJustificativa("");
  }

  function handleReativar(med: Medicamento) {
    setMedicamentos((prev) =>
      prev.map((m) =>
        m.id === med.id
          ? {
              ...m,
              status: "ATIVO",
              logs: [
                ...m.logs,
                {
                  id: m.logs.length + 1,
                  usuario: "Dr. Ricardo",
                  campo: "status",
                  valorAnterior: "INATIVO",
                  valorAtualizado: "ATIVO",
                  data: dataHojeFormatada(),
                  hora: horaAgoraFormatada(),
                  acao: "REATIVACAO" as const,
                },
              ],
            }
          : m
      )
    );
    showToast(`${med.nomeComercial} reativado com sucesso.`, "sucesso");
  }

  // ─ Contraindicação Cruzada ───────────────────────────────────────────────

  function verificarContraIndicacao(med: Medicamento, pacId: number | "") {
    if (pacId === "") return;
    const anamnese = ANAMNESES_MOCK[pacId];
    if (!anamnese) return;
    const alertas: string[] = [];
    anamnese.alergias.forEach((alergia) => {
      if (med.classeFarmacologica.toLowerCase().includes(alergia.toLowerCase()) || alergia.toLowerCase().includes(med.classeFarmacologica.toLowerCase())) {
        alertas.push(`Paciente alérgico a "${alergia}" — conflito com a classe ${med.classeFarmacologica}`);
      }
      if (med.contraindicacoes.toLowerCase().includes(alergia.toLowerCase())) {
        alertas.push(`Contraindicação registrada: paciente com histórico de alergia a "${alergia}"`);
      }
    });
    if (alertas.length > 0) {
      setModalContraIndicacao({ aberto: true, medicamento: med, pacienteId: pacId, alertas });
    } else {
      showToast("Nenhuma contraindicação identificada para este paciente.", "sucesso");
    }
  }

  // Alerta de inativação apenas se houver prescrições nos ÚLTIMOS 30 DIAS (requisito F8)
  const temPrescricoesRecentes = (med: Medicamento) =>
    diasDesdeData(med.dataCriacaoPrescricao) <= 30;

  // ─ Render ────────────────────────────────────────────────────────────────

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      {/* Toast */}
      {toast && (
        <div className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold text-sm shadow-lg max-w-sm ${toast.tipo === "sucesso" ? "bg-green-50 border-green-500 text-green-700" : toast.tipo === "aviso" ? "bg-yellow-50 border-yellow-500 text-yellow-700" : "bg-red-50 border-red-500 text-red-700"}`}>
          {toast.tipo === "sucesso" ? "✓ " : toast.tipo === "aviso" ? "⚠ " : "✕ "}
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-gray-700 flex items-center gap-2">
            <Pill className="w-6 h-6" />
            Catálogo de Medicamentos
          </h1>
          <p className="text-xs text-gray-500 mt-1">Gerencie os medicamentos disponíveis para prescrição no consultório.</p>
        </div>
        <div className="flex gap-2">
          <button onClick={() => setModalCSV(true)} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold text-sm flex items-center gap-2 hover:bg-gray-50">
            <Upload className="w-4 h-4" /> Importar CSV
          </button>
          <button onClick={abrirNovo} className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600">
            + Novo Medicamento
          </button>
        </div>
      </div>

      {/* Cards de resumo */}
      <div className="grid grid-cols-4 gap-3 mb-6">
        {[
          { label: "Total no Catálogo", value: totais.total, color: "border-gray-400 bg-white", textColor: "text-gray-700" },
          { label: "Ativos", value: totais.ativos, color: "border-green-500 bg-green-50", textColor: "text-green-700" },
          { label: "Inativos", value: totais.inativos, color: "border-gray-400 bg-gray-100", textColor: "text-gray-500" },
          { label: "Total Prescrito (histórico)", value: totais.totalPrescricoes, color: "border-blue-500 bg-blue-50", textColor: "text-blue-700" },
        ].map((c) => (
          <div key={c.label} className={`border-2 p-4 ${c.color}`}>
            <div className={`text-2xl font-bold ${c.textColor}`}>{c.value}</div>
            <div className="text-xs text-gray-500 mt-1">{c.label}</div>
          </div>
        ))}
      </div>

      {/* Verificação de Contraindicação Cruzada */}
      <div className="bg-white border-2 border-orange-300 p-4 mb-6">
        <div className="flex items-center gap-2 mb-3">
          <AlertTriangle className="w-5 h-5 text-orange-500" />
          <span className="text-sm font-bold text-orange-700">Verificar Contraindicação com Paciente</span>
          <span className="text-xs text-gray-500">(Simulação via anamnese)</span>
        </div>
        <div className="flex gap-3 items-end flex-wrap">
          <div>
            <label className="text-xs font-bold text-gray-500 mb-1 block">Paciente</label>
            <select
              className="border-2 border-gray-300 p-2 bg-white rounded text-sm focus:border-orange-500 focus:outline-none"
              value={pacienteSelecionado}
              onChange={(e) => setPacienteSelecionado(e.target.value === "" ? "" : Number(e.target.value))}
            >
              <option value="">Selecionar paciente...</option>
              {PACIENTES_MOCK.map((p) => (
                <option key={p.id} value={p.id}>{p.nome}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-bold text-gray-500 mb-1 block">Medicamento</label>
            <select
              className="border-2 border-gray-300 p-2 bg-white rounded text-sm focus:border-orange-500 focus:outline-none"
              onChange={(e) => {
                const med = medicamentos.find((m) => m.id === Number(e.target.value));
                if (med) verificarContraIndicacao(med, pacienteSelecionado);
              }}
              defaultValue=""
            >
              <option value="">Selecionar medicamento...</option>
              {medicamentos.filter((m) => m.status === "ATIVO").map((m) => (
                <option key={m.id} value={m.id}>{m.nomeComercial} ({m.classeFarmacologica})</option>
              ))}
            </select>
          </div>
          <p className="text-xs text-gray-400 self-end pb-2">Selecione paciente e medicamento para verificar conflitos com a anamnese.</p>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white border-2 border-gray-400 p-4 mb-6">
        <div className="grid grid-cols-5 gap-3 items-end">
          <div className="col-span-2">
            <label className="text-sm font-bold mb-2 block">Buscar</label>
            <div className="relative">
              <Search className="absolute left-2 top-2.5 w-4 h-4 text-gray-400" />
              <input
                type="text"
                className="w-full border-2 border-gray-300 p-2 pl-8 bg-white rounded focus:border-blue-500 focus:outline-none"
                placeholder="Nome comercial ou princípio ativo"
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </div>
          </div>
          <div>
            <label className="text-sm font-bold mb-2 block">Categoria</label>
            <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={filtroCategoria} onChange={(e) => setFiltroCategoria(e.target.value)}>
              <option value="TODAS">Todas</option>
              {CATEGORIAS.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="text-sm font-bold mb-2 block">Classe Farmacológica</label>
            <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={filtroClasse} onChange={(e) => setFiltroClasse(e.target.value)}>
              <option value="TODAS">Todas</option>
              {CLASSES.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="text-sm font-bold mb-2 block">Status</label>
            <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value)}>
              <option value="TODOS">Todos</option>
              <option value="ATIVO">Ativo</option>
              <option value="INATIVO">Inativo</option>
            </select>
          </div>
        </div>
        <div className="mt-3 text-xs text-gray-500 text-right">{filtrados.length} medicamento(s) encontrado(s)</div>
      </div>

      {/* Tabela */}
      <div className="border-2 border-gray-400 bg-white overflow-x-auto">
        <div className="min-w-[1100px]">
          <div className="grid grid-cols-8 bg-gray-100 border-b-2 border-gray-400 text-sm">
            <div className="p-3 border-r-2 border-gray-400 font-bold">Nome Comercial</div>
            <div className="p-3 border-r-2 border-gray-400 font-bold">Princípio Ativo</div>
            <div className="p-3 border-r-2 border-gray-400 font-bold">Categoria</div>
            <div className="p-3 border-r-2 border-gray-400 font-bold">Classe Farmacológica</div>
            <div className="p-3 border-r-2 border-gray-400 font-bold">Apresentações</div>
            <div className="p-3 border-r-2 border-gray-400 font-bold">Status</div>
            <div className="p-3 border-r-2 border-gray-400 font-bold text-center">Classe Protegida</div>
            <div className="p-3 font-bold text-center">Ações</div>
          </div>

          {filtrados.length === 0 && <div className="p-12 text-center text-gray-500">Nenhum medicamento encontrado.</div>}

          {filtrados.map((med) => (
            <div key={med.id} className={`grid grid-cols-8 border-b-2 border-gray-400 items-center text-sm hover:bg-gray-50 ${med.status === "INATIVO" ? "opacity-60" : ""}`}>
              <div className="p-3 border-r-2 border-gray-400 font-bold">{med.nomeComercial}</div>
              <div className="p-3 border-r-2 border-gray-400 text-gray-600">{med.principioAtivo}</div>
              <div className="p-3 border-r-2 border-gray-400"><CategoriaBadge categoria={med.categoria} /></div>
              <div className="p-3 border-r-2 border-gray-400 text-xs text-gray-600">{med.classeFarmacologica}</div>
              <div className="p-3 border-r-2 border-gray-400">
                <div className="flex flex-wrap gap-1">
                  {med.apresentacoes.slice(0, 2).map((a, i) => (
                    <span key={i} className="bg-gray-100 border border-gray-300 px-1.5 py-0.5 rounded text-xs">{a}</span>
                  ))}
                  {med.apresentacoes.length > 2 && <span className="text-xs text-gray-400">+{med.apresentacoes.length - 2}</span>}
                </div>
              </div>
              <div className="p-3 border-r-2 border-gray-400"><StatusBadge status={med.status} /></div>
              <div className="p-3 border-r-2 border-gray-400 text-center">
                {classeProtegida(med) ? (
                  <span className="inline-flex items-center gap-1 text-xs text-orange-600 font-bold">
                    <Lock className="w-3 h-3" /> Bloqueada
                  </span>
                ) : (
                  <span className="text-xs text-gray-400">Livre</span>
                )}
              </div>
              <div className="p-3 flex items-center justify-center gap-1 flex-wrap">
                <button onClick={() => { setModalVer({ aberto: true, medicamento: med }); setAbaModalVer("ficha"); }} className="px-2 py-1 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-50">Ver</button>
                <button onClick={() => abrirEditar(med)} className="px-2 py-1 border-2 border-blue-500 bg-white text-blue-600 text-xs font-bold hover:bg-blue-50">Editar</button>
                {med.status === "ATIVO" ? (
                  <button onClick={() => { setModalInativar({ aberto: true, medicamento: med }); setJustificativaInativar(""); setErroJustificativa(""); }} className="px-2 py-1 border-2 border-red-500 bg-white text-red-600 text-xs font-bold hover:bg-red-50">Inativar</button>
                ) : (
                  <button onClick={() => handleReativar(med)} className="px-2 py-1 border-2 border-green-500 bg-white text-green-600 text-xs font-bold hover:bg-green-50">Reativar</button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ══ Modal: Novo / Editar Medicamento ══ */}
      {modalForm.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[640px] max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-blue-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span className="flex items-center gap-2">
                <Pill className="w-5 h-5" />
                {modalForm.medicamento ? "Editar Medicamento" : "Novo Medicamento"}
              </span>
              <button onClick={() => setModalForm({ aberto: false })} className="text-xl leading-none hover:text-gray-200">✕</button>
            </div>

            <div className="p-6 space-y-5">
              {/* Alerta de classe protegida */}
              {alertaBloqueioClasse && (
                <div className="border-2 border-orange-400 bg-orange-50 p-3 rounded flex items-start gap-2">
                  <Lock className="w-4 h-4 text-orange-600 mt-0.5 flex-shrink-0" />
                  <p className="text-sm text-orange-700">{alertaBloqueioClasse}</p>
                </div>
              )}

              {/* Seção 1: Identificação */}
              <div>
                <div className="text-xs font-bold text-gray-500 uppercase mb-3 border-b border-gray-200 pb-1">Identificação</div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-bold mb-2 block">Nome Comercial <span className="text-red-500">*</span></label>
                    <input type="text" className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" placeholder="Ex: Amoxil" value={form.nomeComercial} onChange={(e) => { setForm((f) => ({ ...f, nomeComercial: e.target.value })); setErroForm(""); }} />
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block">Princípio Ativo <span className="text-red-500">*</span></label>
                    <input type="text" className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" placeholder="Ex: Amoxicilina" value={form.principioAtivo} onChange={(e) => { setForm((f) => ({ ...f, principioAtivo: e.target.value })); setErroForm(""); }} />
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block">Categoria Terapêutica <span className="text-red-500">*</span></label>
                    <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={form.categoria} onChange={(e) => { setForm((f) => ({ ...f, categoria: e.target.value })); setErroForm(""); }}>
                      <option value="">Selecione</option>
                      {CATEGORIAS.map((c) => <option key={c} value={c}>{c}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block">
                      Classe Farmacológica <span className="text-red-500">*</span>
                      {alertaBloqueioClasse && <Lock className="w-3 h-3 inline ml-1 text-orange-500" />}
                    </label>
                    <select
                      className={`w-full border-2 p-2 bg-white rounded focus:outline-none ${alertaBloqueioClasse ? "border-orange-400 bg-orange-50 cursor-not-allowed" : "border-gray-300 focus:border-blue-500"}`}
                      value={form.classeFarmacologica}
                      disabled={!!alertaBloqueioClasse}
                      onChange={(e) => { setForm((f) => ({ ...f, classeFarmacologica: e.target.value })); setErroForm(""); }}
                    >
                      <option value="">Selecione</option>
                      {CLASSES.map((c) => <option key={c} value={c}>{c}</option>)}
                    </select>
                    {alertaBloqueioClasse && <p className="text-xs text-orange-600 mt-1 flex items-center gap-1"><Lock className="w-3 h-3" /> Bloqueado — prescrições nos últimos 90 dias</p>}
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block">Via de Administração <span className="text-red-500">*</span></label>
                    <select className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" value={form.viaAdministracao} onChange={(e) => setForm((f) => ({ ...f, viaAdministracao: e.target.value as ViaAdministracao }))}>
                      {VIAS.map((v) => <option key={v} value={v}>{v}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block">Status</label>
                    <div className="flex gap-2">
                      {(["ATIVO", "INATIVO"] as StatusMedicamento[]).map((s) => (
                        <button key={s} type="button" onClick={() => setForm((f) => ({ ...f, status: s }))} className={`flex-1 py-2 border-2 font-bold text-sm ${form.status === s ? "border-blue-500 bg-blue-500 text-white" : "border-gray-400 bg-white hover:bg-gray-50"}`}>{s}</button>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {/* Seção 2: Apresentações */}
              <div>
                <div className="text-xs font-bold text-gray-500 uppercase mb-3 border-b border-gray-200 pb-1">Apresentações Disponíveis</div>
                <div className="flex gap-2 mb-2">
                  <input type="text" className="flex-1 border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" placeholder="Ex: Cáps. 500mg" value={form.novaApresentacao} onChange={(e) => setForm((f) => ({ ...f, novaApresentacao: e.target.value }))} onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); adicionarApresentacao(); } }} />
                  <button onClick={adicionarApresentacao} className="px-3 py-2 border-2 border-blue-500 bg-white text-blue-600 font-bold text-sm hover:bg-blue-50">+ Adicionar</button>
                </div>
                <div className="flex flex-wrap gap-2">
                  {form.apresentacoes.map((a, i) => (
                    <span key={i} className="bg-blue-100 border border-blue-400 px-2 py-1 rounded text-xs flex items-center gap-1">{a}<button onClick={() => removerApresentacao(i)} className="text-blue-500 hover:text-red-500"><X className="w-3 h-3" /></button></span>
                  ))}
                  {form.apresentacoes.length === 0 && <span className="text-xs text-gray-400 italic">Nenhuma apresentação adicionada.</span>}
                </div>
              </div>

              {/* Seção 3: Posologias */}
              <div>
                <div className="text-xs font-bold text-gray-500 uppercase mb-3 border-b border-gray-200 pb-1">Posologias Padrão</div>
                <div className="flex gap-2 mb-2">
                  <input type="text" className="flex-1 border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none" placeholder="Ex: 1 cáps. a cada 8h por 7 dias" value={form.novaPosologia} onChange={(e) => setForm((f) => ({ ...f, novaPosologia: e.target.value }))} onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); adicionarPosologia(); } }} />
                  <button onClick={adicionarPosologia} className="px-3 py-2 border-2 border-blue-500 bg-white text-blue-600 font-bold text-sm hover:bg-blue-50">+ Adicionar</button>
                </div>
                <div className="space-y-1">
                  {form.posologiasPadrao.map((p, i) => (
                    <div key={i} className="flex items-center justify-between bg-gray-50 border border-gray-200 px-3 py-2 rounded text-sm">
                      <span>{i + 1}. {p}</span>
                      <button onClick={() => removerPosologia(i)} className="text-gray-400 hover:text-red-500"><X className="w-3 h-3" /></button>
                    </div>
                  ))}
                  {form.posologiasPadrao.length === 0 && <span className="text-xs text-gray-400 italic">Nenhuma posologia padrão adicionada.</span>}
                </div>
              </div>

              {/* Seção 4: Informações Clínicas */}
              <div>
                <div className="text-xs font-bold text-gray-500 uppercase mb-3 border-b border-gray-200 pb-1">Informações Clínicas (opcional)</div>
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-bold mb-2 block flex items-center gap-1"><AlertTriangle className="w-4 h-4 text-yellow-500" /> Contraindicações</label>
                    <textarea className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none resize-none" rows={2} placeholder="Ex: gestação, insuficiência renal grave, hipersensibilidade" value={form.contraindicacoes} onChange={(e) => setForm((f) => ({ ...f, contraindicacoes: e.target.value }))} />
                  </div>
                  <div>
                    <label className="text-sm font-bold mb-2 block flex items-center gap-1"><AlertTriangle className="w-4 h-4 text-orange-500" /> Interações Medicamentosas</label>
                    <textarea className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none resize-none" rows={2} placeholder="Ex: não associar com anticoagulantes orais" value={form.interacoes} onChange={(e) => setForm((f) => ({ ...f, interacoes: e.target.value }))} />
                  </div>
                </div>
              </div>

              {erroForm && <div className="border-2 border-red-500 bg-red-50 p-3 text-red-700 text-sm font-bold">{erroForm}</div>}
            </div>

            <div className="sticky bottom-0 bg-white border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalForm({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Cancelar</button>
              <button onClick={handleSalvar} className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600">Salvar Medicamento</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Ver / Ficha + Auditoria ══ */}
      {modalVer.aberto && modalVer.medicamento && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-blue-500 w-[680px] max-h-[90vh] overflow-y-auto">
            <div className="bg-blue-50 p-4 border-b-2 border-gray-400 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Pill className="w-6 h-6 text-blue-600" />
                <div>
                  <div className="font-bold text-lg text-gray-800">{modalVer.medicamento.nomeComercial}</div>
                  <div className="text-sm text-gray-500">{modalVer.medicamento.principioAtivo}</div>
                </div>
                <StatusBadge status={modalVer.medicamento.status} />
              </div>
              <button onClick={() => setModalVer({ aberto: false })} className="text-xl leading-none text-gray-400 hover:text-gray-700">✕</button>
            </div>

            {/* Abas */}
            <div className="flex border-b-2 border-gray-300">
              <button onClick={() => setAbaModalVer("ficha")} className={`px-5 py-3 text-sm font-bold flex items-center gap-2 border-b-2 ${abaModalVer === "ficha" ? "border-blue-500 text-blue-600" : "border-transparent text-gray-500 hover:text-gray-700"}`}>
                <ClipboardList className="w-4 h-4" /> Ficha
              </button>
              <button onClick={() => setAbaModalVer("auditoria")} className={`px-5 py-3 text-sm font-bold flex items-center gap-2 border-b-2 ${abaModalVer === "auditoria" ? "border-blue-500 text-blue-600" : "border-transparent text-gray-500 hover:text-gray-700"}`}>
                <History className="w-4 h-4" /> Auditoria
                <span className="bg-gray-200 text-gray-600 text-xs px-1.5 py-0.5 rounded-full">{modalVer.medicamento.logs.length}</span>
              </button>
            </div>

            {abaModalVer === "ficha" && (
              <div className="p-6 space-y-5">
                <div className="grid grid-cols-2 gap-3 text-sm">
                  {[
                    { label: "Categoria Terapêutica", value: <CategoriaBadge categoria={modalVer.medicamento.categoria} /> },
                    { label: "Classe Farmacológica", value: <span className="flex items-center gap-1">{modalVer.medicamento.classeFarmacologica} {classeProtegida(modalVer.medicamento) && <span className="inline-flex items-center gap-1 text-xs text-orange-600 font-bold"><Lock className="w-3 h-3" /> Protegida</span>}</span> },
                    { label: "Via de Administração", value: modalVer.medicamento.viaAdministracao },
                  ].map((row) => (
                    <div key={row.label} className="border border-gray-200 p-3 rounded">
                      <div className="text-xs text-gray-500 mb-1">{row.label}</div>
                      <div className="font-bold">{row.value}</div>
                    </div>
                  ))}
                </div>

                {modalVer.medicamento.apresentacoes.length > 0 && (
                  <div>
                    <div className="text-xs font-bold text-gray-500 uppercase mb-2">Apresentações</div>
                    <div className="flex flex-wrap gap-2">
                      {modalVer.medicamento.apresentacoes.map((a, i) => (
                        <span key={i} className="bg-blue-100 border border-blue-400 px-2 py-1 rounded text-xs">{a}</span>
                      ))}
                    </div>
                  </div>
                )}

                {modalVer.medicamento.posologiasPadrao.length > 0 && (
                  <div>
                    <div className="text-xs font-bold text-gray-500 uppercase mb-2">Posologias Padrão</div>
                    <div className="space-y-1">
                      {modalVer.medicamento.posologiasPadrao.map((p, i) => (
                        <div key={i} className="bg-gray-50 border border-gray-200 p-2 rounded text-sm">{i + 1}. {p}</div>
                      ))}
                    </div>
                  </div>
                )}

                {(modalVer.medicamento.contraindicacoes || modalVer.medicamento.interacoes) && (
                  <div className="space-y-3">
                    {modalVer.medicamento.contraindicacoes && (
                      <div className="bg-yellow-50 border border-yellow-300 p-3 rounded">
                        <div className="text-xs font-bold text-yellow-700 mb-1 flex items-center gap-1"><AlertTriangle className="w-3 h-3" /> Contraindicações</div>
                        <div className="text-sm text-gray-700">{modalVer.medicamento.contraindicacoes}</div>
                      </div>
                    )}
                    {modalVer.medicamento.interacoes && (
                      <div className="bg-orange-50 border border-orange-300 p-3 rounded">
                        <div className="text-xs font-bold text-orange-700 mb-1 flex items-center gap-1"><AlertTriangle className="w-3 h-3" /> Interações Medicamentosas</div>
                        <div className="text-sm text-gray-700">{modalVer.medicamento.interacoes}</div>
                      </div>
                    )}
                  </div>
                )}

                <div className="bg-gray-50 border-2 border-gray-300 p-4">
                  <div className="text-xs font-bold text-gray-500 uppercase mb-3 flex items-center gap-1"><BarChart2 className="w-4 h-4" /> Uso no Consultório</div>
                  <div className="grid grid-cols-5 gap-3">
                    {[
                      { label: "Total prescrito", value: modalVer.medicamento.totalPrescrito + "x" },
                      { label: "Período de maior uso", value: modalVer.medicamento.periodoMaiorUso },
                      { label: "Último uso", value: modalVer.medicamento.ultimoUso },
                      { label: "Dentistas", value: modalVer.medicamento.dentistas },
                      { label: "Pacientes atendidos", value: modalVer.medicamento.pacientesAtendidos },
                    ].map((m) => (
                      <div key={m.label} className="text-center">
                        <div className="text-2xl font-bold text-blue-600">{m.value}</div>
                        <div className="text-xs text-gray-500 mt-1">{m.label}</div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {abaModalVer === "auditoria" && (
              <div className="p-6">
                <div className="flex items-center gap-2 mb-4">
                  <History className="w-5 h-5 text-gray-500" />
                  <span className="font-bold text-gray-700">Histórico de Auditoria</span>
                  <span className="text-xs text-gray-400">Registros imutáveis de todas as alterações</span>
                </div>
                {modalVer.medicamento.logs.length === 0 ? (
                  <div className="text-center text-gray-400 py-8">Nenhuma alteração registrada.</div>
                ) : (
                  <div className="space-y-2">
                    {[...modalVer.medicamento.logs].reverse().map((log) => (
                      <div key={log.id} className="border-2 border-gray-200 p-3 rounded bg-white hover:bg-gray-50">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <AcaoBadge acao={log.acao} />
                            <span className="text-sm font-bold text-gray-700">{log.campo}</span>
                          </div>
                          <div className="text-xs text-gray-400">{log.data} às {log.hora} — {log.usuario}</div>
                        </div>
                        {log.acao !== "CRIACAO" && (
                          <div className="grid grid-cols-2 gap-2 mt-2">
                            <div className="bg-red-50 border border-red-200 p-2 rounded text-xs">
                              <span className="text-red-600 font-bold block mb-0.5">Anterior</span>
                              {log.valorAnterior}
                            </div>
                            <div className="bg-green-50 border border-green-200 p-2 rounded text-xs">
                              <span className="text-green-600 font-bold block mb-0.5">Atualizado</span>
                              {log.valorAtualizado}
                            </div>
                          </div>
                        )}
                        {log.acao === "CRIACAO" && (
                          <div className="text-xs text-gray-500 mt-1">{log.valorAtualizado}</div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            <div className="border-t-2 border-gray-400 p-4 flex gap-3 justify-end">
              <button onClick={() => setModalVer({ aberto: false })} className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-50">Fechar</button>
              <button onClick={() => { abrirEditar(modalVer.medicamento!); setModalVer({ aberto: false }); }} className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600">Editar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Inativar ══ */}
      {modalInativar.aberto && modalInativar.medicamento && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[500px]">
            <div className="bg-orange-50 p-4 border-b-2 border-gray-400 flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-orange-600" />
              <h2 className="text-lg font-bold text-orange-800">Inativar Medicamento</h2>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-sm text-gray-700">Você está prestes a inativar <strong>{modalInativar.medicamento.nomeComercial}</strong> ({modalInativar.medicamento.principioAtivo}).</p>

              {temPrescricoesRecentes(modalInativar.medicamento) && (
                <div className="bg-orange-50 border-2 border-orange-300 p-3 rounded text-sm text-orange-800">
                  <strong>Atenção:</strong> Este medicamento possui <strong>{modalInativar.medicamento.totalPrescrito} prescrições</strong> no histórico e foi usado
                  há <strong>{diasDesdeData(modalInativar.medicamento.dataCriacaoPrescricao)} dias</strong> (último uso: {modalInativar.medicamento.ultimoUso}).
                  Por ter prescrições nos últimos 30 dias, a justificativa é obrigatória e será registrada em auditoria.
                </div>
              )}

              <div>
                <label className="text-sm font-bold mb-2 block">Justificativa <span className="text-red-500">*</span></label>
                <textarea className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-orange-500 focus:outline-none resize-none" rows={3} placeholder="Informe o motivo da inativação..." value={justificativaInativar} onChange={(e) => { setJustificativaInativar(e.target.value); setErroJustificativa(""); }} />
                {erroJustificativa && <div className="text-red-600 text-xs font-bold mt-1">{erroJustificativa}</div>}
              </div>

              <div className="flex gap-3">
                <button onClick={() => setModalInativar({ aberto: false })} className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold">Cancelar</button>
                <button onClick={handleInativar} className="flex-1 px-4 py-2 border-2 border-orange-500 bg-orange-500 text-white font-bold hover:bg-orange-600">Confirmar Inativação</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Importar CSV ══ */}
      {modalCSV && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[500px]">
            <div className="p-4 border-b-2 border-gray-400 flex items-center justify-between">
              <h2 className="text-lg font-bold text-gray-700 flex items-center gap-2"><Upload className="w-5 h-5" /> Importar Medicamentos via CSV</h2>
              <button onClick={() => { setModalCSV(false); setCsvCarregado(false); }} className="text-xl text-gray-400 hover:text-gray-700">✕</button>
            </div>
            <div className="p-6 space-y-4">
              {!csvCarregado ? (
                <>
                  <div className="border-2 border-dashed border-gray-400 bg-gray-50 p-8 text-center rounded cursor-pointer hover:bg-gray-100" onClick={() => setCsvCarregado(true)}>
                    <Upload className="w-10 h-10 text-gray-400 mx-auto mb-3" />
                    <div className="font-bold text-gray-600 mb-1">Arraste o arquivo CSV aqui ou clique para selecionar</div>
                    <div className="text-xs text-gray-500">Colunas esperadas: nome_comercial, principio_ativo, categoria, classe, via, apresentacoes, posologias</div>
                  </div>
                  <button onClick={() => setModalCSV(false)} className="w-full px-4 py-2 border-2 border-gray-400 bg-white font-bold">Cancelar</button>
                </>
              ) : (
                <>
                  <div className="bg-green-50 border-2 border-green-500 p-3 rounded text-sm text-green-800 font-bold flex items-center gap-2"><FileText className="w-4 h-4" /> 12 medicamentos válidos para importação</div>
                  <div className="bg-red-50 border-2 border-red-500 p-3 rounded text-sm">
                    <div className="font-bold text-red-700 mb-2">2 linhas com erro — não serão importadas:</div>
                    <ul className="space-y-1 text-red-600 text-xs">
                      <li>• Linha 4: Nome comercial duplicado (Amoxil já existe no catálogo)</li>
                      <li>• Linha 9: Classe farmacológica "Sulfonamidas" não reconhecida pelo sistema</li>
                    </ul>
                  </div>
                  <div className="flex gap-3">
                    <button onClick={() => { setModalCSV(false); setCsvCarregado(false); }} className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold">Cancelar</button>
                    <button onClick={() => { showToast("12 medicamentos importados com sucesso.", "sucesso"); setModalCSV(false); setCsvCarregado(false); }} className="flex-1 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600">Importar 12 válidos</button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ══ Modal: Alerta de Contraindicação Cruzada ══ */}
      {modalContraIndicacao.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-red-500 w-[520px]">
            <div className="bg-red-50 p-4 border-b-2 border-gray-400 flex items-center gap-3">
              <div className="bg-red-500 text-white rounded-full w-9 h-9 flex items-center justify-center font-bold text-lg">!</div>
              <div>
                <h2 className="text-lg font-bold text-red-800">Alerta de Contraindicação</h2>
                <p className="text-sm text-red-600">Conflito identificado entre medicamento e anamnese do paciente</p>
              </div>
            </div>
            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div className="border border-gray-200 p-3 rounded">
                  <div className="text-xs text-gray-500 mb-1">Paciente</div>
                  <div className="font-bold">{PACIENTES_MOCK.find((p) => p.id === modalContraIndicacao.pacienteId)?.nome}</div>
                </div>
                <div className="border border-gray-200 p-3 rounded">
                  <div className="text-xs text-gray-500 mb-1">Medicamento</div>
                  <div className="font-bold">{modalContraIndicacao.medicamento?.nomeComercial}</div>
                </div>
              </div>

              <div className="space-y-2">
                {modalContraIndicacao.alertas.map((alerta, i) => (
                  <div key={i} className="bg-red-50 border-2 border-red-400 p-3 rounded flex items-start gap-2">
                    <AlertTriangle className="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5" />
                    <p className="text-sm text-red-700">{alerta}</p>
                  </div>
                ))}
              </div>

              <div className="bg-gray-50 border border-gray-200 p-3 rounded text-xs text-gray-600">
                <strong>Classe farmacológica do medicamento:</strong> {modalContraIndicacao.medicamento?.classeFarmacologica}<br />
                <strong>Contraindicações registradas:</strong> {modalContraIndicacao.medicamento?.contraindicacoes || "—"}
              </div>

              <button onClick={() => setModalContraIndicacao({ aberto: false, alertas: [] })} className="w-full px-4 py-3 border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600">
                Entendido — Fechar Alerta
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
