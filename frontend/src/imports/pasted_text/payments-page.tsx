Prompt para a IA do Figma — F17: Gestão de Pagamentos e Quitação de Débitos

Você é um desenvolvedor React trabalhando em um sistema de gestão de clínica odontológica chamado OdontoHub. O projeto usa React + TypeScript + Tailwind CSS + Lucide React + React Router v7. O projeto é gerado pelo ambiente Figma Make, então siga exatamente os padrões visuais e técnicos já estabelecidos no código existente.

CONTEXTO DO PROJETO
O sistema já possui uma página de inadimplência localizada em src/app/pages/recepcionista/Acordos.tsx. Essa página gerencia parcelas vencidas, acordos e inadimplência de pacientes. Ela possui uma função chamada registrarPagamento que atualmente é um mock simples: ao clicar no botão "Registrar Pagamento", a parcela é marcada como PAGA imediatamente, sem nenhum formulário, sem valor, sem forma de pagamento e sem data.
O objetivo desta tarefa é substituir esse mock por um fluxo real de pagamento, criando uma nova página chamada Pagamentos.tsx no mesmo diretório da recepcionista, e integrando-a ao sistema de rotas e ao layout existente.

PADRÃO VISUAL DO PROJETO
Siga rigorosamente o padrão visual já usado em Acordos.tsx. Os elementos são:

Fundo geral: bg-gray-50 min-h-screen p-6
Cards de resumo: border-2 p-4 com cores temáticas (border-red-500 bg-red-50, border-green-500 bg-green-50, etc.)
Tabelas: border-2 border-gray-400 bg-white com header bg-gray-100 border-b-2 border-gray-400
Botões primários: border-2 border-blue-500 bg-blue-500 text-white font-bold hover:bg-blue-600
Botões secundários: border-2 border-gray-400 bg-white font-bold hover:bg-gray-50
Botões de perigo: border-2 border-red-500 bg-red-500 text-white font-bold hover:bg-red-600
Modais: fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 com painel bg-white border-t-4 border-blue-500 w-[600px] max-h-[90vh] overflow-y-auto
Badges de status: inline-block px-2 py-0.5 border text-xs font-bold rounded com cores por status
Toast: fixed top-4 right-4 z-50 px-5 py-3 border-2 font-bold text-sm shadow-lg
Títulos de seção: text-xl font-bold text-gray-700 flex items-center gap-2
Labels de campo: text-sm font-bold mb-2 block
Inputs: w-full border-2 border-gray-300 p-2 bg-white rounded focus:border-blue-500 focus:outline-none
Ícones: sempre de lucide-react


TAREFA 1 — Criar src/app/pages/recepcionista/Pagamentos.tsx
Esta página é a F17. Ela opera sobre os mesmos dados de pacientes/parcelas da F9. Para isso, exporte os dados MOCK_PACIENTES de Acordos.tsx (ou replique a estrutura de dados nesta página com os mesmos tipos) para que ambas as páginas compartilhem o mesmo estado — use useState elevado ou replique o mock localmente por ora, deixando um comentário // TODO: compartilhar estado global com Acordos.tsx.
ESTRUTURA DA PÁGINA
Header:
💳 Pagamentos
Registre e gerencie os pagamentos de parcelas dos pacientes.
Cards de resumo (grid de 4 colunas):

Pagamentos Hoje: quantidade de pagamentos confirmados na data atual — border-green-500 bg-green-50 text-green-700
Aguardando Comprovante: quantidade de lançamentos com status AGUARDANDO — border-yellow-500 bg-yellow-50 text-yellow-700
Total Recebido Hoje: soma dos valores confirmados na data atual em R$ — border-blue-500 bg-blue-50 text-blue-700
Parcelas em Aberto: total de parcelas com status VENCIDA ou PENDENTE entre todos os pacientes — border-red-500 bg-red-50 text-red-700

Filtros (linha única com 3 campos):

Busca por nome ou CPF do paciente
Filtro de status do lançamento: Todos / Confirmado / Aguardando Comprovante / Cancelado
Filtro de forma de pagamento: Todas / PIX / Cartão de Crédito / Cartão de Débito / Dinheiro / Transferência

Tabela de lançamentos de pagamento:
Colunas: Paciente | Parcela | Valor Pago | Forma de Pagamento | Data | Status | Ações
Cada linha representa um RegistroPagamento (tipo descrito abaixo). O status é exibido como badge colorido. A coluna Ações exibe:

Quando status AGUARDANDO: botão "Confirmar" (verde) e botão "Cancelar" (cinza)
Quando status CONFIRMADO: botão "Comprovante" (azul claro, apenas outline)
Quando status CANCELADO: sem botões, apenas texto cinza "Cancelado"


TIPOS NECESSÁRIOS
Adicione estes tipos no arquivo, além de reaproveitar Parcela, Acordo e Paciente de Acordos.tsx (ou redefina-os localmente com comentário TODO):
typescripttype StatusPagamento = "CONFIRMADO" | "AGUARDANDO" | "CANCELADO";

type FormaPagamento =
  | "PIX"
  | "CARTAO_CREDITO"
  | "CARTAO_DEBITO"
  | "DINHEIRO"
  | "TRANSFERENCIA";

interface RegistroPagamento {
  id: number;
  pacienteId: number;
  pacienteNome: string;
  parcelaId: number;
  parcelaNumero: number;
  valorPago: number;
  formaPagamento: FormaPagamento;
  dataPagamento: string; // DD/MM/AAAA
  observacao?: string;
  status: StatusPagamento;
  motivoCancelamento?: string;
  registradoPor: string;
  dataRegistro: string;
}
Crie um mock inicial com pelo menos 4 registros variados (um CONFIRMADO de dinheiro, um CONFIRMADO de PIX, um AGUARDANDO de transferência, um CANCELADO de cartão) para demonstrar todos os estados visualmente.

MODAL 1 — Registrar Pagamento Presencial
Acionado pelo botão "Registrar Pagamento" que já existe em Acordos.tsx (botão nos cards de parcela vencida dentro do modal de detalhe do paciente). Por ora, adicione também um botão "Novo Pagamento" no header da página Pagamentos.tsx que abre este modal com seleção de paciente e parcela.
Conteúdo do modal:
Header: border-t-4 border-green-500 com título "Registrar Pagamento" e subtítulo com nome do paciente quando vier da F9, ou campo de seleção de paciente quando aberto do header.
Seção 1 — Parcela:

Quando vier da F9 com parcela pré-selecionada: exibir card somente leitura com número da parcela, vencimento, valor original e encargos (se vencida). Badge de status atual.
Quando aberto do header: select de paciente → select de parcela elegível (status VENCIDA ou PENDENTE).

Seção 2 — Dados do Pagamento:
Valor Recebido *          Data do Pagamento *
[input number R$]         [input date, max=hoje]

Forma de Pagamento *
[select: PIX / Cartão de Crédito / Cartão de Débito / Dinheiro / Transferência]

Observação (opcional)
[textarea 2 linhas]
Aviso de pagamento parcial: quando o valor digitado for menor que o valor devido da parcela, exibir banner amarelo:
⚠ Valor inferior ao devido. A parcela ficará como Parcialmente Paga e o saldo de R$ X,XX permanecerá em aberto.
Rodapé: botão "Cancelar" (cinza) + botão "Confirmar Pagamento" (verde, border-green-500 bg-green-500).
Ao confirmar:

Validar: valor > 0, data preenchida e não futura, forma de pagamento selecionada. Exibir erros inline sem fechar o modal.
Criar novo RegistroPagamento com status CONFIRMADO.
Atualizar a parcela correspondente no array de pacientes: PAGA se valor >= devido, PARCIALMENTE_PAGA se valor < devido (adicione esse status ao tipo StatusParcela de Acordos se necessário — ou use PAGA com flag de parcial por ora, deixando comentário TODO).
Se todas as parcelas do acordo estiverem pagas, marcar o acordo como QUITADO.
Recalcular se o paciente ainda tem parcelas vencidas. Se não tiver, mudar status do paciente para ATIVO.
Adicionar entrada no fluxo de caixa mock (array local com comentário // TODO: integrar com Financeiro.tsx).
Exibir toast de sucesso: ✓ Pagamento de R$ X,XX registrado com sucesso.


MODAL 2 — Lançar como Aguardando Comprovante
Acionado pelo botão "Aguardar Comprovante" disponível nas parcelas com status VENCIDA ou PENDENTE dentro do modal de detalhe da F9, e também pelo botão "Aguardar Comprovante" no header de Pagamentos.tsx.
Conteúdo do modal:
Header: border-t-4 border-yellow-500 com título "Aguardando Comprovante".
Paciente: [nome — somente leitura se vier da F9, ou select]
Parcela:  [número e vencimento — somente leitura ou select]
Valor esperado: [exibido automaticamente com base na parcela]

Forma de Pagamento Esperada *
[select: PIX / Transferência / Cartão de Crédito / Cartão de Débito / Dinheiro]

Observação (opcional)
[textarea — ex: "PIX enviado pelo WhatsApp, aguardando confirmação"]
Banner informativo azul:
ℹ A parcela não será baixada agora. O pagamento ficará como Aguardando Comprovante até você confirmar o recebimento.
Validação: bloquear se já existir um lançamento AGUARDANDO para a mesma parcela. Exibir mensagem de erro: "Já existe um pagamento aguardando confirmação para esta parcela."
Ao salvar: criar RegistroPagamento com status AGUARDANDO, sem valorPago (0 por ora) e sem dataPagamento. Toast: ⚠ Pagamento lançado como Aguardando Comprovante.

MODAL 3 — Confirmar Comprovante Recebido
Acionado pelo botão "Confirmar" nas linhas com status AGUARDANDO na tabela da página.
Conteúdo do modal:
Header: border-t-4 border-green-500 com título "Confirmar Recebimento".
Exibir resumo somente leitura: paciente, parcela, forma de pagamento declarada.
Valor Recebido *          Data do Pagamento *
[input number R$]         [input date, max=hoje]

Observação (opcional)
[textarea]
Aviso de pagamento parcial (mesmo do Modal 1).
Ao confirmar: atualizar o RegistroPagamento existente para status CONFIRMADO com valor e data preenchidos. Aplicar os mesmos efeitos do Modal 1 na parcela e no paciente.

MODAL 4 — Cancelar Lançamento Aguardando
Acionado pelo botão "Cancelar" nas linhas AGUARDANDO.
Modal simples com:
Tem certeza que deseja cancelar este lançamento?
Paciente: [nome]   Parcela: [número]

Motivo do cancelamento *
[textarea obrigatório]
Botões: "Voltar" (cinza) + "Confirmar Cancelamento" (vermelho).
Ao confirmar: atualizar status do RegistroPagamento para CANCELADO com o motivo preenchido. A parcela volta ao status original (VENCIDA ou PENDENTE). Toast: ✕ Lançamento cancelado.

MODAL 5 — Comprovante de Pagamento
Acionado pelo botão "Comprovante" nas linhas CONFIRMADO.
Modal simples w-[480px] com border-t-4 border-blue-500:
┌────────────────────────────────────┐
│          OdontoHub                 │
│    Clínica Odontológica            │
│                                    │
│  COMPROVANTE DE PAGAMENTO          │
│  Nº [id do registro]               │
│                                    │
│  Paciente:    [nome]               │
│  CPF:         [xxx.***.***/xx]     │
│                                    │
│  Parcela:     [número]             │
│  Vencimento:  [data]               │
│  Valor Pago:  R$ [valor]           │
│  Forma:       [forma]              │
│  Data Pag.:   [data]               │
│                                    │
│  Registrado por: [nome]            │
│  Data do registro: [data]          │
│                                    │
│  ✓ Pagamento confirmado            │
└────────────────────────────────────┘
Botão único: "Fechar".

TAREFA 2 — Modificar Acordos.tsx
Nos botões "Registrar Pagamento" que já existem nas parcelas vencidas (dentro do modal de detalhe e dentro da aba de acordos), substitua a chamada direta a registrarPagamento() por uma abertura do Modal 1 da F17, passando o paciente e a parcela como contexto. Como as páginas são separadas, por ora use navegação react-router (useNavigate) para ir até /recepcionista/pagamentos passando os ids via state, ou simplesmente exiba o formulário de pagamento em um modal local dentro de Acordos.tsx com a mesma estrutura do Modal 1 descrito acima — escolha a abordagem mais simples e deixe comentário // TODO: unificar com Pagamentos.tsx.
Adicione também, ao lado do botão "Ver" na tabela de inadimplentes, um botão "Pagar" (verde outline: border-2 border-green-500 text-green-600 bg-white hover:bg-green-50) que abre diretamente o modal de pagamento para o primeiro caso de parcela vencida daquele paciente.

TAREFA 3 — Registrar a nova rota e adicionar ao menu
Em src/app/routes.tsx, adicione dentro do bloco /recepcionista:
typescript{ path: "pagamentos", Component: RecepcionistaPagamentos }
Em src/app/pages/recepcionista/RecepcionistaLayout.tsx, adicione ao array menuItems:
typescript{ path: "pagamentos", label: "Pagamentos", icon: "💳" }

ALERTAS VISUAIS NA TABELA
Lançamentos com status AGUARDANDO há mais de 2 dias (calcule com base em dataRegistro) devem exibir um ícone de alerta ⚠ em vermelho ao lado do badge de status, com tooltip ou texto auxiliar "Aguardando há X dias".

RESTRIÇÕES OBRIGATÓRIAS

Não use <form> HTML. Use onClick e onChange diretamente nos elementos.
Não instale nenhuma biblioteca nova. Use apenas o que já está no package.json.
Mantenha todos os imports de ícones vindos de lucide-react.
Não remova nenhuma funcionalidade existente em Acordos.tsx. Apenas adicione e substitua o mock de registrarPagamento.
Todos os filtros e botões da nova página devem funcionar — nenhum elemento decorativo sem ação.
Use useState para todo o estado local. Sem Context API ou Zustand por ora.
Siga o padrão de seções comentadas com // ─── Nome ─── já usado em Acordos.tsx.