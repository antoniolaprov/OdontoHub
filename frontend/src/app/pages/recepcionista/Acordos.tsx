import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import { api } from "../../api/client";

// ─── Formatação ───────────────────────────────────────────────────────────────
const brl = (v: number) =>
  (v ?? 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

// ─── Tipos do backend ─────────────────────────────────────────────────────────
interface ParcelaBackend {
  valor: number;
  diasAtraso: number;
  multa: number;
  juros: number;
  valorAtualizado: number;
  status: string;
}
interface AcordoBackend {
  id: number;
  status: string; // ATIVO | INADIMPLIDO | CANCELADO
  valorTotal: number;
  quantidadeParcelas: number;
  justificativa: string;
}
interface InadimplenteBackend {
  paciente: string;
  restrito: boolean;
  semCobrancaRecente: boolean;
  totalAtualizado: number;
  parcelas: ParcelaBackend[];
  acordo: AcordoBackend | null;
}

const LABEL_ACORDO: Record<string, string> = {
  ATIVO: "Ativo",
  INADIMPLIDO: "Inadimplido",
  CANCELADO: "Cancelado",
};
const COR_ACORDO: Record<string, string> = {
  ATIVO: "bg-green-100 text-green-700 border-green-500",
  INADIMPLIDO: "bg-red-100 text-red-700 border-red-500",
  CANCELADO: "bg-gray-100 text-gray-600 border-gray-400",
};
const CANAIS = ["Telefone", "WhatsApp", "E-mail", "SMS"];

// ─── Componente ───────────────────────────────────────────────────────────────
export default function RecepcionistaAcordos() {
  const [lista, setLista] = useState<InadimplenteBackend[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [toast, setToast] = useState<{ msg: string; tipo: "sucesso" | "erro" } | null>(null);

  const [modalNovaVencida, setModalNovaVencida] = useState(false);
  const [modalAcordo, setModalAcordo] = useState<InadimplenteBackend | null>(null);
  const [modalCobranca, setModalCobranca] = useState<InadimplenteBackend | null>(null);
  const [modalCancelar, setModalCancelar] = useState<InadimplenteBackend | null>(null);

  function carregar() {
    setCarregando(true);
    return api
      .get<InadimplenteBackend[]>("/inadimplencia")
      .then((l) => setLista(Array.isArray(l) ? l : []))
      .catch((e) => console.warn("Falha ao carregar inadimplência:", e))
      .finally(() => setCarregando(false));
  }

  useEffect(() => {
    carregar();
  }, []);

  function showToast(msg: string, tipo: "sucesso" | "erro") {
    setToast({ msg, tipo });
    setTimeout(() => setToast(null), 3500);
  }

  const kpis = useMemo(() => {
    const totalDevido = lista.reduce((s, i) => s + i.totalAtualizado, 0);
    const restritos = lista.filter((i) => i.restrito).length;
    const acordosAtivos = lista.filter((i) => i.acordo && i.acordo.status === "ATIVO").length;
    return { totalDevido, restritos, acordosAtivos };
  }, [lista]);

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
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
          <h1 className="text-2xl font-bold text-gray-700">Inadimplência e Acordos</h1>
          <p className="text-sm text-gray-500 mt-1">
            Acompanhe pacientes inadimplentes, registre cobranças e negocie acordos.
          </p>
        </div>
        <button
          onClick={() => setModalNovaVencida(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white font-bold rounded hover:bg-blue-600"
        >
          <Plus size={18} /> Parcela Vencida
        </button>
      </div>

      {/* KPIs */}
      <div className="grid grid-cols-3 gap-6">
        {[
          { label: "Total Devido", value: brl(kpis.totalDevido), color: "text-red-600" },
          { label: "Pacientes Restritos", value: String(kpis.restritos), color: "text-orange-600" },
          { label: "Acordos Ativos", value: String(kpis.acordosAtivos), color: "text-green-600" },
        ].map((k) => (
          <div key={k.label} className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm">
            <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-2">{k.label}</h3>
            <p className={`text-3xl font-bold ${k.color}`}>{k.value}</p>
          </div>
        ))}
      </div>

      {/* Lista de inadimplentes */}
      {lista.length === 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-10 text-center text-gray-500">
          {carregando ? "Carregando..." : "Nenhum paciente inadimplente."}
        </div>
      ) : (
        <div className="space-y-4">
          {lista.map((i) => (
            <div key={i.paciente} className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex items-center justify-between gap-4 flex-wrap">
                <div className="flex items-center gap-3">
                  <span className="font-bold text-gray-800 text-lg">{i.paciente}</span>
                  {i.restrito && (
                    <span className="px-2 py-0.5 border border-red-500 bg-red-100 text-red-700 text-xs font-bold rounded">
                      RESTRITO
                    </span>
                  )}
                  {i.acordo && (
                    <span className={`px-2 py-0.5 border text-xs font-bold rounded ${COR_ACORDO[i.acordo.status]}`}>
                      Acordo {LABEL_ACORDO[i.acordo.status]}
                    </span>
                  )}
                  {!i.acordo && i.semCobrancaRecente && (
                    <span className="px-2 py-0.5 border border-orange-400 bg-orange-50 text-orange-600 text-xs font-bold rounded">
                      Sem cobrança recente
                    </span>
                  )}
                </div>
                <div className="text-right">
                  <div className="text-xs text-gray-500 uppercase">Total atualizado</div>
                  <div className="text-xl font-bold text-red-600">{brl(i.totalAtualizado)}</div>
                </div>
              </div>

              {/* Parcelas / acordo */}
              <div className="px-6 py-3">
                {i.acordo ? (
                  <p className="text-sm text-gray-600">
                    Acordo <b>{LABEL_ACORDO[i.acordo.status]}</b> — {i.acordo.quantidadeParcelas} parcela(s),
                    total {brl(i.acordo.valorTotal)}.
                  </p>
                ) : (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-xs text-gray-500 uppercase">
                        <th className="py-1">Valor</th>
                        <th className="py-1 text-right">Dias atraso</th>
                        <th className="py-1 text-right">Multa</th>
                        <th className="py-1 text-right">Juros</th>
                        <th className="py-1 text-right">Atualizado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {i.parcelas.map((p, idx) => (
                        <tr key={idx} className="border-t border-gray-100">
                          <td className="py-1.5 text-gray-700">{brl(p.valor)}</td>
                          <td className="py-1.5 text-right text-gray-600">{p.diasAtraso}</td>
                          <td className="py-1.5 text-right text-gray-600">{brl(p.multa)}</td>
                          <td className="py-1.5 text-right text-gray-600">{brl(p.juros)}</td>
                          <td className="py-1.5 text-right font-bold text-gray-800">{brl(p.valorAtualizado)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>

              {/* Ações */}
              <div className="px-6 py-3 border-t border-gray-200 bg-gray-50 flex gap-2 flex-wrap">
                <button
                  onClick={() => setModalCobranca(i)}
                  className="px-3 py-1.5 border border-gray-400 bg-white text-gray-700 text-xs font-bold rounded hover:bg-gray-100"
                >
                  Registrar cobrança
                </button>
                {!i.acordo && i.parcelas.length > 0 && (
                  <button
                    onClick={() => setModalAcordo(i)}
                    className="px-3 py-1.5 border border-blue-500 bg-white text-blue-600 text-xs font-bold rounded hover:bg-blue-50"
                  >
                    Criar acordo
                  </button>
                )}
                {i.acordo && i.acordo.status !== "CANCELADO" && (
                  <button
                    onClick={() => setModalCancelar(i)}
                    className="px-3 py-1.5 border border-red-400 bg-white text-red-600 text-xs font-bold rounded hover:bg-red-50"
                  >
                    Cancelar acordo
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {modalNovaVencida && (
        <ModalNovaVencida
          onFechar={() => setModalNovaVencida(false)}
          onSalvo={(m) => {
            setModalNovaVencida(false);
            showToast(m, "sucesso");
            carregar();
          }}
          onErro={(m) => showToast(m, "erro")}
        />
      )}
      {modalAcordo && (
        <ModalAcordo
          inadimplente={modalAcordo}
          onFechar={() => setModalAcordo(null)}
          onSalvo={(m) => {
            setModalAcordo(null);
            showToast(m, "sucesso");
            carregar();
          }}
          onErro={(m) => showToast(m, "erro")}
        />
      )}
      {modalCobranca && (
        <ModalCobranca
          inadimplente={modalCobranca}
          onFechar={() => setModalCobranca(null)}
          onSalvo={(m) => {
            setModalCobranca(null);
            showToast(m, "sucesso");
            carregar();
          }}
          onErro={(m) => showToast(m, "erro")}
        />
      )}
      {modalCancelar && (
        <ModalCancelar
          inadimplente={modalCancelar}
          onFechar={() => setModalCancelar(null)}
          onSalvo={(m) => {
            setModalCancelar(null);
            showToast(m, "sucesso");
            carregar();
          }}
          onErro={(m) => showToast(m, "erro")}
        />
      )}
    </div>
  );
}

// ─── Modais ───────────────────────────────────────────────────────────────────
type ModalProps = { onFechar: () => void; onSalvo: (msg: string) => void; onErro: (msg: string) => void };

function CaixaModal(props: { titulo: string; children: React.ReactNode; onFechar: () => void }) {
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white border border-gray-300 rounded-lg shadow-lg w-full max-w-md">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex justify-between items-center rounded-t-lg">
          <h2 className="font-bold text-gray-700">{props.titulo}</h2>
          <button onClick={props.onFechar} className="text-gray-500 hover:text-gray-700 font-bold">✕</button>
        </div>
        {props.children}
      </div>
    </div>
  );
}

function Campo(props: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="block text-sm font-bold text-gray-700 mb-1">{props.label}</span>
      {props.children}
    </label>
  );
}

function Acoes(props: { onFechar: () => void; salvando: boolean; rotulo: string; perigo?: boolean }) {
  return (
    <div className="flex justify-end gap-3 pt-2">
      <button type="button" onClick={props.onFechar} className="px-4 py-2 border border-gray-300 rounded font-bold text-gray-600">
        Fechar
      </button>
      <button
        type="submit"
        disabled={props.salvando}
        className={`px-4 py-2 text-white font-bold rounded disabled:opacity-50 ${
          props.perigo ? "bg-red-500 hover:bg-red-600" : "bg-blue-500 hover:bg-blue-600"
        }`}
      >
        {props.salvando ? "Salvando..." : props.rotulo}
      </button>
    </div>
  );
}

function ModalNovaVencida(props: ModalProps) {
  const [paciente, setPaciente] = useState("");
  const [valor, setValor] = useState("");
  const [dias, setDias] = useState("31");
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    const v = Number(valor);
    const d = Number(dias);
    if (!paciente.trim() || v <= 0 || d < 0) return props.onErro("Preencha paciente, valor e dias de atraso.");
    setSalvando(true);
    try {
      await api.post("/inadimplencia/parcelas-vencidas", { paciente: paciente.trim(), valor: v, diasAtraso: d });
      props.onSalvo("Parcela vencida registrada.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao registrar.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo="Nova Parcela Vencida" onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <Campo label="Paciente">
          <input value={paciente} onChange={(e) => setPaciente(e.target.value)} placeholder="ex.: João Silva"
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500" />
        </Campo>
        <Campo label="Valor (R$)">
          <input type="number" min="0" step="0.01" value={valor} onChange={(e) => setValor(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500" />
        </Campo>
        <Campo label="Dias de atraso">
          <input type="number" min="0" value={dias} onChange={(e) => setDias(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500" />
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Registrar" />
      </form>
    </CaixaModal>
  );
}

function ModalAcordo(props: ModalProps & { inadimplente: InadimplenteBackend }) {
  const [quantidade, setQuantidade] = useState("3");
  const total = props.inadimplente.totalAtualizado;
  const [salvando, setSalvando] = useState(false);
  const qtd = Math.max(1, Number(quantidade) || 1);
  const valorParcela = total / qtd;

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setSalvando(true);
    try {
      await api.post(`/inadimplencia/${encodeURIComponent(props.inadimplente.paciente)}/acordo`, {
        quantidadeNovas: qtd,
        valorNova: Number(valorParcela.toFixed(2)),
      });
      props.onSalvo("Acordo criado.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao criar acordo.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo={`Acordo — ${props.inadimplente.paciente}`} onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <p className="text-sm text-gray-600">
          Dívida atualizada: <b>{brl(total)}</b>. As parcelas vencidas serão substituídas pelo acordo.
        </p>
        <Campo label="Número de parcelas">
          <input type="number" min="1" value={quantidade} onChange={(e) => setQuantidade(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500" />
        </Campo>
        <div className="text-sm text-gray-700 bg-gray-50 border border-gray-200 rounded p-3">
          {qtd}x de <b>{brl(valorParcela)}</b>
        </div>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Criar acordo" />
      </form>
    </CaixaModal>
  );
}

function ModalCobranca(props: ModalProps & { inadimplente: InadimplenteBackend }) {
  const [canal, setCanal] = useState("Telefone");
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setSalvando(true);
    try {
      await api.post(`/inadimplencia/${encodeURIComponent(props.inadimplente.paciente)}/cobranca`, { canal });
      props.onSalvo("Cobrança registrada.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao registrar cobrança.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo={`Cobrança — ${props.inadimplente.paciente}`} onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <Campo label="Canal">
          <select value={canal} onChange={(e) => setCanal(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 bg-white outline-none focus:border-blue-500">
            {CANAIS.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Registrar cobrança" />
      </form>
    </CaixaModal>
  );
}

function ModalCancelar(props: ModalProps & { inadimplente: InadimplenteBackend }) {
  const [justificativa, setJustificativa] = useState("");
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    if (!justificativa.trim()) return props.onErro("Informe a justificativa.");
    setSalvando(true);
    try {
      await api.post(`/inadimplencia/${encodeURIComponent(props.inadimplente.paciente)}/acordo/cancelar`, {
        justificativa: justificativa.trim(),
      });
      props.onSalvo("Acordo cancelado.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao cancelar acordo.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo={`Cancelar acordo — ${props.inadimplente.paciente}`} onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <Campo label="Justificativa">
          <textarea value={justificativa} onChange={(e) => setJustificativa(e.target.value)} rows={3}
            placeholder="Motivo do cancelamento"
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500" />
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Cancelar acordo" perigo />
      </form>
    </CaixaModal>
  );
}
