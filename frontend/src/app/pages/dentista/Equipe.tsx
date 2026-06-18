import { useState, useMemo, useEffect } from "react";
import { api } from "../../api/client";
import {
  Plus, User, Phone, BadgeCheck, Search, FileText, BarChart2,
  History, ShieldAlert, Lock, AlertTriangle, Clock, Calendar,
  ChevronDown, X, CheckCircle, XCircle
} from "lucide-react";

// ─── Tipos ────────────────────────────────────────────────────────────────────

type StatusColaborador = "Ativo" | "Inativo" | "Suspenso" | "Férias" | "Afastado";
type FuncaoColaborador = "Recepcionista" | "Auxiliar" | "Especialista" | "Administrador";

interface LogAuditoria {
  id: number;
  data: string;
  hora: string;
  acao: string;
  modulo: string;
  ip: string;
}

interface HistoricoAlteracao {
  id: number;
  campo: string;
  valorAnterior: string;
  valorAtualizado: string;
  responsavel: string;
  data: string;
}

interface Colaborador {
  id: number;
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
  funcao: FuncaoColaborador;
  nivelAcesso: string;
  status: StatusColaborador;
  login: string;
  registro: string;
  tentativasLogin: number;
  // Disponibilidade
  diasDisponiveis: string[];
  horarioInicio: string;
  horarioFim: string;
  periodoAusencia: { inicio: string; fim: string; motivo: string };
  // Métricas de desempenho
  metricas: {
    atendimentos: number;
    faltas: number;
    taxaConversao: number;
    avaliacaoMedia: number;
    procedimentosRealizados: number;
    agendamentosCriados: number;
    esterilizacoesRegistradas: number;
  };
  logAuditoria: LogAuditoria[];
  historicoAlteracoes: HistoricoAlteracao[];
}

// ─── Mapeamento de permissões por função ──────────────────────────────────────
// Regra de negócio: permissões são definidas automaticamente pelo perfil do colaborador
const PERMISSOES_POR_FUNCAO: Record<FuncaoColaborador, { modulo: string; permitido: boolean }[]> = {
  Recepcionista: [
    { modulo: "Agenda", permitido: true },
    { modulo: "Cadastro de Pacientes", permitido: true },
    { modulo: "Recall", permitido: true },
    { modulo: "Acordos Financeiros", permitido: true },
    { modulo: "Prontuários Clínicos", permitido: false },
    { modulo: "Prescrições", permitido: false },
    { modulo: "Financeiro Completo", permitido: false },
    { modulo: "Gestão de Equipe", permitido: false },
    { modulo: "Estoque", permitido: false },
  ],
  Auxiliar: [
    { modulo: "Estoque", permitido: true },
    { modulo: "Esterilização", permitido: true },
    { modulo: "Instrumentos", permitido: true },
    { modulo: "Agenda", permitido: false },
    { modulo: "Prontuários Clínicos", permitido: false },
    { modulo: "Prescrições", permitido: false },
    { modulo: "Financeiro Completo", permitido: false },
    { modulo: "Gestão de Equipe", permitido: false },
  ],
  Especialista: [
    { modulo: "Agenda", permitido: true },
    { modulo: "Prontuários Clínicos", permitido: true },
    { modulo: "Plano de Tratamento", permitido: true },
    { modulo: "Prescrições", permitido: true },
    { modulo: "Estoque (leitura)", permitido: true },
    { modulo: "Relatórios de Performance", permitido: true },
    { modulo: "Gestão de Equipe", permitido: false },
    { modulo: "Financeiro Completo", permitido: false },
  ],
  Administrador: [
    { modulo: "Agenda", permitido: true },
    { modulo: "Pacientes e Prontuários", permitido: true },
    { modulo: "Prescrições", permitido: true },
    { modulo: "Financeiro Completo", permitido: true },
    { modulo: "Gestão de Equipe", permitido: true },
    { modulo: "Estoque", permitido: true },
    { modulo: "Configurações do Sistema", permitido: true },
    { modulo: "Relatórios Gerenciais", permitido: true },
  ],
};

// ─── Dados de exemplo ────────────────────────────────────────────────────────
const COLABORADORES_INICIAIS: Colaborador[] = [
  {
    id: 1,
    nome: "Ana Paula Silva",
    cpf: "123.456.789-01",
    telefone: "(11) 98765-4321",
    email: "ana.silva@odontohub.com",
    funcao: "Recepcionista",
    nivelAcesso: "Recepcionista",
    status: "Ativo",
    login: "ana.silva",
    registro: "-",
    tentativasLogin: 0,
    diasDisponiveis: ["Segunda", "Terça", "Quarta", "Quinta", "Sexta"],
    horarioInicio: "08:00",
    horarioFim: "17:00",
    periodoAusencia: { inicio: "", fim: "", motivo: "" },
    metricas: { atendimentos: 142, faltas: 2, taxaConversao: 78, avaliacaoMedia: 4.7, procedimentosRealizados: 0, agendamentosCriados: 142, esterilizacoesRegistradas: 0 },
    logAuditoria: [
      { id: 1, data: "09/06/2026", hora: "08:15", acao: "Login realizado", modulo: "Sistema", ip: "192.168.1.10" },
      { id: 2, data: "09/06/2026", hora: "08:22", acao: "Agendamento criado - João Silva", modulo: "Agenda", ip: "192.168.1.10" },
      { id: 3, data: "08/06/2026", hora: "16:50", acao: "Logout realizado", modulo: "Sistema", ip: "192.168.1.10" },
    ],
    historicoAlteracoes: [
      { id: 1, campo: "Telefone", valorAnterior: "(11) 91111-2222", valorAtualizado: "(11) 98765-4321", responsavel: "Dr. Felipe", data: "01/03/2026" },
    ],
  },
  {
    id: 2,
    nome: "Carlos Eduardo Mendes",
    cpf: "234.567.890-12",
    telefone: "(11) 91234-5678",
    email: "carlos.mendes@odontohub.com",
    funcao: "Auxiliar",
    nivelAcesso: "Auxiliar",
    status: "Ativo",
    login: "carlos.mendes",
    registro: "ASB-SP 12345",
    tentativasLogin: 0,
    diasDisponiveis: ["Segunda", "Terça", "Quarta", "Quinta", "Sexta"],
    horarioInicio: "07:30",
    horarioFim: "16:30",
    periodoAusencia: { inicio: "", fim: "", motivo: "" },
    metricas: { atendimentos: 0, faltas: 1, taxaConversao: 0, avaliacaoMedia: 4.5, procedimentosRealizados: 230, agendamentosCriados: 0, esterilizacoesRegistradas: 87 },
    logAuditoria: [
      { id: 1, data: "09/06/2026", hora: "07:32", acao: "Login realizado", modulo: "Sistema", ip: "192.168.1.11" },
      { id: 2, data: "09/06/2026", hora: "09:10", acao: "Esterilização registrada - Lote #032", modulo: "Esterilização", ip: "192.168.1.11" },
      { id: 3, data: "09/06/2026", hora: "11:45", acao: "Entrada em estoque registrada", modulo: "Estoque", ip: "192.168.1.11" },
    ],
    historicoAlteracoes: [],
  },
  {
    id: 3,
    nome: "Beatriz Costa",
    cpf: "345.678.901-23",
    telefone: "(11) 99876-5432",
    email: "beatriz.costa@odontohub.com",
    funcao: "Recepcionista",
    nivelAcesso: "Recepcionista",
    status: "Férias",
    login: "beatriz.costa",
    registro: "-",
    tentativasLogin: 0,
    diasDisponiveis: ["Segunda", "Terça", "Quarta", "Quinta"],
    horarioInicio: "09:00",
    horarioFim: "18:00",
    periodoAusencia: { inicio: "2026-06-02", fim: "2026-06-20", motivo: "Férias anuais" },
    metricas: { atendimentos: 98, faltas: 5, taxaConversao: 65, avaliacaoMedia: 4.1, procedimentosRealizados: 0, agendamentosCriados: 98, esterilizacoesRegistradas: 0 },
    logAuditoria: [
      { id: 1, data: "01/06/2026", hora: "17:58", acao: "Logout realizado", modulo: "Sistema", ip: "192.168.1.12" },
    ],
    historicoAlteracoes: [
      { id: 1, campo: "Status", valorAnterior: "Ativo", valorAtualizado: "Férias", responsavel: "Dr. Felipe", data: "01/06/2026" },
    ],
  },
  {
    id: 4,
    nome: "Dr. Roberto Alves",
    cpf: "456.789.012-34",
    telefone: "(11) 97777-8888",
    email: "roberto.alves@odontohub.com",
    funcao: "Especialista",
    nivelAcesso: "Especialista",
    status: "Ativo",
    login: "dr.roberto",
    registro: "CRO-SP 54321",
    tentativasLogin: 0,
    diasDisponiveis: ["Terça", "Quarta", "Quinta"],
    horarioInicio: "13:00",
    horarioFim: "19:00",
    periodoAusencia: { inicio: "", fim: "", motivo: "" },
    metricas: { atendimentos: 314, faltas: 0, taxaConversao: 91, avaliacaoMedia: 4.9, procedimentosRealizados: 314, agendamentosCriados: 0, esterilizacoesRegistradas: 0 },
    logAuditoria: [
      { id: 1, data: "09/06/2026", hora: "13:02", acao: "Login realizado", modulo: "Sistema", ip: "192.168.1.13" },
      { id: 2, data: "09/06/2026", hora: "13:15", acao: "Prontuário acessado - Maria Santos", modulo: "Prontuários", ip: "192.168.1.13" },
      { id: 3, data: "09/06/2026", hora: "14:30", acao: "Prescrição registrada - Amoxicilina", modulo: "Prescrições", ip: "192.168.1.13" },
    ],
    historicoAlteracoes: [
      { id: 1, campo: "Horário de Trabalho", valorAnterior: "08:00–17:00", valorAtualizado: "13:00–19:00", responsavel: "Dr. Felipe", data: "15/01/2026" },
    ],
  },
  {
    id: 5,
    nome: "Fernanda Rocha",
    cpf: "567.890.123-45",
    telefone: "(11) 96666-7777",
    email: "fernanda.rocha@odontohub.com",
    funcao: "Auxiliar",
    nivelAcesso: "Auxiliar",
    status: "Suspenso",
    login: "fernanda.rocha",
    registro: "ASB-SP 67890",
    tentativasLogin: 5,
    diasDisponiveis: ["Segunda", "Terça", "Quarta", "Quinta", "Sexta"],
    horarioInicio: "08:00",
    horarioFim: "17:00",
    periodoAusencia: { inicio: "", fim: "", motivo: "" },
    metricas: { atendimentos: 0, faltas: 8, taxaConversao: 0, avaliacaoMedia: 3.2, procedimentosRealizados: 112, agendamentosCriados: 0, esterilizacoesRegistradas: 45 },
    logAuditoria: [
      { id: 1, data: "07/06/2026", hora: "08:03", acao: "Tentativa de login inválida (1/5)", modulo: "Sistema", ip: "192.168.1.20" },
      { id: 2, data: "07/06/2026", hora: "08:04", acao: "Tentativa de login inválida (2/5)", modulo: "Sistema", ip: "192.168.1.20" },
      { id: 3, data: "07/06/2026", hora: "08:05", acao: "Conta bloqueada após 5 tentativas — administrador notificado", modulo: "Sistema", ip: "192.168.1.20" },
    ],
    historicoAlteracoes: [
      { id: 1, campo: "Status", valorAnterior: "Ativo", valorAtualizado: "Suspenso", responsavel: "Sistema (bloqueio automático)", data: "07/06/2026" },
    ],
  },
];

// ─── Utilitários ─────────────────────────────────────────────────────────────

// Validação de formato CPF (XXX.XXX.XXX-XX)
function cpfValido(cpf: string): boolean {
  return /^\d{3}\.\d{3}\.\d{3}-\d{2}$/.test(cpf.trim());
}

// Validação de formato telefone brasileiro
function telefoneValido(tel: string): boolean {
  return /^\(\d{2}\)\s\d{4,5}-\d{4}$/.test(tel.trim());
}

const COR_STATUS: Record<StatusColaborador, string> = {
  Ativo:     "bg-green-100 text-green-800 border-green-500",
  Inativo:   "bg-gray-100 text-gray-600 border-gray-400",
  Suspenso:  "bg-red-100 text-red-800 border-red-500",
  Férias:    "bg-blue-100 text-blue-800 border-blue-500",
  Afastado:  "bg-orange-100 text-orange-800 border-orange-500",
};

const DIAS_SEMANA = ["Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"];

// ─── Estado inicial do formulário ────────────────────────────────────────────
const FORM_VAZIO = {
  nome: "", cpf: "", telefone: "", email: "", funcao: "Recepcionista" as FuncaoColaborador,
  status: "Ativo" as StatusColaborador, login: "", senha: "", registro: "",
  diasDisponiveis: ["Segunda", "Terça", "Quarta", "Quinta", "Sexta"],
  horarioInicio: "08:00", horarioFim: "17:00",
  periodoAusenciaInicio: "", periodoAusenciaFim: "", periodoAusenciaMotivo: "",
};

// ─── Componente Principal ────────────────────────────────────────────────────
// ─── Integração com o backend (GET /api/colaboradores) ────────────────────────

const FUNCAO_BACKEND: Record<string, FuncaoColaborador> = {
  AUXILIAR: "Auxiliar", RECEPCIONISTA: "Recepcionista",
  ESPECIALISTA: "Especialista", ADMINISTRADOR: "Administrador",
};
const STATUS_BACKEND: Record<string, StatusColaborador> = {
  ATIVO: "Ativo", INATIVO: "Inativo", SUSPENSO: "Suspenso", FERIAS: "Férias", AFASTADO: "Afastado",
};
const DIA_BACKEND: Record<string, string> = {
  SEGUNDA: "Seg", TERCA: "Ter", QUARTA: "Qua", QUINTA: "Qui", SEXTA: "Sex", SABADO: "Sáb", DOMINGO: "Dom",
};

function adaptarColaborador(b: any, i: number): Colaborador {
  const disp = b.disponibilidade ?? {};
  const ausencia = (disp.ausencias ?? [])[0] ?? null;
  const auditoria: any[] = b.auditoria ?? [];
  const contar = (modulo: string) => auditoria.filter((a) => a.modulo === modulo).length;
  const atendimentos = b.atendimentos ?? 0;
  const conversoes = b.conversoes ?? 0;
  const hhmm = (h: number | null | undefined) => (h != null ? `${String(h).padStart(2, "0")}:00` : "");
  return {
    id: b.id?.id ?? i + 1,
    nome: b.nomeCompleto ?? "—",
    cpf: b.cpf ?? "—",
    telefone: b.telefone ?? "—",
    email: b.email ?? "—",
    funcao: FUNCAO_BACKEND[b.funcao] ?? "Recepcionista",
    nivelAcesso: FUNCAO_BACKEND[b.funcao] ?? "—",
    status: STATUS_BACKEND[b.status] ?? "Ativo",
    login: b.login ?? "—",
    registro: "—",
    tentativasLogin: b.tentativasLoginInvalidas ?? 0,
    diasDisponiveis: (disp.diasDisponiveis ?? []).map((d: string) => DIA_BACKEND[d] ?? d),
    horarioInicio: hhmm(disp.horaInicio),
    horarioFim: hhmm(disp.horaFim),
    periodoAusencia: ausencia
      ? { inicio: ausencia.inicio ?? "", fim: ausencia.fim ?? "", motivo: ausencia.motivo ?? ausencia.tipo ?? "" }
      : { inicio: "", fim: "", motivo: "" },
    metricas: {
      atendimentos,
      faltas: b.faltas ?? 0,
      taxaConversao: atendimentos > 0 ? Math.round((conversoes / atendimentos) * 100) : 0,
      avaliacaoMedia: 0,
      procedimentosRealizados: contar("PROCEDIMENTO"),
      agendamentosCriados: contar("AGENDAMENTO"),
      esterilizacoesRegistradas: contar("ESTERILIZACAO"),
    },
    logAuditoria: auditoria.map((a, j) => {
      const [data = "", hora = ""] = (a.dataHora ?? "").split("T");
      return { id: j + 1, data, hora: hora.slice(0, 5), acao: a.acao ?? "", modulo: a.modulo ?? "", ip: "—" };
    }),
    historicoAlteracoes: (b.historicoAlteracoes ?? []).map((h: any, j: number) => ({
      id: j + 1,
      campo: h.campo ?? "",
      valorAnterior: h.valorAnterior ?? "",
      valorAtualizado: h.valorAtualizado ?? "",
      responsavel: h.responsavel ?? "",
      data: (h.data ?? "").split("T")[0],
    })),
  };
}

export default function DentistaEquipe() {
  const [colaboradores, setColaboradores] = useState<Colaborador[]>(COLABORADORES_INICIAIS);

  // Carrega os colaboradores reais do backend; mantém o mock como fallback.
  useEffect(() => {
    api.get<any[]>("/colaboradores")
      .then((lista) => {
        if (Array.isArray(lista) && lista.length > 0) {
          setColaboradores(lista.map(adaptarColaborador));
        }
      })
      .catch((e) => console.warn("Falha ao carregar colaboradores:", e));
  }, []);
  const [busca, setBusca] = useState("");
  const [filtroFuncao, setFiltroFuncao] = useState("");
  const [filtroStatus, setFiltroStatus] = useState("");

  // Modais
  const [modalCadastro, setModalCadastro] = useState<{ aberto: boolean; editando?: Colaborador }>({ aberto: false });
  const [modalIndicadores, setModalIndicadores] = useState<{ aberto: boolean; colab?: Colaborador }>({ aberto: false });
  const [modalAuditoria, setModalAuditoria] = useState<{ aberto: boolean; colab?: Colaborador }>({ aberto: false });
  const [modalHistorico, setModalHistorico] = useState<{ aberto: boolean; colab?: Colaborador }>({ aberto: false });
  const [modalStatus, setModalStatus] = useState<{ aberto: boolean; colab?: Colaborador }>({ aberto: false });

  // Formulário de cadastro/edição
  const [form, setForm] = useState({ ...FORM_VAZIO });
  const [abaModal, setAbaModal] = useState<"dados" | "disponibilidade" | "permissoes">("dados");
  const [erros, setErros] = useState<Record<string, string>>({});

  // ─── Filtragem ──────────────────────────────────────────────────────────
  const colaboradoresFiltrados = useMemo(() => {
    return colaboradores.filter((c) => {
      if (busca && !c.nome.toLowerCase().includes(busca.toLowerCase())) return false;
      if (filtroFuncao && c.funcao !== filtroFuncao) return false;
      if (filtroStatus && c.status !== filtroStatus) return false;
      return true;
    });
  }, [colaboradores, busca, filtroFuncao, filtroStatus]);

  // Contadores de status para os cards de resumo
  const contagens = useMemo(() => ({
    total: colaboradores.length,
    ativos: colaboradores.filter((c) => c.status === "Ativo").length,
    ferias: colaboradores.filter((c) => c.status === "Férias" || c.status === "Afastado").length,
    suspensos: colaboradores.filter((c) => c.status === "Suspenso").length,
    bloqueados: colaboradores.filter((c) => c.tentativasLogin >= 5).length,
  }), [colaboradores]);

  // ─── Abertura do modal de cadastro ──────────────────────────────────────
  function abrirCadastro(editando?: Colaborador) {
    if (editando) {
      setForm({
        nome: editando.nome, cpf: editando.cpf, telefone: editando.telefone,
        email: editando.email, funcao: editando.funcao, status: editando.status,
        login: editando.login, senha: "", registro: editando.registro,
        diasDisponiveis: [...editando.diasDisponiveis],
        horarioInicio: editando.horarioInicio, horarioFim: editando.horarioFim,
        periodoAusenciaInicio: editando.periodoAusencia.inicio,
        periodoAusenciaFim: editando.periodoAusencia.fim,
        periodoAusenciaMotivo: editando.periodoAusencia.motivo,
      });
    } else {
      setForm({ ...FORM_VAZIO, diasDisponiveis: [...FORM_VAZIO.diasDisponiveis] });
    }
    setErros({});
    setAbaModal("dados");
    setModalCadastro({ aberto: true, editando });
  }

  // ─── Validação do formulário ─────────────────────────────────────────────
  // Regra de negócio: impede CPF duplicado, telefone inválido, cadastro incompleto, e-mail já utilizado
  function validarForm(): boolean {
    const novosErros: Record<string, string> = {};
    if (!form.nome.trim()) novosErros.nome = "Nome completo é obrigatório.";
    if (!form.cpf.trim()) {
      novosErros.cpf = "CPF é obrigatório.";
    } else if (!cpfValido(form.cpf)) {
      novosErros.cpf = "Formato inválido. Use XXX.XXX.XXX-XX.";
    } else {
      // Verificar duplicidade de CPF (exceto o próprio colaborador em edição)
      const cpfExiste = colaboradores.some(
        (c) => c.cpf === form.cpf && c.id !== modalCadastro.editando?.id
      );
      if (cpfExiste) novosErros.cpf = "CPF já cadastrado no sistema.";
    }
    if (!form.telefone.trim()) {
      novosErros.telefone = "Telefone é obrigatório.";
    } else if (!telefoneValido(form.telefone)) {
      novosErros.telefone = "Formato inválido. Use (XX) XXXXX-XXXX.";
    }
    if (!form.email.trim()) {
      novosErros.email = "E-mail é obrigatório.";
    } else {
      // Verificar duplicidade de e-mail
      const emailExiste = colaboradores.some(
        (c) => c.email === form.email && c.id !== modalCadastro.editando?.id
      );
      if (emailExiste) novosErros.email = "E-mail já utilizado por outro colaborador.";
    }
    if (!form.funcao) novosErros.funcao = "Função é obrigatória.";
    if (!form.login.trim()) novosErros.login = "Login é obrigatório.";
    if (!modalCadastro.editando && !form.senha.trim()) novosErros.senha = "Senha é obrigatória para novos cadastros.";
    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  }

  // ─── Salvar cadastro ─────────────────────────────────────────────────────
  function salvarColaborador() {
    if (!validarForm()) return;

    const agora = new Date();
    const dataHoje = agora.toLocaleDateString("pt-BR");

    if (modalCadastro.editando) {
      // Edição: gerar histórico de alterações para campos modificados
      const original = modalCadastro.editando;
      const novasAlteracoes: HistoricoAlteracao[] = [];
      const camposVerificar: { campo: string; ant: string; novo: string }[] = [
        { campo: "Nome", ant: original.nome, novo: form.nome },
        { campo: "Telefone", ant: original.telefone, novo: form.telefone },
        { campo: "E-mail", ant: original.email, novo: form.email },
        { campo: "Função", ant: original.funcao, novo: form.funcao },
        { campo: "Status", ant: original.status, novo: form.status },
        { campo: "Login", ant: original.login, novo: form.login },
        { campo: "Registro Profissional", ant: original.registro, novo: form.registro },
        { campo: "Horário de Trabalho", ant: `${original.horarioInicio}–${original.horarioFim}`, novo: `${form.horarioInicio}–${form.horarioFim}` },
        { campo: "Dias Disponíveis", ant: original.diasDisponiveis.join(", "), novo: form.diasDisponiveis.join(", ") },
      ];
      camposVerificar.forEach(({ campo, ant, novo }) => {
        if (ant !== novo) {
          novasAlteracoes.push({
            id: Date.now() + Math.random(),
            campo, valorAnterior: ant, valorAtualizado: novo,
            responsavel: "Dr. Felipe", data: dataHoje,
          });
        }
      });

      setColaboradores((prev) =>
        prev.map((c) =>
          c.id === original.id
            ? {
                ...c,
                nome: form.nome, cpf: form.cpf, telefone: form.telefone,
                email: form.email, funcao: form.funcao, nivelAcesso: form.funcao,
                status: form.status, login: form.login, registro: form.registro,
                diasDisponiveis: [...form.diasDisponiveis],
                horarioInicio: form.horarioInicio, horarioFim: form.horarioFim,
                periodoAusencia: {
                  inicio: form.periodoAusenciaInicio,
                  fim: form.periodoAusenciaFim,
                  motivo: form.periodoAusenciaMotivo,
                },
                historicoAlteracoes: [...c.historicoAlteracoes, ...novasAlteracoes],
              }
            : c
        )
      );
    } else {
      // Novo colaborador
      const novo: Colaborador = {
        id: Date.now(),
        nome: form.nome, cpf: form.cpf, telefone: form.telefone,
        email: form.email, funcao: form.funcao, nivelAcesso: form.funcao,
        status: form.status, login: form.login, registro: form.registro,
        tentativasLogin: 0,
        diasDisponiveis: [...form.diasDisponiveis],
        horarioInicio: form.horarioInicio, horarioFim: form.horarioFim,
        periodoAusencia: {
          inicio: form.periodoAusenciaInicio,
          fim: form.periodoAusenciaFim,
          motivo: form.periodoAusenciaMotivo,
        },
        metricas: { atendimentos: 0, faltas: 0, taxaConversao: 0, avaliacaoMedia: 0, procedimentosRealizados: 0, agendamentosCriados: 0, esterilizacoesRegistradas: 0 },
        logAuditoria: [{ id: 1, data: dataHoje, hora: agora.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }), acao: "Colaborador cadastrado no sistema", modulo: "Gestão de Equipe", ip: "192.168.1.1" }],
        historicoAlteracoes: [],
      };
      setColaboradores((prev) => [...prev, novo]);
    }
    setModalCadastro({ aberto: false });
  }

  // ─── Alterar status rapidamente ──────────────────────────────────────────
  function confirmarAlteracaoStatus(novoStatus: StatusColaborador) {
    if (!modalStatus.colab) return;
    const dataHoje = new Date().toLocaleDateString("pt-BR");
    setColaboradores((prev) =>
      prev.map((c) =>
        c.id === modalStatus.colab!.id
          ? {
              ...c,
              status: novoStatus,
              tentativasLogin: novoStatus === "Ativo" ? 0 : c.tentativasLogin,
              historicoAlteracoes: [
                ...c.historicoAlteracoes,
                {
                  id: Date.now(),
                  campo: "Status",
                  valorAnterior: c.status,
                  valorAtualizado: novoStatus,
                  responsavel: "Dr. Felipe",
                  data: dataHoje,
                },
              ],
            }
          : c
      )
    );
    setModalStatus({ aberto: false });
  }

  // ─── Toggle dia disponível no formulário ─────────────────────────────────
  function toggleDia(dia: string) {
    setForm((f) => ({
      ...f,
      diasDisponiveis: f.diasDisponiveis.includes(dia)
        ? f.diasDisponiveis.filter((d) => d !== dia)
        : [...f.diasDisponiveis, dia],
    }));
  }

  // ─── Render ──────────────────────────────────────────────────────────────
  return (
    <div className="p-6 bg-gray-50 min-h-full">

      {/* Cabeçalho */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-gray-700">Gestão de Equipe e Colaboradores</h1>
        <button
          onClick={() => abrirCadastro()}
          className="flex items-center gap-2 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold rounded hover:bg-blue-600 transition-colors"
        >
          <Plus className="w-4 h-4" /> Cadastrar Colaborador
        </button>
      </div>

      {/* Cards de Resumo */}
      <div className="grid grid-cols-5 gap-4 mb-6">
        {[
          { label: "Total", valor: contagens.total, cor: "border-gray-400 bg-white" },
          { label: "Ativos", valor: contagens.ativos, cor: "border-green-500 bg-green-50" },
          { label: "Ausentes", valor: contagens.ferias, cor: "border-blue-500 bg-blue-50" },
          { label: "Suspensos", valor: contagens.suspensos, cor: "border-red-500 bg-red-50" },
          { label: "Bloqueados (Login)", valor: contagens.bloqueados, cor: "border-orange-500 bg-orange-50" },
        ].map(({ label, valor, cor }) => (
          <div key={label} className={`border-2 ${cor} p-4 rounded`}>
            <div className="text-2xl font-bold text-gray-800">{valor}</div>
            <div className="text-xs text-gray-500 font-bold mt-1">{label}</div>
          </div>
        ))}
      </div>

      {/* Barra de filtros */}
      <div className="bg-white border-2 border-gray-400 p-4 mb-4">
        <div className="grid grid-cols-4 gap-4">
          <div className="col-span-2 relative">
            <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nome..."
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              className="w-full pl-9 border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none text-sm"
            />
          </div>
          <select
            value={filtroFuncao}
            onChange={(e) => setFiltroFuncao(e.target.value)}
            className="border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none text-sm bg-white"
          >
            <option value="">Todas as funções</option>
            <option>Recepcionista</option>
            <option>Auxiliar</option>
            <option>Especialista</option>
            <option>Administrador</option>
          </select>
          <select
            value={filtroStatus}
            onChange={(e) => setFiltroStatus(e.target.value)}
            className="border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none text-sm bg-white"
          >
            <option value="">Todos os status</option>
            <option>Ativo</option>
            <option>Inativo</option>
            <option>Suspenso</option>
            <option>Férias</option>
            <option>Afastado</option>
          </select>
        </div>
      </div>

      {/* Tabela de colaboradores */}
      <div className="border-2 border-gray-400 bg-white overflow-x-auto">
        <div className="min-w-[1100px]">
          <div className="grid grid-cols-7 bg-gray-100 border-b-2 border-gray-400 text-xs font-bold text-gray-600 uppercase">
            <div className="p-3 border-r-2 border-gray-400 col-span-2">Colaborador</div>
            <div className="p-3 border-r-2 border-gray-400">Função / Nível</div>
            <div className="p-3 border-r-2 border-gray-400">Status</div>
            <div className="p-3 border-r-2 border-gray-400">Contato</div>
            <div className="p-3 border-r-2 border-gray-400">Tentativas Login</div>
            <div className="p-3 text-center">Ações</div>
          </div>

          {colaboradoresFiltrados.length === 0 ? (
            <div className="p-8 text-center text-gray-500 text-sm">
              Nenhum colaborador encontrado para os filtros selecionados.
            </div>
          ) : (
            colaboradoresFiltrados.map((colab) => (
              <div key={colab.id} className="grid grid-cols-7 border-b-2 border-gray-400 items-center text-sm hover:bg-gray-50">
                {/* Nome */}
                <div className="p-3 border-r-2 border-gray-400 col-span-2 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                    <User className="w-4 h-4 text-blue-600" />
                  </div>
                  <div>
                    <div className="font-bold text-gray-800">{colab.nome}</div>
                    <div className="text-xs text-gray-400">{colab.login}</div>
                  </div>
                </div>

                {/* Função */}
                <div className="p-3 border-r-2 border-gray-400">
                  <div className="font-bold text-gray-700">{colab.funcao}</div>
                  <div className="text-xs text-gray-400">{colab.registro !== "-" ? colab.registro : "Sem registro"}</div>
                </div>

                {/* Status */}
                <div className="p-3 border-r-2 border-gray-400">
                  <span className={`px-2 py-1 rounded text-xs font-bold border ${COR_STATUS[colab.status]}`}>
                    {colab.status}
                  </span>
                  {/* Alerta de bloqueio por segurança */}
                  {colab.tentativasLogin >= 5 && (
                    <div className="flex items-center gap-1 mt-1 text-xs text-red-600">
                      <Lock className="w-3 h-3" /> Conta bloqueada
                    </div>
                  )}
                </div>

                {/* Contato */}
                <div className="p-3 border-r-2 border-gray-400 text-gray-600">
                  <div className="flex items-center gap-1">
                    <Phone className="w-3 h-3 text-gray-400" />
                    {colab.telefone}
                  </div>
                  <div className="text-xs text-gray-400 truncate">{colab.email}</div>
                </div>

                {/* Tentativas de login */}
                <div className="p-3 border-r-2 border-gray-400 text-center">
                  <span className={`text-sm font-bold ${colab.tentativasLogin >= 5 ? "text-red-600" : colab.tentativasLogin >= 3 ? "text-orange-500" : "text-gray-500"}`}>
                    {colab.tentativasLogin}/5
                  </span>
                  {colab.tentativasLogin >= 3 && (
                    <div className="text-xs text-orange-500 flex items-center justify-center gap-1 mt-1">
                      <AlertTriangle className="w-3 h-3" /> Atenção
                    </div>
                  )}
                </div>

                {/* Ações */}
                <div className="p-3 flex items-center justify-center gap-1 flex-wrap">
                  <button
                    onClick={() => abrirCadastro(colab)}
                    title="Editar"
                    className="px-2 py-1 border-2 border-blue-500 text-blue-600 text-xs font-bold rounded hover:bg-blue-50 transition-colors"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => setModalStatus({ aberto: true, colab })}
                    title="Alterar status"
                    className="px-2 py-1 border-2 border-gray-400 text-gray-600 text-xs font-bold rounded hover:bg-gray-50 transition-colors flex items-center gap-1"
                  >
                    <ChevronDown className="w-3 h-3" /> Status
                  </button>
                  <button
                    onClick={() => setModalIndicadores({ aberto: true, colab })}
                    title="Indicadores de Desempenho"
                    className="px-2 py-1 border-2 border-green-500 text-green-700 text-xs font-bold rounded hover:bg-green-50 transition-colors"
                  >
                    <BarChart2 className="w-3 h-3" />
                  </button>
                  <button
                    onClick={() => setModalAuditoria({ aberto: true, colab })}
                    title="Log de Auditoria"
                    className="px-2 py-1 border-2 border-orange-400 text-orange-700 text-xs font-bold rounded hover:bg-orange-50 transition-colors"
                  >
                    <FileText className="w-3 h-3" />
                  </button>
                  <button
                    onClick={() => setModalHistorico({ aberto: true, colab })}
                    title="Histórico de Alterações"
                    className="px-2 py-1 border-2 border-purple-400 text-purple-700 text-xs font-bold rounded hover:bg-purple-50 transition-colors"
                  >
                    <History className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="mt-3 text-xs text-gray-500">
        Exibindo {colaboradoresFiltrados.length} de {colaboradores.length} colaboradores
      </div>


      {/* ═══ Modal: Cadastrar / Editar Colaborador ══════════════════════════════ */}
      {modalCadastro.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white border-4 border-gray-400 w-full max-w-2xl max-h-[90vh] flex flex-col">
            <div className="p-4 border-b-2 border-gray-400 bg-gray-50 flex justify-between items-center">
              <h2 className="text-lg font-bold text-gray-700">
                {modalCadastro.editando ? `Editar: ${modalCadastro.editando.nome}` : "Cadastrar Novo Colaborador"}
              </h2>
              <button onClick={() => setModalCadastro({ aberto: false })} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Abas do modal */}
            <div className="flex border-b-2 border-gray-400">
              {(["dados", "disponibilidade", "permissoes"] as const).map((aba) => (
                <button
                  key={aba}
                  onClick={() => setAbaModal(aba)}
                  className={`flex-1 p-3 font-bold text-sm border-r-2 border-gray-400 last:border-r-0 ${
                    abaModal === aba ? "bg-blue-500 text-white" : "bg-gray-100 hover:bg-gray-200"
                  }`}
                >
                  {aba === "dados" ? "Dados Cadastrais" : aba === "disponibilidade" ? "Disponibilidade" : "Permissões"}
                </button>
              ))}
            </div>

            <div className="p-6 overflow-y-auto flex-1">

              {/* Aba: Dados Cadastrais */}
              {abaModal === "dados" && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    {/* Nome */}
                    <div className="col-span-2">
                      <label className="text-sm font-bold block mb-1">Nome Completo <span className="text-red-500">*</span></label>
                      <input
                        type="text"
                        value={form.nome}
                        onChange={(e) => setForm({ ...form, nome: e.target.value })}
                        className={`w-full border-2 p-2 rounded focus:outline-none ${erros.nome ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        placeholder="Nome completo do colaborador"
                      />
                      {erros.nome && <p className="text-xs text-red-500 mt-1">{erros.nome}</p>}
                    </div>

                    {/* CPF */}
                    <div>
                      <label className="text-sm font-bold block mb-1">CPF <span className="text-red-500">*</span></label>
                      <input
                        type="text"
                        value={form.cpf}
                        onChange={(e) => setForm({ ...form, cpf: e.target.value })}
                        className={`w-full border-2 p-2 rounded focus:outline-none ${erros.cpf ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        placeholder="XXX.XXX.XXX-XX"
                      />
                      {erros.cpf && <p className="text-xs text-red-500 mt-1">{erros.cpf}</p>}
                    </div>

                    {/* Telefone */}
                    <div>
                      <label className="text-sm font-bold block mb-1">Telefone <span className="text-red-500">*</span></label>
                      <input
                        type="text"
                        value={form.telefone}
                        onChange={(e) => setForm({ ...form, telefone: e.target.value })}
                        className={`w-full border-2 p-2 rounded focus:outline-none ${erros.telefone ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        placeholder="(XX) XXXXX-XXXX"
                      />
                      {erros.telefone && <p className="text-xs text-red-500 mt-1">{erros.telefone}</p>}
                    </div>

                    {/* E-mail */}
                    <div className="col-span-2">
                      <label className="text-sm font-bold block mb-1">E-mail <span className="text-red-500">*</span></label>
                      <input
                        type="email"
                        value={form.email}
                        onChange={(e) => setForm({ ...form, email: e.target.value })}
                        className={`w-full border-2 p-2 rounded focus:outline-none ${erros.email ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        placeholder="email@odontohub.com"
                      />
                      {erros.email && <p className="text-xs text-red-500 mt-1">{erros.email}</p>}
                    </div>

                    {/* Função */}
                    <div>
                      <label className="text-sm font-bold block mb-1">Função <span className="text-red-500">*</span></label>
                      <select
                        value={form.funcao}
                        onChange={(e) => setForm({ ...form, funcao: e.target.value as FuncaoColaborador })}
                        className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none bg-white"
                      >
                        <option value="Recepcionista">Recepcionista</option>
                        <option value="Auxiliar">Auxiliar</option>
                        <option value="Especialista">Especialista</option>
                        <option value="Administrador">Administrador</option>
                      </select>
                      <p className="text-xs text-gray-400 mt-1">As permissões são definidas automaticamente pela função.</p>
                    </div>

                    {/* Status */}
                    <div>
                      <label className="text-sm font-bold block mb-1">Status Operacional <span className="text-red-500">*</span></label>
                      <select
                        value={form.status}
                        onChange={(e) => setForm({ ...form, status: e.target.value as StatusColaborador })}
                        className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none bg-white"
                      >
                        <option value="Ativo">Ativo</option>
                        <option value="Inativo">Inativo</option>
                        <option value="Suspenso">Suspenso</option>
                        <option value="Férias">Férias</option>
                        <option value="Afastado">Afastado</option>
                      </select>
                      {(form.status === "Inativo" || form.status === "Suspenso") && (
                        <p className="text-xs text-red-600 mt-1 flex items-center gap-1">
                          <AlertTriangle className="w-3 h-3" /> Usuário não poderá realizar login.
                        </p>
                      )}
                    </div>

                    {/* Registro Profissional */}
                    <div>
                      <label className="text-sm font-bold block mb-1">Registro Profissional</label>
                      <input
                        type="text"
                        value={form.registro}
                        onChange={(e) => setForm({ ...form, registro: e.target.value })}
                        className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none"
                        placeholder="Ex: CRO-SP 12345"
                      />
                    </div>

                    {/* Login */}
                    <div>
                      <label className="text-sm font-bold block mb-1">Login <span className="text-red-500">*</span></label>
                      <input
                        type="text"
                        value={form.login}
                        onChange={(e) => setForm({ ...form, login: e.target.value })}
                        className={`w-full border-2 p-2 rounded focus:outline-none ${erros.login ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        placeholder="nome.sobrenome"
                      />
                      {erros.login && <p className="text-xs text-red-500 mt-1">{erros.login}</p>}
                    </div>

                    {/* Senha */}
                    <div className="col-span-2">
                      <label className="text-sm font-bold block mb-1">
                        Senha {!modalCadastro.editando && <span className="text-red-500">*</span>}
                        {modalCadastro.editando && <span className="text-gray-400 font-normal"> (deixe em branco para manter)</span>}
                      </label>
                      <input
                        type="password"
                        value={form.senha}
                        onChange={(e) => setForm({ ...form, senha: e.target.value })}
                        className={`w-full border-2 p-2 rounded focus:outline-none ${erros.senha ? "border-red-500" : "border-gray-300 focus:border-blue-500"}`}
                        placeholder="••••••••"
                      />
                      {erros.senha && <p className="text-xs text-red-500 mt-1">{erros.senha}</p>}
                    </div>
                  </div>
                </div>
              )}

              {/* Aba: Disponibilidade */}
              {abaModal === "disponibilidade" && (
                <div className="space-y-6">
                  <div>
                    <label className="text-sm font-bold block mb-3">Dias de Trabalho</label>
                    <div className="flex flex-wrap gap-2">
                      {DIAS_SEMANA.map((dia) => (
                        <button
                          key={dia}
                          type="button"
                          onClick={() => toggleDia(dia)}
                          className={`px-3 py-2 border-2 text-sm font-bold rounded transition-colors ${
                            form.diasDisponiveis.includes(dia)
                              ? "border-blue-500 bg-blue-500 text-white"
                              : "border-gray-300 bg-white text-gray-600 hover:border-blue-300"
                          }`}
                        >
                          {dia}
                        </button>
                      ))}
                    </div>
                    <p className="text-xs text-gray-400 mt-2">Agendamentos fora dos dias selecionados serão bloqueados.</p>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-sm font-bold block mb-1">Horário de Início</label>
                      <input
                        type="time"
                        value={form.horarioInicio}
                        onChange={(e) => setForm({ ...form, horarioInicio: e.target.value })}
                        className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none"
                      />
                    </div>
                    <div>
                      <label className="text-sm font-bold block mb-1">Horário de Término</label>
                      <input
                        type="time"
                        value={form.horarioFim}
                        onChange={(e) => setForm({ ...form, horarioFim: e.target.value })}
                        className="w-full border-2 border-gray-300 p-2 rounded focus:border-blue-500 focus:outline-none"
                      />
                    </div>
                  </div>

                  <div className="border-2 border-orange-300 bg-orange-50 p-4">
                    <div className="text-sm font-bold text-orange-800 mb-3 flex items-center gap-2">
                      <Calendar className="w-4 h-4" /> Período de Ausência (Férias / Afastamento)
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="text-xs font-bold block mb-1">Data de Início</label>
                        <input
                          type="date"
                          value={form.periodoAusenciaInicio}
                          onChange={(e) => setForm({ ...form, periodoAusenciaInicio: e.target.value })}
                          className="w-full border-2 border-gray-300 p-2 rounded focus:border-orange-400 focus:outline-none text-sm"
                        />
                      </div>
                      <div>
                        <label className="text-xs font-bold block mb-1">Data de Término</label>
                        <input
                          type="date"
                          value={form.periodoAusenciaFim}
                          onChange={(e) => setForm({ ...form, periodoAusenciaFim: e.target.value })}
                          className="w-full border-2 border-gray-300 p-2 rounded focus:border-orange-400 focus:outline-none text-sm"
                        />
                      </div>
                      <div className="col-span-2">
                        <label className="text-xs font-bold block mb-1">Motivo</label>
                        <input
                          type="text"
                          value={form.periodoAusenciaMotivo}
                          onChange={(e) => setForm({ ...form, periodoAusenciaMotivo: e.target.value })}
                          className="w-full border-2 border-gray-300 p-2 rounded focus:border-orange-400 focus:outline-none text-sm"
                          placeholder="Ex: Férias anuais"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Aba: Permissões (somente leitura — geradas automaticamente pela função) */}
              {abaModal === "permissoes" && (
                <div>
                  <div className="bg-blue-50 border-2 border-blue-300 p-3 mb-4 text-sm text-blue-800 flex items-start gap-2">
                    <ShieldAlert className="w-4 h-4 mt-0.5 flex-shrink-0" />
                    <span>Permissões definidas automaticamente pela função <strong>{form.funcao}</strong>. Somente administradores podem alterar permissões individuais.</span>
                  </div>
                  <div className="space-y-2">
                    {PERMISSOES_POR_FUNCAO[form.funcao].map(({ modulo, permitido }) => (
                      <div key={modulo} className={`flex items-center justify-between p-3 border-2 rounded ${permitido ? "border-green-300 bg-green-50" : "border-gray-200 bg-gray-50"}`}>
                        <span className={`text-sm font-bold ${permitido ? "text-green-800" : "text-gray-400"}`}>{modulo}</span>
                        {permitido
                          ? <CheckCircle className="w-5 h-5 text-green-600" />
                          : <XCircle className="w-5 h-5 text-gray-400" />
                        }
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Rodapé do modal */}
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 bg-gray-50">
              <button
                onClick={() => setModalCadastro({ aberto: false })}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold rounded hover:bg-gray-50 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={salvarColaborador}
                className="flex-1 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold rounded hover:bg-blue-600 transition-colors"
              >
                {modalCadastro.editando ? "Salvar Alterações" : "Cadastrar Colaborador"}
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ═══ Modal: Alterar Status ══════════════════════════════════════════════ */}
      {modalStatus.aberto && modalStatus.colab && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[400px] p-6">
            <h2 className="text-lg font-bold text-gray-700 mb-4">Alterar Status — {modalStatus.colab.nome}</h2>
            <p className="text-sm text-gray-600 mb-4">
              Status atual: <span className={`px-2 py-0.5 rounded text-xs font-bold border ${COR_STATUS[modalStatus.colab.status]}`}>{modalStatus.colab.status}</span>
            </p>
            <div className="grid grid-cols-2 gap-2 mb-4">
              {(["Ativo", "Inativo", "Suspenso", "Férias", "Afastado"] as StatusColaborador[]).map((s) => (
                <button
                  key={s}
                  onClick={() => confirmarAlteracaoStatus(s)}
                  className={`p-3 border-2 rounded font-bold text-sm transition-colors ${
                    modalStatus.colab!.status === s
                      ? "border-blue-500 bg-blue-500 text-white"
                      : "border-gray-300 hover:border-blue-400 bg-white"
                  }`}
                >
                  {s}
                </button>
              ))}
            </div>
            <div className="bg-yellow-50 border-2 border-yellow-300 p-3 mb-4 text-xs text-yellow-800">
              <AlertTriangle className="w-3 h-3 inline mr-1" />
              Colaboradores Inativos ou Suspensos não podem realizar login. A alteração será registrada no histórico de auditoria.
            </div>
            <button
              onClick={() => setModalStatus({ aberto: false })}
              className="w-full px-4 py-2 border-2 border-gray-400 bg-white font-bold rounded hover:bg-gray-50 transition-colors"
            >
              Fechar
            </button>
          </div>
        </div>
      )}


      {/* ═══ Modal: Indicadores de Desempenho ══════════════════════════════════ */}
      {modalIndicadores.aberto && modalIndicadores.colab && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[560px] p-6">
            <div className="flex justify-between items-center mb-5">
              <div>
                <h2 className="text-lg font-bold text-gray-700">Indicadores de Desempenho</h2>
                <p className="text-sm text-gray-500">{modalIndicadores.colab.nome} — {modalIndicadores.colab.funcao}</p>
              </div>
              <button onClick={() => setModalIndicadores({ aberto: false })} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4 mb-4">
              {[
                { label: "Atendimentos Realizados", valor: modalIndicadores.colab.metricas.atendimentos, unidade: "", cor: "border-blue-500 bg-blue-50" },
                { label: "Procedimentos Registrados", valor: modalIndicadores.colab.metricas.procedimentosRealizados, unidade: "", cor: "border-green-500 bg-green-50" },
                { label: "Agendamentos Criados", valor: modalIndicadores.colab.metricas.agendamentosCriados, unidade: "", cor: "border-indigo-500 bg-indigo-50" },
                { label: "Esterilizações Registradas", valor: modalIndicadores.colab.metricas.esterilizacoesRegistradas, unidade: "", cor: "border-teal-500 bg-teal-50" },
                { label: "Taxa de Conversão", valor: modalIndicadores.colab.metricas.taxaConversao, unidade: "%", cor: "border-orange-400 bg-orange-50" },
                { label: "Faltas no Período", valor: modalIndicadores.colab.metricas.faltas, unidade: "", cor: "border-red-400 bg-red-50" },
              ].map(({ label, valor, unidade, cor }) => (
                <div key={label} className={`border-2 ${cor} p-4 rounded`}>
                  <div className="text-2xl font-bold text-gray-800">{valor}{unidade}</div>
                  <div className="text-xs text-gray-500 font-bold mt-1">{label}</div>
                </div>
              ))}
            </div>

            {/* Avaliação média */}
            <div className="border-2 border-yellow-400 bg-yellow-50 p-4 mb-4 flex items-center gap-4">
              <div>
                <div className="text-3xl font-bold text-yellow-700">{modalIndicadores.colab.metricas.avaliacaoMedia.toFixed(1)}</div>
                <div className="text-xs text-gray-500 font-bold">Avaliação Média (0–5)</div>
              </div>
              <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map((estrela) => (
                  <span key={estrela} className={`text-xl ${estrela <= Math.round(modalIndicadores.colab!.metricas.avaliacaoMedia) ? "text-yellow-500" : "text-gray-300"}`}>★</span>
                ))}
              </div>
            </div>

            <button
              onClick={() => setModalIndicadores({ aberto: false })}
              className="w-full px-4 py-2 border-2 border-gray-400 bg-white font-bold rounded hover:bg-gray-50 transition-colors"
            >
              Fechar
            </button>
          </div>
        </div>
      )}


      {/* ═══ Modal: Log de Auditoria ════════════════════════════════════════════ */}
      {modalAuditoria.aberto && modalAuditoria.colab && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[600px] max-h-[80vh] flex flex-col">
            <div className="p-4 bg-orange-50 border-b-2 border-gray-300 flex justify-between items-center">
              <div>
                <h2 className="text-lg font-bold text-orange-800">Log de Auditoria</h2>
                <p className="text-sm text-gray-600">{modalAuditoria.colab.nome}</p>
              </div>
              <button onClick={() => setModalAuditoria({ aberto: false })} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="overflow-y-auto flex-1">
              <div className="grid grid-cols-4 bg-gray-100 border-b-2 border-gray-300 text-xs font-bold text-gray-600 uppercase p-3">
                <div>Data / Hora</div>
                <div className="col-span-2">Ação Executada</div>
                <div>Módulo / IP</div>
              </div>
              {modalAuditoria.colab.logAuditoria.length === 0 ? (
                <div className="p-6 text-center text-gray-500 text-sm">Nenhum registro de auditoria.</div>
              ) : (
                modalAuditoria.colab.logAuditoria.map((log) => (
                  <div key={log.id} className="grid grid-cols-4 border-b border-gray-200 p-3 text-sm hover:bg-gray-50 items-center">
                    <div className="text-gray-500">
                      <div className="font-bold">{log.data}</div>
                      <div className="text-xs flex items-center gap-1"><Clock className="w-3 h-3" />{log.hora}</div>
                    </div>
                    <div className="col-span-2 text-gray-800">{log.acao}</div>
                    <div className="text-gray-500 text-xs">
                      <div>{log.modulo}</div>
                      <div className="text-gray-400">{log.ip}</div>
                    </div>
                  </div>
                ))
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-300">
              <button
                onClick={() => setModalAuditoria({ aberto: false })}
                className="w-full px-4 py-2 border-2 border-gray-400 bg-white font-bold rounded hover:bg-gray-50 transition-colors"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}


      {/* ═══ Modal: Histórico de Alterações ════════════════════════════════════ */}
      {modalHistorico.aberto && modalHistorico.colab && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-purple-500 w-[640px] max-h-[80vh] flex flex-col">
            <div className="p-4 bg-purple-50 border-b-2 border-gray-300 flex justify-between items-center">
              <div>
                <h2 className="text-lg font-bold text-purple-800">Histórico de Alterações Cadastrais</h2>
                <p className="text-sm text-gray-600">{modalHistorico.colab.nome}</p>
              </div>
              <button onClick={() => setModalHistorico({ aberto: false })} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="overflow-y-auto flex-1">
              <div className="grid grid-cols-5 bg-gray-100 border-b-2 border-gray-300 text-xs font-bold text-gray-600 uppercase p-3">
                <div>Campo</div>
                <div className="col-span-2">Alteração</div>
                <div>Responsável</div>
                <div>Data</div>
              </div>
              {modalHistorico.colab.historicoAlteracoes.length === 0 ? (
                <div className="p-6 text-center text-gray-500 text-sm">Nenhuma alteração registrada.</div>
              ) : (
                modalHistorico.colab.historicoAlteracoes.map((h) => (
                  <div key={h.id} className="grid grid-cols-5 border-b border-gray-200 p-3 text-sm hover:bg-gray-50 items-center">
                    <div className="font-bold text-gray-700">{h.campo}</div>
                    <div className="col-span-2">
                      <div className="flex items-center gap-2 text-xs">
                        <span className="bg-red-100 text-red-700 border border-red-300 px-2 py-0.5 rounded line-through">{h.valorAnterior}</span>
                        <span className="text-gray-400">→</span>
                        <span className="bg-green-100 text-green-700 border border-green-300 px-2 py-0.5 rounded">{h.valorAtualizado}</span>
                      </div>
                    </div>
                    <div className="text-xs text-gray-600">{h.responsavel}</div>
                    <div className="text-xs text-gray-500">{h.data}</div>
                  </div>
                ))
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-300">
              <button
                onClick={() => setModalHistorico({ aberto: false })}
                className="w-full px-4 py-2 border-2 border-gray-400 bg-white font-bold rounded hover:bg-gray-50 transition-colors"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
