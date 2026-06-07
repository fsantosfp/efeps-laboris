# Plano de Implementação - Funcionalidade de Gestão de Funcionários

Este plano descreve a especificação e o design para a funcionalidade de **Gestão de Funcionários** (Employee Management) no sistema Laboris. Permite que Gestores (`MANAGER`) gerenciem as contas de funcionários pertencentes à mesma empresa.

---

## 🎯 Alinhamento de Escopo e Regras (Product Discovery)

### Regras de Negócio
1. **Listagem de Funcionários:** O gestor deve visualizar uma listagem contendo o Nome, E-mail, a Taxa Horária Atual e a Data de Vigência correspondente de todos os funcionários ativos da empresa.
2. **Filtro de Inativos:** Por padrão, a listagem exibe apenas funcionários ativos. Um botão de alternância (toggle) permitirá exibir funcionários inativos.
3. **Inativação de Funcionários:** Um funcionário pode ser inativado (DELETE). Isso altera seu `status` para `INACTIVE`. Funcionários inativos não podem registrar ponto ou acessar o aplicativo.
4. **Cadastro de Funcionário:** O cadastro será feito via Modal na listagem. Dados necessários: Nome, E-mail, Taxa Horária Inicial (maior que zero) e Data de Vigência Inicial. A criação dispara o fluxo de senha temporária e e-mail de boas-vindas.
5. **Histórico Salarial:** A alteração de salário consiste em anexar uma nova taxa horária com data de vigência ao histórico. O sistema mantém todos os registros anteriores salvos para manter a consistência dos relatórios de folha de pagamento antigos.

### Fluxos com User Stories (Gherkin)

#### Cenário 1: Gestor lista funcionários ativos da sua empresa
```gherkin
Funcionalidade: Listagem de Funcionários
  Como um Gestor autenticado
  Quero visualizar a listagem de funcionários ativos da minha empresa
  Para acompanhar quem está na equipe e suas taxas horárias atuais

  Cenário: Listagem de funcionários ativos carregada com sucesso
    Dado que estou autenticado como Gestor
    Quando acesso a página "/employees"
    Então o sistema carrega e exibe a lista de funcionários ativos da minha empresa
    E cada linha exibe o nome, e-mail, taxa horária atual e data de vigência do salário
```

#### Cenário 2: Cadastro de novo funcionário com sucesso
```gherkin
Funcionalidade: Cadastro de Funcionários
  Como um Gestor autenticado
  Quero cadastrar um novo funcionário na minha empresa
  Para que ele possa acessar o sistema e registrar suas horas

  Cenário: Cadastro realizado com sucesso
    Dado que estou na página de listagem de funcionários
    Quando clico em "Cadastrar Funcionário"
    E insiro os dados válidos: nome "Felipe Silva", e-mail "felipe@company.com", taxa "25.00" e vigência "2026-06-01"
    E confirmo o cadastro
    Então o sistema salva o funcionário como ativo com a taxa e vigência fornecidas
    E dispara o e-mail de boas-vindas com a senha temporária
    E atualiza a listagem de funcionários ativos exibindo o novo membro
```

#### Cenário 3: Atualização do histórico salarial do funcionário
```gherkin
Funcionalidade: Histórico e Alteração Salarial
  Como um Gestor autenticado
  Quero atualizar a taxa horária de um funcionário para uma vigência específica
  Para que os futuros registros de ponto calculem o custo correto

  Cenário: Adição de nova taxa horária no histórico
    Dado que estou visualizando o histórico salarial de um funcionário
    Quando insiro uma nova taxa de "30.00" com vigência em "2026-07-01"
    E confirmo a alteração
    Então o sistema salva o novo registro no histórico
    E atualiza a visualização do histórico exibindo a nova taxa no topo da lista
```

---

## 📂 Proposed Changes

### Backend Layer (Java / Spring Boot)

#### [NEW] [CreateSalaryHistoryRequestDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/request/CreateSalaryHistoryRequestDto.java)
- DTO de entrada para novas taxas de salário.
- Atributos:
  - `@NotNull @Positive BigDecimal hourlyRate`
  - `@NotNull LocalDate effectiveDate`

#### [NEW] [SalaryHistoryResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/SalaryHistoryResponseDto.java)
- DTO de retorno de histórico salarial.
- Atributos: `UUID id`, `BigDecimal hourlyRate`, `LocalDate effectiveDate`, `Instant createdAt`.

#### [MODIFY] [EmployeeService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/EmployeeService.java)
- Adicionar o método `findSalariesByEmployee(UUID employeeId, User manager)` retornando a lista de `SalaryHistory` ordenada por data de vigência decrescente.
- Adicionar o método `addSalaryHistory(UUID employeeId, CreateSalaryHistoryRequestDto request, User manager)` que cria e persiste uma nova vigência salarial.

#### [MODIFY] [EmployeeController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/EmployeeController.java)
- Adicionar rotas:
  - `GET /api/v1/employees/{employeeId}/salaries`
  - `POST /api/v1/employees/{employeeId}/salaries`

---

### Frontend Layer (React Web)

#### [MODIFY] [Layout.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/components/Layout.jsx)
- Adicionar link de navegação "Equipe" ligando para a rota `/employees`.

#### [MODIFY] [App.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/App.jsx)
- Registrar rota `/employees` ligada a `EmployeesPage`.

#### [NEW] [EmployeesPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/EmployeesPage.jsx)
- Página com visual glassmorphic contendo:
  - Tabela responsiva de funcionários.
  - Filtro toggle "Exibir Inativos".
  - Botão de cadastro abrindo modal.
  - Botão de histórico de salário abrindo modal com timeline.
  - Botão de inativação com diálogo de confirmação.

#### [NEW] [EmployeesPage.css](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/EmployeesPage.css)
- Estilização premium e harmoniosa para tabelas, modais, timelines e botões.

---

## 🛡️ Verification Plan

### Automated Tests
- Testes unitários para busca e inserção de salários em `EmployeeServiceTest.java`.
- Testes MockMvc no backend para verificar os novos caminhos do controller.

### Manual Verification
- Cadastrar funcionário e confirmar logs de e-mail mock.
- Inativar e checar se o filtro toggle funciona.
- Inserir novas vigências salariais e confirmar na timeline do modal.
