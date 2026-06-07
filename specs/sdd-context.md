# SDD - Contexto Operacional da Tarefa Atual

- Nunca executar todas as tarefas de um slice de uma vez. Sempre execute de uma em uma tarefa, testando e validando localmente cada uma.
- sempre comitar o projeto antes de iniciar uma tarefa utilizando conventional commits
- sempre que preciso crie ou renome as branches de forma que faça sentido para a tarefa em questão e faça um pull request para a branch develop mantendo a coerencia e isolando tarefas que possam impactar umas as outras.
- Sempre obdeça o plano e as tasks definida nele, em caso de duvidas sobre a ordem ou prioridade das tarefas, pergunte.
- Sempre atualize as tasks no documento

---

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.1: Criar a tabela displacements (Slice 1)
* **Objetivo:** Criar o script SQL de migração `displacement_migration.sql` e atualizar o script `laboris_database.sql` para suportar a tabela `displacements`. Executar o script SQL no container de banco de dados ativo.

---

## 📂 Escopo dos Arquivos
* **Criar:**
  - [displacement_migration.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/displacement_migration.sql)
* **Modificar:**
  - [laboris_database.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/laboris_database.sql)

---

## 🛡️ Diretrizes de Qualidade Mandatórias
1. **Compilação Limpa:** Certificar que os scripts SQL estejam sintaticamente corretos para PostgreSQL.
2. **Conventional Commit:** Commitar no formato `feat(db): create displacements table migration and schema` antes de prosseguir.