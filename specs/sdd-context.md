# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 2.1: Nova Página de Relatórios e Rota (Slice 2)
* **Objetivo:** Criar a página `ReportsPage.jsx`, configurar a rota `/reports` no `App.jsx` (removendo a página `PayrollReportPage.jsx` antiga e obsoleta) e desenhar um layout de Abas (Tabs) moderno para alternar entre "Folha de Pagamento" e "Custos de Serviços".

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  - [App.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/App.jsx)
* **Criar:**
  - [ReportsPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/ReportsPage.jsx)
* **Deletar:**
  - [PayrollReportPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/PayrollReportPage.jsx)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Design Moderno:** Utilizar o sistema de design com abas modernas, estilização dark mode e glassmorphism premium.
2. **Conventional Commit:** Commitar no formato `feat(reports): add ReportsPage component and configure route in App` antes de prosseguir.