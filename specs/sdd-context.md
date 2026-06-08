# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.5: Testes Unitários de Backend (Slice 1)
* **Objetivo:** Escrever testes unitários em ReportServiceTest.java cobrando a geração de eventos da linha do tempo, e rodar a suíte inteira.

---

## 📂 Escopo dos Arquivos
* **Verificar:**
  - [ReportServiceTest.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/test/java/br/com/effies/laboris/backend/domain/service/ReportServiceTest.java)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Build Sucesso:** Assegurar que o aplicativo móvel React Native compile e rode perfeitamente.
2. **Conventional Commit:** Commitar no formato `feat(mobile): validate mobile build` antes de prosseguir.