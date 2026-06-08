# Lista de Tarefas - Relatório de Jornada e Deslocamento por Funcionário

Este documento contém a decomposição das tarefas para a implementação do novo Relatório de Jornada e Deslocamento por Funcionário seguindo as diretrizes do **Specification-Driven Development (SDD)**. Cada tarefa deve ser executada individualmente, testada e validada antes do commit correspondente.

---

## 💾 Slice 1: Estrutura de DTOs e Endpoints do Backend

- [x] **Tarefa 1.1: Criar os DTOs do Relatório**
  - **Ações:**
    - Criar [JourneyEventDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/JourneyEventDto.java) para representar eventos da linha do tempo.
    - Criar [EmployeeJourneyResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/EmployeeJourneyResponseDto.java) para encapsular a lista de eventos de cada funcionário.
  - **Critério de Aceitação:** Classes criadas, compilando sem erros.

- [ ] **Tarefa 1.2: Injetar UserRepository no ReportService**
  - **Ações:**
    - Modificar o construtor de [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java) para injetar e usar a interface `UserRepository`.
    - Atualizar a instanciação no arquivo de testes correspondente.
  - **Critério de Aceitação:** Projeto compilando perfeitamente após a injeção.

- [ ] **Tarefa 1.3: Implementar o cálculo cronológico no ReportService**
  - **Ações:**
    - Criar o método `calculateEmployeeJourneyReport` no [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java) para obter a lista de funcionários ativos, buscar suas batidas e deslocamentos, mesclar os dados de forma cronológica diária em eventos `WORK`, `BREAK` e `DISPLACEMENT`, ordenar e retornar os dados.
  - **Critério de Aceitação:** Lógica implementada e sem erros de execução.

- [ ] **Tarefa 1.4: Adicionar o endpoint no ReportController**
  - **Ações:**
    - Adicionar a rota `GET /api/v1/reports/employee-journey` em [ReportController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/ReportController.java).
  - **Critério de Aceitação:** Endpoint exposto com segurança (restrito ao perfil `MANAGER`).

- [ ] **Tarefa 1.5: Testes Unitários de Backend**
  - **Ações:**
    - Escrever testes unitários em [ReportServiceTest.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/test/java/br/com/effies/laboris/backend/domain/service/ReportServiceTest.java) cobrando a geração de eventos da linha do tempo.
    - Executar os testes usando `SPRING_PROFILES_ACTIVE=local mvn test`.
  - **Critério de Aceitação:** Todos os testes passando com sucesso.

---

## 🖥️ Slice 2: Interface Web - Aba de Jornadas e Timeline

- [ ] **Tarefa 2.1: Carregar lista de funcionários no frontend**
  - **Ações:**
    - Atualizar o componente [ReportsPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/ReportsPage.jsx) para consultar a lista completa de funcionários da empresa e disponibilizá-los como opções de filtro.
  - **Critério de Aceitação:** Funcionários carregados e armazenados no estado da página.

- [ ] **Tarefa 2.2: Adicionar a nova aba "Jornada de Funcionários"**
  - **Ações:**
    - Modificar a barra de abas e renderizar o novo formulário de filtro contendo multiselect de funcionários, data de início e de fim.
  - **Critério de Aceitação:** Abas alternando suavemente e sem erros de layout.

- [ ] **Tarefa 2.3: Implementar a chamada e renderização da Timeline**
  - **Ações:**
    - Invocar o novo endpoint HTTP e renderizar os resultados agrupados por funcionário em formato de linha do tempo vertical ou blocos cronológicos premium.
  - **Critério de Aceitação:** Jornadas exibidas de forma clara e visual.

- [ ] **Tarefa 2.4: Estilizar a Timeline no CSS**
  - **Ações:**
    - Criar estilos premium em [ReportsPage.css](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/ReportsPage.css) com badges de cores harmoniosas (azul/teal para trabalho, cinza para intervalo e laranja/bronze para deslocamentos) e linhas conectoras de linha do tempo.
  - **Critério de Aceitação:** Layout fluído e alinhado aos padrões estéticos de alto nível estabelecidos.
