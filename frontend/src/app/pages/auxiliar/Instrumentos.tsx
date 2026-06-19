import { useState, useMemo, useEffect, useCallback } from "react";
import { api } from "../../api/client";

// ─── Types ───────────────────────────────────────────────────────────────────

type TipoInstrumento = "INDIVIDUAL" | "KIT";
type StatusInstrumento = "ATIVO" | "INATIVO";

interface InstrumentoBase {
  id: number;
  nome: string;
  categoria: string;
  codigo: string;
  tipo: TipoInstrumento;
  status: StatusInstrumento;
  prazoValidadeDias: number | null;
  componentesIds: number[];
}

// ─── Mock ────────────────────────────────────────────────────────────────────

const CATEGORIAS = [
  "Kits de Exame",
  "Kits Cirúrgicos",
  "Pinças",
  "Espelhos e Sondas",
  "Afastadores",
  "Bisturis",
  "Curetas",
];

// Prazo padrão por categoria (global)
const PRAZO_PADRAO_GLOBAL = 30;
const PRAZO_INICIAL_POR_CATEGORIA: Record<string, number> = {
  "Kits de Exame": 30,
  "Kits Cirúrgicos": 60,
  Pinças: 30,
  "Espelhos e Sondas": 30,
  Afastadores: 45,
  Bisturis: 30,
  Curetas: 30,
};

// ─── Helpers ─────────────────────────────────────────────────────────────────

function StatusBadge({
  status,
}: {
  status: StatusInstrumento;
}) {
  return (
    <span
      className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${
        status === "ATIVO"
          ? "bg-green-100 border-green-500 text-green-700"
          : "bg-gray-100 border-gray-400 text-gray-500"
      }`}
    >
      {status}
    </span>
  );
}

function TipoBadge({ tipo }: { tipo: TipoInstrumento }) {
  return (
    <span
      className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${
        tipo === "KIT"
          ? "bg-blue-100 border-blue-400 text-blue-700"
          : "bg-gray-100 border-gray-400 text-gray-700"
      }`}
    >
      {tipo}
    </span>
  );
}

function emptyForm() {
  return {
    nome: "",
    categoria: "",
    codigo: "",
    tipo: "INDIVIDUAL" as TipoInstrumento,
    status: "ATIVO" as StatusInstrumento,
    prazoValidadeDias: "30",
    componentesIds: [] as number[],
  };
}

// ─── Integração com o backend (GET /api/instrumentos) ─────────────────────────
// tipo (INDIVIDUAL/KIT) e status (ATIVO/INATIVO) do backend já batem com a UI.
// componentesIds aqui referencia ids locais (desta lista); o backend guarda os
// códigos dos componentes — resolve-se o código de volta para o id local.

function adaptarInstrumentos(lista: any[]): InstrumentoBase[] {
  const base = lista.map((b, i) => ({
    id: b.id?.id ?? i + 1,
    nome: b.nome ?? "—",
    categoria: b.categoria ?? "—",
    codigo: b.codigoIdentificador ?? "—",
    tipo: (b.tipo as TipoInstrumento) ?? "INDIVIDUAL",
    status: (b.statusInstrumento as StatusInstrumento) ?? "ATIVO",
    prazoValidadeDias: b.prazoValidadeDias ?? null,
    codigosComponentes: (b.codigosComponentes ?? []) as string[],
  }));
  const idPorCodigo = new Map(base.map((i) => [i.codigo, i.id]));
  return base.map(({ codigosComponentes, ...resto }) => ({
    ...resto,
    componentesIds: codigosComponentes
      .map((c) => idPorCodigo.get(c))
      .filter((x): x is number => x !== undefined),
  }));
}

// ─── Main Component ───────────────────────────────────────────────────────────

export default function AuxiliarInstrumentos() {
  const [instrumentos, setInstrumentos] = useState<InstrumentoBase[]>([]);
  const [carregandoInstrumentos, setCarregandoInstrumentos] = useState(true);

  // Carrega os instrumentos reais do backend — sem fallback: vazio/erro mostra a tela vazia.
  const carregarInstrumentos = useCallback(() => {
    return api.get<any[]>("/instrumentos")
      .then((lista) => setInstrumentos(Array.isArray(lista) ? adaptarInstrumentos(lista) : []))
      .catch((e) => {
        console.warn("Falha ao carregar instrumentos do backend:", e);
        setInstrumentos([]);
      })
      .finally(() => setCarregandoInstrumentos(false));
  }, []);

  useEffect(() => { carregarInstrumentos(); }, [carregarInstrumentos]);

  // Prazo por categoria (editável globalmente)
  const [prazosPorCategoria, setPrazosPorCategoria] = useState<
    Record<string, number>
  >({ ...PRAZO_INICIAL_POR_CATEGORIA });
  const [prazoGlobal, setPrazoGlobal] = useState<string>(
    String(PRAZO_PADRAO_GLOBAL),
  );

  const [busca, setBusca] = useState("");
  const [filtroCategoria, setFiltroCategoria] =
    useState("TODAS");
  const [filtroTipo, setFiltroTipo] = useState("TODOS");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");

  // Toast
  const [toast, setToast] = useState<{
    msg: string;
    tipo: "sucesso" | "erro";
  } | null>(null);

  // Modal form
  const [modalForm, setModalForm] = useState<{
    aberto: boolean;
    instrumento?: InstrumentoBase;
  }>({ aberto: false });
  const [form, setForm] = useState(emptyForm());
  const [erroCodigo, setErroCodigo] = useState("");

  // Modal detalhes
  const [modalDetalhes, setModalDetalhes] = useState<{
    aberto: boolean;
    instrumento?: InstrumentoBase;
  }>({ aberto: false });

  // Modal desativar
  const [modalDesativar, setModalDesativar] = useState<{
    aberto: boolean;
    instrumento?: InstrumentoBase;
  }>({ aberto: false });

  // Modal configuração de prazo
  const [modalPrazo, setModalPrazo] = useState(false);
  const [prazoFormCategoria, setPrazoFormCategoria] =
    useState("GLOBAL");
  const [prazoFormValor, setPrazoFormValor] = useState("30");
  const [erroPrazo, setErroPrazo] = useState("");

  // ─ Derived ──────────────────────────────────────────────────────────────

  const individuais = useMemo(
    () =>
      instrumentos.filter(
        (i) => i.tipo === "INDIVIDUAL" && i.status === "ATIVO",
      ),
    [instrumentos],
  );

  const filtrados = useMemo(() => {
    return instrumentos.filter((i) => {
      const q = busca.toLowerCase();
      const matchBusca =
        busca === "" ||
        i.nome.toLowerCase().includes(q) ||
        i.codigo.toLowerCase().includes(q);
      const matchCat =
        filtroCategoria === "TODAS" ||
        i.categoria === filtroCategoria;
      const matchTipo =
        filtroTipo === "TODOS" || i.tipo === filtroTipo;
      const matchStat =
        filtroStatus === "TODOS" || i.status === filtroStatus;
      return matchBusca && matchCat && matchTipo && matchStat;
    });
  }, [
    instrumentos,
    busca,
    filtroCategoria,
    filtroTipo,
    filtroStatus,
  ]);

  const totais = useMemo(
    () => ({
      total: instrumentos.length,
      kits: instrumentos.filter((i) => i.tipo === "KIT").length,
      individuais: instrumentos.filter(
        (i) => i.tipo === "INDIVIDUAL",
      ).length,
      ativos: instrumentos.filter((i) => i.status === "ATIVO")
        .length,
      inativos: instrumentos.filter(
        (i) => i.status === "INATIVO",
      ).length,
    }),
    [instrumentos],
  );

  // ─ Handlers ─────────────────────────────────────────────────────────────

  function showToast(msg: string, tipo: "sucesso" | "erro") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 3500);
  }

  function abrirNovo() {
    setForm(emptyForm());
    setErroCodigo("");
    setModalForm({ aberto: true });
  }

  function abrirEditar(i: InstrumentoBase) {
    setForm({
      nome: i.nome,
      categoria: i.categoria,
      codigo: i.codigo,
      tipo: i.tipo,
      status: i.status,
      prazoValidadeDias:
        i.prazoValidadeDias !== null
          ? String(i.prazoValidadeDias)
          : "",
      componentesIds: [...i.componentesIds],
    });
    setErroCodigo("");
    setModalForm({ aberto: true, instrumento: i });
  }

  function toggleComponente(id: number) {
    setForm((f) => ({
      ...f,
      componentesIds: f.componentesIds.includes(id)
        ? f.componentesIds.filter((x) => x !== id)
        : [...f.componentesIds, id],
    }));
  }

  const [salvandoInstrumento, setSalvandoInstrumento] = useState(false);

  async function handleSalvar() {
    if (!form.nome.trim()) {
      setErroCodigo("Nome é obrigatório.");
      return;
    }
    if (!form.categoria) {
      setErroCodigo("Categoria é obrigatória.");
      return;
    }
    if (!form.codigo.trim()) {
      setErroCodigo("Código é obrigatório.");
      return;
    }

    const codigoUsado = instrumentos.some(
      (i) =>
        i.codigo.toLowerCase() ===
          form.codigo.trim().toLowerCase() &&
        i.id !== modalForm.instrumento?.id,
    );
    if (codigoUsado) {
      setErroCodigo(
        "Código já cadastrado. Use um código único.",
      );
      return;
    }

    const prazo = form.prazoValidadeDias.trim()
      ? parseInt(form.prazoValidadeDias)
      : null;

    if (modalForm.instrumento) {
      // O backend não suporta editar nome/categoria/código/tipo de um instrumento já
      // cadastrado (só esterilizar/contaminar/desativar e configurar prazo) —
      // mantido como atualização local, igual ao padrão de campos sem endpoint no Recall.
      setInstrumentos((prev) =>
        prev.map((i) =>
          i.id === modalForm.instrumento!.id
            ? {
                ...i,
                nome: form.nome.trim(),
                categoria: form.categoria,
                codigo: form.codigo.trim().toUpperCase(),
                tipo: form.tipo,
                status: form.status,
                prazoValidadeDias: prazo,
                componentesIds:
                  form.tipo === "KIT"
                    ? form.componentesIds
                    : [],
              }
            : i,
        ),
      );
      showToast(
        "Instrumento atualizado com sucesso.",
        "sucesso",
      );
      setModalForm({ aberto: false });
      return;
    }

    setSalvandoInstrumento(true);
    setErroCodigo("");
    try {
      const codigosComponentes = form.componentesIds
        .map((id) => instrumentos.find((i) => i.id === id)?.codigo)
        .filter((c): c is string => !!c);
      await api.post("/instrumentos", {
        nome: form.nome.trim(),
        categoria: form.categoria,
        codigoIdentificador: form.codigo.trim().toUpperCase(),
        prazoValidadeDias: prazo ?? 30,
        tipo: form.tipo,
        codigosComponentes,
      });
      await carregarInstrumentos();
      showToast(
        "Instrumento cadastrado com sucesso.",
        "sucesso",
      );
      setModalForm({ aberto: false });
    } catch (e: any) {
      setErroCodigo(e.message ?? "Falha ao cadastrar instrumento.");
    } finally {
      setSalvandoInstrumento(false);
    }
  }

  async function handleDesativar() {
    if (!modalDesativar.instrumento) return;
    try {
      await api.post(`/instrumentos/${encodeURIComponent(modalDesativar.instrumento.nome)}/desativar`);
      await carregarInstrumentos();
      showToast("Instrumento desativado.", "sucesso");
    } catch (e: any) {
      showToast(e.message ?? "Falha ao desativar instrumento.", "erro");
    } finally {
      setModalDesativar({ aberto: false });
    }
  }

  // ── Prazo global/por categoria ──────────────────────────────────────────

  function abrirModalPrazo() {
    setPrazoFormCategoria("GLOBAL");
    setPrazoFormValor(prazoGlobal);
    setErroPrazo("");
    setModalPrazo(true);
  }

  function handlePrazoCategoriaMudou(cat: string) {
    setPrazoFormCategoria(cat);
    if (cat === "GLOBAL") {
      setPrazoFormValor(prazoGlobal);
    } else {
      setPrazoFormValor(
        String(prazosPorCategoria[cat] ?? PRAZO_PADRAO_GLOBAL),
      );
    }
    setErroPrazo("");
  }

  async function handleSalvarPrazo() {
    const valor = parseInt(prazoFormValor);
    if (isNaN(valor) || valor < 1) {
      setErroPrazo(
        "Informe um número de dias válido (mínimo 1).",
      );
      return;
    }

    try {
      if (prazoFormCategoria === "GLOBAL") {
        await api.post(`/instrumentos/prazo-global?dias=${valor}`);
        setPrazoGlobal(String(valor));
        const novosPrazos: Record<string, number> = {};
        CATEGORIAS.forEach((c) => {
          novosPrazos[c] = valor;
        });
        setPrazosPorCategoria(novosPrazos);
        await carregarInstrumentos();
        showToast(
          `Prazo global atualizado para ${valor} dias. Todos os instrumentos foram recalculados.`,
          "sucesso",
        );
      } else {
        await api.post(
          `/instrumentos/categorias/${encodeURIComponent(prazoFormCategoria)}/prazo?dias=${valor}`,
        );
        setPrazosPorCategoria((prev) => ({
          ...prev,
          [prazoFormCategoria]: valor,
        }));
        const afetados = instrumentos.filter(
          (i) => i.categoria === prazoFormCategoria,
        ).length;
        await carregarInstrumentos();
        showToast(
          `Prazo da categoria "${prazoFormCategoria}" atualizado para ${valor} dias. ${afetados} instrumento(s) recalculado(s).`,
          "sucesso",
        );
      }
      setModalPrazo(false);
    } catch (e: any) {
      setErroPrazo(e.message ?? "Falha ao configurar prazo.");
    }
  }

  // ─ Render ────────────────────────────────────────────────────────────────

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold text-sm shadow-lg max-w-sm ${
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
            Cadastro de Instrumentos
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Gerencie o cadastro de todos os instrumentos e kits
            da clínica.
          </p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={abrirModalPrazo}
            className="px-4 py-2 bg-white text-gray-700 border-2 border-gray-400 font-bold hover:bg-gray-100 text-sm"
          >
            ⏱ Configurar Prazos
          </button>
          <button
            onClick={abrirNovo}
            className="px-4 py-2 bg-blue-500 text-white border-2 border-blue-500 font-bold hover:bg-blue-600"
          >
            + Novo Instrumento
          </button>
        </div>
      </div>

      {/* Prazo por categoria — painel resumo */}
      <div className="border-2 border-yellow-400 bg-yellow-50 p-3">
        <div className="text-xs font-bold text-yellow-800 mb-2">
          ⏱ Prazos de Validade Configurados por Categoria
        </div>
        <div className="flex flex-wrap gap-2">
          {CATEGORIAS.map((cat) => (
            <div
              key={cat}
              className="border border-yellow-300 bg-white px-2 py-1 text-xs"
            >
              <span className="font-bold text-gray-700">
                {cat}:
              </span>{" "}
              <span className="text-yellow-700 font-bold">
                {prazosPorCategoria[cat] ?? PRAZO_PADRAO_GLOBAL}
                d
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-5 gap-3">
        {[
          {
            label: "Total",
            value: totais.total,
            color: "border-gray-400 bg-white",
          },
          {
            label: "Kits",
            value: totais.kits,
            color: "border-blue-400 bg-blue-50",
          },
          {
            label: "Individuais",
            value: totais.individuais,
            color: "border-gray-400 bg-gray-50",
          },
          {
            label: "Ativos",
            value: totais.ativos,
            color: "border-green-500 bg-green-50",
          },
          {
            label: "Inativos",
            value: totais.inativos,
            color: "border-red-400 bg-red-50",
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
          className="border-2 border-gray-400 p-2 text-sm outline-none w-56"
          placeholder="Buscar por nome ou código…"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <select
          className="border-2 border-gray-400 p-2 text-sm bg-white outline-none"
          value={filtroCategoria}
          onChange={(e) => setFiltroCategoria(e.target.value)}
        >
          <option value="TODAS">Todas as categorias</option>
          {CATEGORIAS.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
        <select
          className="border-2 border-gray-400 p-2 text-sm bg-white outline-none"
          value={filtroTipo}
          onChange={(e) => setFiltroTipo(e.target.value)}
        >
          <option value="TODOS">Todos os tipos</option>
          <option value="INDIVIDUAL">Individual</option>
          <option value="KIT">Kit</option>
        </select>
        <select
          className="border-2 border-gray-400 p-2 text-sm bg-white outline-none"
          value={filtroStatus}
          onChange={(e) => setFiltroStatus(e.target.value)}
        >
          <option value="TODOS">Todos os status</option>
          <option value="ATIVO">Ativo</option>
          <option value="INATIVO">Inativo</option>
        </select>
        <span className="text-xs text-gray-500 ml-auto">
          {filtrados.length} instrumento(s)
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
                Categoria
              </th>
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                Código
              </th>
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                Tipo
              </th>
              <th className="text-left p-3 border-r border-gray-300 font-bold">
                Validade (dias)
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
                  colSpan={7}
                  className="p-8 text-center text-gray-500"
                >
                  {carregandoInstrumentos
                    ? "Carregando instrumentos..."
                    : "Nenhum instrumento encontrado."}
                </td>
              </tr>
            )}
            {filtrados.map((inst, i) => (
              <tr
                key={inst.id}
                className={`border-b border-gray-200 ${inst.status === "INATIVO" ? "opacity-60" : ""} ${i % 2 === 0 ? "bg-white" : "bg-gray-50"} hover:bg-blue-50`}
              >
                <td className="p-3 border-r border-gray-200 font-bold">
                  {inst.nome}
                </td>
                <td className="p-3 border-r border-gray-200 text-gray-700">
                  {inst.categoria}
                </td>
                <td className="p-3 border-r border-gray-200 font-mono text-gray-700">
                  {inst.codigo}
                </td>
                <td className="p-3 border-r border-gray-200">
                  <TipoBadge tipo={inst.tipo} />
                </td>
                <td className="p-3 border-r border-gray-200 text-center">
                  {inst.prazoValidadeDias !== null
                    ? `${inst.prazoValidadeDias}d`
                    : "—"}
                </td>
                <td className="p-3 border-r border-gray-200">
                  <StatusBadge status={inst.status} />
                </td>
                <td className="p-3">
                  <div className="flex gap-2">
                    <button
                      onClick={() =>
                        setModalDetalhes({
                          aberto: true,
                          instrumento: inst,
                        })
                      }
                      className="px-2 py-1 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-100"
                    >
                      Detalhes
                    </button>
                    <button
                      onClick={() => abrirEditar(inst)}
                      className="px-2 py-1 border-2 border-blue-500 bg-white text-blue-600 text-xs font-bold hover:bg-blue-50"
                    >
                      Editar
                    </button>
                    {inst.status === "ATIVO" && (
                      <button
                        onClick={() =>
                          setModalDesativar({
                            aberto: true,
                            instrumento: inst,
                          })
                        }
                        className="px-2 py-1 border-2 border-red-400 bg-white text-red-600 text-xs font-bold hover:bg-red-50"
                      >
                        Desativar
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* ══ Modal: Configurar Prazo ══ */}
      {modalPrazo && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-yellow-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>⏱ Configurar Prazo de Validade</span>
              <button
                onClick={() => setModalPrazo(false)}
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="border-2 border-yellow-300 bg-yellow-50 p-3 text-sm text-yellow-800">
                Ao alterar o prazo de uma categoria ou
                globalmente,{" "}
                <strong>
                  todos os instrumentos afetados serão
                  recalculados automaticamente
                </strong>
                .
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">
                  Aplicar para
                </label>
                <select
                  className="w-full border-2 border-gray-400 p-2 bg-white outline-none"
                  value={prazoFormCategoria}
                  onChange={(e) =>
                    handlePrazoCategoriaMudou(e.target.value)
                  }
                >
                  <option value="GLOBAL">
                    🌐 Todas as categorias (Global)
                  </option>
                  {CATEGORIAS.map((c) => (
                    <option key={c} value={c}>
                      {c} — atual:{" "}
                      {prazosPorCategoria[c] ??
                        PRAZO_PADRAO_GLOBAL}
                      d
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">
                  Novo prazo (em dias) *
                </label>
                <input
                  type="number"
                  min="1"
                  className="w-full border-2 border-gray-400 p-2 outline-none"
                  placeholder="Ex: 15"
                  value={prazoFormValor}
                  onChange={(e) => {
                    setPrazoFormValor(e.target.value);
                    setErroPrazo("");
                  }}
                />
                <div className="text-xs text-gray-500 mt-1">
                  {prazoFormCategoria === "GLOBAL"
                    ? `Isso irá atualizar todos os ${instrumentos.length} instrumentos cadastrados.`
                    : `Isso irá atualizar ${instrumentos.filter((i) => i.categoria === prazoFormCategoria).length} instrumento(s) da categoria "${prazoFormCategoria}".`}
                </div>
              </div>

              {erroPrazo && (
                <div className="border-2 border-red-500 bg-red-50 p-3 text-red-700 text-sm font-bold">
                  {erroPrazo}
                </div>
              )}
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex gap-3 justify-end">
              <button
                onClick={() => setModalPrazo(false)}
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
              >
                Cancelar
              </button>
              <button
                onClick={handleSalvarPrazo}
                className="px-4 py-2 border-2 border-yellow-500 bg-yellow-500 text-white font-bold hover:bg-yellow-600"
              >
                Aplicar e Recalcular
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal: Novo / Editar ── */}
      {modalForm.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-lg max-h-screen overflow-y-auto">
            <div className="bg-blue-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center sticky top-0">
              <span>
                {modalForm.instrumento
                  ? "Editar Instrumento"
                  : "Novo Instrumento"}
              </span>
              <button
                onClick={() => setModalForm({ aberto: false })}
                className="text-xl leading-none hover:text-gray-200"
              >
                ✕
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-bold mb-1">
                  Nome *
                </label>
                <input
                  className="w-full border-2 border-gray-400 p-2 outline-none"
                  placeholder="Ex: Kit Exame Padrão"
                  value={form.nome}
                  onChange={(e) => {
                    setForm((f) => ({
                      ...f,
                      nome: e.target.value,
                    }));
                    setErroCodigo("");
                  }}
                />
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">
                  Categoria *
                </label>
                <select
                  className="w-full border-2 border-gray-400 p-2 bg-white outline-none"
                  value={form.categoria}
                  onChange={(e) => {
                    setForm((f) => ({
                      ...f,
                      categoria: e.target.value,
                    }));
                    setErroCodigo("");
                  }}
                >
                  <option value="">
                    Selecione uma categoria
                  </option>
                  {CATEGORIAS.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">
                  Código Identificador *
                </label>
                <input
                  className="w-full border-2 border-gray-400 p-2 outline-none font-mono"
                  placeholder="Ex: KE-A03"
                  value={form.codigo}
                  onChange={(e) => {
                    setForm((f) => ({
                      ...f,
                      codigo: e.target.value,
                    }));
                    setErroCodigo("");
                  }}
                />
              </div>

              <div>
                <label className="block text-sm font-bold mb-2">
                  Tipo *
                </label>
                <div className="grid grid-cols-2 gap-2">
                  {(
                    ["INDIVIDUAL", "KIT"] as TipoInstrumento[]
                  ).map((t) => (
                    <button
                      key={t}
                      type="button"
                      onClick={() =>
                        setForm((f) => ({
                          ...f,
                          tipo: t,
                          componentesIds: [],
                        }))
                      }
                      className={`px-3 py-2 border-2 font-bold text-sm ${
                        form.tipo === t
                          ? "border-blue-500 bg-blue-500 text-white"
                          : "border-gray-400 bg-white hover:bg-gray-100"
                      }`}
                    >
                      {t}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">
                  Prazo de validade após esterilização (dias)
                </label>
                <input
                  type="number"
                  min="1"
                  className="w-full border-2 border-gray-400 p-2 outline-none"
                  placeholder="Ex: 30"
                  value={form.prazoValidadeDias}
                  onChange={(e) =>
                    setForm((f) => ({
                      ...f,
                      prazoValidadeDias: e.target.value,
                    }))
                  }
                />
                {form.categoria && (
                  <div className="text-xs text-gray-500 mt-1">
                    Padrão configurado para "{form.categoria}":{" "}
                    {prazosPorCategoria[form.categoria] ??
                      PRAZO_PADRAO_GLOBAL}{" "}
                    dias
                  </div>
                )}
              </div>

              <div>
                <label className="block text-sm font-bold mb-1">
                  Status inicial
                </label>
                <select
                  className="w-full border-2 border-gray-400 p-2 bg-white outline-none"
                  value={form.status}
                  onChange={(e) =>
                    setForm((f) => ({
                      ...f,
                      status: e.target
                        .value as StatusInstrumento,
                    }))
                  }
                >
                  <option value="ATIVO">Ativo</option>
                  <option value="INATIVO">Inativo</option>
                </select>
              </div>

              {form.tipo === "KIT" && (
                <div className="border-2 border-blue-300 bg-blue-50 p-4 space-y-2">
                  <div className="text-sm font-bold text-blue-800">
                    Instrumentos individuais do kit
                  </div>
                  <div className="text-xs text-gray-500 mb-2">
                    Selecione os instrumentos individuais que
                    compõem este kit.
                  </div>
                  {individuais.length === 0 ? (
                    <div className="text-xs text-gray-400 italic">
                      Nenhum instrumento individual ativo
                      cadastrado.
                    </div>
                  ) : (
                    <div className="max-h-40 overflow-y-auto space-y-1">
                      {individuais.map((ind) => (
                        <label
                          key={ind.id}
                          className="flex items-center gap-2 cursor-pointer p-1 hover:bg-blue-100"
                        >
                          <input
                            type="checkbox"
                            checked={form.componentesIds.includes(
                              ind.id,
                            )}
                            onChange={() =>
                              toggleComponente(ind.id)
                            }
                            className="w-4 h-4"
                          />
                          <span className="text-sm">
                            {ind.nome}
                          </span>
                          <span className="text-xs font-mono text-gray-500">
                            ({ind.codigo})
                          </span>
                        </label>
                      ))}
                    </div>
                  )}
                  {form.componentesIds.length > 0 && (
                    <div className="text-xs text-blue-700 font-bold mt-1">
                      {form.componentesIds.length}{" "}
                      instrumento(s) selecionado(s)
                    </div>
                  )}
                </div>
              )}

              {erroCodigo && (
                <div className="border-2 border-red-500 bg-red-50 p-3 text-red-700 text-sm font-bold">
                  {erroCodigo}
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
                disabled={salvandoInstrumento}
                className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600 disabled:opacity-50"
              >
                {salvandoInstrumento ? "Salvando..." : "Salvar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal: Detalhes ── */}
      {modalDetalhes.aberto && modalDetalhes.instrumento && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-gray-700 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Detalhes do Instrumento</span>
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
                  value: modalDetalhes.instrumento.nome,
                },
                {
                  label: "Categoria",
                  value: modalDetalhes.instrumento.categoria,
                },
                {
                  label: "Código",
                  value: modalDetalhes.instrumento.codigo,
                },
                {
                  label: "Validade",
                  value:
                    modalDetalhes.instrumento
                      .prazoValidadeDias !== null
                      ? `${modalDetalhes.instrumento.prazoValidadeDias} dias após esterilização`
                      : "—",
                },
              ].map((row) => (
                <div
                  key={row.label}
                  className="flex gap-2 border-b border-gray-200 pb-2"
                >
                  <span className="font-bold w-28 text-gray-600">
                    {row.label}
                  </span>
                  <span>{row.value}</span>
                </div>
              ))}
              <div className="flex gap-2 items-center border-b border-gray-200 pb-2">
                <span className="font-bold w-28 text-gray-600">
                  Tipo
                </span>
                <TipoBadge
                  tipo={modalDetalhes.instrumento.tipo}
                />
              </div>
              <div className="flex gap-2 items-center">
                <span className="font-bold w-28 text-gray-600">
                  Status
                </span>
                <StatusBadge
                  status={modalDetalhes.instrumento.status}
                />
              </div>
              {modalDetalhes.instrumento.tipo === "KIT" &&
                modalDetalhes.instrumento.componentesIds
                  .length > 0 && (
                  <div className="border-t border-gray-200 pt-3">
                    <div className="font-bold text-gray-600 mb-2">
                      Instrumentos do kit:
                    </div>
                    <ul className="space-y-1">
                      {modalDetalhes.instrumento.componentesIds.map(
                        (cid) => {
                          const comp = instrumentos.find(
                            (i) => i.id === cid,
                          );
                          return comp ? (
                            <li
                              key={cid}
                              className="text-xs flex gap-2"
                            >
                              <span className="font-mono text-gray-500">
                                {comp.codigo}
                              </span>
                              <span>{comp.nome}</span>
                            </li>
                          ) : null;
                        },
                      )}
                    </ul>
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
                  abrirEditar(modalDetalhes.instrumento!);
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

      {/* ── Modal: Desativar ── */}
      {modalDesativar.aberto && modalDesativar.instrumento && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-sm">
            <div className="bg-red-500 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Desativar Instrumento</span>
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
                Tem certeza que deseja desativar{" "}
                <strong>
                  {modalDesativar.instrumento.nome}
                </strong>
                ?
              </p>
              <p className="text-gray-500">
                Instrumentos inativos não aparecem nos fluxos de
                esterilização e não podem ser adicionados a
                kits.
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