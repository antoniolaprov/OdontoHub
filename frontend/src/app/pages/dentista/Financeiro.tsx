import { Lock, Plus } from "lucide-react";
import { useEffect, useState } from "react";
import { api } from "../../api/client";

const brl = (v: number) => (v ?? 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const dataBR = (iso: string) => {
  if (!iso) return "—";
  const [ano, mes, dia] = iso.split("T")[0].split("-");
  return `${dia}/${mes}/${ano}`;
};

interface LancamentoUI {
  id: number; data: string; descricao: string; categoria: string;
  tipo: "Entrada" | "Saída"; valor: string; automatico: boolean;
}

export default function DentistaFinanceiro() {
  const [lancamentos, setLancamentos] = useState<LancamentoUI[]>([]);
  const [modalAberto, setModalAberto] = useState(false);
  const [kpis, setKpis] = useState([
    { label: "Saldo Atual", value: "R$ 0,00", color: "text-green-600" },
    { label: "Entradas Previstas", value: "R$ 0,00", color: "text-blue-600" },
    { label: "Saídas Pendentes", value: "R$ 0,00", color: "text-red-600" },
  ]);

  function carregar() {
    Promise.all([
      api.get<any[]>("/fluxo-caixa/lancamentos"),
      api.get<number>("/fluxo-caixa/saldo-atual"),
    ])
      .then(([lista, saldo]) => {
        const dados = Array.isArray(lista) ? lista : [];
        setLancamentos(
          dados.map((b, i) => ({
            id: b.id?.id ?? i + 1,
            data: dataBR(b.data),
            descricao: b.descricao ?? "—",
            categoria: b.categoria ?? "—",
            tipo: b.tipo === "ENTRADA" ? "Entrada" : "Saída",
            valor: brl(b.valor),
            automatico: !!b.geradoAutomaticamente,
          })),
        );
        const soma = (tipo: string) =>
          dados.filter((b) => b.tipo === tipo && b.previsto).reduce((s, b) => s + (b.valor ?? 0), 0);
        setKpis([
          { label: "Saldo Atual", value: brl(saldo), color: "text-green-600" },
          { label: "Entradas Previstas", value: brl(soma("ENTRADA")), color: "text-blue-600" },
          { label: "Saídas Pendentes", value: brl(soma("SAIDA")), color: "text-red-600" },
        ]);
      })
      .catch((e) => console.warn("Falha ao carregar fluxo de caixa:", e));
  }

  useEffect(carregar, []);

  return (
    <div className="p-6 h-full flex flex-col">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-gray-700">Fluxo de Caixa</h1>
        <button
          onClick={() => setModalAberto(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white font-bold rounded hover:bg-blue-600"
        >
          <Plus size={18} /> Novo Lançamento Manual
        </button>
      </div>

      <div className="grid grid-cols-3 gap-6 mb-8">
        {kpis.map((kpi, index) => (
          <div key={index} className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm">
            <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-2">{kpi.label}</h3>
            <p className={`text-3xl font-bold ${kpi.color}`}>{kpi.value}</p>
          </div>
        ))}
      </div>

      <div className="flex-1 bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
          <h2 className="text-lg font-bold text-gray-700">Lançamentos</h2>
          <div className="flex gap-4">
            <select className="border border-gray-300 rounded px-3 py-1.5 text-sm bg-white">
              <option>Todos os tipos</option>
              <option>Entradas</option>
              <option>Saídas</option>
            </select>
            <select className="border border-gray-300 rounded px-3 py-1.5 text-sm bg-white">
              <option>Este Mês</option>
              <option>Mês Passado</option>
            </select>
          </div>
        </div>
        
        <div className="overflow-auto flex-1">
          <table className="w-full text-left border-collapse">
            <thead className="bg-gray-50 sticky top-0 border-b border-gray-200 z-10">
              <tr>
                <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Data</th>
                <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Descrição</th>
                <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Categoria</th>
                <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Tipo</th>
                <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-right">Valor</th>
                <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-center">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {lancamentos.map((item) => (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-700">{item.data}</td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{item.descricao}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    <span className="px-2 py-1 bg-gray-100 rounded text-xs font-medium text-gray-600">
                      {item.categoria}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm">
                    {item.tipo === "Entrada" ? (
                      <span className="flex items-center gap-1.5 text-green-600 font-bold">
                        <span className="w-2 h-2 rounded-full bg-green-500"></span> Entrada
                      </span>
                    ) : (
                      <span className="flex items-center gap-1.5 text-red-600 font-bold">
                        <span className="w-2 h-2 rounded-full bg-red-500"></span> Saída
                      </span>
                    )}
                  </td>
                  <td className={`px-6 py-4 text-sm font-bold text-right ${item.tipo === "Entrada" ? "text-green-600" : "text-red-600"}`}>
                    {item.tipo === "Entrada" ? "+" : "-"} {item.valor}
                  </td>
                  <td className="px-6 py-4 text-center">
                    {item.automatico ? (
                      <button className="text-gray-400 hover:text-gray-500 flex items-center justify-center w-full" title="Gerado automaticamente (Não editável)">
                        <Lock size={16} />
                      </button>
                    ) : (
                      <button className="text-blue-500 hover:text-blue-700 text-sm font-bold">
                        Editar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {modalAberto && (
        <ModalLancamento
          onFechar={() => setModalAberto(false)}
          onSalvo={() => {
            setModalAberto(false);
            carregar();
          }}
        />
      )}
    </div>
  );
}

function ModalLancamento(props: { onFechar: () => void; onSalvo: () => void }) {
  const [tipo, setTipo] = useState<"Entrada" | "Saída">("Entrada");
  const [valor, setValor] = useState("");
  const [descricao, setDescricao] = useState("");
  const [categoria, setCategoria] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    const v = Number(valor);
    if (v <= 0 || !descricao.trim()) {
      setErro("Informe um valor positivo e uma descrição.");
      return;
    }
    setSalvando(true);
    try {
      if (tipo === "Entrada") {
        // POST real: /api/fluxo-caixa/entradas (EntradaRequest).
        await api.post("/fluxo-caixa/entradas", { valor: v, descricao });
      } else {
        // POST real: /api/fluxo-caixa/saidas (SaidaRequest).
        await api.post("/fluxo-caixa/saidas", { valor: v, categoria: categoria || "Geral", descricao });
      }
      props.onSalvo();
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Falha ao registrar lançamento.");
      setSalvando(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white border border-gray-300 rounded-lg shadow-lg w-full max-w-md">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex justify-between items-center rounded-t-lg">
          <h2 className="font-bold text-gray-700">Novo Lançamento Manual</h2>
          <button onClick={props.onFechar} className="text-gray-500 hover:text-gray-700 font-bold">✕</button>
        </div>
        <form onSubmit={salvar} className="p-6 space-y-4">
          {erro && (
            <div className="border border-red-300 bg-red-50 text-red-700 text-sm p-3 rounded">{erro}</div>
          )}
          <div className="flex gap-2">
            {(["Entrada", "Saída"] as const).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setTipo(t)}
                className={`flex-1 py-2 font-bold rounded border ${
                  tipo === t
                    ? t === "Entrada"
                      ? "bg-green-500 text-white border-green-500"
                      : "bg-red-500 text-white border-red-500"
                    : "bg-white text-gray-600 border-gray-300"
                }`}
              >
                {t}
              </button>
            ))}
          </div>
          <label className="block">
            <span className="block text-sm font-bold text-gray-700 mb-1">Valor (R$)</span>
            <input
              type="number" min="0" step="0.01" value={valor}
              onChange={(e) => setValor(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2 focus:border-blue-500 outline-none"
            />
          </label>
          <label className="block">
            <span className="block text-sm font-bold text-gray-700 mb-1">Descrição</span>
            <input
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="ex.: Consulta - Maria Silva"
              className="w-full border border-gray-300 rounded px-3 py-2 focus:border-blue-500 outline-none"
            />
          </label>
          {tipo === "Saída" && (
            <label className="block">
              <span className="block text-sm font-bold text-gray-700 mb-1">Categoria</span>
              <input
                value={categoria}
                onChange={(e) => setCategoria(e.target.value)}
                placeholder="ex.: Insumos, Aluguel"
                className="w-full border border-gray-300 rounded px-3 py-2 focus:border-blue-500 outline-none"
              />
            </label>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={props.onFechar} className="px-4 py-2 border border-gray-300 rounded font-bold text-gray-600">
              Cancelar
            </button>
            <button type="submit" disabled={salvando} className="px-4 py-2 bg-blue-500 text-white font-bold rounded disabled:opacity-50">
              {salvando ? "Salvando..." : "Lançar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
