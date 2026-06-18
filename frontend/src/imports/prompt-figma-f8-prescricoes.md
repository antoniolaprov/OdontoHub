# Prompt para Figma AI — Funcionalidade 8: Prescrições Medicamentosas

---

## Contexto do Projeto

Você está trabalhando no **OdontoHub**, um sistema de gestão para consultórios odontológicos. O projeto já possui telas implementadas para Prontuários, Agendamentos, Estoque, Esterilização, Acordos Financeiros e Recall.

### Identidade Visual Estabelecida (siga rigorosamente)

- **Fundo geral da página:** `bg-gray-50` com padding `p-6`
- **Cards e tabelas:** `bg-white` com bordas `border-2 border-gray-400`
- **Cabeçalho de tabela:** `bg-gray-100` com `border-b-2 border-gray-400`; células separadas por `border-r-2 border-gray-400`
- **Botão primário:** `border-2 border-blue-500 bg-blue-500 text-white font-bold`
- **Botão secundário/neutro:** `border-2 border-gray-400 bg-white`
- **Botão de ação de linha:** `border-2 border-blue-500 bg-white text-blue-600 text-sm`
- **Badge verde (Realizado/Estéril):** `bg-green-100 text-green-800 border border-green-500 px-2 py-1 rounded text-xs font-bold`
- **Badge azul (Pendente/Ativo):** `bg-blue-100 text-blue-800 border border-blue-500 px-2 py-1 rounded text-xs font-bold`
- **Badge vermelho (Cancelado/Restrito):** `bg-red-100 text-red-800 border border-red-500 px-2 py-1 rounded text-xs font-bold`
- **Badge amarelo (Alerta):** `bg-yellow-100 text-yellow-800 border border-yellow-500 px-2 py-1 rounded text-xs font-bold`
- **Inputs:** `border-2 border-gray-300 p-2 bg-white rounded` com foco em `border-blue-500`
- **Labels:** `text-sm font-bold mb-2 block`
- **Títulos de página:** `text-xl font-bold text-gray-700`
- **Texto auxiliar/data:** `text-xs text-gray-500`
- **Modais:** fundo `fixed inset-0 bg-black bg-opacity-50`; caixa `bg-white border-4 border-gray-400 w-[500px] p-6`
- **Modal de alerta crítico:** `border-4 border-red-500`
- **Modal de justificativa/aviso:** `border-t-4 border-orange-500` com cabeçalho `bg-orange-50`
- **Tipografia:** sem fontes externas, usar a fonte padrão do sistema; peso `font-bold` para títulos e labels, `font-normal` para corpo
- **Ícones:** biblioteca Lucide React (mesmo padrão já usado nas outras telas)

---

## O Que Construir

Implemente a **tela de Prescrições Medicamentosas** acessível dentro do prontuário do paciente, como uma aba adicional ao lado de "Anamnese" e "Plano de Tratamento" — exatamente como já existe na tela `Prontuarios.tsx`.

A tela deve cobrir os seguintes estados e fluxos:

---

### 1. Aba "Prescrições" no Prontuário

Dentro da seção de abas do prontuário (`Anamnese | Plano de Tratamento | Prescrições`), ao clicar em **Prescrições**, exibir:

**Estado bloqueado (sem anamnese preenchida):**
Igual ao bloqueio já existente no Plano de Tratamento — ícone de cadeado centralizado, texto explicando que a anamnese precisa ser preenchida antes, botão "Preencher Anamnese" que redireciona para a aba correta.

**Estado ativo (anamnese preenchida):**

- Cabeçalho com título "Prescrições" à esquerda e botão `+ Nova Prescrição` à direita
- Tabela com colunas: **Data | Medicamento | Dosagem | Posologia | Período | Agendamento Vinculado | Responsável | Ações**
- Cada linha tem dois botões de ação: `Repetir` (azul, borda) e `Excluir` (vermelho, borda) — o botão Excluir deve aparecer com ícone de cadeado e desabilitado quando o registro tiver mais de 24h, com tooltip "Registro imutável após 24h"
- Registros com menos de 24h exibem badge amarelo `Editável` na coluna de ações
- Registros com mais de 24h exibem ícone 🔒 e texto `>24h` (mesmo padrão da coluna "Edição" do Plano de Tratamento)

**Dados de exemplo para popular a tabela:**

| Data | Medicamento | Dosagem | Posologia | Período | Agendamento | Responsável |
|---|---|---|---|---|---|---|
| 15/04/2026 | Ibuprofeno | 600mg | 1 comp. a cada 8h | 3 dias | AGE-0412 | Dr. Felipe | → bloqueado (>24h) |
| 20/04/2026 | Amoxicilina | 500mg | 1 cáps. a cada 8h | 7 dias | AGE-0419 | Dr. Felipe | → bloqueado (>24h) |
| 08/06/2026 | Nimesulida | 100mg | 1 comp. a cada 12h | 2 dias | AGE-0607 | Dr. Felipe | → editável (<24h) |

---

### 2. Modal: Nova Prescrição

Acionado pelo botão `+ Nova Prescrição`. Seguir o padrão de modal com `border-4 border-gray-400`.

**Campos do formulário:**
- **Medicamento** — input de texto (obrigatório)
- **Dosagem** — input de texto (obrigatório), ex: "500mg"
- **Posologia** — input de texto (obrigatório), ex: "1 cáps. a cada 8h"
- **Período de uso** — input de texto (obrigatório), ex: "7 dias"
- **Agendamento vinculado** — select/dropdown com agendamentos confirmados do paciente (obrigatório)
- **Observações terapêuticas** — textarea opcional, placeholder: "Motivo da prescrição ou recomendações verbais"

**Botões:** `Cancelar` (neutro) e `Salvar Prescrição` (azul primário)

---

### 3. Modal: Alerta de Contraindicação por Alergia

Exibido automaticamente ao digitar um medicamento que conste na lista de alergias da anamnese do paciente (ex: paciente alérgico a Penicilina — ao digitar "Amoxicilina", o alerta dispara).

Seguir o padrão de alerta crítico: `border-4 border-red-500`, ícone 🚨, título em `text-red-700`.

**Conteúdo:**
- Título: "Alerta de Contraindicação"
- Texto: "O medicamento informado pertence a uma classe à qual o paciente possui alergia registrada na anamnese: **Penicilina**."
- Aviso em destaque: "O salvamento está bloqueado. Para prosseguir, o Cirurgião-Dentista deve confirmar ciência do risco. Essa confirmação será registrada em log de auditoria."
- Dois botões: `Revisar Medicamento` (neutro) e `Confirmo Ciência do Risco` (vermelho, `bg-red-500 text-white font-bold`)

---

### 4. Modal: Alerta de Prescrição Recente

Exibido ao tentar salvar um medicamento que já foi prescrito ao mesmo paciente nos últimos 30 dias. Não bloqueia — apenas informa.

Seguir padrão de aviso: `border-t-4 border-orange-500`, cabeçalho `bg-orange-50`, ícone ⚠️.

**Conteúdo:**
- Título: "Prescrição Recente Identificada"
- Texto: "Este medicamento já foi prescrito a este paciente nos últimos 30 dias. Última prescrição: **20/04/2026** — Amoxicilina 500mg, 1 cáps. a cada 8h por 7 dias."
- Dois botões: `Cancelar` (neutro) e `Salvar Mesmo Assim` (laranja, `bg-orange-500 text-white font-bold`)

---

### 5. Modal: Re-prescrição

Acionado pelo botão `Repetir` em uma linha da tabela. Exibir um modal de confirmação simples.

**Conteúdo:**
- Título: "Repetir Prescrição"
- Bloco cinza com os dados da prescrição original (medicamento, dosagem, posologia, período)
- Aviso em amarelo: "O sistema irá revalidar automaticamente as alergias da anamnese atualizada do paciente antes de confirmar."
- Dois botões: `Cancelar` (neutro) e `Confirmar Repetição` (azul primário)

---

### 6. Modal: Justificativa de Exclusão

Acionado pelo botão `Excluir` em registros ainda dentro da janela de 24h.

Seguir padrão de justificativa: `border-t-4 border-orange-500`, ícone ShieldAlert da Lucide, cabeçalho `bg-orange-50`.

**Conteúdo:**
- Título: "Exclusão de Prescrição"
- Texto: "Esta exclusão será registrada em log de auditoria. Forneça o motivo obrigatoriamente."
- Textarea obrigatória
- Dois botões: `Cancelar` e `Confirmar Exclusão` (laranja)

---

### 7. Visão Geral de Prescrições (rota separada no menu do Dentista)

Item de menu adicional no sidebar do Dentista: `💊 Prescrições` apontando para `/dentista/prescricoes`.

**Tela independente** com:
- Título "Filtro Geral de Prescrições" 
- Barra de filtros horizontais: campo de texto "Buscar medicamento", select "Categoria terapêutica" (Antibiótico, Anti-inflamatório, Analgésico, Outro), date picker "Período de" e "Período até", botão `Filtrar` (azul primário)
- Tabela com colunas: **Data | Paciente | Medicamento | Categoria | Dosagem | Período | Agendamento**
- Dados de exemplo com pelo menos 5 linhas variadas entre diferentes pacientes e medicamentos

---

## Instruções Finais para o Figma AI

- Respeite rigorosamente todos os tokens visuais descritos na seção "Identidade Visual Estabelecida"
- Não invente novos padrões de cores, bordas ou tipografia — apenas os já existentes no projeto
- Todos os modais devem usar sobreposição escura `bg-black bg-opacity-50` como fundo
- Use ícones da biblioteca Lucide: `Pill`, `AlertTriangle`, `Lock`, `ShieldAlert`, `RotateCcw`, `FileText` são os mais adequados para este contexto
- Mantenha o mesmo espaçamento interno das outras telas: `p-6` na página, `p-3` nas células de tabela, `p-4` nos cards de formulário
- Os estados de loading ou campos calculados devem usar `bg-gray-100 text-gray-500 cursor-not-allowed` para indicar não-editabilidade
