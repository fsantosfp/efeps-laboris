# Lista de Tarefas - Funcionalidade de Gestão de Funcionários

Este documento contém a decomposição das tarefas para a implementação da feature de gestão de funcionários seguindo os princípios de **Spec-Driven Development (SDD)**. Cada tarefa deve ser realizada de forma incremental, testada localmente e revisada antes de prosseguir.

---

## 🛠️ Slice 1: Camada de Backend (API de Histórico Salarial)

- [x] **Tarefa 1.1: Criação dos DTOs de Histórico Salarial**
  - **Ações:**
    - Criar `CreateSalaryHistoryRequestDto.java` em `br.com.effies.laboris.backend.presentation.dto.request`. Validar que `hourlyRate` é positivo e não nulo, e `effectiveDate` não é nulo.
    - Criar `SalaryHistoryResponseDto.java` em `br.com.effies.laboris.backend.presentation.dto.response` contendo `id`, `hourlyRate`, `effectiveDate` e `createdAt`.
  - **Critério de Aceitação:** O projeto compila sem erros.

- [x] **Tarefa 1.2: Métodos de Serviço no EmployeeService**
  - **Ações:**
    - Implementar o método `findSalariesByEmployee(UUID employeeId, User manager)` em `EmployeeService.java`. Deve validar se o funcionário pertence à empresa do gestor e retornar todas as entradas do histórico salarial do funcionário ordenadas por `effectiveDate` decrescente.
    - Implementar o método `addSalaryHistory(UUID employeeId, CreateSalaryHistoryRequestDto request, User manager)` em `EmployeeService.java`. Deve salvar um novo registro de `SalaryHistory` associado ao funcionário.
  - **Critério de Aceitação:** Métodos de serviços prontos e logicamente protegidos contra acesso cruzado de empresas.

- [x] **Tarefa 1.3: Endpoints no EmployeeController**
  - **Ações:**
    - Adicionar a rota `GET /api/v1/employees/{employeeId}/salaries` em `EmployeeController.java`.
    - Adicionar a rota `POST /api/v1/employees/{employeeId}/salaries` em `EmployeeController.java`.
    - Ambas as rotas devem exigir o perfil `MANAGER`.
  - **Critério de Aceitação:** Endpoints expostos e mapeados de acordo com os DTOs.

- [x] **Tarefa 1.4: Testes de Unidade e Integração do Backend**
  - **Ações:**
    - Escrever testes unitários em `EmployeeServiceTest.java` validando buscas, inserções e restrições de empresa para os salários.
    - Criar testes MockMvc em `EmployeeControllerTest.java` validando a chamada dos endpoints `GET` e `POST` de histórico de salários.
  - **Critério de Aceitação:** Toda a suíte de testes do backend (`mvn test`) executa com 100% de sucesso.

---

## 🚪 Slice 2: Interface e Listagem (Frontend)

- [ ] **Tarefa 2.1: Navegação e Registro de Rota**
  - **Ações:**
    - Adicionar o link de navegação "Equipe" no menu do [Layout.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/components/Layout.jsx).
    - Registrar a rota protegida `/employees` apontando para `EmployeesPage` no [App.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/App.jsx).
  - **Critério de Aceitação:** O menu exibe o link e a rota é acessível quando autenticado.

- [ ] **Tarefa 2.2: Tabela Principal e Estilização**
  - **Ações:**
    - Criar o arquivo `EmployeesPage.jsx` e `EmployeesPage.css` em `frontend/src/pages/`.
    - Exibir a tabela contendo Nome, E-mail, Taxa Horária Atual e Data de Vigência de todos os funcionários ativos da empresa.
    - Adicionar um filtro de alternância (toggle) "Mostrar Inativos" que inclui/exclui registros com `status = INACTIVE` da listagem.
  - **Critério de Aceitação:** A tabela renderiza corretamente com visual premium, listando os funcionários ativos.

- [ ] **Tarefa 2.3: Ação de Inativação**
  - **Ações:**
    - Adicionar o botão "Inativar" em cada linha de funcionário ativo.
    - Exibir um diálogo de confirmação moderno antes de disparar o `DELETE /api/v1/employees/{id}`.
    - Após o sucesso, recarregar a listagem.
  - **Critério de Aceitação:** Um funcionário pode ser inativado com sucesso.

---

## ✏️ Slice 3: Cadastro e Histórico Salarial (Frontend)

- [ ] **Tarefa 3.1: Modal de Cadastro de Funcionário**
  - **Ações:**
    - Criar um modal na página de listagem contendo formulário com os campos: Nome, E-mail, Taxa Horária Inicial e Data de Vigência Inicial.
    - Integrar com o endpoint `POST /api/v1/employees`.
    - Adicionar tratamento de erros e feedbacks visuais em caso de e-mail duplicado ou campos inválidos.
  - **Critério de Aceitação:** Gestores conseguem cadastrar novos funcionários direto do modal e a tabela atualiza após o sucesso.

- [ ] **Tarefa 3.2: Modal de Histórico Salarial**
  - **Ações:**
    - Criar um modal contendo a timeline/tabela do histórico de salários do funcionário selecionado (carregando de `GET /api/v1/employees/{id}/salaries`).
    - Permitir a adição de uma nova vigência salarial (Taxa Horária e Data de Vigência) enviando os dados em um formulário integrado com `POST /api/v1/employees/{id}/salaries`.
    - Atualizar a timeline dinamicamente ao salvar a nova taxa.
  - **Critério de Aceitação:** O histórico é carregado corretamente e novas taxas são adicionadas com sucesso.
