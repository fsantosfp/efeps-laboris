# Lista de Tarefas - Cálculo de Intervalos & Sistema de Deslocamento (Translado)

Este documento contém a decomposição das tarefas para a implementação das melhorias de intervalo e sistema de deslocamento seguindo as diretrizes do **Specification-Driven Development (SDD)**. Cada tarefa deve ser executada individualmente, testada e validada antes do commit correspondente.

---

## 💾 Slice 1: Estrutura de Banco de Dados e Entidades (Backend)

- [x] **Tarefa 1.1: Criar a tabela `displacements`**
  - **Ações:**
    - Criar o script SQL de migração [displacement_migration.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/displacement_migration.sql) com a criação da tabela.
    - Atualizar o script original [laboris_database.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/laboris_database.sql).
    - Executar o script SQL no container PostgreSQL ativo para aplicar as modificações.
  - **Critério de Aceitação:** Tabela criada no banco de dados e scripts atualizados.

- [x] **Tarefa 1.2: Criar a entidade JPA `Displacement.java`**
  - **Ações:**
    - Criar a classe [Displacement.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/entity/Displacement.java) mapeando a tabela de banco de dados com seus relacionamentos.
  - **Critério de Aceitação:** Entidade JPA mapeada corretamente e sem erros de compilação.

- [x] **Tarefa 1.3: Criar o repositório `DisplacementRepository.java`**
  - **Ações:**
    - Criar a interface [DisplacementRepository.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/repository/DisplacementRepository.java) com as consultas de deslocamento ativo e por período.
  - **Critério de Aceitação:** Repositório criado e compilável.

---

## 🛠️ Slice 2: Serviço e Lógica de Deslocamento (Backend)

- [x] **Tarefa 2.1: Implementar reverse geocoding no GeoService**
  - **Ações:**
    - Adicionar o método `reverseGeocode` no [GeoService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/GeoService.java) usando a API do Google Maps para traduzir latitude/longitude em endereço de texto.
  - **Critério de Aceitação:** Método criado, integrando com o SDK do Google.

- [x] **Tarefa 2.2: Criar os DTOs de Deslocamento**
  - **Ações:**
    - Criar as classes de DTO para receber coordenadas (`DisplacementRequestDto.java`) e para responder dados da viagem (`DisplacementResponseDto.java`).
  - **Critério de Aceitação:** Classes DTO criadas e estruturadas.

- [x] **Tarefa 2.3: Criar o serviço `DisplacementService`**
  - **Ações:**
    - Implementar a classe [DisplacementService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/DisplacementService.java) contendo os métodos `startDisplacement` (validando assignments >= 2, última batida OUT, sem viagem ativa, geocode do início) e `endDisplacement` (validando geolocalização perto do destino final).
  - **Critério de Aceitação:** Classe compilando normalmente com as validações de regras de negócio.

- [x] **Tarefa 2.4: Criar o controlador `DisplacementController`**
  - **Ações:**
    - Criar a classe [DisplacementController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/DisplacementController.java) expondo endpoints `/start`, `/end` e `/active`.
  - **Critério de Aceitação:** Endpoints expostos e acessíveis via HTTP.

- [x] **Tarefa 2.5: Adicionar validação de deslocamento no TimeEntryService**
  - **Ações:**
    - Modificar [TimeEntryService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/TimeEntryService.java) no método `create` para impedir o clock-in (`IN`) se houver um deslocamento ativo (`endTimestamp` nulo).
  - **Critério de Aceitação:** Bloqueio ativado e testes de ponto existentes passando.

---

## 📈 Slice 3: Cálculo de Horas, Intervalos e Relatórios (Backend)

- [x] **Tarefa 3.1: Adicionar propriedade no DTO do Relatório**
  - **Ações:**
    - Adicionar o campo `displacement` (String) em `DailyHoursDto` dentro de [JobTimesheetResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/JobTimesheetResponseDto.java).
  - **Critério de Aceitação:** DTO estendido compilável.

- [x] **Tarefa 3.2: Implementar lógica de cálculo de intervalos no mesmo job**
  - **Ações:**
    - Modificar a lógica de serviço de relatórios para que o cálculo de intervalos só considere transições de `OUT` e `IN` consecutivos ocorridos no mesmo Trabalho (Job), ignorando transições de trabalhos diferentes.
  - **Critério de Aceitação:** Cálculo de intervalos corrigido de acordo com a regra.

- [x] **Tarefa 3.3: Apropriar horas de deslocamento ao job de destino**
  - **Ações:**
    - Atualizar a apuração de horas no [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java) (custo e timesheet) e no [PayrollService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/PayrollService.java) (folha de pagamento) para somar os tempos de trânsito ao job de destino.
    - Mapear a propriedade `displacement` do DTO com o endereço de partida (`start_address`).
  - **Critério de Aceitação:** Custos, folha e timesheet refletindo a apropriação dos tempos de deslocamento.

- [x] **Tarefa 3.4: Testes Unitários de Deslocamento e Regras**
  - **Ações:**
    - Escrever a suíte de testes `DisplacementServiceTest.java` para cobrir regras de início, fim, geolocalização e bloqueios de ponto.
    - Rodar suíte completa do backend com `SPRING_PROFILES_ACTIVE=local mvn test`.
  - **Critério de Aceitação:** Suíte de testes passando com sucesso.

---

## 🖥️ Slice 4: Integração de Interface Web (Frontend)

- [x] **Tarefa 4.1: Adicionar coluna de Deslocamento no JobDetailPage**
  - **Ações:**
    - Modificar o componente [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx) para exibir a nova coluna "Deslocamento" na tabela do Timesheet, renderizando o endereço de partida (`row.displacement`) ou `-`.
  - **Critério de Aceitação:** Tabela atualizada exibindo o endereço ou `-` sem quebrar o layout.

---

## 📱 Slice 5: Fluxo de Trânsito no Aplicativo Mobile (Frontend)

- [x] **Tarefa 5.1: Condicionar visibilidade dos botões de deslocamento**
  - **Ações:**
    - Modificar [MainScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/MainScreen.js) para carregar as atribuições e exibir botões de deslocamento somente se o usuário tiver designação para 2 ou mais jobs.
  - **Critério de Aceitação:** Botões exibidos apenas para colaboradores multi-job.

- [x] **Tarefa 5.2: Ação de Iniciar Deslocamento no Mobile**
  - **Ações:**
    - Implementar chamada à API `POST /api/v1/displacements/start` capturando a posição GPS atual.
  - **Critério de Aceitação:** Início do deslocamento gravado e estado do app atualizado.

- [x] **Tarefa 5.3: Ação de Finalizar Deslocamento e Bater Ponto no Mobile**
  - **Ações:**
    - Adicionar modal/seletor para o usuário escolher o job de destino.
    - Chamar `POST /api/v1/displacements/end` e registrar o ponto `IN` para o respectivo Job.
  - **Critério de Aceitação:** Finalização e clock-in integrados com sucesso.

- [ ] **Tarefa 5.4: Validação da Build e Fluxo Mobile**
  - **Ações:**
    - Rodar build e validações do app React Native e atestar funcionamento com o emulador.
  - **Critério de Aceitação:** Build compilada com sucesso.
