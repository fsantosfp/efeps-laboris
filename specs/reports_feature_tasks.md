# Lista de Tarefas - Reestruturação e Novos Relatórios (Custo de Jobs e Timesheet)

Este documento contém a decomposição das tarefas para a implementação das melhorias e novos relatórios seguindo as diretrizes do **Specification-Driven Development (SDD)**. Cada tarefa deve ser executada individualmente, testada e validada antes de iniciar o commit e avançar.

---

## ☕ Slice 1: Camada de Backend (API de Timesheet do Job)

- [x] **Tarefa 1.1: Criação do DTO de Resposta do Timesheet**
  - **Ações:**
    - Criar a classe [JobTimesheetResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/JobTimesheetResponseDto.java) contendo a estrutura com dados do Job, lista de colaboradores e detalhamento de data/horas diárias.
  - **Critério de Aceitação:** Classe DTO criada e compilando normalmente.

- [x] **Tarefa 1.2: Adição do Método de Busca no Repositório**
  - **Ações:**
    - Adicionar a declaração de método `findAllByJobIdOrderByEntryTimestampAsc` no [TimeEntryRepository.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/repository/TimeEntryRepository.java).
  - **Critério de Aceitação:** Repositório atualizado e compilável.

- [x] **Tarefa 1.3: Lógica de Serviço no ReportService**
  - **Ações:**
    - Criar o método `calculateJobTimesheetReport(User manager, UUID jobId, Instant start, Instant end)` no [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java).
    - Agrupar e somar as horas diárias trabalhadas por cada colaborador usando `TimeEntryCalculationHelper.calculateHoursWorked`.
  - **Critério de Aceitação:** Retorno correto dos dados no DTO estruturado.

- [x] **Tarefa 1.4: Endpoint no ReportController**
  - **Ações:**
    - Criar a rota `GET /api/v1/reports/jobs/{jobId}/timesheet` no [ReportController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/ReportController.java) recebendo parâmetros de período opcionais.
  - **Critério de Aceitação:** Endpoint mapeado e acessível.

- [x] **Tarefa 1.5: Testes Unitários do Novo Relatório**
  - **Ações:**
    - Escrever testes em `ReportServiceTest.java` para validar os cálculos de horas por funcionário e permissões de segurança.
    - Executar `SPRING_PROFILES_ACTIVE=local mvn test` para certificar-se do sucesso de todos os testes do backend.
  - **Critério de Aceitação:** Todos os 60+ testes passando sem falhas.

---

## 📂 Slice 2: Interface Unificada de Relatórios (Frontend)

- [ ] **Tarefa 2.1: Nova Página de Relatórios e Rota**
  - **Ações:**
    - Criar [ReportsPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/ReportsPage.jsx).
    - Atualizar as rotas do [App.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/App.jsx) fazendo `/reports` renderizar `ReportsPage` (e remover a página `PayrollReportPage.jsx` obsoleta e sua importação).
    - Montar a interface moderna de abas (Tabs) para alternar entre "Folha de Pagamento" e "Custos de Serviços".
  - **Critério de Aceitação:** Acesso à página com abas estruturadas funcionando no menu.

- [ ] **Tarefa 2.2: Seletor Múltiplo de Jobs na Aba de Custos**
  - **Ações:**
    - Na aba "Custos de Serviços", carregar todos os Jobs disponíveis da empresa usando `GET /api/v1/jobs`.
    - Exibir uma lista de seleção múltipla (checkboxes ou cards selecionáveis) para o gestor escolher quais Jobs incluir no relatório.
  - **Critério de Aceitação:** Lista de Jobs renderizada e seleções ativas em estado.

- [ ] **Tarefa 2.3: Geração Paralela e Exibição Individual**
  - **Ações:**
    - Ao submeter, executar requisições HTTP em paralelo para obter os dados de custo de cada Job selecionado.
    - Exibir os relatórios individualmente e empilhados na tela (sem somar horas de Jobs distintos).
  - **Critério de Aceitação:** Exibição clara e correta dos custos de cada Job selecionado individualmente.

---

## 💼 Slice 3: Atualizações no Detalhe do Job (Frontend)

- [ ] **Tarefa 3.1: Ajuste de Layout e Nova Seção**
  - **Ações:**
    - Modificar [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx) para remover o link/botão antigo de "Gerar Relatório de Custo".
    - Adicionar a seção visível inferior "Relatório de Presença e Horas Diárias" (Timesheet).
  - **Critério de Aceitação:** Interface do Job adaptada sem o botão de custos e com a nova seção.

- [ ] **Tarefa 3.2: Integração e Renderização do Timesheet**
  - **Ações:**
    - Chamar a API `GET /api/v1/reports/jobs/{jobId}/timesheet` na montagem do componente.
    - Renderizar a listagem contendo colaboradores, dias e horas trabalhadas de forma intuitiva e premium.
  - **Critério de Aceitação:** Timesheet sendo exibido dinamicamente com informações do banco.

- [ ] **Tarefa 3.3: Validação Final e Build**
  - **Ações:**
    - Executar `npm run build` na pasta frontend.
    - Validar o fluxo completo de emissão de folha, custos múltiplos e visualização do timesheet localmente.
  - **Critério de Aceitação:** Build concluído com sucesso e fluxos 100% validados.
