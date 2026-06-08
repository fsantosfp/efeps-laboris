# Lista de Tarefas - Exibição do Total de Intervalo no Histórico Mobile

Este documento contém a decomposição das tarefas para a exibição do tempo total de intervalo (break) na tela de histórico de ponto do aplicativo móvel, seguindo as diretrizes do **Specification-Driven Development (SDD)**.

---

## 💾 Slice 1: Cálculo e Exibição do Total de Intervalo no Histórico Mobile

- [ ] **Tarefa 1.1: Criar a função utilitária `calculateBreakHours`**
  - **Ações:**
    - Criar o arquivo [calculateBreakHours.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/utils/calculateBreakHours.js).
    - Implementar a lógica para ordenar as batidas, rastrear pares consecutivos de `OUT` ➔ `IN` que ocorrem no mesmo dia do calendário e somar suas durações.
  - **Critério de Aceitação:** Função criada, retornando as horas em formato decimal.

- [ ] **Tarefa 1.2: Atualizar a tela de Histórico (HistoryScreen)**
  - **Ações:**
    - Modificar [HistoryScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/HistoryScreen.js) para importar e utilizar `calculateBreakHours` no método `fetchHistory`.
    - Definir o estado `totalBreakHours`.
    - Atualizar o componente de layout `summaryContainer` para exibir em duas colunas: "Total Trabalhado" e "Total Intervalo".
  - **Critério de Aceitação:** Layout renderizado corretamente com as duas informações.
