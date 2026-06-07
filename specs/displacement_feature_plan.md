# Plano de Implementação - Cálculo de Intervalos & Sistema de Deslocamento (Translado)

Este plano descreve o design e as alterações necessárias para implementar o cálculo automático de intervalos (break time) e o suporte a deslocamentos pagos entre trabalhos (translado) com geolocalização no destino final, atendendo a todas as regras de negócio especificadas.

---

## Regras de Negócio Importantes para Homologação

1. **Cálculo de Intervalo (Break Time):** Será computado no backend calculando o tempo decorrido entre uma batida de `OUT` e a próxima de `IN` do funcionário no mesmo dia. Esse intervalo só será considerado se ambas as batidas (saída e entrada consecutivas) ocorrerem no mesmo Trabalho (Job). A transição de um trabalho para outro diferente não conta como intervalo. O dado será exibido na central de relatórios Web e no histórico do aplicativo Mobile.
2. **Opção C para Deslocamentos (Transit Time):** Adicionaremos um fluxo de transição ("Em deslocamento") controlado pelo aplicativo Mobile e persistido em uma nova tabela de banco de dados (`displacements`).
3. **Visibilidade do Botão:** O botão de iniciar deslocamento no Mobile só aparece se o usuário tiver **2 ou mais trabalhos** (jobs) designados a ele.
4. **Apropriação de Horas:** O tempo gasto em trânsito será adicionado às horas trabalhadas do **trabalho de destino** (no qual ele fizer o clock-in ao chegar).
5. **Validação de Fluxo:**
   - Não é permitido iniciar deslocamento sem estar em estado de `OUT` (fora do trabalho).
   - Não é permitido bater ponto de `IN` (entrada) se houver um deslocamento em andamento. Ele deve primeiro finalizar o deslocamento.
6. **Validação Geográfica:** O início da viagem pode ser registrado em qualquer local, mas a finalização exige que o colaborador esteja no perímetro geográfico do trabalho de destino (tolerância de distância `< 0.1` graus de latitude/longitude). A geolocalização será capturada e persistida em ambos os momentos.
7. **Coluna de Deslocamento no Relatório:** A central de relatórios exibirá o endereço de início do deslocamento para a respectiva diária (ou `null`/vazio se não houve deslocamento).

---

## Proposed Changes

### 1. Banco de Dados (PostgreSQL)
- Criar a tabela `displacements` e registrar a migração em [displacement_migration.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/displacement_migration.sql).
- Atualizar o script [laboris_database.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/laboris_database.sql).

### 2. Backend (Java / Spring Boot)
- Criar a entidade JPA [Displacement.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/entity/Displacement.java).
- Criar o repositório [DisplacementRepository.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/repository/DisplacementRepository.java).
- Adicionar reverse geocoding em [GeoService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/GeoService.java).
- Criar o serviço [DisplacementService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/DisplacementService.java).
- Criar o controlador [DisplacementController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/DisplacementController.java).
- Bloquear clock-in se houver deslocamento ativo em [TimeEntryService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/TimeEntryService.java).
- Mapear o campo `displacement` em [JobTimesheetResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/JobTimesheetResponseDto.java).
- Somar horas de deslocamento e computar no relatório em [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java) e no [PayrollService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/PayrollService.java).

### 3. Frontend (Web & Mobile)
- Web: Adicionar a coluna de deslocamento na tabela de presença no [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx).
- Mobile: Adicionar a lógica de deslocamento em [MainScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/MainScreen.js).

---

## Verification Plan

### Automated Tests
- Testes unitários no `DisplacementServiceTest.java`.
- Rodar suíte completa do backend:
  ```bash
  SPRING_PROFILES_ACTIVE=local "/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
  ```

### Manual Verification
- Validar botões de deslocamento baseados em atribuições de jobs no Mobile.
- Registrar deslocamento e conferir as coordenadas no banco.
- Finalizar deslocamento e validar integração do clock-in.
- Validar coluna de deslocamento no timesheet da Web.
