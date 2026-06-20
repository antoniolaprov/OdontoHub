import { useState, useMemo, useEffect, useCallback } from "react";
import { Link } from "react-router";
import { api } from "../../api/client";

// ─── Types ───────────────────────────────────────────────────────────────────

type StatusEsterilizacao =
  | "ESTERIL"
  | "CONTAMINADO"
  | "VENCIDO";

interface Instrumento {
  id: number;
  nome: string;
  categoria: string;
  codigo: string;
  prazoValidade: string;
  statusEsterilizacao: StatusEsterilizacao;
  ativo: boolean;
  dataUltimaEsterilizacao: string;
  responsavelEsterilizacao: string;
  historico: {
    data: string;
    acao: string;
    responsavel?: string;
  }[];
}

const CATEGORIAS = [
  "Kits de Exame",
  "Kits Cirúrgicos",
  "Pinças",
  "Espelhos e Sondas",
  "Afastadores",
  "Bisturis",
  "Curetas",
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

function statusEfetivo(item: Instrumento): StatusEsterilizacao {
  if (item.prazoValidade !== "-") {
    const [d, m, y] = item.prazoValidade.split("/").map(Number);
    const validade = new Date(y, m - 1, d);
    const agora = new Date();
    agora.setHours(0, 0, 0, 0);
    if (validade < agora) return "VENCIDO";
  }
  return item.statusEsterilizacao;
}

// ─── Sub-components ───────────────────────────────────────────────────────────

function StatusBadge({
  status,
}: {
  status: StatusEsterilizacao;
}) {
  const cls: Record<StatusEsterilizacao, string> = {
    ESTERIL: "bg-green-500 border-green-600 text-white",
    CONTAMINADO: "bg-yellow-500 border-yellow-600 text-white",
    VENCIDO: "bg-red-500 border-red-600 text-white",
  };
  return (
    <span
      className={`inline-block px-2 py-1 border-2 text-xs font-bold ${cls[status]}`}
    >
      {status}
    </span>
  );
}

interface SummaryCardProps {
  valor: number;
  label: string;
  cor: string;
}

function SummaryCard({ valor, label, cor }: SummaryCardProps) {
  return (
    <div className={`border-2 ${cor} p-3 text-center`}>
      <div className="text-2xl font-bold">{valor}</div>
      <div className="text-xs font-bold mt-1">{label}</div>
    </div>
  );
}

// ─── Integração com o backend (GET /api/instrumentos) ─────────────────────────
// statusEsterilizacao do backend (ESTERIL/CONTAMINADO/VENCIDO) já bate com a UI.
// Backend não guarda um histórico de operações por instrumento — deriva-se um
// registro a partir da última esterilização conhecida (igual ao default usado no Recall).

function formatarDataISO(d: string | null | undefined): string {
  if (!d) return "-";
  const [y, m, dia] = d.split("-");
  return `${dia}/${m}/${y}`;
}

function adaptarInstrumentoEsterilizacao(b: any, indice: number): Instrumento {
  const historico: Instrumento["historico"] = [];
  if (b.dataUltimaEsterilizacao) {
    historico.push({
      data: formatarDataISO(b.dataUltimaEsterilizacao),
      acao: "Esterilizado",
      responsavel: b.responsavelEsterilizacao || undefined,
    });
  }
  return {
    id: b.id?.id ?? indice + 1,
    nome: b.nome ?? "—",
    categoria: b.categoria ?? "—",
    codigo: b.codigoIdentificador ?? "—",
    prazoValidade: formatarDataISO(b.dataVencimento),
    statusEsterilizacao: (b.status as StatusEsterilizacao) ?? "CONTAMINADO",
    ativo: b.statusInstrumento !== "INATIVO",
    dataUltimaEsterilizacao: formatarDataISO(b.dataUltimaEsterilizacao),
    responsavelEsterilizacao: b.responsavelEsterilizacao ?? "",
    historico,
  };
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function AuxiliarEsterilizacao() {
  const [instrumentos, setInstrumentos] = useState<Instrumento[]>([]);
  const [carregandoInstrumentos, setCarregandoInstrumentos] = useState(true);

  // Carrega os instrumentos reais do backend — sem fallback: vazio/erro mostra a tela vazia.
  const carregarInstrumentos = useCallback(() => {
    return api.get<any[]>("/instrumentos")
      .then((lista) => setInstrumentos(Array.isArray(lista) ? lista.map(adaptarInstrumentoEsterilizacao) : []))
      .catch((e) => {
        console.warn("Falha ao carregar instrumentos do backend:", e);
        setInstrumentos([]);
      })
      .finally(() => setCarregandoInstrumentos(false));
  }, []);

  useEffect(() => { carregarInstrumentos(); }, [carregarInstrumentos]);

  // Auxiliares cadastrados — usado no select de "Responsável pela esterilização".
  const [auxiliares, setAuxiliares] = useState<string[]>([]);
  useEffect(() => {
    api.get<any[]>("/colaboradores")
      .then((lista) => setAuxiliares(
        (lista ?? []).filter((c: any) => c.funcao === "AUXILIAR").map((c: any) => c.nomeCompleto ?? "—"),
      ))
      .catch((e) => console.warn("Falha ao carregar colaboradores:", e));
  }, []);

  // Filters
  const [busca, setBusca] = useState("");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");
  const [filtroCategoria, setFiltroCategoria] =
    useState("TODAS");
  const [filtroAtivo, setFiltroAtivo] = useState("TODOS");

  // Toast
  const [toast, setToast] = useState<{
    msg: string;
    tipo: "sucesso" | "erro";
  } | null>(null);

  // Modal states
  const [modalEsteril, setModalEsteril] = useState<{
    aberto: boolean;
    item?: Instrumento;
    acao?: "ESTERIL" | "CONTAMINADO";
  }>({ aberto: false });
  const [responsavelInput, setResponsavelInput] = useState("");
  const [erroResponsavel, setErroResponsavel] = useState("");
  const [modalDetalhes, setModalDetalhes] = useState<{
    aberto: boolean;
    item?: Instrumento;
  }>({ aberto: false });
  const [modalDesativar, setModalDesativar] = useState<{
    aberto: boolean;
    item?: Instrumento;
  }>({ aberto: false });

  // ─ Derived ─
  const filtrados = useMemo(() => {
    return instrumentos.filter((i) => {
      const matchBusca =
        busca === "" ||
        i.nome.toLowerCase().includes(busca.toLowerCase()) ||
        i.codigo.toLowerCase().includes(busca.toLowerCase());
      const matchStatus =
        filtroStatus === "TODOS" ||
        statusEfetivo(i) === filtroStatus;
      const matchCategoria =
        filtroCategoria === "TODAS" ||
        i.categoria === filtroCategoria;
      const matchAtivo =
        filtroAtivo === "TODOS" ||
        (filtroAtivo === "ATIVO" ? i.ativo : !i.ativo);
      return (
        matchBusca &&
        matchStatus &&
        matchCategoria &&
        matchAtivo
      );
    });
  }, [
    instrumentos,
    busca,
    filtroStatus,
    filtroCategoria,
    filtroAtivo,
  ]);

  const totais = useMemo(
    () => ({
      esteril: instrumentos.filter(
        (i) => i.ativo && statusEfetivo(i) === "ESTERIL",
      ).length,
      contaminado: instrumentos.filter(
        (i) => i.ativo && statusEfetivo(i) === "CONTAMINADO",
      ).length,
      vencido: instrumentos.filter(
        (i) => i.ativo && statusEfetivo(i) === "VENCIDO",
      ).length,
      ativos: instrumentos.filter((i) => i.ativo).length,
      inativos: instrumentos.filter((i) => !i.ativo).length,
    }),
    [instrumentos],
  );

  // ─ Handlers ─
  function showToast(msg: string, tipo: "sucesso" | "erro") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 3000);
  }

  function abrirModalEsteril(
    item: Instrumento,
    acao: "ESTERIL" | "CONTAMINADO",
  ) {
    setResponsavelInput("");
    setErroResponsavel("");
    setModalEsteril({ aberto: true, item, acao });
  }

  const [salvandoStatus, setSalvandoStatus] = useState(false);

  async function handleMudarStatus(
    id: number,
    novoStatus: "ESTERIL" | "CONTAMINADO",
  ) {
    if (novoStatus === "ESTERIL" && !responsavelInput.trim()) {
      setErroResponsavel(
        "Responsável é obrigatório para esterilização.",
      );
      return;
    }
    const item = instrumentos.find((i) => i.id === id);
    if (!item) return;
    const responsavel = responsavelInput.trim();

    setSalvandoStatus(true);
    try {
      if (novoStatus === "ESTERIL") {
        await api.post(
          `/instrumentos/${encodeURIComponent(item.nome)}/esterilizar?responsavel=${encodeURIComponent(responsavel)}`,
        );
      } else {
        await api.post(`/instrumentos/${encodeURIComponent(item.nome)}/contaminar`);
      }
      await carregarInstrumentos();
      setModalEsteril({ aberto: false });
      showToast(
        novoStatus === "ESTERIL"
          ? `Instrumento marcado como Estéril! Responsável: ${responsavel}`
          : "Instrumento marcado como Contaminado.",
        "sucesso",
      );
    } catch (e: any) {
      setErroResponsavel(e.message ?? "Falha ao atualizar status do instrumento.");
    } finally {
      setSalvandoStatus(false);
    }
  }

  async function handleDesativar(id: number) {
    const item = instrumentos.find((i) => i.id === id);
    if (!item) return;
    try {
      await api.post(`/instrumentos/${encodeURIComponent(item.nome)}/desativar`);
      await carregarInstrumentos();
      showToast(
        "Instrumento desativado. Mantido no histórico.",
        "sucesso",
      );
    } catch (e: any) {
      showToast(e.message ?? "Falha ao desativar instrumento.", "erro");
    } finally {
      setModalDesativar({ aberto: false });
    }
  }

  /** Único jeito de tirar um instrumento de Inativo — desativar() era uma via de mão única. */
  async function handleReativar(id: number) {
    const item = instrumentos.find((i) => i.id === id);
    if (!item) return;
    try {
      await api.post(`/instrumentos/${encodeURIComponent(item.nome)}/reativar`);
      await carregarInstrumentos();
      showToast("Instrumento reativado.", "sucesso");
    } catch (e: any) {
      showToast(e.message ?? "Falha ao reativar instrumento.", "erro");
    }
  }

  function limparFiltros() {
    setBusca("");
    setFiltroStatus("TODOS");
    setFiltroCategoria("TODAS");
    setFiltroAtivo("TODOS");
  }

  // ─ Render ─
  return (
    <div className="p-6">
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

      {/* Header */}
      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-xl font-bold text-gray-700">
          Controle de Esterilização
        </h1>
        <Link
          to="/auxiliar/instrumentos"
          className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600 flex items-center gap-2"
        >
          + Cadastrar Instrumento
        </Link>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-5 gap-3 mb-6">
        <SummaryCard
          valor={totais.esteril}
          label="ESTÉREIS"
          cor="border-green-500 bg-green-50 text-green-700"
        />
        <SummaryCard
          valor={totais.contaminado}
          label="CONTAMINADOS"
          cor="border-yellow-500 bg-yellow-50 text-yellow-700"
        />
        <SummaryCard
          valor={totais.vencido}
          label="VENCIDOS"
          cor="border-red-500 bg-red-50 text-red-700"
        />
        <SummaryCard
          valor={totais.ativos}
          label="ATIVOS"
          cor="border-blue-400 bg-blue-50 text-blue-700"
        />
        <SummaryCard
          valor={totais.inativos}
          label="INATIVOS"
          cor="border-gray-400 bg-gray-100 text-gray-500"
        />
      </div>

      {/* Filters */}
      <div className="border-2 border-gray-400 bg-gray-50 p-4 mb-4">
        <div className="grid grid-cols-5 gap-3 mb-2">
          <div className="col-span-2 flex items-center border-2 border-gray-400 bg-white">
            <span className="px-2 text-gray-400 text-sm">
              🔍
            </span>
            <input
              type="text"
              placeholder="Buscar por nome ou código..."
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              className="flex-1 p-2 outline-none bg-transparent text-sm"
            />
          </div>
          <select
            value={filtroStatus}
            onChange={(e) => setFiltroStatus(e.target.value)}
            className="border-2 border-gray-400 bg-white p-2 outline-none text-sm"
          >
            <option value="TODOS">Todos os Status</option>
            <option value="ESTERIL">Estéril</option>
            <option value="CONTAMINADO">Contaminado</option>
            <option value="VENCIDO">Vencido</option>
          </select>
          <select
            value={filtroCategoria}
            onChange={(e) => setFiltroCategoria(e.target.value)}
            className="border-2 border-gray-400 bg-white p-2 outline-none text-sm"
          >
            <option value="TODAS">Todas as Categorias</option>
            {CATEGORIAS.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
          <select
            value={filtroAtivo}
            onChange={(e) => setFiltroAtivo(e.target.value)}
            className="border-2 border-gray-400 bg-white p-2 outline-none text-sm"
          >
            <option value="TODOS">Ativos e Inativos</option>
            <option value="ATIVO">Somente Ativos</option>
            <option value="INATIVO">Somente Inativos</option>
          </select>
        </div>
        <div className="flex items-center gap-4">
          <button
            onClick={limparFiltros}
            className="text-sm text-blue-600 underline hover:text-blue-800"
          >
            Limpar filtros
          </button>
          <span className="text-sm text-gray-500">
            {filtrados.length} instrumento(s) encontrado(s)
          </span>
        </div>
      </div>

      {/* Table */}
      <div className="border-2 border-gray-400 bg-white">
        <div
          className="grid bg-gray-100 border-b-2 border-gray-400"
          style={{
            gridTemplateColumns:
              "2fr 1.5fr 0.9fr 1.2fr 1.1fr 0.8fr 2.2fr",
          }}
        >
          <div className="p-3 border-r-2 border-gray-400 font-bold text-sm">
            Instrumento
          </div>
          <div className="p-3 border-r-2 border-gray-400 font-bold text-sm">
            Categoria
          </div>
          <div className="p-3 border-r-2 border-gray-400 font-bold text-sm">
            Código
          </div>
          <div className="p-3 border-r-2 border-gray-400 font-bold text-sm">
            Status
          </div>
          <div className="p-3 border-r-2 border-gray-400 font-bold text-sm">
            Validade
          </div>
          <div className="p-3 border-r-2 border-gray-400 font-bold text-sm">
            Situação
          </div>
          <div className="p-3 font-bold text-sm">Ações</div>
        </div>

        {filtrados.length === 0 && (
          <div className="p-10 text-center text-gray-400">
            {carregandoInstrumentos
              ? "Carregando instrumentos..."
              : "Nenhum instrumento encontrado com os filtros aplicados."}
          </div>
        )}

        {filtrados.map((item) => {
          const inativo = !item.ativo;
          return (
            <div
              key={item.id}
              className={`grid border-b-2 border-gray-400 ${inativo ? "bg-gray-100" : ""}`}
              style={{
                gridTemplateColumns:
                  "2fr 1.5fr 0.9fr 1.2fr 1.1fr 0.8fr 2.2fr",
              }}
            >
              <div
                className={`p-3 border-r-2 border-gray-400 ${inativo ? "opacity-50" : ""}`}
              >
                <div className="font-medium text-sm">
                  {item.nome}
                </div>
                {inativo && (
                  <div className="text-xs text-gray-400 italic">
                    inativo
                  </div>
                )}
              </div>
              <div
                className={`p-3 border-r-2 border-gray-400 text-sm ${inativo ? "opacity-50" : ""}`}
              >
                {item.categoria}
              </div>
              <div
                className={`p-3 border-r-2 border-gray-400 text-sm font-mono ${inativo ? "opacity-50" : ""}`}
              >
                {item.codigo}
              </div>
              <div
                className={`p-3 border-r-2 border-gray-400 ${inativo ? "opacity-50" : ""}`}
              >
                <StatusBadge status={statusEfetivo(item)} />
              </div>
              <div
                className={`p-3 border-r-2 border-gray-400 text-sm ${inativo ? "opacity-50" : ""}`}
              >
                {statusEfetivo(item) === "VENCIDO" ? (
                  <span className="text-red-600 font-bold">
                    {item.prazoValidade}
                  </span>
                ) : (
                  item.prazoValidade
                )}
              </div>
              <div className="p-3 border-r-2 border-gray-400">
                <span
                  className={`inline-block px-2 py-1 border-2 text-xs font-bold ${
                    inativo
                      ? "bg-gray-200 border-gray-400 text-gray-500"
                      : "bg-blue-100 border-blue-400 text-blue-700"
                  }`}
                >
                  {inativo ? "INATIVO" : "ATIVO"}
                </span>
              </div>
              <div className="p-3 flex gap-1 flex-wrap items-center">
                <button
                  disabled={inativo}
                  onClick={() =>
                    abrirModalEsteril(item, "ESTERIL")
                  }
                  title={
                    inativo
                      ? "Instrumento inativo"
                      : "Marcar como Estéril"
                  }
                  className={`px-2 py-1 border-2 text-xs font-bold ${
                    inativo
                      ? "border-gray-300 bg-gray-200 text-gray-400 cursor-not-allowed"
                      : "border-green-500 bg-green-50 text-green-700 hover:bg-green-500 hover:text-white"
                  }`}
                >
                  Estéril
                </button>
                <button
                  disabled={inativo}
                  onClick={() =>
                    abrirModalEsteril(item, "CONTAMINADO")
                  }
                  title={
                    inativo
                      ? "Instrumento inativo"
                      : "Marcar como Contaminado"
                  }
                  className={`px-2 py-1 border-2 text-xs font-bold ${
                    inativo
                      ? "border-gray-300 bg-gray-200 text-gray-400 cursor-not-allowed"
                      : "border-yellow-500 bg-yellow-50 text-yellow-700 hover:bg-yellow-500 hover:text-white"
                  }`}
                >
                  Contaminar
                </button>
                <button
                  onClick={() =>
                    setModalDetalhes({ aberto: true, item })
                  }
                  className="px-2 py-1 border-2 border-gray-400 bg-white text-gray-600 hover:bg-gray-200 text-xs font-bold"
                >
                  Detalhes
                </button>
                {item.ativo ? (
                  <button
                    onClick={() =>
                      setModalDesativar({ aberto: true, item })
                    }
                    className="px-2 py-1 border-2 border-red-400 bg-red-50 text-red-600 hover:bg-red-500 hover:text-white text-xs font-bold"
                  >
                    Desativar
                  </button>
                ) : (
                  <button
                    onClick={() => handleReativar(item.id)}
                    className="px-2 py-1 border-2 border-green-500 bg-green-50 text-green-700 hover:bg-green-500 hover:text-white text-xs font-bold"
                  >
                    Reativar
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* ── Modal: Esterilizar / Contaminar ── */}
      {modalEsteril.aberto && modalEsteril.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div
              className={`text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center ${
                modalEsteril.acao === "ESTERIL"
                  ? "bg-green-500"
                  : "bg-yellow-500"
              }`}
            >
              <span>
                Confirmar:{" "}
                {modalEsteril.acao === "ESTERIL"
                  ? "Esterilizar"
                  : "Marcar como Contaminado"}
              </span>
              <button
                onClick={() =>
                  setModalEsteril({ aberto: false })
                }
                className="text-white hover:text-gray-200 text-xl leading-none"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-sm">
                Instrumento:{" "}
                <strong>{modalEsteril.item.nome}</strong> (
                {modalEsteril.item.codigo})
              </p>
              <p className="text-sm text-gray-600">
                {modalEsteril.acao === "ESTERIL"
                  ? "O status será alterado para ESTÉRIL e o prazo de validade será calculado automaticamente (30 dias a partir de hoje)."
                  : "O status será alterado para CONTAMINADO. O instrumento deverá ser reprocessado antes do próximo uso."}
              </p>

              {/* Campo Responsável — obrigatório apenas para esterilização */}
              {modalEsteril.acao === "ESTERIL" && (
                <div>
                  <label className="block text-sm font-bold mb-1">
                    Responsável pela esterilização *
                  </label>
                  <select
                    className={`w-full border-2 p-2 outline-none text-sm bg-white ${
                      erroResponsavel
                        ? "border-red-500"
                        : "border-gray-400"
                    }`}
                    value={responsavelInput}
                    onChange={(e) => {
                      setResponsavelInput(e.target.value);
                      setErroResponsavel("");
                    }}
                  >
                    <option value="">Selecione o auxiliar</option>
                    {auxiliares.map((nome) => (
                      <option key={nome} value={nome}>{nome}</option>
                    ))}
                  </select>
                  {erroResponsavel && (
                    <p className="text-red-600 text-xs mt-1 font-bold">
                      {erroResponsavel}
                    </p>
                  )}
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() =>
                  setModalEsteril({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                onClick={() =>
                  handleMudarStatus(
                    modalEsteril.item!.id,
                    modalEsteril.acao!,
                  )
                }
                disabled={salvandoStatus}
                className={`px-4 py-2 border-2 text-white font-bold disabled:opacity-50 ${
                  modalEsteril.acao === "ESTERIL"
                    ? "border-green-500 bg-green-500 hover:bg-green-600"
                    : "border-yellow-500 bg-yellow-500 hover:bg-yellow-600"
                }`}
              >
                {salvandoStatus ? "Salvando..." : "Confirmar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal: Detalhes / Histórico ── */}
      {modalDetalhes.aberto && modalDetalhes.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-gray-700 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Detalhes do Instrumento</span>
              <button
                onClick={() =>
                  setModalDetalhes({ aberto: false })
                }
                className="text-white hover:text-gray-200 text-xl leading-none"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    NOME
                  </div>
                  <div className="font-medium">
                    {modalDetalhes.item.nome}
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    CÓDIGO
                  </div>
                  <div className="font-mono">
                    {modalDetalhes.item.codigo}
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    CATEGORIA
                  </div>
                  <div>{modalDetalhes.item.categoria}</div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    STATUS
                  </div>
                  <StatusBadge
                    status={statusEfetivo(modalDetalhes.item)}
                  />
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    ÚLTIMA ESTERILIZAÇÃO
                  </div>
                  <div>
                    {modalDetalhes.item.dataUltimaEsterilizacao}
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    RESPONSÁVEL
                  </div>
                  <div>
                    {modalDetalhes.item
                      .responsavelEsterilizacao || "—"}
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    VALIDADE
                  </div>
                  <div
                    className={
                      statusEfetivo(modalDetalhes.item) ===
                      "VENCIDO"
                        ? "text-red-600 font-bold"
                        : ""
                    }
                  >
                    {modalDetalhes.item.prazoValidade}
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold mb-1">
                    SITUAÇÃO
                  </div>
                  <span
                    className={`inline-block px-2 py-1 border-2 text-xs font-bold ${
                      modalDetalhes.item.ativo
                        ? "bg-blue-100 border-blue-400 text-blue-700"
                        : "bg-gray-200 border-gray-400 text-gray-500"
                    }`}
                  >
                    {modalDetalhes.item.ativo
                      ? "ATIVO"
                      : "INATIVO"}
                  </span>
                </div>
              </div>

              <div className="border-t-2 border-gray-300 pt-4">
                <div className="text-xs text-gray-500 font-bold mb-2">
                  HISTÓRICO DE OPERAÇÕES
                </div>
                <div className="space-y-1 max-h-36 overflow-y-auto">
                  {modalDetalhes.item.historico.map((h, i) => (
                    <div
                      key={i}
                      className="flex justify-between text-sm border-b border-gray-200 pb-1"
                    >
                      <span className="text-gray-400">
                        {h.data}
                      </span>
                      <span className="font-medium">
                        {h.acao}
                        {h.responsavel
                          ? ` — ${h.responsavel}`
                          : ""}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex justify-end">
              <button
                onClick={() =>
                  setModalDetalhes({ aberto: false })
                }
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal: Desativar ── */}
      {modalDesativar.aberto && modalDesativar.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-red-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Desativar Instrumento</span>
              <button
                onClick={() =>
                  setModalDesativar({ aberto: false })
                }
                className="text-white hover:text-gray-200 text-xl leading-none"
              >
                ✕
              </button>
            </div>
            <div className="p-6">
              <p className="mb-3 text-sm">
                Desativar{" "}
                <strong>{modalDesativar.item.nome}</strong>?
              </p>
              <p className="text-sm text-gray-600">
                O instrumento será marcado como inativo. Ele
                permanecerá salvo para fins históricos, mas não
                aparecerá nas operações ativas e não poderá
                sofrer alterações.
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
                onClick={() =>
                  handleDesativar(modalDesativar.item!.id)
                }
                className="px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600"
              >
                Desativar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}