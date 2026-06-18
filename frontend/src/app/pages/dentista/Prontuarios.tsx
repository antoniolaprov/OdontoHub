import { useState, useMemo, useRef, useEffect } from "react";
import { FileText, Lock, ShieldAlert, FileClock, Pill, AlertTriangle, RotateCcw, Search, X } from "lucide-react";

// ─── Catálogo de medicamentos (espelha a F17) ────────────────────────────────
interface MedicamentoCatalogo {
  id: number;
  nomeComercial: string;
  principioAtivo: string;
  categoria: string;
  classeFarmacologica: string;
  contraindicacoes: string;
  interacoes: string;
  posologiasPadrao: string[];
  apresentacoes: string[];
}

const CATALOGO_MEDICAMENTOS: MedicamentoCatalogo[] = [
  { id: 1, nomeComercial: "Amoxil", principioAtivo: "Amoxicilina", categoria: "Antibiótico", classeFarmacologica: "Beta-lactâmicos", contraindicacoes: "Alergia a penicilinas ou cefalosporinas, mononucleose infecciosa", interacoes: "Anticoagulantes orais (warfarina), metotrexato", posologiasPadrao: ["1 cáps. a cada 8h por 7 dias", "1 cáps. a cada 12h por 5 dias"], apresentacoes: ["Cáps. 500mg", "Susp. 250mg/5ml"] },
  { id: 2, nomeComercial: "Ibupril", principioAtivo: "Ibuprofeno", categoria: "Anti-inflamatório", classeFarmacologica: "AINEs", contraindicacoes: "Úlcera péptica ativa, insuficiência renal grave, gestação (3º trimestre)", interacoes: "Anticoagulantes, diuréticos, anti-hipertensivos", posologiasPadrao: ["1 comp. de 600mg a cada 8h por 3 dias"], apresentacoes: ["Comp. 600mg", "Comp. 400mg"] },
  { id: 3, nomeComercial: "Nimesil", principioAtivo: "Nimesulida", categoria: "Anti-inflamatório", classeFarmacologica: "AINEs", contraindicacoes: "Hepatopatia, insuficiência renal, gestação", interacoes: "Anticoagulantes, lítio, metotrexato", posologiasPadrao: ["1 comp. a cada 12h por 2 dias"], apresentacoes: ["Comp. 100mg", "Granulado 100mg/sachê"] },
  { id: 4, nomeComercial: "Dipirax", principioAtivo: "Dipirona", categoria: "Analgésico", classeFarmacologica: "Pirazolonas", contraindicacoes: "Hipersensibilidade à dipirona, porfiria aguda", interacoes: "Ciclosporina, cloranfenicol", posologiasPadrao: ["1 comp. a cada 6h se dor"], apresentacoes: ["Comp. 500mg", "Gotas 500mg/ml"] },
  { id: 5, nomeComercial: "Paracetol", principioAtivo: "Paracetamol", categoria: "Analgésico", classeFarmacologica: "Paraaminofenóis", contraindicacoes: "Hepatopatia grave, alcoolismo crônico", interacoes: "Warfarina (uso prolongado), isoniazida", posologiasPadrao: ["1 comp. de 750mg a cada 6h se dor"], apresentacoes: ["Comp. 750mg", "Susp. 200mg/ml"] },
  { id: 6, nomeComercial: "Clindoxyl", principioAtivo: "Clindamicina", categoria: "Antibiótico", classeFarmacologica: "Lincosamidas", contraindicacoes: "Colite ulcerativa, hipersensibilidade", interacoes: "Bloqueadores neuromusculares", posologiasPadrao: ["1 cáps. a cada 6h por 7 dias"], apresentacoes: ["Cáps. 300mg"] },
];

// ─── Alergias registradas na anamnese do paciente (dado fixo do protótipo) ───
const ALERGIAS_PACIENTE = ["Penicilina", "Dipirona"];

// Mapeamento de medicamentos para classes alérgicas
// Regra de negócio: amoxicilina pertence à classe das penicilinas (beta-lactâmicos)
const CLASSE_ALERGICA: Record<string, string> = {
  amoxicilina: "Penicilina",
  ampicilina: "Penicilina",
  cloxacilina: "Penicilina",
};

// Verifica se o medicamento informado corresponde a uma alergia registrada
// Retorna o nome da alergia identificada, ou null se não houver contraindicação
function verificarContraindicacao(medicamento: string): string | null {
  const medLower = medicamento.toLowerCase().trim();
  if (!medLower) return null;

  // Verificação direta por nome de alérgeno
  for (const alergia of ALERGIAS_PACIENTE) {
    if (medLower.includes(alergia.toLowerCase())) return alergia;
  }
  // Verificação por classe farmacológica mapeada
  for (const [nomeMed, classe] of Object.entries(CLASSE_ALERGICA)) {
    if (medLower.includes(nomeMed) && ALERGIAS_PACIENTE.includes(classe)) {
      return classe;
    }
  }
  return null;
}

// Converte string DD/MM/YYYY para objeto Date
function parseDateBR(ddmmyyyy: string): Date | null {
  const parts = ddmmyyyy.split("/");
  if (parts.length !== 3) return null;
  return new Date(parseInt(parts[2]), parseInt(parts[1]) - 1, parseInt(parts[0]));
}

export default function DentistaProntuarios() {
  const [abaAtiva, setAbaAtiva] = useState("anamnese");
  const [alertaAberto, setAlertaAberto] = useState(true);

  const [anamnesePreenchida, setAnamnesePreenchida] = useState(false);
  const [modalEvolucao, setModalEvolucao] = useState<{aberto: boolean, proc?: any}>({aberto: false});
  const [modalJustificativa, setModalJustificativa] = useState<{aberto: boolean, acao?: string}>({aberto: false});

  // Estados para modais de prescrição
  const [modalNovaPrescricao, setModalNovaPrescricao] = useState(false);
  const [modalAlertaAlergia, setModalAlertaAlergia] = useState(false);
  const [modalAlertaRecente, setModalAlertaRecente] = useState(false);
  const [modalRepetirPrescricao, setModalRepetirPrescricao] = useState<{aberto: boolean, prescricao?: any}>({aberto: false});
  const [modalExcluirPrescricao, setModalExcluirPrescricao] = useState<{aberto: boolean, prescricao?: any}>({aberto: false});

  // ── Seleção de medicamento do catálogo ──────────────────────────────────
  const [buscaMedicamento, setBuscaMedicamento] = useState("");
  const [medicamentoSelecionado, setMedicamentoSelecionado] = useState<MedicamentoCatalogo | null>(null);
  const [dropdownAberto, setDropdownAberto] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const medicamentosFiltrados = useMemo(() => {
    if (!buscaMedicamento.trim()) return CATALOGO_MEDICAMENTOS;
    const q = buscaMedicamento.toLowerCase();
    return CATALOGO_MEDICAMENTOS.filter(
      (m) => m.nomeComercial.toLowerCase().includes(q) || m.principioAtivo.toLowerCase().includes(q)
    );
  }, [buscaMedicamento]);

  useEffect(() => {
    function handleClickFora(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownAberto(false);
      }
    }
    document.addEventListener("mousedown", handleClickFora);
    return () => document.removeEventListener("mousedown", handleClickFora);
  }, []);

  function selecionarMedicamento(med: MedicamentoCatalogo) {
    setMedicamentoSelecionado(med);
    setBuscaMedicamento(med.nomeComercial);
    setDropdownAberto(false);
  }

  function limparMedicamento() {
    setMedicamentoSelecionado(null);
    setBuscaMedicamento("");
    setDropdownAberto(false);
  }

  // Campos do formulário de nova prescrição (controlados para permitir validação no submit)
  const [medicamentoInput, setMedicamentoInput] = useState("");
  const [dosagemInput, setDosagemInput] = useState("");
  const [posologiaInput, setPosologiaInput] = useState("");
  const [periodoInput, setPeriodoInput] = useState("");
  const [agendamentoInput, setAgendamentoInput] = useState("");
  const [observacoesInput, setObservacoesInput] = useState("");

  // Estado da contraindicação detectada (para exibir no modal de alerta)
  const [contraindicacaoDetectada, setContraindicacaoDetectada] = useState<string | null>(null);
  // Estado da prescrição recente detectada (para exibir no modal de alerta)
  const [prescricaoRecenteDetectada, setPrescricaoRecenteDetectada] = useState<any | null>(null);

  // Dados de prescrições do paciente (histórico para validação de recência)
  const prescricoes = [
    {
      id: 1,
      data: "15/04/2026",
      medicamento: "Ibuprofeno",
      dosagem: "600mg",
      posologia: "1 comp. a cada 8h",
      periodo: "3 dias",
      agendamento: "AGE-0412",
      responsavel: "Dr. Felipe",
      editavel: false
    },
    {
      id: 2,
      data: "20/04/2026",
      medicamento: "Amoxicilina",
      dosagem: "500mg",
      posologia: "1 cáps. a cada 8h",
      periodo: "7 dias",
      agendamento: "AGE-0419",
      responsavel: "Dr. Felipe",
      editavel: false
    },
    {
      id: 3,
      data: "08/06/2026",
      medicamento: "Nimesulida",
      dosagem: "100mg",
      posologia: "1 comp. a cada 12h",
      periodo: "2 dias",
      agendamento: "AGE-0607",
      responsavel: "Dr. Felipe",
      editavel: true
    }
  ];

  // Verifica se o medicamento foi prescrito ao mesmo paciente nos últimos 30 dias
  // Retorna o registro da prescrição recente, ou null se não houver
  function verificarPrescricaoRecente(medicamento: string): typeof prescricoes[0] | null {
    const medLower = medicamento.toLowerCase().trim();
    if (!medLower) return null;

    const agora = new Date();
    const trintaDiasAtras = new Date(agora.getTime() - 30 * 24 * 60 * 60 * 1000);

    for (const p of prescricoes) {
      const dataPrescricao = parseDateBR(p.data);
      if (!dataPrescricao) continue;
      // Apenas prescrições dos últimos 30 dias com o mesmo medicamento
      if (
        dataPrescricao >= trintaDiasAtras &&
        (p.medicamento.toLowerCase().includes(medLower) || medLower.includes(p.medicamento.toLowerCase()))
      ) {
        return p;
      }
    }
    return null;
  }

  // Etapa final de salvamento (após todas as validações serem aprovadas)
  function salvarPrescricaoFinal() {
    // Neste protótipo o salvamento é simulado; em produção persistiria no backend
    limparFormulario();
    setModalNovaPrescricao(false);
    setModalAlertaAlergia(false);
    setModalAlertaRecente(false);
  }

  function limparFormulario() {
    setMedicamentoInput("");
    limparMedicamento();
    setDosagemInput("");
    setPosologiaInput("");
    setPeriodoInput("");
    setAgendamentoInput("");
    setObservacoesInput("");
    setContraindicacaoDetectada(null);
    setPrescricaoRecenteDetectada(null);
  }

  // Validação de contraindicação executada somente ao clicar em "Salvar Prescrição"
  // Usa o nome do medicamento selecionado no catálogo (ou o input manual como fallback)
  function handleSalvarPrescricao() {
    const nomeMed = medicamentoSelecionado ? medicamentoSelecionado.principioAtivo : medicamentoInput;
    // Verifica contraindicação pela classe farmacológica do catálogo
    const contraindicacaoPorClasse = medicamentoSelecionado
      ? ALERGIAS_PACIENTE.includes(medicamentoSelecionado.classeFarmacologica.replace("Beta-lactâmicos", "Penicilina"))
        ? medicamentoSelecionado.classeFarmacologica
        : verificarContraindicacao(medicamentoSelecionado.principioAtivo)
      : null;
    const contraindicacao = contraindicacaoPorClasse || verificarContraindicacao(nomeMed);

    if (contraindicacao) {
      // Bloqueio: exibir modal de contraindicação e aguardar confirmação explícita do dentista
      setContraindicacaoDetectada(contraindicacao);
      setModalNovaPrescricao(false);
      setModalAlertaAlergia(true);
      return;
    }

    // CORREÇÃO 2: Verificar prescrição recente somente no momento do salvamento
    const recente = verificarPrescricaoRecente(medicamentoInput);
    if (recente) {
      // Alerta informativo: não bloqueia o salvamento
      setPrescricaoRecenteDetectada(recente);
      setModalNovaPrescricao(false);
      setModalAlertaRecente(true);
      return;
    }

    // Nenhuma pendência: salvar diretamente
    salvarPrescricaoFinal();
  }

  // Após dentista confirmar ciência do risco de contraindicação, registrar no log de auditoria
  // e prosseguir com a verificação de prescrição recente antes de salvar
  function handleConfirmarCienciaRisco() {
    // Registro do evento de confirmação de risco no log de auditoria
    console.log(
      "[AUDITORIA] Dentista confirmou ciência do risco de contraindicação. Medicamento:",
      medicamentoInput,
      "| Alergia identificada:",
      contraindicacaoDetectada,
      "| Data/hora:",
      new Date().toISOString()
    );

    setModalAlertaAlergia(false);

    // Após confirmação do risco, verificar prescrição recente antes de concluir
    const recente = verificarPrescricaoRecente(medicamentoInput);
    if (recente) {
      setPrescricaoRecenteDetectada(recente);
      setModalAlertaRecente(true);
    } else {
      salvarPrescricaoFinal();
    }
  }

  // Fluxo de re-prescrição: reutiliza as mesmas validações de contraindicação e recência
  function handleConfirmarRepeticao() {
    const med = modalRepetirPrescricao.prescricao?.medicamento || "";
    const contraindicacao = verificarContraindicacao(med);

    if (contraindicacao) {
      // Re-prescrição também passa pela validação de alergias atualizadas da anamnese
      setMedicamentoInput(med);
      setContraindicacaoDetectada(contraindicacao);
      setModalRepetirPrescricao({aberto: false});
      setModalAlertaAlergia(true);
      return;
    }

    const recente = verificarPrescricaoRecente(med);
    if (recente) {
      setPrescricaoRecenteDetectada(recente);
      setModalRepetirPrescricao({aberto: false});
      setModalAlertaRecente(true);
      return;
    }

    setModalRepetirPrescricao({aberto: false});
  }

  return (
    <div className="p-6">
      <div className="mb-6 border-4 border-red-500 bg-red-50 p-4">
        <div className="flex items-center gap-3">
          <span className="text-2xl">⚠️</span>
          <div>
            <div className="font-bold text-red-700">ALERTA: ALERGIA</div>
            <div className="text-sm text-red-600">
              Paciente alérgico a: Penicilina, Dipirona
            </div>
          </div>
        </div>
      </div>

      <div className="mb-6 border-2 border-gray-400 bg-gray-50 p-4">
        <div className="grid grid-cols-3 gap-4">
          <div>
            <div className="text-sm text-gray-500">Paciente</div>
            <div className="font-bold">João Silva</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">CPF</div>
            <div className="font-bold">123.456.789-00</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">Data Nascimento</div>
            <div className="font-bold">15/03/1985 (41 anos)</div>
          </div>
        </div>
      </div>

      <div className="border-2 border-gray-400 mb-4">
        <div className="flex border-b-2 border-gray-400">
          <button
            onClick={() => setAbaAtiva("anamnese")}
            className={`flex-1 p-3 border-r-2 border-gray-400 font-bold ${
              abaAtiva === "anamnese" ? "bg-blue-500 text-white" : "bg-gray-100"
            }`}
          >
            Anamnese
          </button>
          <button
            onClick={() => setAbaAtiva("plano")}
            className={`flex-1 p-3 border-r-2 border-gray-400 font-bold ${
              abaAtiva === "plano" ? "bg-blue-500 text-white" : "bg-gray-100"
            }`}
          >
            Plano de Tratamento
          </button>
          <button
            onClick={() => setAbaAtiva("prescricoes")}
            className={`flex-1 p-3 font-bold ${
              abaAtiva === "prescricoes" ? "bg-blue-500 text-white" : "bg-gray-100"
            }`}
          >
            Prescrições
          </button>
        </div>

        <div className="p-6 bg-white">
          {abaAtiva === "anamnese" && (
            <div className="space-y-4">
              <div className="flex justify-between items-center mb-4">
                <h3 className="font-bold text-lg">Dados Clínicos</h3>
                <button
                  onClick={() => setAnamnesePreenchida(true)}
                  className="px-4 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 transition-colors flex items-center gap-2"
                >
                  <FileText className="w-4 h-4" />
                  Editar Anamnese
                </button>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="border-2 border-gray-300 p-3 rounded">
                  <div className="text-sm font-bold mb-1">Pressão Arterial</div>
                  <div className="text-gray-600">{anamnesePreenchida ? "120/80 mmHg" : "-"}</div>
                </div>
                <div className="border-2 border-gray-300 p-3 rounded">
                  <div className="text-sm font-bold mb-1">Diabetes</div>
                  <div className="text-gray-600">{anamnesePreenchida ? "Não" : "-"}</div>
                </div>
                <div className="border-2 border-gray-300 p-3 rounded">
                  <div className="text-sm font-bold mb-1">Medicamentos em Uso</div>
                  <div className="text-gray-600">{anamnesePreenchida ? "Losartana 50mg" : "-"}</div>
                </div>
                <div className="border-2 border-gray-300 p-3 rounded">
                  <div className="text-sm font-bold mb-1">Alergias</div>
                  <div className={anamnesePreenchida ? "text-red-600 font-bold" : "text-gray-600"}>
                    {anamnesePreenchida ? "Penicilina, Dipirona" : "-"}
                  </div>
                </div>
              </div>

              {anamnesePreenchida && (
                <div className="mt-6 pt-4 border-t border-gray-200 flex items-center gap-4">
                  <span className="text-xs text-gray-500">Última atualização: 10/04/2026 por Dr. Felipe</span>
                  <button className="text-xs text-blue-600 hover:text-blue-800 underline flex items-center gap-1">
                    <FileClock className="w-3 h-3" />
                    Ver histórico de alterações
                  </button>
                </div>
              )}
            </div>
          )}

          {abaAtiva === "plano" && (
            <div>
              {!anamnesePreenchida ? (
                <div className="flex flex-col items-center justify-center py-16 px-4 text-center border-2 border-dashed border-gray-300 bg-gray-50 rounded">
                  <Lock className="w-16 h-16 text-gray-400 mb-4" />
                  <h3 className="text-lg font-bold text-gray-700 mb-2">Ação bloqueada</h3>
                  <p className="text-gray-500 mb-6 max-w-md">
                    É obrigatório o preenchimento da Anamnese antes de criar um plano.
                  </p>
                  <button
                    onClick={() => setAbaAtiva("anamnese")}
                    className="px-6 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 transition-colors"
                  >
                    Preencher Anamnese
                  </button>
                </div>
              ) : (
                <div>
                  <div className="flex justify-between items-center mb-4">
                    <h3 className="font-bold text-lg">Plano Atual (Versão 1)</h3>
                    <div className="flex gap-2">
                      <button
                        onClick={() => setModalJustificativa({aberto: true, acao: 'encerrar'})}
                        className="px-4 py-2 border border-gray-400 bg-white hover:bg-gray-50 text-gray-700 font-bold rounded text-sm transition-colors"
                      >
                        Encerrar Plano
                      </button>
                      <button
                        onClick={() => setModalJustificativa({aberto: true, acao: 'excluir'})}
                        className="px-4 py-2 border border-red-500 bg-white hover:bg-red-50 text-red-600 font-bold rounded text-sm transition-colors"
                      >
                        Excluir Plano
                      </button>
                    </div>
                  </div>

                  <div className="border-2 border-gray-400 bg-white overflow-x-auto">
                    <div className="min-w-[800px]">
                      <div className="grid grid-cols-7 bg-gray-100 border-b-2 border-gray-400 text-sm">
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Data</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Procedimento</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Dente</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Executor</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Status</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold text-center">Evolução</div>
                        <div className="p-3 font-bold text-center">Edição</div>
                      </div>

                      <div className="grid grid-cols-7 border-b-2 border-gray-400 items-center text-sm">
                        <div className="p-3 border-r-2 border-gray-400">15/04/2026</div>
                        <div className="p-3 border-r-2 border-gray-400">Restauração</div>
                        <div className="p-3 border-r-2 border-gray-400">16</div>
                        <div className="p-3 border-r-2 border-gray-400">Dra. Ana Costa</div>
                        <div className="p-3 border-r-2 border-gray-400">
                          <span className="bg-green-100 text-green-800 border border-green-500 px-2 py-1 rounded text-xs font-bold">
                            Realizado
                          </span>
                        </div>
                        <div className="p-3 border-r-2 border-gray-400 flex justify-center">
                          <button
                            onClick={() => setModalEvolucao({aberto: true, proc: { data: '15/04/2026', nome: 'Restauração', dente: '16' }})}
                            className="text-blue-600 hover:text-blue-800 transition-colors"
                            title="Ver/Editar Evolução"
                          >
                            <FileText className="w-5 h-5" />
                          </button>
                        </div>
                        <div className="p-3 flex items-center justify-center gap-2">
                          <span className="text-xl">🔒</span>
                          <span className="text-xs text-gray-500">&gt;24h</span>
                        </div>
                      </div>

                      <div className="grid grid-cols-7 border-b-2 border-gray-400 items-center text-sm">
                        <div className="p-3 border-r-2 border-gray-400">20/04/2026</div>
                        <div className="p-3 border-r-2 border-gray-400">Limpeza</div>
                        <div className="p-3 border-r-2 border-gray-400">-</div>
                        <div className="p-3 border-r-2 border-gray-400">Dr. Felipe</div>
                        <div className="p-3 border-r-2 border-gray-400">
                          <span className="bg-green-100 text-green-800 border border-green-500 px-2 py-1 rounded text-xs font-bold">
                            Realizado
                          </span>
                        </div>
                        <div className="p-3 border-r-2 border-gray-400 flex justify-center">
                          <button
                            onClick={() => setModalEvolucao({aberto: true, proc: { data: '20/04/2026', nome: 'Limpeza', dente: '-' }})}
                            className="text-blue-600 hover:text-blue-800 transition-colors"
                            title="Ver/Editar Evolução"
                          >
                            <FileText className="w-5 h-5" />
                          </button>
                        </div>
                        <div className="p-3 flex items-center justify-center gap-2">
                          <span className="text-xl">🔒</span>
                          <span className="text-xs text-gray-500">&gt;24h</span>
                        </div>
                      </div>

                      <div className="grid grid-cols-7 border-b-2 border-gray-400 items-center text-sm bg-gray-50">
                        <div className="p-3 border-r-2 border-gray-400 text-gray-500">22/04/2026</div>
                        <div className="p-3 border-r-2 border-gray-400 text-gray-500">Clareamento</div>
                        <div className="p-3 border-r-2 border-gray-400 text-gray-500">-</div>
                        <div className="p-3 border-r-2 border-gray-400 text-gray-500">Dra. Ana Costa</div>
                        <div className="p-3 border-r-2 border-gray-400">
                          <span className="bg-red-100 text-red-800 border border-red-500 px-2 py-1 rounded text-xs font-bold">
                            Cancelado
                          </span>
                        </div>
                        <div className="p-3 border-r-2 border-gray-400 flex justify-center text-gray-400">
                          -
                        </div>
                        <div className="p-3 flex justify-center">
                          <button
                            className="px-3 py-1 border border-gray-300 text-gray-500 bg-gray-100 text-xs rounded cursor-not-allowed"
                            disabled
                          >
                            Cancelado
                          </button>
                        </div>
                      </div>

                      <div className="grid grid-cols-7 border-gray-400 items-center text-sm">
                        <div className="p-3 border-r-2 border-gray-400">05/05/2026</div>
                        <div className="p-3 border-r-2 border-gray-400">Canal</div>
                        <div className="p-3 border-r-2 border-gray-400">26</div>
                        <div className="p-3 border-r-2 border-gray-400">Dr. Roberto</div>
                        <div className="p-3 border-r-2 border-gray-400">
                          <span className="bg-blue-100 text-blue-800 border border-blue-500 px-2 py-1 rounded text-xs font-bold">
                            Pendente
                          </span>
                        </div>
                        <div className="p-3 border-r-2 border-gray-400 flex justify-center text-gray-400">
                          -
                        </div>
                        <div className="p-3 flex justify-center">
                          <button className="px-3 py-1 border border-blue-500 text-blue-600 hover:bg-blue-50 bg-white text-xs font-bold rounded transition-colors">
                            Editar
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {abaAtiva === "prescricoes" && (
            <div>
              {!anamnesePreenchida ? (
                <div className="flex flex-col items-center justify-center py-16 px-4 text-center border-2 border-dashed border-gray-300 bg-gray-50 rounded">
                  <Lock className="w-16 h-16 text-gray-400 mb-4" />
                  <h3 className="text-lg font-bold text-gray-700 mb-2">Ação bloqueada</h3>
                  <p className="text-gray-500 mb-6 max-w-md">
                    É obrigatório o preenchimento da Anamnese antes de criar prescrições.
                  </p>
                  <button
                    onClick={() => setAbaAtiva("anamnese")}
                    className="px-6 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 transition-colors"
                  >
                    Preencher Anamnese
                  </button>
                </div>
              ) : (
                <div>
                  <div className="flex justify-between items-center mb-4">
                    <h3 className="font-bold text-lg">Prescrições</h3>
                    <button
                      onClick={() => setModalNovaPrescricao(true)}
                      className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold rounded hover:bg-blue-600 transition-colors flex items-center gap-2"
                    >
                      <Pill className="w-4 h-4" />
                      + Nova Prescrição
                    </button>
                  </div>

                  <div className="border-2 border-gray-400 bg-white overflow-x-auto">
                    <div className="min-w-[1000px]">
                      <div className="grid grid-cols-8 bg-gray-100 border-b-2 border-gray-400 text-sm">
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Data</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Medicamento</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Dosagem</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Posologia</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Período</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Agendamento</div>
                        <div className="p-3 border-r-2 border-gray-400 font-bold">Responsável</div>
                        <div className="p-3 font-bold text-center">Ações</div>
                      </div>

                      {prescricoes.map((prescricao) => (
                        <div key={prescricao.id} className="grid grid-cols-8 border-b-2 border-gray-400 items-center text-sm">
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.data}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.medicamento}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.dosagem}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.posologia}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.periodo}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.agendamento}</div>
                          <div className="p-3 border-r-2 border-gray-400">{prescricao.responsavel}</div>
                          <div className="p-3 flex items-center justify-center gap-2">
                            <button
                              onClick={() => setModalRepetirPrescricao({aberto: true, prescricao})}
                              className="px-3 py-1 border-2 border-blue-500 bg-white text-blue-600 text-xs font-bold rounded hover:bg-blue-50 transition-colors flex items-center gap-1"
                              title="Repetir prescrição"
                            >
                              <RotateCcw className="w-3 h-3" />
                              Repetir
                            </button>
                            {prescricao.editavel ? (
                              <>
                                <button
                                  onClick={() => setModalExcluirPrescricao({aberto: true, prescricao})}
                                  className="px-3 py-1 border-2 border-red-500 bg-white text-red-600 text-xs font-bold rounded hover:bg-red-50 transition-colors"
                                >
                                  Excluir
                                </button>
                                <span className="bg-yellow-100 text-yellow-800 border border-yellow-500 px-2 py-1 rounded text-xs font-bold">
                                  Editável
                                </span>
                              </>
                            ) : (
                              <div className="flex items-center gap-2 text-gray-400" title="Registro imutável após 24h">
                                <Lock className="w-4 h-4" />
                                <span className="text-xs">&gt;24h</span>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {alertaAberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-red-500 w-[500px] p-6">
            <div className="flex items-start gap-3 mb-4">
              <span className="text-4xl">🚨</span>
              <div>
                <h2 className="text-lg font-bold text-red-700">
                  Alerta de Complicação Pós-Operatória
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  Paciente Marcos Pereira reportou sintomas pós-cirúrgicos
                </p>
              </div>
            </div>

            <div className="border-2 border-gray-300 bg-gray-50 p-4 mb-4">
              <div className="text-sm space-y-2">
                <div>
                  <span className="font-bold">Procedimento:</span> Extração do dente 38
                </div>
                <div>
                  <span className="font-bold">Data:</span> 26/04/2026
                </div>
                <div>
                  <span className="font-bold">Sintomas reportados:</span>
                  <ul className="list-disc ml-5 mt-1">
                    <li>Dor intensa</li>
                    <li>Sangramento excessivo</li>
                  </ul>
                </div>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setAlertaAberto(false)}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white"
              >
                Dispensar
              </button>
              <button onClick={() => setAlertaAberto(false)} className="flex-1 px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold">
                Abrir Prontuário
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Evolução Clínica */}
      {modalEvolucao.aberto && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white w-full max-w-lg rounded shadow-xl">
            <div className="p-4 border-b border-gray-200 flex justify-between items-center bg-gray-50 rounded-t">
              <h2 className="text-lg font-bold text-gray-800">Confirmar Procedimento</h2>
            </div>
            <div className="p-6">
              <div className="flex gap-4 mb-4 text-sm text-gray-600 bg-gray-100 p-3 rounded">
                <div><span className="font-bold">Data:</span> {modalEvolucao.proc?.data || '-'}</div>
                <div><span className="font-bold">Procedimento:</span> {modalEvolucao.proc?.nome || '-'}</div>
                <div><span className="font-bold">Dente:</span> {modalEvolucao.proc?.dente || '-'}</div>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-bold text-gray-700 mb-2">
                  Descreva a Evolução Clínica <span className="text-red-500">*</span>
                </label>
                <textarea
                  className="w-full border-2 border-gray-300 p-3 rounded h-32 focus:border-blue-500 focus:outline-none resize-none"
                  placeholder="Descreva os detalhes do procedimento realizado, materiais utilizados, reações do paciente..."
                  required
                ></textarea>
              </div>
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setModalEvolucao({aberto: false})}
                  className="px-4 py-2 border-2 border-gray-300 text-gray-700 font-bold rounded hover:bg-gray-50 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  onClick={() => setModalEvolucao({aberto: false})}
                  className="px-4 py-2 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 transition-colors"
                >
                  Salvar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Justificativa */}
      {modalJustificativa.aberto && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white w-full max-w-md rounded shadow-xl border-t-4 border-orange-500">
            <div className="p-4 border-b border-gray-200 bg-orange-50 rounded-t flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-orange-600" />
              <h2 className="text-lg font-bold text-orange-800">Justificativa Obrigatória</h2>
            </div>
            <div className="p-6">
              <p className="text-sm text-gray-600 mb-4">
                Você está prestes a {modalJustificativa.acao === 'excluir' ? 'excluir' : 'encerrar'} este plano de tratamento. Por favor, forneça o motivo para registrar no histórico do paciente.
              </p>
              <div className="mb-4">
                <textarea
                  className="w-full border-2 border-gray-300 p-3 rounded h-24 focus:border-orange-500 focus:outline-none resize-none"
                  placeholder="Digite o motivo..."
                  required
                ></textarea>
              </div>
              <div className="flex gap-3">
                <button
                  onClick={() => setModalJustificativa({aberto: false})}
                  className="flex-1 px-4 py-2 border-2 border-gray-300 text-gray-700 font-bold rounded hover:bg-gray-50 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  onClick={() => setModalJustificativa({aberto: false})}
                  className="flex-1 px-4 py-2 bg-orange-500 text-white font-bold rounded hover:bg-orange-600 transition-colors"
                >
                  Confirmar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Nova Prescrição */}
      {/* CORREÇÃO 1: Remoção da validação onChange; validação ocorre somente no botão "Salvar Prescrição" */}
      {modalNovaPrescricao && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[500px] p-6">
            <h2 className="text-xl font-bold text-gray-700 mb-4">Nova Prescrição</h2>

            <div className="space-y-4">
              <div>
                <label className="text-sm font-bold mb-2 block">
                  Medicamento <span className="text-red-500">*</span>
                </label>
                {/* Campo de busca no catálogo */}
                <div ref={dropdownRef} className="relative">
                  <div className={`flex items-center border-2 ${dropdownAberto ? "border-blue-500" : "border-gray-300"} bg-white rounded`}>
                    <Search className="w-4 h-4 text-gray-400 ml-2 shrink-0" />
                    <input
                      type="text"
                      className="flex-1 p-2 bg-transparent focus:outline-none text-sm"
                      placeholder="Buscar por nome ou princípio ativo"
                      value={buscaMedicamento}
                      onChange={(e) => { setBuscaMedicamento(e.target.value); setMedicamentoSelecionado(null); setDropdownAberto(true); }}
                      onFocus={() => setDropdownAberto(true)}
                    />
                    {medicamentoSelecionado && (
                      <button onClick={limparMedicamento} className="mr-2 text-gray-400 hover:text-red-500">
                        <X className="w-4 h-4" />
                      </button>
                    )}
                  </div>

                  {/* Dropdown */}
                  {dropdownAberto && (
                    <div className="absolute left-0 right-0 top-full mt-1 bg-white border-2 border-gray-400 shadow-lg z-50 max-h-48 overflow-y-auto">
                      {medicamentosFiltrados.length === 0 ? (
                        <div className="p-3 text-sm text-gray-400 italic">Nenhum medicamento encontrado.</div>
                      ) : (
                        medicamentosFiltrados.map((med) => (
                          <button
                            key={med.id}
                            type="button"
                            onClick={() => selecionarMedicamento(med)}
                            className="w-full text-left px-3 py-2 hover:bg-blue-50 border-b border-gray-100 last:border-0"
                          >
                            <span className="font-bold text-sm text-gray-800">{med.nomeComercial}</span>
                            <span className="text-xs text-gray-500 ml-2">{med.principioAtivo}</span>
                          </button>
                        ))
                      )}
                    </div>
                  )}
                </div>

                {/* Painel informativo após seleção */}
                {medicamentoSelecionado && (
                  <div className="mt-2 bg-blue-50 border border-blue-300 p-3 rounded text-sm space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-bold text-gray-500">Categoria:</span>
                      <span className="bg-blue-100 border border-blue-400 px-2 py-0.5 rounded text-xs font-bold text-blue-700">{medicamentoSelecionado.categoria}</span>
                      <span className="text-xs font-bold text-gray-500 ml-2">Classe:</span>
                      <span className="text-xs text-gray-700">{medicamentoSelecionado.classeFarmacologica}</span>
                    </div>
                    {medicamentoSelecionado.contraindicacoes && (
                      <div className="text-xs text-yellow-800 flex gap-1">
                        <AlertTriangle className="w-3 h-3 mt-0.5 shrink-0" />
                        <span><strong>Contraindicações:</strong> {medicamentoSelecionado.contraindicacoes}</span>
                      </div>
                    )}
                    {medicamentoSelecionado.interacoes && (
                      <div className="text-xs text-orange-700 flex gap-1">
                        <AlertTriangle className="w-3 h-3 mt-0.5 shrink-0" />
                        <span><strong>Interações:</strong> {medicamentoSelecionado.interacoes}</span>
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Dosagem <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  placeholder="Ex: 500mg"
                  value={dosagemInput}
                  onChange={(e) => setDosagemInput(e.target.value)}
                />
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Posologia <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  placeholder="Ex: 1 cáps. a cada 8h"
                  value={posologiaInput}
                  onChange={(e) => setPosologiaInput(e.target.value)}
                />
                {/* Atalho: posologias padrão do catálogo */}
                {medicamentoSelecionado && medicamentoSelecionado.posologiasPadrao.length > 0 && (
                  <div className="mt-2 space-y-1">
                    <div className="text-xs text-gray-500 font-bold">Posologias padrão do catálogo:</div>
                    {medicamentoSelecionado.posologiasPadrao.map((p, i) => (
                      <button
                        key={i}
                        type="button"
                        onClick={() => setPosologiaInput(p)}
                        className="w-full text-left text-xs px-2 py-1.5 border border-blue-400 bg-blue-50 text-blue-700 rounded hover:bg-blue-100 font-medium"
                      >
                        ↳ {p}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Período de uso <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  placeholder="Ex: 7 dias"
                  value={periodoInput}
                  onChange={(e) => setPeriodoInput(e.target.value)}
                />
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Agendamento vinculado <span className="text-red-500">*</span>
                </label>
                <select
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none"
                  value={agendamentoInput}
                  onChange={(e) => setAgendamentoInput(e.target.value)}
                >
                  <option value="">Selecione um agendamento</option>
                  <option value="AGE-0607">AGE-0607 - 08/06/2026 - Restauração</option>
                  <option value="AGE-0419">AGE-0419 - 20/04/2026 - Limpeza</option>
                  <option value="AGE-0412">AGE-0412 - 15/04/2026 - Extração</option>
                </select>
              </div>

              <div>
                <label className="text-sm font-bold mb-2 block">
                  Observações terapêuticas
                </label>
                <textarea
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none resize-none"
                  rows={3}
                  placeholder="Motivo da prescrição ou recomendações verbais"
                  value={observacoesInput}
                  onChange={(e) => setObservacoesInput(e.target.value)}
                ></textarea>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => {
                  setModalNovaPrescricao(false);
                  limparFormulario();
                }}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
              >
                Cancelar
              </button>
              {/* CORREÇÃO 1: Validação cruzada com anamnese executada aqui, não no onChange */}
              <button
                onClick={handleSalvarPrescricao}
                className="flex-1 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold"
              >
                Salvar Prescrição
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Alerta de Contraindicação por Alergia */}
      {/* CORREÇÃO 1: Modal exibido somente após clicar em "Salvar"; confirmação libera o salvamento */}
      {modalAlertaAlergia && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-red-500 w-[500px] p-6">
            <div className="flex items-start gap-3 mb-4">
              <span className="text-4xl">🚨</span>
              <div>
                <h2 className="text-xl font-bold text-red-700">Alerta de Contraindicação</h2>
              </div>
            </div>

            <p className="text-sm text-gray-700 mb-4">
              O medicamento informado pertence a uma classe à qual o paciente possui alergia registrada na anamnese:{" "}
              <strong>{contraindicacaoDetectada}</strong>.
            </p>

            <div className="bg-red-50 border-2 border-red-300 p-4 mb-4">
              <p className="text-sm text-red-800 font-bold">
                O salvamento está bloqueado. Para prosseguir, o Cirurgião-Dentista deve confirmar ciência do risco. Essa confirmação será registrada em log de auditoria.
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  setModalAlertaAlergia(false);
                  setModalNovaPrescricao(true);
                }}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
              >
                Revisar Medicamento
              </button>
              {/* CORREÇÃO 1: Confirmação explícita registrada em auditoria, depois conclui o salvamento */}
              <button
                onClick={handleConfirmarCienciaRisco}
                className="flex-1 px-4 py-2 border-2 border-red-500 bg-red-500 text-white font-bold"
              >
                Confirmo Ciência do Risco
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Alerta de Prescrição Recente */}
      {/* CORREÇÃO 2: Exibido somente se o mesmo medicamento foi prescrito nos últimos 30 dias; apenas informativo */}
      {modalAlertaRecente && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[500px]">
            <div className="bg-orange-50 p-4 flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <h2 className="text-xl font-bold text-orange-800">Prescrição Recente Identificada</h2>
            </div>

            <div className="p-6">
              <p className="text-sm text-gray-700 mb-4">
                Este medicamento já foi prescrito a este paciente nos últimos 30 dias.
              </p>

              {/* CORREÇÃO 2: Dados dinâmicos da última prescrição encontrada no histórico */}
              <div className="bg-gray-100 border-2 border-gray-300 p-4 mb-4">
                <div className="text-sm">
                  <div className="font-bold mb-2">Última prescrição:</div>
                  <div><strong>Data:</strong> {prescricaoRecenteDetectada?.data}</div>
                  <div><strong>Medicamento:</strong> {prescricaoRecenteDetectada?.medicamento} {prescricaoRecenteDetectada?.dosagem}</div>
                  <div><strong>Posologia:</strong> {prescricaoRecenteDetectada?.posologia} por {prescricaoRecenteDetectada?.periodo}</div>
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => {
                    setModalAlertaRecente(false);
                    limparFormulario();
                  }}
                  className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
                >
                  Cancelar
                </button>
                {/* CORREÇÃO 2: Alerta não bloqueia o salvamento; usuário pode confirmar mesmo assim */}
                <button
                  onClick={salvarPrescricaoFinal}
                  className="flex-1 px-4 py-2 border-2 border-orange-500 bg-orange-500 text-white font-bold"
                >
                  Salvar Mesmo Assim
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Re-prescrição */}
      {modalRepetirPrescricao.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-4 border-gray-400 w-[500px] p-6">
            <h2 className="text-xl font-bold text-gray-700 mb-4">Repetir Prescrição</h2>

            <div className="bg-gray-100 border-2 border-gray-300 p-4 mb-4">
              <div className="text-sm space-y-2">
                <div><strong>Medicamento:</strong> {modalRepetirPrescricao.prescricao?.medicamento}</div>
                <div><strong>Dosagem:</strong> {modalRepetirPrescricao.prescricao?.dosagem}</div>
                <div><strong>Posologia:</strong> {modalRepetirPrescricao.prescricao?.posologia}</div>
                <div><strong>Período:</strong> {modalRepetirPrescricao.prescricao?.periodo}</div>
              </div>
            </div>

            <div className="bg-yellow-50 border-2 border-yellow-300 p-3 mb-4">
              <p className="text-xs text-yellow-800">
                <AlertTriangle className="w-4 h-4 inline mr-1" />
                O sistema irá revalidar automaticamente as alergias da anamnese atualizada do paciente antes de confirmar.
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setModalRepetirPrescricao({aberto: false})}
                className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
              >
                Cancelar
              </button>
              {/* Re-prescrição passa pelas mesmas validações de contraindicação e recência */}
              <button
                onClick={handleConfirmarRepeticao}
                className="flex-1 px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold"
              >
                Confirmar Repetição
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Justificativa de Exclusão */}
      {modalExcluirPrescricao.aberto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-t-4 border-orange-500 w-[500px]">
            <div className="bg-orange-50 p-4 flex items-center gap-3">
              <ShieldAlert className="w-6 h-6 text-orange-600" />
              <h2 className="text-xl font-bold text-orange-800">Exclusão de Prescrição</h2>
            </div>

            <div className="p-6">
              <p className="text-sm text-gray-700 mb-4">
                Esta exclusão será registrada em log de auditoria. Forneça o motivo obrigatoriamente.
              </p>

              <div className="mb-4">
                <label className="text-sm font-bold mb-2 block">
                  Justificativa <span className="text-red-500">*</span>
                </label>
                <textarea
                  className="w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-orange-500 focus:outline-none resize-none"
                  rows={4}
                  placeholder="Digite o motivo da exclusão..."
                ></textarea>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setModalExcluirPrescricao({aberto: false})}
                  className="flex-1 px-4 py-2 border-2 border-gray-400 bg-white font-bold"
                >
                  Cancelar
                </button>
                <button
                  onClick={() => setModalExcluirPrescricao({aberto: false})}
                  className="flex-1 px-4 py-2 border-2 border-orange-500 bg-orange-500 text-white font-bold"
                >
                  Confirmar Exclusão
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
