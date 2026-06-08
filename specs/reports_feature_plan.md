# Especificação de Funcionalidade - Reestruturação e Novos Relatórios (Custo de Jobs e Timesheet)

Esta especificação define as melhorias na visualização e emissão de relatórios de gestão no sistema Laboris.

---

## 📋 Regras de Negócio e Requisitos

### 1. Centralização do Relatório de Custos (Multi-Job)
- O Relatório de Custo por Serviço (Job Cost Report) não deve mais ser acessado de dentro do Job individual.
- Ele deve ser integrado no menu unificado **Relatórios** (`/reports`) do painel do Gestor.
- O usuário poderá filtrar o relatório selecionando um ou mais Jobs da empresa.
- **Visualização:** Ao selecionar múltiplos Jobs, a tela deve exibir o relatório de cada um individualmente, mantendo os resumos e tabelas separados por Job (sem consolidar ou somar horas/valores entre Jobs diferentes).

### 2. Novo Relatório de Timesheet (Presença no Job)
- Dentro da página de detalhes de cada Job (`/jobs/{jobId}`), adicionaremos uma nova seção: **"Relatório de Presença e Horas Diárias"** (Timesheet).
- Este relatório exibirá uma lista de quem trabalhou naquele Job, especificando quais dias cada pessoa trabalhou e quantas horas trabalhou em cada um desses dias.

---

## 🛠️ Arquitetura Técnica e Contratos

### Banco de Dados & Repositório
- Não são necessárias alterações na estrutura de tabelas.
- Adicionar no `TimeEntryRepository` a capacidade de recuperar todos os registros de ponto de um Job ordenados de forma crescente por data/hora:
  `List<TimeEntry> findAllByJobIdOrderByEntryTimestampAsc(UUID jobId);`

### Backend (Java / Spring Boot)
- Criar `JobTimesheetResponseDto.java` contendo as informações do Job e a lista de colaboradores com suas respectivas datas e horas diárias trabalhadas.
- Implementar no `ReportService.java` o método `calculateJobTimesheetReport` para agrupar as batidas de ponto por funcionário e depois por dia, utilizando a classe utilitária de cálculo de horas trabalhadas.
- Expor o endpoint no `ReportController.java`:
  `GET /api/v1/reports/jobs/{jobId}/timesheet`

### Frontend (React)
- **Menu Relatórios (`/reports`):**
  - Substituir o componente simples de folha de pagamento por um painel em abas (tabs).
  - Aba 1: **Folha de Pagamento** (funcionalidade existente).
  - Aba 2: **Custos de Serviços** (nova funcionalidade com multi-seletor de Jobs e período, fazendo requisições em paralelo e exibindo os resultados individualmente).
- **Detalhes do Job (`/jobs/{jobId}`):**
  - Remover o botão de acesso ao antigo relatório de custo.
  - Carregar e exibir a nova seção de Timesheet a partir do novo endpoint do backend.
