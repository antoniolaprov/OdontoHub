import { 
  LineChart, 
  Line, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  ComposedChart
} from "recharts";
import { Calendar } from "lucide-react";
import { useEffect, useState } from "react";
import { api } from "../../api/client";

export default function DentistaDashboard() {
  // Série mensal de churn: sem endpoint de série temporal no backend — mantida como mock.
  const churnData = [
    { mes: "Jan", taxa: 5.2 },
    { mes: "Fev", taxa: 4.8 },
    { mes: "Mar", taxa: 6.1 },
    { mes: "Abr", taxa: 5.5 },
    { mes: "Mai", taxa: 4.2 },
    { mes: "Jun", taxa: 3.8 },
  ];

  // Pareto dos motivos de cancelamento — ligado ao backend (GET /api/churn/pareto-cancelamentos).
  const [motivosData, setMotivosData] = useState([
    { motivo: "Preço", qtd: 15, cumulativo: 37 },
    { motivo: "Mudança", qtd: 12, cumulativo: 67 },
    { motivo: "Insatisfação", qtd: 8, cumulativo: 87 },
    { motivo: "Agenda", qtd: 5, cumulativo: 100 },
  ]);

  useEffect(() => {
    api.get<any[]>("/churn/pareto-cancelamentos")
      .then((pareto) => {
        if (Array.isArray(pareto) && pareto.length > 0) {
          setMotivosData(
            pareto.map((it) => ({
              motivo: it.motivo,
              qtd: it.quantidade,
              cumulativo: Math.round((it.percentualAcumulado ?? 0) * 100),
            })),
          );
        }
      })
      .catch((e) => console.warn("Falha ao carregar Pareto de churn:", e));
  }, []);

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
        <div className="col-span-1 md:col-span-3 border-2 border-red-500 bg-red-50 p-6 rounded-lg flex items-center justify-between">
          <div>
            <div className="text-sm font-bold text-red-700 mb-1 uppercase tracking-wider">Custo de Cadeira Vazia</div>
            <div className="text-3xl font-bold text-red-900">R$ 1.250,00 / dia</div>
          </div>
          <div className="text-right">
            <div className="text-sm text-red-600 mb-1">Impacto Mensal Estimado</div>
            <div className="text-xl font-bold text-red-800">R$ 27.500,00</div>
          </div>
        </div>
      </div>

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
          <h2 className="font-bold text-gray-700 mb-6">Motivos de Cancelamento (Pareto)</h2>
          <div className="flex-1 min-h-[250px]">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={motivosData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
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
