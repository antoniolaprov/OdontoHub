import { useState, useMemo } from "react";
import { Link } from "react-router";

// ─── Types ──────────────────────────────────────────────────────────────────

type StatusPaciente =
  | "ATIVO"
  | "INATIVO"
  | "INCOMPLETO"
  | "RESTRITO";

interface AlteracaoCadastral {
  campo: string;
  valorAnterior: string;
  valorAtualizado: string;
  responsavel: string;
  dataAlteracao: string;
}

interface Paciente {
  id: number;
  nome: string;
  cpf: string;
  dataNascimento: string;
  telefone: string;
  email: string;
  status: StatusPaciente;
  temPlano: boolean;
  inadimplente: boolean;
  historicoAlteracoes: AlteracaoCadastral[];
}

// ─── Mock ────────────────────────────────────────────────────────────────────

const PACIENTES_MOCK: Paciente[] = [
  {
    id: 1,
    nome: "Ana Costa",
    cpf: "123.456.789-00",
    dataNascimento: "12/03/1988",
    telefone: "81 9 9999-1111",
    email: "ana.costa@email.com",
    status: "ATIVO",
    temPlano: true,
    inadimplente: false,
    historicoAlteracoes: [],
  },
  {
    id: 2,
    nome: "Bruno Ferreira",
    cpf: "234.567.890-11",
    dataNascimento: "05/07/1975",
    telefone: "81 9 9888-2222",
    email: "brunoferreira@email.com",
    status: "ATIVO",
    temPlano: false,
    inadimplente: true,
    historicoAlteracoes: [],
  },
  {
    id: 3,
    nome: "Carla Mendes",
    cpf: "345.678.901-22",
    dataNascimento: "22/11/1992",
    telefone: "81 9 9777-3333",
    email: "carla.m@email.com",
    status: "ATIVO",
    temPlano: true,
    inadimplente: false,
    historicoAlteracoes: [],
  },
  {
    id: 4,
    nome: "Diego Alves",
    cpf: "456.789.012-33",
    dataNascimento: "30/01/1983",
    telefone: "81 9 9666-4444",
    email: "diego.alves@email.com",
    status: "RESTRITO",
    temPlano: false,
    inadimplente: true,
    historicoAlteracoes: [],
  },
  {
    id: 5,
    nome: "Elena Rodrigues",
    cpf: "567.890.123-44",
    dataNascimento: "18/06/2000",
    telefone: "81 9 9555-5555",
    email: "elena.r@email.com",
    status: "ATIVO",
    temPlano: false,
    inadimplente: false,
    historicoAlteracoes: [],
  },
  {
    id: 6,
    nome: "Fábio Santos",
    cpf: "678.901.234-55",
    dataNascimento: "09/09/1969",
    telefone: "81 9 9444-6666",
    email: "",
    status: "INCOMPLETO",
    temPlano: false,
    inadimplente: false,
    historicoAlteracoes: [],
  },
  {
    id: 7,
    nome: "Gisele Pereira",
    cpf: "789.012.345-66",
    dataNascimento: "14/02/1995",
    telefone: "81 9 9333-7777",
    email: "gisele.p@email.com",
    status: "ATIVO",
    temPlano: true,
    inadimplente: false,
    historicoAlteracoes: [],
  },
  {
    id: 8,
    nome: "Henrique Lima",
    cpf: "890.123.456-77",
    dataNascimento: "27/12/1980",
    telefone: "81 9 9222-8888",
    email: "h.lima@email.com",
    status: "INATIVO",
    temPlano: false,
    inadimplente: false,
    historicoAlteracoes: [
      {
        campo: "status",
        valorAnterior: "ATIVO",
        valorAtualizado: "INATIVO",
        responsavel: "Recepcionista",
        dataAlteracao: "01/06/2026",
      },
    ],
  },
  {
    id: 9,
    nome: "Isabela Teixeira",
    cpf: "",
    dataNascimento: "",
    telefone: "81 9 9111-9999",
    email: "",
    status: "INCOMPLETO",
    temPlano: false,
    inadimplente: false,
    historicoAlteracoes: [],
  },
  {
    id: 10,
    nome: "Jorge Cavalcanti",
    cpf: "012.345.678-99",
    dataNascimento: "03/05/1972",
    telefone: "81 9 9000-0000",
    email: "jorge.cav@email.com",
    status: "RESTRITO",
    temPlano: false,
    inadimplente: true,
    historicoAlteracoes: [],
  },
];

// ─── Helpers ─────────────────────────────────────────────────────────────────

function hoje(): string {
  return new Date().toLocaleDateString("pt-BR");
}

function StatusBadge({ status }: { status: StatusPaciente }) {
  const cls: Record<StatusPaciente, string> = {
    ATIVO: "bg-green-100 border-green-500 text-green-700",
    INATIVO: "bg-gray-100 border-gray-500 text-gray-600",
    INCOMPLETO:
      "bg-yellow-100 border-yellow-500 text-yellow-700",
    RESTRITO: "bg-red-100 border-red-500 text-red-700",
  };
  return (
    <span
      className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${cls[status]}`}
    >
      {status}
    </span>
  );
}

function emptyForm(): Omit<
  Paciente,
  "id" | "historicoAlteracoes"
> {
  return {
    nome: "",
    cpf: "",
    dataNascimento: "",
    telefone: "",
    email: "",
    status: "ATIVO",
    temPlano: false,
    inadimplente: false,
  };
}

// ─── Main Component ───────────────────────────────────────────────────────────

export default function RecepcionistaPacientes() {
  const [pacientes, setPacientes] =
    useState<Paciente[]>(PACIENTES_MOCK);
  const [busca, setBusca] = useState("");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");

  // Toast
  const [toast, setToast] = useState<{
    msg: string;
    tipo: "sucesso" | "erro";
  } | null>(null);

  // Modal novo/editar
  const [modalForm, setModalForm] = useState<{
    aberto: boolean;
    paciente?: Paciente;
  }>({ aberto: false });
  const [form, setForm] = useState(emptyForm());
  const [cadastroRapido, setCadastroRapido] = useState(false);
  const [erroForm, setErroForm] = useState("");

  // Modal detalhes
  const [modalDetalhes, setModalDetalhes] = useState<{
    aberto: boolean;
    paciente?: Paciente;
  }>({ aberto: false });

  // Modal desativar
  const [modalDesativar, setModalDesativar] = useState<{
    aberto: boolean;
    paciente?: Paciente;
  }>({ aberto: false });

  // ─ Derived ──────────────────────────────────────────────────────────────

  const filtrados = useMemo(() => {
    return pacientes.filter((p) => {
      const q = busca.toLowerCase();
      const matchBusca =
        busca === "" ||
        p.nome.toLowerCase().includes(q) ||
        p.cpf.includes(busca) ||
        p.telefone.includes(busca);
      const matchStatus =
        filtroStatus === "TODOS" || p.status === filtroStatus;
      return matchBusca && matchStatus;
    });
  }, [pacientes, busca, filtroStatus]);

  const totais = useMemo(
    () => ({
      total: pacientes.length,
      ativos: pacientes.filter((p) => p.status === "ATIVO")
        .length,
      inativos: pacientes.filter((p) => p.status === "INATIVO")
        .length,
      incompletos: pacientes.filter(
        (p) => p.status === "INCOMPLETO",
      ).length,
      restritos: pacientes.filter(
        (p) => p.status === "RESTRITO",
      ).length,
      inadimplentes: pacientes.filter((p) => p.inadimplente)
        .length,
    }),
    [pacientes],
  );

  // ─ Handlers ─────────────────────────────────────────────────────────────

  function showToast(msg: string, tipo: "sucesso" | "erro") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 3000);
  }

  function abrirNovo() {
    setForm(emptyForm());
    setCadastroRapido(false);
    setErroForm("");
    setModalForm({ aberto: true });
  }

  function abrirEditar(p: Paciente) {
    setForm({
      nome: p.nome,
      cpf: p.cpf,
      dataNascimento: p.dataNascimento,
      telefone: p.telefone,
      email: p.email,
      status: p.status,
      temPlano: p.temPlano,
      inadimplente: p.inadimplente,
    });
    setCadastroRapido(false);
    setErroForm("");
    setModalForm({ aberto: true, paciente: p });
  }

  function handleSalvar() {
    if (!form.nome.trim()) {
      setErroForm("Nome é obrigatório.");
      return;
    }
    if (!cadastroRapido && !form.cpf.trim()) {
      setErroForm("CPF é obrigatório no cadastro completo.");
      return;
    }
    if (!form.telefone.trim()) {
      setErroForm("Telefone é obrigatório.");
      return;
    }

    if (modalForm.paciente) {
      // editar — registrar alterações no histórico
      const pacienteAtual = modalForm.paciente;
      const novasAlteracoes: AlteracaoCadastral[] = [];
      const campos: (keyof typeof form)[] = [
        "nome",
        "cpf",
        "dataNascimento",
        "telefone",
        "email",
        "status",
      ];
      campos.forEach((campo) => {
        const anterior = String(
          pacienteAtual[campo as keyof Paciente] ?? "",
        );
        const atualizado = String(form[campo] ?? "");
        if (anterior !== atualizado) {
          novasAlteracoes.push({
            campo,
            valorAnterior: anterior,
            valorAtualizado: atualizado,
            responsavel: "Recepcionista",
            dataAlteracao: hoje(),
          });
        }
      });
      setPacientes((prev) =>
        prev.map((p) =>
          p.id === pacienteAtual.id
            ? {
                ...p,
                ...form,
                historicoAlteracoes: [
                  ...novasAlteracoes,
                  ...p.historicoAlteracoes,
                ],
              }
            : p,
        ),
      );
      showToast("Paciente atualizado com sucesso.", "sucesso");
    } else {
      // novo
      const novo: Paciente = {
        id: Math.max(...pacientes.map((p) => p.id), 0) + 1,
        ...form,
        status: cadastroRapido ? "INCOMPLETO" : form.status,
        historicoAlteracoes: [],
      };
      setPacientes((prev) => [...prev, novo]);
      showToast("Paciente cadastrado com sucesso.", "sucesso");
    }
    setModalForm({ aberto: false });
  }

  function handleDesativar() {
    if (!modalDesativar.paciente) return;
    const alteracao: AlteracaoCadastral = {
      campo: "status",
      valorAnterior: modalDesativar.paciente.status,
      valorAtualizado: "RESTRITO",
      responsavel: "Recepcionista",
      dataAlteracao: hoje(),
    };
    setPacientes((prev) =>
      prev.map((p) =>
        p.id === modalDesativar.paciente!.id
          ? {
              ...p,
              status: "RESTRITO",
              historicoAlteracoes: [
                alteracao,
                ...p.historicoAlteracoes,
              ],
            }
          : p,
      ),
    );
    showToast("Paciente marcado como RESTRITO.", "sucesso");
    setModalDesativar({ aberto: false });
  }

  // ─ Render ────────────────────────────────────────────────────────────────

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold text-sm shadow-lg ${
            toast.tipo === "sucesso"
              ? "bg-green-50 border-green-500 text-green-700"
              : "bg-red-50 border-red-500 text-red-700"
          }`}
        >
          {toast.tipo === "sucesso" ? "✓ " : "✕ "}
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">
            Cadastro de Pacientes
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Gerencie o cadastro de todos os pacientes da
            clínica.
          </p>
        </div>
        <button
          onClick={abrirNovo}
          className="px-4 py-2 bg-blue-500 text-white border-2 border-blue-500 font-bold hover:bg-blue-600"
        >
          + Novo Paciente
        </button>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-6 gap-3">
        {[
          {
            label: "Total",
            value: totais.total,
            color: "border-gray-400 bg-white",
          },
          {
            label: "Ativos",
            value: totais.ativos,
            color: "border-green-500 bg-green-50",
          },
          {
            label: "Inativos",
            value: totais.inativos,
            color: "border-gray-500 bg-gray-50",
          },
          {
            label: "Incompletos",
            value: totais.incompletos,
            color: "border-yellow-500 bg-yellow-50",
          },
          {
            label: "Restritos",
            value: totais.restritos,
            color: "border-red-500 bg-red-50",
          },
          {
            label: "Inadimplentes",
            value: totais.inadimplentes,
            color: "border-orange-500 bg-orange-50",
          },
        ].map((c) => (
          <div
            key={c.label}
            className={`border-2 p-3 text-center ${c.color}`}
          >
            <div className="text-2xl font-bold">{c.value}</div>
            <div className="text-xs text-gray-600 mt-0.5">
              {c.label}
            </div>
          </div>
        ))}
      </div>

      {/* Filter bar */}
      <div className="border-2 border-gray-400 bg-gray-50 p-3 flex gap-3 flex-wrap items-center">
        <input
          className="border-2 border-gray-400 p-2 text-sm outline-none w-64"
          placeholder="Buscar por nome, CPF ou telefone…"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <select
          className="border-2 border-gray-400 p-2 text-sm bg-white outline-none"
          value={filtroStatus}
          onChange={(e) => setFiltroStatus(e.target.value)}
        >
          <option value="TODOS">Todos os status</option>
          <option value="ATIVO">Ativo</option>
          <option value="INATIVO">Inativo</option>
          <option value="INCOMPLETO">Incompleto</option>
          <option value="RESTRITO">Restrito</option>
        </select>
        <span className="text-xs text-gray-500 ml-auto">
          {filtrados.length} paciente(s)
        </span>
      </div>

      {/* Table */}
      <div className="border-2 border-gray-400 overflow-auto">
        <table className="w-full text-sm border-collapse">
          <thead>
            <tr className="bg-gray-100 border-b-2 border-gray-400">
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                Nome
              </th>
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                CPF
              </th>
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                Telefone
              </th>
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                Status
              </th>
              <th className="text-left p-3 font-bold">Ações</th>
            </tr>
          </thead>
          <tbody>
            {filtrados.length === 0 && (
              <tr>
                <td
                  colSpan={5}
                  className="p-8 text-center text-gray-500"
                >
                  Nenhum paciente encontrado.
                </td>
              </tr>
            )}
            {filtrados.map((p, i) => (
              <tr
                key={p.id}
                className={`border-b border-gray-200 ${i % 2 === 0 ? "bg-white" : "bg-gray-50"} hover:bg-blue-50`}
              >
                <td className="p-3 border-r border-gray-200">
                  <div className="font-bold">{p.nome}</div>
                  {p.inadimplente && (
                    <div className="text-xs text-red-600 font-bold">
                      ⚠ Inadimplente
                    </div>
                  )}
                </td>
                <td className="p-3 border-r border-gray-200 font-mono text-gray-700">
                  {p.cpf || (
                    <span className="text-gray-400 italic">
                      não informado
                    </span>
                  )}
                </td>
                <td className="p-3 border-r border-gray-200">
                  {p.telefone}
                </td>
                <td className="p-3 border-r border-gray-200">
                  <StatusBadge status={p.status} />
                </td>
                <td className="p-3">
                  <div className="flex gap-2">
                    <button
                      onClick={() =>
                        setModalDetalhes({
                          aberto: true,
                          paciente: p,
                        })
                      }
                      className="px-2 py-1 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-100"
                    >
                      Detalhes
                    </button>
                    <button
                      onClick={() => abrirEditar(p)}
                      className="px-2 py-1 border-2 border-blue-500 bg-white text-blue-600 text-xs font-bold hover:bg-blue-50"
                    >
                      Editar
                    </button>
                    {p.status !== "RESTRITO" && (
                      <button
                        onClick={() =>
                          setModalDesativar({
                            aberto: true,
                            paciente: p,
                          })
                        }
                        className="px-2 py-1 border-2 border-red-400 bg-white text-red-600 text-xs font-bold hover:bg-red-50"
                      >
                        Restringir
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* ── Modal: Novo / Editar ── */}
      {modalForm.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-lg max-h-screen overflow-y-auto">
            <div className="bg-blue-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center sticky top-0">
              <span>
                {modalForm.paciente
                  ? "Editar Paciente"
                  : "Novo Paciente"}
              </span>
              <button
                onClick={() => setModalForm({ aberto: false })}
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              {/* Cadastro Rápido toggle — só no cadastro novo */}
              {!modalForm.paciente && (
                <div className="border-2 border-gray-300 bg-gray-50 p-3">
                  <label className="block text-sm font-bold mb-2">
                    Tipo de cadastro
                  </label>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        setCadastroRapido(false);
                        setErroForm("");
                      }}
                      className={`px-3 py-2 border-2 font-bold text-sm ${
                        !cadastroRapido
                          ? "border-blue-500 bg-blue-500 text-white"
                          : "border-gray-400 bg-white hover:bg-gray-100"
                      }`}
                    >
                      Cadastro completo
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setCadastroRapido(true);
                        setErroForm("");
                      }}
                      className={`px-3 py-2 border-2 font-bold text-sm ${
                        cadastroRapido
                          ? "border-blue-500 bg-blue-500 text-white"
                          : "border-gray-400 bg-white hover:bg-gray-100"
                      }`}
                    >
                      Cadastro rápido
                    </button>
                  </div>
                  {cadastroRapido && (
                    <div className="mt-2 flex items-center gap-2">
                      <span className="inline-block px-2 py-0.5 border-2 border-yellow-500 bg-yellow-100 text-yellow-700 text-xs font-bold">
                        PERFIL INCOMPLETO
                      </span>
                      <span className="text-xs text-gray-500">
                        O paciente poderá ser agendado, mas terá
                        o status Incompleto até que os dados
                        sejam complementados.
                      </span>
                    </div>
                  )}
                </div>
              )}

              {/* Nome */}
              <div>
                <label className="block text-sm font-bold mb-1">
                  Nome completo *
                </label>
                <input
                  className="w-full border-2 border-gray-400 p-2 outline-none"
                  placeholder="Ex: Ana Costa"
                  value={form.nome}
                  onChange={(e) => {
                    setForm((f) => ({
                      ...f,
                      nome: e.target.value,
                    }));
                    setErroForm("");
                  }}
                />
              </div>

              {/* Telefone */}
              <div>
                <label className="block text-sm font-bold mb-1">
                  Telefone *
                </label>
                <input
                  className="w-full border-2 border-gray-400 p-2 outline-none"
                  placeholder="Ex: 81 9 9999-1111"
                  value={form.telefone}
                  onChange={(e) => {
                    setForm((f) => ({
                      ...f,
                      telefone: e.target.value,
                    }));
                    setErroForm("");
                  }}
                />
              </div>

              {/* Campos adicionais — apenas no cadastro completo */}
              {!cadastroRapido && (
                <>
                  <div>
                    <label className="block text-sm font-bold mb-1">
                      CPF *
                    </label>
                    <input
                      className="w-full border-2 border-gray-400 p-2 outline-none font-mono"
                      placeholder="Ex: 123.456.789-00"
                      value={form.cpf}
                      onChange={(e) => {
                        setForm((f) => ({
                          ...f,
                          cpf: e.target.value,
                        }));
                        setErroForm("");
                      }}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-bold mb-1">
                      Data de nascimento
                    </label>
                    <input
                      className="w-full border-2 border-gray-400 p-2 outline-none"
                      placeholder="DD/MM/AAAA"
                      value={form.dataNascimento}
                      onChange={(e) =>
                        setForm((f) => ({
                          ...f,
                          dataNascimento: e.target.value,
                        }))
                      }
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-bold mb-1">
                      E-mail
                    </label>
                    <input
                      type="email"
                      className="w-full border-2 border-gray-400 p-2 outline-none"
                      placeholder="Ex: ana@email.com"
                      value={form.email}
                      onChange={(e) =>
                        setForm((f) => ({
                          ...f,
                          email: e.target.value,
                        }))
                      }
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-sm font-bold mb-1">
                        Status
                      </label>
                      <select
                        className="w-full border-2 border-gray-400 p-2 bg-white outline-none"
                        value={form.status}
                        onChange={(e) =>
                          setForm((f) => ({
                            ...f,
                            status: e.target
                              .value as StatusPaciente,
                          }))
                        }
                      >
                        <option value="ATIVO">Ativo</option>
                        <option value="INATIVO">Inativo</option>
                        <option value="INCOMPLETO">
                          Incompleto
                        </option>
                        <option value="RESTRITO">
                          Restrito
                        </option>
                      </select>
                    </div>
                    <div className="flex flex-col gap-2 pt-6">
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={form.temPlano}
                          onChange={(e) =>
                            setForm((f) => ({
                              ...f,
                              temPlano: e.target.checked,
                            }))
                          }
                          className="w-4 h-4"
                        />
                        <span className="text-sm font-bold">
                          Tem plano
                        </span>
                      </label>
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={form.inadimplente}
                          onChange={(e) =>
                            setForm((f) => ({
                              ...f,
                              inadimplente: e.target.checked,
                            }))
                          }
                          className="w-4 h-4"
                        />
                        <span className="text-sm font-bold text-red-600">
                          Inadimplente
                        </span>
                      </label>
                    </div>
                  </div>
                </>
              )}

              {erroForm && (
                <div className="border-2 border-red-500 bg-red-50 p-3 text-red-700 text-sm font-bold">
                  {erroForm}
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end sticky bottom-0 bg-white">
              <button
                onClick={() => setModalForm({ aberto: false })}
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                onClick={handleSalvar}
                className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600"
              >
                Salvar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal: Detalhes ── */}
      {modalDetalhes.aberto && modalDetalhes.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-lg">
            <div className="bg-gray-700 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Detalhes do Paciente</span>
              <button
                onClick={() =>
                  setModalDetalhes({ aberto: false })
                }
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-3 text-sm">
              {[
                {
                  label: "Nome",
                  value: modalDetalhes.paciente.nome,
                },
                {
                  label: "CPF",
                  value: modalDetalhes.paciente.cpf || "—",
                },
                {
                  label: "Nascimento",
                  value:
                    modalDetalhes.paciente.dataNascimento ||
                    "—",
                },
                {
                  label: "Telefone",
                  value: modalDetalhes.paciente.telefone,
                },
                {
                  label: "E-mail",
                  value: modalDetalhes.paciente.email || "—",
                },
                {
                  label: "Tem plano",
                  value: modalDetalhes.paciente.temPlano
                    ? "Sim"
                    : "Não",
                },
                {
                  label: "Inadimplente",
                  value: modalDetalhes.paciente.inadimplente
                    ? "Sim ⚠"
                    : "Não",
                },
              ].map((row) => (
                <div
                  key={row.label}
                  className="flex gap-2 border-b border-gray-200 pb-2"
                >
                  <span className="font-bold w-32 text-gray-600">
                    {row.label}
                  </span>
                  <span>{row.value}</span>
                </div>
              ))}
              <div className="flex gap-2 items-center border-b border-gray-200 pb-2">
                <span className="font-bold w-32 text-gray-600">
                  Status
                </span>
                <StatusBadge
                  status={modalDetalhes.paciente.status}
                />
              </div>

              {/* Histórico de Alterações */}
              {modalDetalhes.paciente.historicoAlteracoes
                .length > 0 && (
                <div className="pt-2">
                  <div className="text-xs text-gray-500 font-bold mb-2">
                    HISTÓRICO DE ALTERAÇÕES
                  </div>
                  <div className="space-y-2 max-h-40 overflow-y-auto">
                    {modalDetalhes.paciente.historicoAlteracoes.map(
                      (alt, i) => (
                        <div
                          key={i}
                          className="border border-gray-200 p-2 text-xs bg-gray-50"
                        >
                          <div className="flex justify-between mb-1">
                            <span className="font-bold text-gray-700">
                              Campo: {alt.campo}
                            </span>
                            <span className="text-gray-400">
                              {alt.dataAlteracao}
                            </span>
                          </div>
                          <div className="text-gray-500">
                            <span className="line-through">
                              {alt.valorAnterior || "—"}
                            </span>
                            {" → "}
                            <span className="font-bold text-gray-700">
                              {alt.valorAtualizado}
                            </span>
                          </div>
                          <div className="text-gray-400 mt-0.5">
                            Responsável: {alt.responsavel}
                          </div>
                        </div>
                      ),
                    )}
                  </div>
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() =>
                  setModalDetalhes({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Fechar
              </button>
              <button
                onClick={() => {
                  abrirEditar(modalDetalhes.paciente!);
                  setModalDetalhes({ aberto: false });
                }}
                className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600"
              >
                Editar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal: Restringir ── */}
      {modalDesativar.aberto && modalDesativar.paciente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-red-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Restringir Paciente</span>
              <button
                onClick={() =>
                  setModalDesativar({ aberto: false })
                }
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 text-sm space-y-3">
              <p>
                Tem certeza que deseja marcar{" "}
                <strong>{modalDesativar.paciente.nome}</strong>{" "}
                como <strong>RESTRITO</strong>?
              </p>
              <p className="text-gray-500">
                Pacientes restritos não podem ser agendados até
                que o status seja alterado manualmente.
              </p>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() =>
                  setModalDesativar({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                onClick={handleDesativar}
                className="px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600"
              >
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}