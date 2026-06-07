# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.5: Testes Unitários do Novo Relatório (Slice 1)
* **Objetivo:** Escrever testes em `ReportServiceTest.java` para validar os cálculos de horas por funcionário e permissões de segurança. Executar `SPRING_PROFILES_ACTIVE=local mvn test` para certificar-se do sucesso de todos os testes do backend.

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  - [ReportServiceTest.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/test/java/br/com/effies/laboris/backend/domain/service/ReportServiceTest.java)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Compilação Limpa:** Certificar que os testes unitários compilem e executem com sucesso.
2. **Conventional Commit:** Commitar no formato `test(reports): add unit tests for calculateJobTimesheetReport` antes de prosseguir.