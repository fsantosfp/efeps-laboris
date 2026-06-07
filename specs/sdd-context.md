# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 3.1: Ajuste de Layout e Nova Seção (Slice 3)
* **Objetivo:** Modificar `JobDetailPage.jsx` para remover o botão antigo de "Gerar Relatório de Custo" e adicionar a nova seção "Relatório de Presença e Horas Diárias" (Timesheet).

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  - [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Compilação Limpa:** Certificar que a página de detalhes do job compile e renderize corretamente com a nova seção e sem o link de custo.
2. **Conventional Commit:** Commitar no formato `feat(reports): add timesheet layout section in JobDetailPage` antes de prosseguir.