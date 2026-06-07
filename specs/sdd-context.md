# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.3: Lógica de Serviço no ReportService (Slice 1)
* **Objetivo:** Implementar o método `calculateJobTimesheetReport(User manager, UUID jobId, Instant start, Instant end)` no `ReportService.java` para retornar o timesheet detalhado de um Job agrupando horas por colaborador e por dia.

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  - [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Compilação Limpa:** Certificar que as lógicas de agrupamento do Stream compilem corretamente.
2. **Conventional Commit:** Commitar no formato `feat(reports): implement calculateJobTimesheetReport in ReportService` antes de prosseguir.