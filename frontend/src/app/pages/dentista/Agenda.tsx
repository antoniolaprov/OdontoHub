import { useState, useMemo, useEffect } from "react";
import { api } from "../../api/client";

// ─── Agenda do Cirurgião-Dentista — somente leitura ────────────────────────────
// Mesma fonte de dados da Agenda da Recepcionista (GET /api/agendamentos), mas
// sem nenhuma ação de agendamento (criar/confirmar/remarcar/cancelar) — isso é
// responsabilidade da Recepcionista. Aqui é só um auxílio visual pro dentista
// ver a própria agenda.

type TipoAgendamento = "CONSULTA" | "RETORNO";
type StatusAgendamento = "AGENDADO" | "CONFIRMADO" | "CANCELADO" | "REMARCADO";

interface Agendamento {
  id: number;
  paciente: string;
  dentista: string;
  data: string; // DD/MM/YYYY
  hora: string; // HH:MM
  tipo: TipoAgendamento;
  status: StatusAgendamento;
}

const HORARIOS = ["08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"];
const DIAS_SEMANA_LABELS = ["SEG", "TER", "QUA", "QUI", "SEX"];

function getMondayOf(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay();
  d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day));
  d.setHours(0, 0, 0, 0);
  return d;
}

function addDays(date: Date, days: number): Date {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function dateToBR(d: Date): string {
  return d.toLocaleDateString("pt-BR");
}

function isToday(d: Date): boolean {
  const t = new Date();
  return d.getDate() === t.getDate() && d.getMonth() === t.getMonth() && d.getFullYear() === t.getFullYear();
}

function splitISODateTime(iso: string | null | undefined): { data: string; hora: string } {
  if (!iso) return { data: "—", hora: "—" };
  const [datePart, timePart = ""] = iso.split("T");
  const [y, m, d] = datePart.split("-");
  return { data: `${d}/${m}/${y}`, hora: timePart.slice(0, 5) };
}

function adaptarAgendamento(b: any, indice: number): Agendamento {
  const { data, hora } = splitISODateTime(b.dataHora);
  return {
    id: b.id ?? indice + 1,
    paciente: b.paciente ?? "—",
    dentista: b.dentista ?? "—",
    data,
    hora,
    tipo: (b.tipo as TipoAgendamento) ?? "CONSULTA",
    status: (b.status as StatusAgendamento) ?? "AGENDADO",
  };
}

function StatusBadge({ status }: { status: StatusAgendamento }) {
  const cls: Record<StatusAgendamento, string> = {
    AGENDADO: "bg-blue-100 border-blue-400 text-blue-700",
    CONFIRMADO: "bg-green-100 border-green-500 text-green-700",
    CANCELADO: "bg-red-100 border-red-500 text-red-700",
    REMARCADO: "bg-yellow-100 border-yellow-500 text-yellow-700",
  };
  return <span className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${cls[status]}`}>{status}</span>;
}

function TipoBadge({ tipo }: { tipo: TipoAgendamento }) {
  return (
    <span
      className={`inline-block px-2 py-0.5 border-2 text-xs font-bold ${
        tipo === "RETORNO" ? "bg-purple-100 border-purple-400 text-purple-700" : "bg-gray-100 border-gray-400 text-gray-700"
      }`}
    >
      {tipo}
    </span>
  );
}

function corCelula(status: StatusAgendamento): string {
  return {
    AGENDADO: "border-blue-400 bg-blue-50",
    CONFIRMADO: "border-green-500 bg-green-50",
    CANCELADO: "border-red-400 bg-red-50",
    REMARCADO: "border-yellow-400 bg-yellow-50",
  }[status];
}

export default function DentistaAgenda() {
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    api.get<any[]>("/agendamentos")
      .then((lista) => setAgendamentos(Array.isArray(lista) ? lista.map(adaptarAgendamento) : []))
      .catch((e) => {
        console.warn("Falha ao carregar agendamentos:", e);
        setAgendamentos([]);
      })
      .finally(() => setCarregando(false));
  }, []);

  const [semanaBase, setSemanaBase] = useState<Date>(() => getMondayOf(new Date()));
  const [view, setView] = useState<"calendario" | "lista">("calendario");
  const [filtroDentista, setFiltroDentista] = useState("TODOS");
  const [filtroStatus, setFiltroStatus] = useState("TODOS");
  const [busca, setBusca] = useState("");
  const [modalDetalhes, setModalDetalhes] = useState<{ aberto: boolean; item?: Agendamento }>({ aberto: false });

  const dentistasDisponiveis = useMemo(() => [...new Set(agendamentos.map((a) => a.dentista))], [agendamentos]);

  const diasDaSemana = useMemo(() => Array.from({ length: 5 }, (_, i) => addDays(semanaBase, i)), [semanaBase]);

  const filtrados = useMemo(() => {
    return agendamentos.filter((a) => {
      const matchD = filtroDentista === "TODOS" || a.dentista === filtroDentista;
      const matchS = filtroStatus === "TODOS" || a.status === filtroStatus;
      const matchB = busca === "" || a.paciente.toLowerCase().includes(busca.toLowerCase());
      return matchD && matchS && matchB;
    });
  }, [agendamentos, filtroDentista, filtroStatus, busca]);

  function getCelula(dia: Date, hora: string): Agendamento[] {
    const str = dateToBR(dia);
    return filtrados.filter((a) => a.data === str && a.hora === hora);
  }

  const hojeStr = dateToBR(new Date());

  return (
    <div className="p-6">
      <div className="mb-4">
        <h1 className="text-xl font-bold text-gray-700">Minha Agenda</h1>
        <p className="text-sm text-gray-500 mt-1">
          Visão dos seus agendamentos. Criar, confirmar, remarcar ou cancelar é feito pela Recepcionista.
        </p>
      </div>

      {/* Filters */}
      <div className="border-2 border-gray-400 bg-gray-50 p-3 mb-4">
        <div className="grid grid-cols-4 gap-2">
          <div className="col-span-2 flex items-center border-2 border-gray-400 bg-white">
            <span className="px-2 text-gray-400 text-sm">🔍</span>
            <input
              type="text"
              placeholder="Buscar paciente..."
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              className="flex-1 p-1.5 outline-none bg-transparent text-sm"
            />
          </div>
          <select
            value={filtroDentista}
            onChange={(e) => setFiltroDentista(e.target.value)}
            className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
          >
            <option value="TODOS">Todos os Dentistas</option>
            {dentistasDisponiveis.map((d) => (
              <option key={d} value={d}>{d}</option>
            ))}
          </select>
          <select
            value={filtroStatus}
            onChange={(e) => setFiltroStatus(e.target.value)}
            className="border-2 border-gray-400 bg-white p-1.5 outline-none text-sm"
          >
            <option value="TODOS">Todos os Status</option>
            <option value="AGENDADO">Agendado</option>
            <option value="CONFIRMADO">Confirmado</option>
            <option value="CANCELADO">Cancelado</option>
            <option value="REMARCADO">Remarcado</option>
          </select>
        </div>
      </div>

      {/* View tabs */}
      <div className="flex border-2 border-gray-400 mb-4">
        <button
          onClick={() => setView("calendario")}
          className={`flex-1 p-2 font-bold border-r-2 border-gray-400 text-sm ${
            view === "calendario" ? "bg-blue-500 text-white" : "bg-gray-100 hover:bg-gray-200"
          }`}
        >
          📅 Calendário Semanal
        </button>
        <button
          onClick={() => setView("lista")}
          className={`flex-1 p-2 font-bold text-sm ${
            view === "lista" ? "bg-blue-500 text-white" : "bg-gray-100 hover:bg-gray-200"
          }`}
        >
          📋 Lista de Agendamentos
        </button>
      </div>

      {carregando ? (
        <div className="text-center text-gray-400 py-12">Carregando agenda...</div>
      ) : view === "calendario" ? (
        <>
          <div className="flex justify-between items-center mb-2">
            <button
              onClick={() => setSemanaBase((prev) => addDays(prev, -7))}
              className="px-3 py-1.5 border-2 border-gray-400 bg-white text-sm hover:bg-gray-100"
            >
              ← Semana Anterior
            </button>
            <span className="text-sm font-bold text-gray-600">
              {dateToBR(semanaBase)} — {dateToBR(addDays(semanaBase, 4))}
            </span>
            <button
              onClick={() => setSemanaBase((prev) => addDays(prev, 7))}
              className="px-3 py-1.5 border-2 border-gray-400 bg-white text-sm hover:bg-gray-100"
            >
              Próxima Semana →
            </button>
          </div>

          <div className="border-2 border-gray-400">
            <div className="grid grid-cols-6 border-b-2 border-gray-400">
              <div className="p-2 border-r-2 border-gray-400 bg-gray-100" />
              {diasDaSemana.map((dia, i) => (
                <div
                  key={i}
                  className={`p-2 text-center border-r-2 border-gray-400 font-bold text-sm ${
                    isToday(dia) ? "bg-blue-500 text-white" : "bg-gray-100"
                  }`}
                >
                  <div>{DIAS_SEMANA_LABELS[i]}</div>
                  <div className="text-xs">
                    {String(dia.getDate()).padStart(2, "0")}/{String(dia.getMonth() + 1).padStart(2, "0")}
                  </div>
                </div>
              ))}
            </div>

            {HORARIOS.map((hora) => (
              <div key={hora} className="grid grid-cols-6 border-b-2 border-gray-400">
                <div className="p-2 border-r-2 border-gray-400 bg-gray-50 font-bold text-center text-sm">{hora}</div>
                {diasDaSemana.map((dia, dIdx) => {
                  const items = getCelula(dia, hora);
                  return (
                    <div key={dIdx} className="p-1 border-r-2 border-gray-400 min-h-[72px]">
                      {items.map((item) => (
                        <div
                          key={item.id}
                          onClick={() => setModalDetalhes({ aberto: true, item })}
                          className={`border-2 p-1.5 mb-1 cursor-pointer hover:opacity-75 ${corCelula(item.status)}`}
                        >
                          <span className="text-xs font-bold leading-tight block">{item.paciente}</span>
                          <div className="text-[9px] text-gray-600 mt-0.5">{item.tipo}</div>
                          <div className="text-[9px] font-bold">{item.status}</div>
                        </div>
                      ))}
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        </>
      ) : (
        <div className="border-2 border-gray-400 bg-white">
          <div
            className="grid bg-gray-100 border-b-2 border-gray-400"
            style={{ gridTemplateColumns: "1.2fr 1.5fr 0.8fr 0.7fr 1fr 1fr 0.8fr" }}
          >
            {["Paciente", "Dentista", "Data", "Hora", "Tipo", "Status", "Ações"].map((h) => (
              <div key={h} className="p-2 border-r-2 border-gray-400 font-bold text-sm last:border-r-0">{h}</div>
            ))}
          </div>

          {filtrados.length === 0 && (
            <div className="p-8 text-center text-gray-400">Nenhum agendamento encontrado com os filtros aplicados.</div>
          )}

          {filtrados.map((item) => (
            <div
              key={item.id}
              className="grid border-b-2 border-gray-400"
              style={{ gridTemplateColumns: "1.2fr 1.5fr 0.8fr 0.7fr 1fr 1fr 0.8fr" }}
            >
              <div className="p-2 border-r-2 border-gray-400 text-sm">{item.paciente}</div>
              <div className="p-2 border-r-2 border-gray-400 text-sm">{item.dentista}</div>
              <div className="p-2 border-r-2 border-gray-400 text-sm">{item.data}</div>
              <div className="p-2 border-r-2 border-gray-400 text-sm font-mono">{item.hora}</div>
              <div className="p-2 border-r-2 border-gray-400"><TipoBadge tipo={item.tipo} /></div>
              <div className="p-2 border-r-2 border-gray-400"><StatusBadge status={item.status} /></div>
              <div className="p-2 flex items-center justify-center">
                <button
                  onClick={() => setModalDetalhes({ aberto: true, item })}
                  className="px-2 py-0.5 border-2 border-gray-400 bg-white text-xs font-bold hover:bg-gray-100"
                >
                  Ver
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Agenda de hoje — só leitura */}
      <div className="mt-6">
        <h2 className="font-bold text-gray-700 mb-2 flex items-center gap-2">📋 Agenda de Hoje</h2>
        {agendamentos.filter((a) => a.data === hojeStr).length === 0 ? (
          <div className="text-sm text-gray-400 border-2 border-gray-200 p-3 bg-white max-w-md">
            Nenhum agendamento para hoje.
          </div>
        ) : (
          <div className="space-y-1 max-w-md">
            {agendamentos
              .filter((a) => a.data === hojeStr)
              .sort((a, b) => a.hora.localeCompare(b.hora))
              .map((a) => (
                <div
                  key={a.id}
                  onClick={() => setModalDetalhes({ aberto: true, item: a })}
                  className={`border-2 p-2 cursor-pointer hover:opacity-75 ${corCelula(a.status)}`}
                >
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-bold">{a.hora} — {a.paciente}</span>
                    <StatusBadge status={a.status} />
                  </div>
                  <div className="text-xs text-gray-500 mt-0.5">{a.dentista}</div>
                </div>
              ))}
          </div>
        )}
      </div>

      {/* Modal: Detalhes (somente leitura) */}
      {modalDetalhes.aberto && modalDetalhes.item && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40">
          <div className="bg-white border-2 border-gray-400 w-full max-w-md">
            <div className="bg-gray-700 text-white p-4 font-bold border-b-2 border-gray-400 flex justify-between items-center">
              <span>Detalhes do Agendamento</span>
              <button onClick={() => setModalDetalhes({ aberto: false })} className="text-xl leading-none hover:text-gray-200">✕</button>
            </div>
            <div className="p-6 space-y-3">
              <div className="grid grid-cols-2 gap-3">
                {([
                  ["PACIENTE", modalDetalhes.item.paciente],
                  ["DENTISTA", modalDetalhes.item.dentista],
                  ["DATA", modalDetalhes.item.data],
                  ["HORÁRIO", modalDetalhes.item.hora],
                ] as [string, string][]).map(([label, val]) => (
                  <div key={label}>
                    <div className="text-xs text-gray-500 font-bold">{label}</div>
                    <div className="font-medium text-sm">{val}</div>
                  </div>
                ))}
                <div>
                  <div className="text-xs text-gray-500 font-bold">TIPO</div>
                  <TipoBadge tipo={modalDetalhes.item.tipo} />
                </div>
                <div>
                  <div className="text-xs text-gray-500 font-bold">STATUS</div>
                  <StatusBadge status={modalDetalhes.item.status} />
                </div>
              </div>
            </div>
            <div className="p-4 border-t-2 border-gray-400 flex justify-end">
              <button
                onClick={() => setModalDetalhes({ aberto: false })}
                className="px-4 py-2 border-2 border-gray-400 bg-white font-bold hover:bg-gray-100"
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
