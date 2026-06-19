import { useState } from "react";
import { useNavigate } from "react-router";
import { api } from "../api/client";

// Resposta do backend (ClinicaResponse) — F18.
type ClinicaResponse = {
  id: number;
  nome: string;
  cnpj: string;
  email: string;
  status: string;
};

type Aba = "entrar" | "cadastrar";

/** Guarda a clínica autenticada na sessão (consumida pelo resto do app, se desejado). */
function salvarSessao(clinica: ClinicaResponse) {
  localStorage.setItem("odontohub.clinica", JSON.stringify(clinica));
}

export default function Login() {
  const navigate = useNavigate();
  const [aba, setAba] = useState<Aba>("entrar");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  // Campos de login
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");

  // Campos extras de cadastro
  const [nome, setNome] = useState("");
  const [cnpj, setCnpj] = useState("");

  async function entrar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const clinica = await api.post<ClinicaResponse>("/clinicas/login", { email, senha });
      salvarSessao(clinica);
      navigate("/portal");
    } catch (err) {
      setErro(extrairMensagem(err, "E-mail ou senha inválidos."));
    } finally {
      setCarregando(false);
    }
  }

  async function cadastrar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const clinica = await api.post<ClinicaResponse>("/clinicas", { nome, cnpj, email, senha });
      salvarSessao(clinica);
      navigate("/portal");
    } catch (err) {
      setErro(extrairMensagem(err, "Não foi possível concluir o cadastro."));
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="size-full min-h-screen bg-gray-100 flex items-center justify-center p-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <div className="h-20 border-4 border-gray-400 mb-4 flex items-center justify-center bg-white">
            <span className="text-2xl font-bold text-gray-700">OdontoHub</span>
          </div>
          <p className="text-gray-600">Acesso da Clínica</p>
        </div>

        <div className="border-4 border-gray-400 bg-white">
          {/* Abas */}
          <div className="grid grid-cols-2">
            <button
              type="button"
              onClick={() => { setAba("entrar"); setErro(null); }}
              className={`py-3 font-bold ${aba === "entrar"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}
            >
              Entrar
            </button>
            <button
              type="button"
              onClick={() => { setAba("cadastrar"); setErro(null); }}
              className={`py-3 font-bold ${aba === "cadastrar"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}
            >
              Criar conta
            </button>
          </div>

          <div className="p-8">
            {erro && (
              <div className="mb-4 border-2 border-red-400 bg-red-50 text-red-700 text-sm p-3">
                {erro}
              </div>
            )}

            {aba === "entrar" ? (
              <form onSubmit={entrar} className="space-y-4">
                <Campo label="E-mail" type="email" value={email} onChange={setEmail}
                  placeholder="admin@odontohub.com" />
                <Campo label="Senha" type="password" value={senha} onChange={setSenha}
                  placeholder="••••••••" />
                <Botao carregando={carregando}>Entrar</Botao>
                <p className="text-xs text-gray-500 text-center">
                  Conta de demonstração: <strong>admin@odontohub.com</strong> / <strong>odonto123</strong>
                </p>
              </form>
            ) : (
              <form onSubmit={cadastrar} className="space-y-4">
                <Campo label="Nome da clínica" value={nome} onChange={setNome}
                  placeholder="Clínica Sorriso Feliz" />
                <Campo label="CNPJ" value={cnpj} onChange={setCnpj}
                  placeholder="00.000.000/0001-00" />
                <Campo label="E-mail" type="email" value={email} onChange={setEmail}
                  placeholder="contato@clinica.com" />
                <Campo label="Senha" type="password" value={senha} onChange={setSenha}
                  placeholder="mínimo 6 caracteres" />
                <Botao carregando={carregando}>Criar conta</Botao>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function extrairMensagem(err: unknown, padrao: string): string {
  // O client lança Error com o corpo da resposta (que é {"erro":"..."}); tenta extrair.
  const texto = err instanceof Error ? err.message : String(err);
  const match = texto.match(/\{"erro":"(.*?)"\}/);
  return match ? match[1] : padrao;
}

function Campo(props: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  type?: string;
  placeholder?: string;
}) {
  return (
    <label className="block">
      <span className="block text-sm font-bold text-gray-700 mb-1">{props.label}</span>
      <input
        type={props.type ?? "text"}
        value={props.value}
        onChange={(e) => props.onChange(e.target.value)}
        placeholder={props.placeholder}
        required
        className="w-full border-2 border-gray-300 px-3 py-2 focus:border-blue-500 outline-none"
      />
    </label>
  );
}

function Botao(props: { carregando: boolean; children: React.ReactNode }) {
  return (
    <button
      type="submit"
      disabled={props.carregando}
      className="w-full bg-blue-500 text-white font-bold py-3 hover:bg-blue-600 disabled:opacity-50"
    >
      {props.carregando ? "Aguarde..." : props.children}
    </button>
  );
}
