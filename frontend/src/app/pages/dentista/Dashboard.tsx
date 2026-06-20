import {
  LineChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ComposedChart,
} from "recharts";
import { Calendar } from "lucide-react";
import { useEffect, useState } from "react";
import { api } from "../../api/client";

type StatusChurn = "ATIVO" | "ZONA_DE_RISCO" | "EVADIDO" | "REATIVADO";

interface MotivoCancelamento {
  motivo: string;
  qtd: number;
  cumulativo: number;
}

const STATUS_LABEL: Record<StatusChurn, string> = {
  ATIVO: "Ativo",
  ZONA_DE_RISCO: "Zona de Risco",
  EVADIDO: "Evadido",
  REATIVADO: "Reativado",
};

const VALOR_MEDIO_HORA = 300;

function adaptarPareto(it: any): MotivoCancelamento {
  return {
    motivo: it.motivo ?? "Sem categoria",
    qtd: Number(it.quantidade ?? 0),
    cumulativo: Math.round(Number(it.percentualAcumulado ?? 0) * 100),
  };
}

function dinheiro(valor: number) {
  return valor.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });
}

function calcularReceitaPerdidaLocal(horas: number) {
  return horas * VALOR_MEDIO_HORA;
}

export default function DentistaDashboard() {
  // Serie mensal de churn: sem endpoint de serie temporal no backend, mantida como mock.
  const churnData = [
    { mes: "Jan", taxa: 5.2 },
    { mes: "Fev", taxa: 4.8 },
    { mes: "Mar", taxa: 6.1 },
    { mes: "Abr", taxa: 5.5 },
    { mes: "Mai", taxa: 4.2 },
    { mes: "Jun", taxa: 3.8 },
  ];

  const [motivosData, setMotivosData] = useState<MotivoCancelamento[]>([
    { motivo: "Preco", qtd: 15, cumulativo: 37 },
    { motivo: "Mudanca", qtd: 12, cumulativo: 67 },
    { motivo: "Insatisfacao", qtd: 8, cumulativo: 87 },
    { motivo: "Agenda", qtd: 5, cumulativo: 100 },
  ]);
  const [paciente, setPaciente] = useState("Marcos Vieira");
  const [mesesSemRetorno, setMesesSemRetorno] = useState(13);
  const [possuiPlanoAtivo, setPossuiPlanoAtivo] = useState(true);
  const [agendamentoFuturo, setAgendamentoFuturo] = useState(false);
  const [statusPaciente, setStatusPaciente] = useState<StatusChurn | "">("");
  const [categoriaMotivo, setCategoriaMotivo] = useState("Preco");
  const [horasOciosas, setHorasOciosas] = useState(3);
  const [receitaPerdida, setReceitaPerdida] = useState(0);
  const [tipoProcedimento, setTipoProcedimento] = useState("Ortodontia");
  const [qtdProcedimento, setQtdProcedimento] = useState<number | null>(null);
  const [mensagem, setMensagem] = useState("");
  const [categoriasOcultas, setCategoriasOcultas] = useState<string[]>([]);

  function carregarPareto() {
    return api.get<any[]>("/churn/pareto-cancelamentos")
      .then((pareto) => {
        if (Array.isArray(pareto) && pareto.length > 0) {
          setMotivosData(pareto.map(adaptarPareto));
        }
      })
      .catch((e) => console.warn("Falha ao carregar Pareto de churn:", e));
  }

  useEffect(() => {
    carregarPareto();
    setReceitaPerdida(calcularReceitaPerdidaLocal(horasOciosas));
  }, []);

  async function handleRecalcularPaciente() {
    setMensagem("");
    try {
      await api.post(`/churn/paciente/${encodeURIComponent(paciente)}/dados`, {
        paciente,
        agendamentoFuturo,
        mesesSemRetorno: Number(mesesSemRetorno),
        planoAtivo: possuiPlanoAtivo,
      });
      await api.post("/churn/recalcular");
      const status = await api.get<StatusChurn>(`/churn/paciente/${encodeURIComponent(paciente)}/status`);
      setStatusPaciente(status);
      setMensagem("Status de churn atualizado.");
    } catch (e: any) {
      setMensagem(e.message ?? "Falha ao recalcular churn.");
    }
  }

  async function handleRegistrarCancelamento() {
    setMensagem("");
    try {
      await api.post("/churn/cancelamentos", {
        paciente,
        categoriaMotivo,
      });
      await carregarPareto();
      setMensagem("Cancelamento contabilizado no Pareto.");
    } catch (e: any) {
      setMensagem(e.message ?? "Falha ao registrar cancelamento.");
    }
  }

  function handleOcultarCategoria() {
    const categoria = categoriaMotivo.trim();
    if (!categoria) {
      return;
    }
    setCategoriasOcultas((atuais) =>
      atuais.includes(categoria) ? atuais : [...atuais, categoria],
    );
  }

  function handleRestaurarCategorias() {
    setCategoriasOcultas([]);
  }

  async function handleCalcularReceita() {
    setMensagem("");
    try {
      const valor = await api.get<number>(`/churn/receita-perdida?horas=${horasOciosas}`);
      const receita = Number(valor ?? 0);
      setReceitaPerdida(receita > 0 ? receita : calcularReceitaPerdidaLocal(horasOciosas));
      setMensagem("Receita perdida recalculada.");
    } catch (e: any) {
      setReceitaPerdida(calcularReceitaPerdidaLocal(horasOciosas));
      setMensagem("Receita perdida recalculada.");
    }
  }

  async function handleFiltrarProcedimento() {
    setMensagem("");
    try {
      await api.post(`/churn/paciente/${encodeURIComponent(paciente)}/tipo-procedimento`, {
        tipoProcedimento,
      });
      const qtd = await api.get<number>(`/churn/por-procedimento?tipo=${encodeURIComponent(tipoProcedimento)}`);
      setQtdProcedimento(Number(qtd ?? 0));
      setMensagem("Filtro por procedimento atualizado.");
    } catch (e: any) {
      setMensagem(e.message ?? "Falha ao filtrar procedimento.");
    }
  }

  const motivosVisiveis = motivosData.filter(
    (item) => !categoriasOcultas.includes(item.motivo),
  );

  return (
    <div className="p-6 h-full flex flex-col">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold text-gray-700">Dashboards & Inteligência</h1>

        <div className="flex items-center gap-2 border border-gray-300 rounded bg-white px-3 py-2 text-sm text-gray-600">
          <Calendar size={16} />
          <span>Filtro de Data:</span>
          <select className="bg-transparent border-none outline-none font-medium text-gray-800 cursor-pointer">
            <option>Últimos 6 meses</option>
            <option>Este Ano</option>
            <option>Ano Passado</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="border-2 border-red-500 bg-red-50 p-5 rounded-lg">
          <div className="text-sm font-bold text-red-700 mb-1 uppercase tracking-wider">Receita perdida</div>
          <div className="text-3xl font-bold text-red-900">{dinheiro(receitaPerdida)}</div>
          <div className="flex gap-2 mt-3">
            <input
              type="number"
              min={0}
              className="w-24 border border-red-200 rounded px-2 py-1 text-sm"
              value={horasOciosas}
              onChange={(e) => setHorasOciosas(Number(e.target.value))}
            />
            <button
              onClick={handleCalcularReceita}
              className="px-3 py-1 border border-red-500 bg-red-500 text-white rounded text-sm font-bold"
            >
              Calcular horas
            </button>
          </div>
        </div>

        <div className="border border-gray-200 bg-white p-5 rounded-lg shadow-sm">
          <div className="text-sm font-bold text-gray-700 mb-3">Status do Paciente</div>
          <input
            className="w-full border border-gray-300 rounded px-2 py-1 text-sm mb-2"
            value={paciente}
            onChange={(e) => setPaciente(e.target.value)}
          />
          <div className="grid grid-cols-2 gap-2 mb-2">
            <input
              type="number"
              min={0}
              className="border border-gray-300 rounded px-2 py-1 text-sm"
              value={mesesSemRetorno}
              onChange={(e) => setMesesSemRetorno(Number(e.target.value))}
            />
            <label className="flex items-center gap-2 text-xs">
              <input
                type="checkbox"
                checked={possuiPlanoAtivo}
                onChange={(e) => setPossuiPlanoAtivo(e.target.checked)}
              />
              Plano ativo
            </label>
          </div>
          <label className="flex items-center gap-2 text-xs mb-2">
            <input
              type="checkbox"
              checked={agendamentoFuturo}
              onChange={(e) => setAgendamentoFuturo(e.target.checked)}
            />
            Possui agendamento futuro
          </label>
          <button
            onClick={handleRecalcularPaciente}
            className="w-full px-3 py-1 border border-blue-600 bg-blue-600 text-white rounded text-sm font-bold"
          >
            Recalcular churn
          </button>
          {statusPaciente && (
            <div className="mt-2 text-sm font-bold text-gray-700">
              {STATUS_LABEL[statusPaciente]}
            </div>
          )}
        </div>

        <div className="border border-gray-200 bg-white p-5 rounded-lg shadow-sm">
          <div className="text-sm font-bold text-gray-700 mb-3">Retenção por Procedimento</div>
          <input
            className="w-full border border-gray-300 rounded px-2 py-1 text-sm mb-2"
            value={tipoProcedimento}
            onChange={(e) => setTipoProcedimento(e.target.value)}
          />
          <button
            onClick={handleFiltrarProcedimento}
            className="w-full px-3 py-1 border border-emerald-600 bg-emerald-600 text-white rounded text-sm font-bold"
          >
            Filtrar evadidos
          </button>
          <div className="mt-2 text-sm text-gray-600">
            {qtdProcedimento === null ? "Sem filtro aplicado" : `${qtdProcedimento} paciente(s) evadido(s)`}
          </div>
        </div>
      </div>

      {mensagem && (
        <div className="mb-4 border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 rounded">
          {mensagem}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 flex-1">
        <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm flex flex-col">
          <h2 className="font-bold text-gray-700 mb-6">Taxa de Evasão (Churn)</h2>
          <div className="flex-1 min-h-[250px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={churnData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="mes" />
                <YAxis unit="%" />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="taxa" name="Taxa de Churn (%)" stroke="#ef4444" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm flex flex-col">
          <div className="flex items-start justify-between gap-3 mb-4">
            <h2 className="font-bold text-gray-700">Motivos de Cancelamento (Pareto)</h2>
            <div className="flex gap-2">
              <input
                className="w-32 border border-gray-300 rounded px-2 py-1 text-xs"
                value={categoriaMotivo}
                onChange={(e) => setCategoriaMotivo(e.target.value)}
              />
              <button
                onClick={handleOcultarCategoria}
                disabled={!categoriaMotivo.trim()}
                className="px-3 py-1 border border-amber-600 bg-amber-600 text-white rounded text-xs font-bold disabled:opacity-40"
              >
                Tirar
              </button>
              <button
                onClick={handleRegistrarCancelamento}
                className="px-3 py-1 border border-blue-600 bg-blue-600 text-white rounded text-xs font-bold"
              >
                Registrar
              </button>
            </div>
          </div>
          {categoriasOcultas.length > 0 && (
            <div className="mb-3 flex items-center justify-between gap-3 text-xs text-gray-600">
              <span>Ocultas na tela: {categoriasOcultas.join(", ")}</span>
              <button
                onClick={handleRestaurarCategorias}
                className="px-2 py-1 border border-gray-300 bg-white rounded font-bold"
              >
                Restaurar todas
              </button>
            </div>
          )}
          <div className="flex-1 min-h-[250px]">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={motivosVisiveis} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="motivo" />
                <YAxis yAxisId="left" orientation="left" />
                <YAxis yAxisId="right" orientation="right" unit="%" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="left" dataKey="qtd" name="Quantidade" fill="#3b82f6" barSize={40} />
                <Line yAxisId="right" type="monotone" dataKey="cumulativo" name="Acumulado (%)" stroke="#10b981" strokeWidth={3} dot={{ r: 4 }} />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
