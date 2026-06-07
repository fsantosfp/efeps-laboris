# Lista de Tarefas - Aprimoramento do Gerenciamento de Trabalhos (Jobs)

Este documento contém a decomposição das tarefas para a implementação das melhorias de Jobs seguindo os princípios de **Spec-Driven Development (SDD)**. Cada tarefa deve ser realizada de forma incremental, testada localmente e revisada antes de prosseguir.

---

## 🗄️ Slice 1: Banco de Dados & Migração

- [x] **Tarefa 1.1: Script de Migração do Banco de Dados**
  - **Ações:**
    - Executar o comando SQL para adicionar as novas colunas `responsible_name` (VARCHAR 255), `responsible_phone` (VARCHAR 50) e `responsible_email` (VARCHAR 255) na tabela `jobs`.
    - Executar o comando para definir valores padrão para os jobs existentes e atualizar a coluna `billing_rate` para `NOT NULL`.
    - Atualizar a estrutura original da tabela `jobs` no script [laboris_database.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/laboris_database.sql).
  - **Critério de Aceitação:** As tabelas estão atualizadas e o banco de dados reflete o novo esquema.

---

## ☕ Slice 2: Camada de Backend (Java / Spring Boot)

- [x] **Tarefa 2.1: Atualização da Entidade Job**
  - **Ações:**
    - Modificar [Job.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/entity/Job.java) para incluir as colunas `responsibleName`, `responsiblePhone`, `responsibleEmail` mapeadas.
    - Alterar a coluna `billingRate` para `@Column(name = "billing_rate", nullable = false)`.
  - **Critério de Aceitação:** O código compila com a nova estrutura da entidade.

- [x] **Tarefa 2.2: Atualização do CreateJobRequestDto e Validações**
  - **Ações:**
    - Modificar [CreateJobRequestDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/request/CreateJobRequestDto.java) para adicionar `latitude` e `longitude` (como Double) com `@NotNull`.
    - Adicionar `@NotNull` e `@Positive` em `billingRate` e `budget`.
    - Adicionar `responsibleName`, `responsiblePhone` com `@NotBlank` and `responsibleEmail` com `@Email` (opcional).
  - **Critério de Aceitação:** Os testes do backend validam as restrições obrigatórias.

- [x] **Tarefa 2.3: Atualização do UpdateJobRequestDto**
  - **Ações:**
    - Modificar [UpdateJobRequestDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/request/UpdateJobRequestDto.java) para incluir todos os campos editáveis: `billingRate`, `budget`, `startDate`, `endDate`, `responsibleName`, `responsiblePhone`, `responsibleEmail`.
    - Garantir que os campos obrigatórios tenham suas respectivas anotações de validação (`@NotNull`, `@NotBlank`, etc.).
  - **Critério de Aceitação:** O DTO de atualização está pronto para receber todas as propriedades de edição.

- [x] **Tarefa 2.4: Atualização do JobResponseDto**
  - **Ações:**
    - Modificar [JobResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/JobResponseDto.java) para incluir os campos do responsável e mapear os valores a partir da entidade `Job`.
  - **Critério de Aceitação:** Os detalhes retornados pela API contêm os novos campos do responsável.

- [x] **Tarefa 2.5: Atualização das Regras de Serviço no JobService**
  - **Ações:**
    - Atualizar o método `create` em [JobService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/JobService.java) para salvar a latitude/longitude do DTO e os dados do responsável. Validar que as coordenadas não são nulas.
    - Renomear/expandir o método `updateStatus` para `update` geral, aplicando todos os dados do `UpdateJobRequestDto` no Job encontrado antes de salvá-lo.
  - **Critério de Aceitação:** Criação e edição persistem os novos campos de forma consistente.

- [x] **Tarefa 2.6: Testes Automatizados do Backend**
  - **Ações:**
    - Adaptar instâncias de `new Job()` nos testes unitários e de integração se necessário.
    - Escrever testes unitários específicos em `JobServiceTest.java` (ou equivalente) validando as novas regras de criação e atualização.
  - **Critério de Aceitação:** Toda a suíte de testes (`mvn test` com profile local) executa com 100% de sucesso.

---

## 💻 Slice 3: Camada de Frontend (React)

- [x] **Tarefa 3.1: Obtenção de Coordenadas no PlacesAutocompleteInput**
  - **Ações:**
    - Modificar [PlacesAutocompleteInput.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/components/PlacesAutocompleteInput.jsx) para buscar o campo `location` na API de Places e passar lat/lng na callback `onPlaceSelect`.
  - **Critério de Aceitação:** Ao selecionar o endereço, as coordenadas são corretamente passadas à página de criação.

- [x] **Tarefa 3.2: Formulário de Criação de Trabalhos**
  - **Ações:**
    - Atualizar [CreateJobPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/CreateJobPage.jsx) para corrigir a chave `billinRate` para `billingRate`.
    - Capturar e passar `latitude` e `longitude` reais a partir de `onPlaceSelect`.
    - Adicionar campos visuais para dados do responsável (Nome, Telefone, E-mail).
    - Enviar as propriedades atualizadas ao backend.
  - **Critério de Aceitação:** Criação de novos trabalhos funcionando perfeitamente com todas as validações de campos obrigatórios.

- [x] **Tarefa 3.3: Exibição dos Detalhes Completos**
  - **Ações:**
    - Modificar [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx) para renderizar a data final estimada (`endDate`), latitude, longitude e todos os dados do responsável.
  - **Critério de Aceitação:** A tela exibe todas as informações do trabalho de forma estruturada.

- [x] **Tarefa 3.4: Implementação de Edição no Frontend**
  - **Ações:**
    - Adicionar recurso de edição em [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx) (ex: toggle de modo de edição ou formulário inline) permitindo alterar orçamento, taxa, datas e responsável.
    - Chamar o endpoint `PATCH /api/v1/jobs/{id}` e recarregar a visualização ao salvar.
  - **Critério de Aceitação:** O gestor pode alterar e atualizar as propriedades do trabalho na hora.

- [x] **Tarefa 3.5: Confirmação Customizada de Deleção**
  - **Ações:**
    - Criar um diálogo modal moderno em [JobDetailPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/JobDetailPage.jsx) solicitando que o usuário digite o endereço (`address`) do trabalho antes de habilitar e executar o clique do botão de exclusão.
  - **Critério de Aceitação:** Exclusão segura e protegida contra deleções acidentais.
