import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import { api } from "../../api/client";

// ─── Formatação ───────────────────────────────────────────────────────────────
const brl = (v: number) =>
  (v ?? 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const dataBR = (iso?: string | null) => {
  if (!iso) return "—";
  const [ano, mes, dia] = iso.split("T")[0].split("-");
  return `${dia}/${mes}/${ano}`;
};

// ─── Tipos do backend ─────────────────────────────────────────────────────────
type StatusParcelaBackend = "EM_ABERTO" | "PAGA" | "PARCIALMENTE_PAGA";
type StatusPagamentoBackend = "PAGO" | "AGUARDANDO_COMPROVANTE" | "CANCELADO";

interface ParcelaBackend {
  referencia: string;
  valorDevido: number;
  valorPago: number;
  saldoRemanescente: number;
  status: StatusParcelaBackend;
}
interface PagamentoBackend {
  id: number;
  referencia: string;
  valorRecebido: number;
  dataPagamento: string | null;
  formaPagamento: string;
  status: StatusPagamentoBackend;
  observacao: string | null;
  justificativaCancelamento: string | null;
}

// ─── Rótulos ──────────────────────────────────────────────────────────────────
const LABEL_PARCELA: Record<StatusParcelaBackend, string> = {
  EM_ABERTO: "Em Aberto",
  PAGA: "Paga",
  PARCIALMENTE_PAGA: "Parcialmente Paga",
};
const COR_PARCELA: Record<StatusParcelaBackend, string> = {
  EM_ABERTO: "bg-yellow-100 text-yellow-700 border-yellow-500",
  PAGA: "bg-green-100 text-green-700 border-green-500",
  PARCIALMENTE_PAGA: "bg-blue-100 text-blue-700 border-blue-500",
};
const LABEL_PAGAMENTO: Record<StatusPagamentoBackend, string> = {
  PAGO: "Pago",
  AGUARDANDO_COMPROVANTE: "Aguardando Comprovante",
  CANCELADO: "Cancelado",
};
const COR_PAGAMENTO: Record<StatusPagamentoBackend, string> = {
  PAGO: "bg-green-100 text-green-700 border-green-500",
  AGUARDANDO_COMPROVANTE: "bg-yellow-100 text-yellow-700 border-yellow-500",
  CANCELADO: "bg-gray-100 text-gray-600 border-gray-400",
};
const FORMAS = ["PIX", "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Transferência"];
const labelForma = (f: string) =>
  ({
    PIX: "PIX",
    DINHEIRO: "Dinheiro",
    CARTAO_CREDITO: "Cartão de Crédito",
    CARTAO_DEBITO: "Cartão de Débito",
    TRANSFERENCIA: "Transferência",
  }[f] ?? f);

// ─── Componente ───────────────────────────────────────────────────────────────
export default function RecepcionistaPagamentos() {
  const [parcelas, setParcelas] = useState<ParcelaBackend[]>([]);
  const [historico, setHistorico] = useState<PagamentoBackend[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [toast, setToast] = useState<{ msg: string; tipo: "sucesso" | "erro" } | null>(null);

  const [modalNovaParcela, setModalNovaParcela] = useState(false);
  const [modalReceber, setModalReceber] = useState<ParcelaBackend | null>(null);
  const [modalConfirmar, setModalConfirmar] = useState<PagamentoBackend | null>(null);
  const [modalCancelar, setModalCancelar] = useState<PagamentoBackend | null>(null);

  function carregar() {
    setCarregando(true);
    return Promise.all([
      api.get<ParcelaBackend[]>("/pagamentos/parcelas"),
      api.get<PagamentoBackend[]>("/pagamentos"),
    ])
      .then(([ps, hs]) => {
        setParcelas(Array.isArray(ps) ? ps : []);
        setHistorico(Array.isArray(hs) ? hs : []);
      })
      .catch((e) => console.warn("Falha ao carregar pagamentos:", e))
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
    const aReceber = parcelas.reduce((s, p) => s + p.saldoRemanescente, 0);
    const recebido = parcelas.reduce((s, p) => s + p.valorPago, 0);
    const aguardando = historico.filter((h) => h.status === "AGUARDANDO_COMPROVANTE").length;
    return { aReceber, recebido, aguardando };
  }, [parcelas, historico]);

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
          <h1 className="text-2xl font-bold text-gray-700">Pagamentos</h1>
          <p className="text-sm text-gray-500 mt-1">
            Registre recebimentos, confirme comprovantes e acompanhe o histórico.
          </p>
        </div>
        <button
          onClick={() => setModalNovaParcela(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white font-bold rounded hover:bg-blue-600"
        >
          <Plus size={18} /> Nova Parcela
        </button>
      </div>

      {/* KPIs */}
      <div className="grid grid-cols-3 gap-6">
        {[
          { label: "A Receber", value: brl(kpis.aReceber), color: "text-yellow-600" },
          { label: "Total Recebido", value: brl(kpis.recebido), color: "text-green-600" },
          { label: "Aguardando Comprovante", value: String(kpis.aguardando), color: "text-blue-600" },
        ].map((k) => (
          <div key={k.label} className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm">
            <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-2">{k.label}</h3>
            <p className={`text-3xl font-bold ${k.color}`}>{k.value}</p>
          </div>
        ))}
      </div>

      {/* Parcelas */}
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <h2 className="text-lg font-bold text-gray-700">Parcelas</h2>
        </div>
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Referência</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-right">Valor</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-right">Pago</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-right">Saldo</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-center">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {parcelas.length === 0 && (
              <tr>
                <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                  {carregando ? "Carregando..." : "Nenhuma parcela cadastrada."}
                </td>
              </tr>
            )}
            {parcelas.map((p) => (
              <tr key={p.referencia} className="hover:bg-gray-50">
                <td className="px-6 py-4 font-medium text-gray-900">{p.referencia}</td>
                <td className="px-6 py-4 text-right text-gray-700">{brl(p.valorDevido)}</td>
                <td className="px-6 py-4 text-right text-green-600">{brl(p.valorPago)}</td>
                <td className="px-6 py-4 text-right font-bold text-gray-800">{brl(p.saldoRemanescente)}</td>
                <td className="px-6 py-4">
                  <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${COR_PARCELA[p.status]}`}>
                    {LABEL_PARCELA[p.status]}
                  </span>
                </td>
                <td className="px-6 py-4 text-center">
                  {p.status === "PAGA" ? (
                    <span className="text-gray-400 text-xs">Quitada</span>
                  ) : (
                    <button
                      onClick={() => setModalReceber(p)}
                      className="px-3 py-1 border border-blue-500 text-blue-600 text-xs font-bold rounded hover:bg-blue-50"
                    >
                      Registrar pagamento
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Histórico */}
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <h2 className="text-lg font-bold text-gray-700">Histórico de Pagamentos</h2>
        </div>
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Data</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Referência</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-right">Valor</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Forma</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-xs font-bold text-gray-500 uppercase text-center">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {historico.length === 0 && (
              <tr>
                <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                  {carregando ? "Carregando..." : "Nenhum pagamento registrado."}
                </td>
              </tr>
            )}
            {historico.map((h) => (
              <tr key={h.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-gray-700">{dataBR(h.dataPagamento)}</td>
                <td className="px-6 py-4 font-medium text-gray-900">{h.referencia}</td>
                <td className="px-6 py-4 text-right text-gray-700">{brl(h.valorRecebido)}</td>
                <td className="px-6 py-4 text-gray-600">{labelForma(h.formaPagamento)}</td>
                <td className="px-6 py-4">
                  <span className={`inline-block px-2 py-0.5 border text-xs font-bold rounded ${COR_PAGAMENTO[h.status]}`}>
                    {LABEL_PAGAMENTO[h.status]}
                  </span>
                </td>
                <td className="px-6 py-4 text-center">
                  {h.status === "AGUARDANDO_COMPROVANTE" ? (
                    <div className="flex gap-2 justify-center">
                      <button
                        onClick={() => setModalConfirmar(h)}
                        className="px-3 py-1 border border-green-500 text-green-600 text-xs font-bold rounded hover:bg-green-50"
                      >
                        Confirmar
                      </button>
                      <button
                        onClick={() => setModalCancelar(h)}
                        className="px-3 py-1 border border-red-400 text-red-600 text-xs font-bold rounded hover:bg-red-50"
                      >
                        Cancelar
                      </button>
                    </div>
                  ) : (
                    <span className="text-gray-300 text-xs">—</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modalNovaParcela && (
        <ModalNovaParcela
          onFechar={() => setModalNovaParcela(false)}
          onSalvo={(msg) => {
            setModalNovaParcela(false);
            showToast(msg, "sucesso");
            carregar();
          }}
          onErro={(msg) => showToast(msg, "erro")}
        />
      )}
      {modalReceber && (
        <ModalReceber
          parcela={modalReceber}
          onFechar={() => setModalReceber(null)}
          onSalvo={(msg) => {
            setModalReceber(null);
            showToast(msg, "sucesso");
            carregar();
          }}
          onErro={(msg) => showToast(msg, "erro")}
        />
      )}
      {modalConfirmar && (
        <ModalConfirmar
          pagamento={modalConfirmar}
          saldo={parcelas.find((p) => p.referencia === modalConfirmar.referencia)?.saldoRemanescente ?? 0}
          onFechar={() => setModalConfirmar(null)}
          onSalvo={(msg) => {
            setModalConfirmar(null);
            showToast(msg, "sucesso");
            carregar();
          }}
          onErro={(msg) => showToast(msg, "erro")}
        />
      )}
      {modalCancelar && (
        <ModalCancelar
          pagamento={modalCancelar}
          onFechar={() => setModalCancelar(null)}
          onSalvo={(msg) => {
            setModalCancelar(null);
            showToast(msg, "sucesso");
            carregar();
          }}
          onErro={(msg) => showToast(msg, "erro")}
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

function ModalNovaParcela(props: ModalProps) {
  const [referencia, setReferencia] = useState("");
  const [valor, setValor] = useState("");
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    const v = Number(valor);
    if (!referencia.trim() || v <= 0) return props.onErro("Informe a referência e um valor positivo.");
    setSalvando(true);
    try {
      await api.post("/pagamentos/parcelas", { referencia: referencia.trim(), valor: v });
      props.onSalvo("Parcela criada.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao criar parcela.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo="Nova Parcela" onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <Campo label="Referência (paciente / tratamento)">
          <input
            value={referencia}
            onChange={(e) => setReferencia(e.target.value)}
            placeholder="ex.: Maria Santos"
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500"
          />
        </Campo>
        <Campo label="Valor (R$)">
          <input
            type="number" min="0" step="0.01" value={valor}
            onChange={(e) => setValor(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500"
          />
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Criar" />
      </form>
    </CaixaModal>
  );
}

function ModalReceber(props: ModalProps & { parcela: ParcelaBackend }) {
  const [modo, setModo] = useState<"confirmar" | "aguardar">("confirmar");
  const [valor, setValor] = useState(String(props.parcela.saldoRemanescente));
  const [forma, setForma] = useState("PIX");
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setSalvando(true);
    try {
      if (modo === "confirmar") {
        const v = Number(valor);
        if (v <= 0) {
          setSalvando(false);
          return props.onErro("Informe um valor positivo.");
        }
        await api.post("/pagamentos/presencial", { referencia: props.parcela.referencia, valor: v, forma });
        props.onSalvo("Pagamento registrado.");
      } else {
        await api.post("/pagamentos/aguardando", { referencia: props.parcela.referencia, valor: 0, forma });
        props.onSalvo("Lançamento aguardando comprovante criado.");
      }
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao registrar pagamento.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo={`Pagamento — ${props.parcela.referencia}`} onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <div className="flex gap-2">
          {([["confirmar", "Confirmar agora"], ["aguardar", "Aguardar comprovante"]] as const).map(([m, lbl]) => (
            <button
              key={m} type="button" onClick={() => setModo(m)}
              className={`flex-1 py-2 font-bold rounded border text-sm ${
                modo === m ? "bg-blue-500 text-white border-blue-500" : "bg-white text-gray-600 border-gray-300"
              }`}
            >
              {lbl}
            </button>
          ))}
        </div>
        {modo === "confirmar" && (
          <Campo label="Valor recebido (R$)">
            <input
              type="number" min="0" step="0.01" value={valor}
              onChange={(e) => setValor(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500"
            />
          </Campo>
        )}
        <Campo label="Forma de pagamento">
          <select
            value={forma} onChange={(e) => setForma(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 bg-white outline-none focus:border-blue-500"
          >
            {FORMAS.map((f) => <option key={f} value={f}>{f}</option>)}
          </select>
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo={modo === "confirmar" ? "Receber" : "Lançar"} />
      </form>
    </CaixaModal>
  );
}

function ModalConfirmar(props: ModalProps & { pagamento: PagamentoBackend; saldo: number }) {
  const [valor, setValor] = useState(String(props.saldo || ""));
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    const v = Number(valor);
    if (v <= 0) return props.onErro("Informe um valor positivo.");
    setSalvando(true);
    try {
      await api.post("/pagamentos/confirmar", { referencia: props.pagamento.referencia, valor: v });
      props.onSalvo("Comprovante confirmado.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao confirmar.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo={`Confirmar — ${props.pagamento.referencia}`} onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <Campo label="Valor recebido (R$)">
          <input
            type="number" min="0" step="0.01" value={valor}
            onChange={(e) => setValor(e.target.value)}
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500"
          />
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Confirmar" />
      </form>
    </CaixaModal>
  );
}

function ModalCancelar(props: ModalProps & { pagamento: PagamentoBackend }) {
  const [justificativa, setJustificativa] = useState("");
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    if (!justificativa.trim()) return props.onErro("Informe a justificativa.");
    setSalvando(true);
    try {
      await api.post("/pagamentos/cancelar", {
        referencia: props.pagamento.referencia,
        justificativa: justificativa.trim(),
      });
      props.onSalvo("Lançamento cancelado.");
    } catch (err) {
      props.onErro(err instanceof Error ? err.message : "Falha ao cancelar.");
      setSalvando(false);
    }
  }

  return (
    <CaixaModal titulo={`Cancelar — ${props.pagamento.referencia}`} onFechar={props.onFechar}>
      <form onSubmit={salvar} className="p-6 space-y-4">
        <Campo label="Justificativa">
          <textarea
            value={justificativa}
            onChange={(e) => setJustificativa(e.target.value)}
            rows={3}
            placeholder="Motivo do cancelamento"
            className="w-full border border-gray-300 rounded px-3 py-2 outline-none focus:border-blue-500"
          />
        </Campo>
        <Acoes onFechar={props.onFechar} salvando={salvando} rotulo="Cancelar lançamento" perigo />
      </form>
    </CaixaModal>
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
