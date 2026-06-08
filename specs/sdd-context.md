# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.3: Implementar o cálculo cronológico no ReportService (Slice 1)
* **Objetivo:** Criar o método calculateEmployeeJourneyReport no ReportService.java para obter a lista de funcionários ativos, buscar suas batidas e deslocamentos, mesclar os dados de forma cronológica diária em eventos WORK, BREAK e DISPLACEMENT, ordenar e retornar os dados.

---

## 📂 Escopo dos Arquivos
* **Verificar:**
  - [ReportService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ReportService.java)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Build Sucesso:** Assegurar que o aplicativo móvel React Native compile e rode perfeitamente.
2. **Conventional Commit:** Commitar no formato `feat(mobile): validate mobile build` antes de prosseguir.