# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.4: Endpoint no ReportController (Slice 1)
* **Objetivo:** Criar a rota `GET /api/v1/reports/jobs/{jobId}/timesheet` no `ReportController.java` recebendo parâmetros de período opcionais.

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  - [ReportController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/ReportController.java)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Compilação Limpa:** Certificar que o endpoint do controlador compile corretamente e mapeie a rota e parâmetros esperados.
2. **Conventional Commit:** Commitar no formato `feat(reports): add timesheet endpoint in ReportController` antes de prosseguir.