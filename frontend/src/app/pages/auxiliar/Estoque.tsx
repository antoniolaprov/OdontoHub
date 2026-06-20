import { useEffect, useState } from "react";
import { api } from "../../api/client";

// Formato real vindo do backend (MaterialConsumivel) — F05.
interface MaterialBackend {
  id?: { id: number };
  nome: string;
  unidadeMedida: string;
  quantidadeEmEstoque: number;
  pontoMinimo: number;
  ultimaAtualizacao?: string;
}

interface MaterialUI {
  nome: string;
  unidade: string;
  quantidade: number;
  pontoMinimo: number;
  baixo: boolean;
}

function adaptarMaterial(b: MaterialBackend): MaterialUI {
  return {
    nome: b.nome,
    unidade: b.unidadeMedida ?? "—",
    quantidade: b.quantidadeEmEstoque ?? 0,
    pontoMinimo: b.pontoMinimo ?? 0,
    // "estoque baixo" = saldo igual ou abaixo do ponto mínimo (mesma regra do domínio).
    baixo: (b.quantidadeEmEstoque ?? 0) <= (b.pontoMinimo ?? 0),
  };
}

export default function AuxiliarEstoque() {
  const [materiais, setMateriais] = useState<MaterialUI[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [modalAberto, setModalAberto] = useState(false);
  const [modalNovoAberto, setModalNovoAberto] = useState(false);

  function carregar() {
    api
      .get<MaterialBackend[]>("/materiais")
      .then((lista) => setMateriais((lista ?? []).map(adaptarMaterial)))
      .catch((e) => {
        console.warn("Falha ao carregar materiais:", e);
        setErro("Não foi possível carregar o estoque do servidor.");
      });
  }

  useEffect(carregar, []);

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-xl font-bold text-gray-700">Controle de Estoque</h1>
        <div className="flex gap-2">
          <button
            onClick={() => setModalNovoAberto(true)}
            className="px-4 py-2 border-2 border-gray-400 bg-white text-gray-700 font-bold hover:bg-gray-50"
          >
            + Novo Material
          </button>
          <button
            onClick={() => setModalAberto(true)}
            className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold"
          >
            + Registrar Reposição
          </button>
        </div>
      </div>

      {erro && (
        <div className="mb-4 border-2 border-red-400 bg-red-50 text-red-700 text-sm p-3">{erro}</div>
      )}

      <div className="border-2 border-gray-400 bg-white">
        <div className="grid grid-cols-4 bg-gray-100 border-b-2 border-gray-400">
          <div className="p-3 border-r-2 border-gray-400 font-bold">Material</div>
          <div className="p-3 border-r-2 border-gray-400 font-bold">Quantidade</div>
          <div className="p-3 border-r-2 border-gray-400 font-bold">Estoque Mínimo</div>
          <div className="p-3 font-bold">Status</div>
        </div>

        {materiais.length === 0 && (
          <div className="p-6 text-center text-gray-400">Nenhum material cadastrado.</div>
        )}

        {materiais.map((material, i) => (
          <div
            key={i}
            className={`grid grid-cols-4 border-b-2 border-gray-400 ${material.baixo ? "bg-yellow-50" : ""}`}
          >
            <div className="p-3 border-r-2 border-gray-400 flex items-center gap-2">
              {material.baixo && <span className="text-yellow-600">⚠️</span>}
              {material.nome}
            </div>
            <div className="p-3 border-r-2 border-gray-400">
              {material.quantidade} <span className="text-gray-400 text-sm">{material.unidade}</span>
            </div>
            <div className="p-3 border-r-2 border-gray-400">{material.pontoMinimo}</div>
            <div className="p-3">
              {material.baixo ? (
                <span className="bg-yellow-200 border border-yellow-600 px-2 py-1 text-sm font-bold">
                  ESTOQUE BAIXO
                </span>
              ) : (
                <span className="text-green-600 font-bold">OK</span>
              )}
            </div>
          </div>
        ))}
      </div>

      {modalAberto && (
        <ModalReposicao
          materiais={materiais}
          onFechar={() => setModalAberto(false)}
          onSalvo={() => {
            setModalAberto(false);
            carregar();
          }}
        />
      )}

      {modalNovoAberto && (
        <ModalNovoMaterial
          onFechar={() => setModalNovoAberto(false)}
          onSalvo={() => {
            setModalNovoAberto(false);
            carregar();
          }}
        />
      )}
    </div>
  );
}

/** Cadastro de material novo (F05) — único jeito de um material aparecer na Reposição. */
function ModalNovoMaterial(props: { onFechar: () => void; onSalvo: () => void }) {
  const [nome, setNome] = useState("");
  const [unidadeMedida, setUnidadeMedida] = useState("");
  const [saldoInicial, setSaldoInicial] = useState("");
  const [pontoMinimo, setPontoMinimo] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    if (!nome.trim() || !unidadeMedida.trim()) {
      setErro("Informe nome e unidade de medida.");
      return;
    }
    const saldo = Number(saldoInicial);
    const minimo = Number(pontoMinimo);
    if (saldo < 0 || minimo < 0) {
      setErro("Saldo inicial e ponto mínimo não podem ser negativos.");
      return;
    }
    setSalvando(true);
    try {
      // POST real: /api/materiais (MaterialRequest).
      await api.post("/materiais", {
        nome: nome.trim(), unidadeMedida: unidadeMedida.trim(),
        saldoInicial: saldo || 0, pontoMinimo: minimo || 0,
      });
      props.onSalvo();
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Falha ao cadastrar material.");
      setSalvando(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white border-4 border-gray-400 w-full max-w-md">
        <div className="px-6 py-4 border-b-2 border-gray-400 flex justify-between items-center bg-gray-100">
          <h2 className="font-bold text-gray-700">Novo Material</h2>
          <button onClick={props.onFechar} className="text-gray-500 hover:text-gray-700 font-bold">✕</button>
        </div>
        <form onSubmit={salvar} className="p-6 space-y-4">
          {erro && (
            <div className="border-2 border-red-400 bg-red-50 text-red-700 text-sm p-3">{erro}</div>
          )}
          <label className="block">
            <span className="block text-sm font-bold text-gray-700 mb-1">Nome *</span>
            <input
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="ex.: Luva cirúrgica"
              className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
            />
          </label>
          <label className="block">
            <span className="block text-sm font-bold text-gray-700 mb-1">Unidade de medida *</span>
            <input
              value={unidadeMedida}
              onChange={(e) => setUnidadeMedida(e.target.value)}
              placeholder="ex.: caixa, unidade, pacote"
              className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
            />
          </label>
          <div className="grid grid-cols-2 gap-4">
            <label className="block">
              <span className="block text-sm font-bold text-gray-700 mb-1">Saldo inicial</span>
              <input
                type="number" min="0" value={saldoInicial}
                onChange={(e) => setSaldoInicial(e.target.value)}
                className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
              />
            </label>
            <label className="block">
              <span className="block text-sm font-bold text-gray-700 mb-1">Estoque mínimo</span>
              <input
                type="number" min="0" value={pontoMinimo}
                onChange={(e) => setPontoMinimo(e.target.value)}
                className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
              />
            </label>
          </div>
          <p className="text-xs text-gray-400">
            O status (OK / Estoque Baixo) é calculado automaticamente comparando o
            saldo com o estoque mínimo — não é um campo separado.
          </p>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={props.onFechar} className="px-4 py-2 border-2 border-gray-300 font-bold text-gray-600">
              Cancelar
            </button>
            <button type="submit" disabled={salvando} className="px-4 py-2 bg-blue-500 text-white font-bold disabled:opacity-50">
              {salvando ? "Salvando..." : "Cadastrar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function ModalReposicao(props: {
  materiais: MaterialUI[];
  onFechar: () => void;
  onSalvo: () => void;
}) {
  const [nome, setNome] = useState(props.materiais[0]?.nome ?? "");
  const [fornecedor, setFornecedor] = useState("");
  const [quantidade, setQuantidade] = useState("");
  const [custoUnitario, setCustoUnitario] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    const qtd = Number(quantidade);
    const custo = Number(custoUnitario);
    if (!nome || qtd <= 0 || custo <= 0) {
      setErro("Informe material, quantidade e custo unitário positivos.");
      return;
    }
    setSalvando(true);
    try {
      // POST real: /api/materiais/{nome}/reposicao (ReposicaoRequest).
      await api.post(`/materiais/${encodeURIComponent(nome)}/reposicao`, {
        fornecedor,
        quantidade: qtd,
        custoUnitario: custo,
      });
      props.onSalvo();
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Falha ao registrar reposição.");
      setSalvando(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white border-4 border-gray-400 w-full max-w-md">
        <div className="px-6 py-4 border-b-2 border-gray-400 flex justify-between items-center bg-gray-100">
          <h2 className="font-bold text-gray-700">Registrar Reposição</h2>
          <button onClick={props.onFechar} className="text-gray-500 hover:text-gray-700 font-bold">✕</button>
        </div>
        <form onSubmit={salvar} className="p-6 space-y-4">
          {erro && (
            <div className="border-2 border-red-400 bg-red-50 text-red-700 text-sm p-3">{erro}</div>
          )}
          <label className="block">
            <span className="block text-sm font-bold text-gray-700 mb-1">Material</span>
            <select
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none bg-white"
            >
              {props.materiais.map((m) => (
                <option key={m.nome} value={m.nome}>{m.nome}</option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="block text-sm font-bold text-gray-700 mb-1">Fornecedor</span>
            <input
              value={fornecedor}
              onChange={(e) => setFornecedor(e.target.value)}
              placeholder="ex.: Dental Cremer"
              className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
            />
          </label>
          <div className="grid grid-cols-2 gap-4">
            <label className="block">
              <span className="block text-sm font-bold text-gray-700 mb-1">Quantidade</span>
              <input
                type="number" min="1" value={quantidade}
                onChange={(e) => setQuantidade(e.target.value)}
                className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
              />
            </label>
            <label className="block">
              <span className="block text-sm font-bold text-gray-700 mb-1">Custo unitário (R$)</span>
              <input
                type="number" min="0" step="0.01" value={custoUnitario}
                onChange={(e) => setCustoUnitario(e.target.value)}
                className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
              />
            </label>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={props.onFechar} className="px-4 py-2 border-2 border-gray-300 font-bold text-gray-600">
              Cancelar
            </button>
            <button type="submit" disabled={salvando} className="px-4 py-2 bg-blue-500 text-white font-bold disabled:opacity-50">
              {salvando ? "Salvando..." : "Registrar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
