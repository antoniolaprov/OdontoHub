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
  const [kpis, setKpis] = useState([
    { label: "Saldo Atual", value: "R$ 0,00", color: "text-green-600" },
    { label: "Entradas Previstas", value: "R$ 0,00", color: "text-blue-600" },
    { label: "Saídas Pendentes", value: "R$ 0,00", color: "text-red-600" },
  ]);

  useEffect(() => {
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
  }, []);

  return (
    <div className="p-6 h-full flex flex-col">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-gray-700">Fluxo de Caixa</h1>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white font-bold rounded hover:bg-blue-600">
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
    </div>
  );
}
