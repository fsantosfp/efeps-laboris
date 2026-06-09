# Laboris
### Description


## Applications
### *Backend*
- Java 21
- Maven
- Postgres
- Docker

### *Frontend*
- Reactive
- Vite
### *Mobile*
- Reactive-native

## Routes
**USER LOGIN**
- **Endpoint:** POST */api/v1/auth/login*
- **Descrição:** Autentica um usuário (Dono, Gestor ou Funcionário) com email e senha.
- **Autenticação:** Pública
- **Payload:** (JSON)
    ```json
    {
        "email": "usuario@exemplo.com",
        "password": "senha_do_usuario"
    }
    ```
- **Responsta de Sucesso (200 OK):** Retorna um token de acesso (JWT) que será usado para autenticar as próximas requisições, junto com informações do usuário.
    ```json
    {
        "token": "um_token_jwt_muito_longo_e_seguro",
        "user": {
            "id": "uuid-do-usuario",
            "name": "Nome do Usuário",
            "role": "MANAGER"
        }
    }
    ```

---
**CRIAR NOVO TRABALHO**
- **Endpoint:** POST */api/v1/jobs*
- **Descrição:** Cadastra um novo trabalho para a empresa do gestor.
- **Autenticação:** Requer token de *MANAGER*
- **Payload:** (JSON)
    ```json
    {
        "address": "Rua das Flores, 123, São Paulo, SP",
        "client_name": "Contratante Exemplo",
        "budget": 5000.00,
        "billing_rate": 55.00,
        "start_date": "2025-09-15",
        "end_date": "2025-10-15",
        "latitude": -23.55052,
        "longitude": -46.63330
    }
    ```
- **Responsta de Sucesso (201 Created):** Retorna o objeto completo do trabalho que foi criado, incluindo o *id* gerado pelo sistema.
    ```json
    {
        "id":"uuid-do-trabalho-1",
        "address": "Rua das Flores, 123, São Paulo, SP",
        "client_name": "Contratante Exemplo",
        "budget": 5000.00,
        "billing_rate": 55.00,
        "start_date": "2025-09-15",
        "end_date": "2025-10-15",
        "latitude": -23.55052,
        "longitude": -46.63330
    }
    ```

---
**LISTAR TODOS OS TRABALHOS**
- **Endpoint:** GET */api/v1/jobs*
- **Descrição:** Retorna uma lista de todos os trabalhos da empresa do gestor.
- **Autenticação:** Requer token de *MANAGER*
- **Responsta de Sucesso (200 OK):** Retorna um array de objetos de trabalho.
    ```json
    [
        {
            "id": "uuid-do-trabalho-1",
            "address": "Rua das Flores, 123, São Paulo, SP",
            "status": "PENDING"
        },
        {
            "id": "uuid-do-trabalho-2",
            "address": "Avenida Paulista, 1000, São Paulo, SP",
            "status": "IN_PROGRESS"
        }
    ]
    ```

---
**OBTER DETALHES DE UM TRABALHO**
- **Endpoint:** GET */api/v1/jobs/*{ jobId }
- **Descrição:** Retorna os detalhes completos de um trabalho específico.
- **Autenticação:** Requer token de *MANAGER*
- **Responsta de Sucesso (200 OK):** Retorna um único objeto de trabalho com todos os seus detalhes.
    ```json
    {
        "id":"uuid-do-trabalho-1",
        "address": "Rua das Flores, 123, São Paulo, SP",
        "client_name": "Contratante Exemplo",
        "budget": 5000.00,
        "billing_rate": 55.00,
        "start_date": "2025-09-15",
        "end_date": "2025-10-15",
        "latitude": -23.55052,
        "longitude": -46.63330
    }
    ```

---
**CADASTRO DE FUNCIONÁRIO**
- **Endpoint:** POST */api/v1/employees*
- **Descrição:** Cadastra um novo funcionário na empresa do gestor logado e cria seu primeiro registro de salário.
- **Autenticação:** Requer token de *MANAGER*
- **Payload:** JSON
    ```json
    {
        "name": "Nome Completo do Novo Funcionário",
        "email": "funcionario.novo@email.com",
        "HourlyRate": 25.00,
        "effectiveDate": "2025-08-15"
    }
    ```
    > Ação do Backend:
    > - Verifica se o gestor autenticado pertence a uma empresa.
    > - Cria um novo registro na tabela users com o name, email, role = EMPLOYEE e associado à mesma company_id do gestor. O usuário é criado com um status "pendente".
    > - Cria o primeiro registro na tabela salary_history com o hourly_rate e a effectiveDate fornecidos.
    >- Dispara o e-mail de "Boas-vindas" para o email do novo funcionário com o link para ele definir sua senha e baixar o app.
- **Responsta de Sucesso (201 CREATED):** 
    ```json
    {
        "id": "uuid-do-novo-funcionario",
        "name": "Nome Completo do Novo Funcionário",
        "email": "funcionario.novo@email.com",
        "role": "EMPLOYEE",
        "status": "Pending" 
    }
    ```

---
**DESIGNAR FUNCIONÁRIOS PARA UM TRABALHO**
- **Endpoint:** POST */api/v1/jobs/*{ jobId }*/assignments*
- **Descrição:** Associa um ou mais funcionários a um trabalho específico.
- **Autenticação:** Requer token de *MANAGER*
- **Payload:** JSON
    ```json
    {
        "userIds": ["uuid-do-funcionario-1", "uuid-do-funcionario-2"]
    }
    ```
- **Responsta de Sucesso (204 No Content):** Apenas confirma que a operação foi bem-sucedida, sem retornar conteúdo.

---
**OBTER MEUS TRABALHOS DESIGNADOS**
- **Endpoint:** GET */api/v1/my-assignments
- **Descrição:** Retorna uma lista de trabalhos ativos e pendentes aos quais o funcionário autenticado está atualmente designado. O app mobile usará essa lista para comparar com a geolocalização do usuário.
- **Autenticação:** Requer token de *EMPLOYEE*
- **Responsta de Sucesso (200 OK):**
    ```json
    [
        {
            "id": "uuid-do-trabalho-1",
            "address": "Rua das Flores, 123, São Paulo, SP",
            "latitude": -23.55052,
            "longitude": -46.63330
        },
        {
            "id": "uuid-do-trabalho-2",
            "address": "Avenida Paulista, 1000, São Paulo, SP",
            "latitude": -23.5613,
            "longitude": -46.6565
        }
    ]
    ```

---
**REGISTRAR BATIDA DE PONTO**
- **Endpoint:** POST */api/v1/time-entries
- **Descrição:** Registra qualquer tipo de batida de ponto (entrada, início/fim de intervalo ou saída).
- **Autenticação:** Requer token de *EMPLOYEE*
- **Payload:** JSON
    ```json
    {
        "jobId": "uuid-do-trabalho-onde-ele-esta",
        "entryType": "CLOCK_IN",
        "latitude": -23.55048,
        "longitude": -46.63325,
        "isManual": false,
        "justification": null,
        "reportedTimestamp": "2025-08-12T08:05:00Z"
    }
    ```
    - **entryType:** CLOCK_IN, START_BREAK, END_BREAK, CLOCK_OUT
    - **justification:** Preenchido apenas se isManual for true
    - **reportedTimestamp:** Para batidas manuais (isManual: true), o corpo da requisição deve incluir a hora informada pelo usuário. Para batidas automáticas (isManual: false), o corpo da requisição não envia a hora. O servidor atribui o NOW() no momento em que salva.
- **Responsta de Sucesso (201 CREATED):**
    ```json
    {
        "jobId": "uuid-do-trabalho-onde-ele-esta",
        "entryType": "CLOCK_IN",
        "latitude": -23.55048,
        "longitude": -46.63325,
        "isManual": false,
        "justification": null
    }
    ```

---
**HISTÓRICO DE BATIDA DE PONTO**
- **Endpoint:** GET */api/v1/my-time-entries
- **Descrição:** Retorna a lista bruta de todas as batidas de ponto do funcionário autenticado, para fins de auditoria.
- **Autenticação:** Requer token de *EMPLOYEE*
- **Exemplo de uso:** *GET /api/v1/my-time-entries?startDate=2025-08-12&endDate=2025-08-12*
- **Responsta de Sucesso (200 OK):**
    ```json
    {
        [
            { "entryType": "CLOCK_IN", "timestamp": "2025-08-12T08:01:15Z", "isManual": false },
            { "entryType": "START_BREAK", "timestamp": "2025-08-12T12:05:30Z", "isManual": false },
            { "entryType": "END_BREAK", "timestamp": "2025-08-12T13:02:10Z", "isManual": true },
            { "entryType": "CLOCK_OUT", "timestamp": "2025-08-12T17:30:05Z", "isManual": false }
        ]
    }
    ```

---

**CONSULTAR MEU EXTRATO DE PAGAMENTO**
- **Endpoint:** GET */api/v1/my-payroll
- **Descrição:** Retorna o extrato de pagamento detalhado para o funcionário autenticado. O período é definido por query parameters.
- **Exemplo de Uso:** *GET /api/v1/my-payroll?startDate=2025-08-01&endDate=2025-08-10*
- **Autenticação:** Requer token de *EMPLOYEE*
- **Responsta de Sucesso (200 OK):**
    ```json
    {
        "period": {
            "start": "2025-08-01",
            "end": "2025-08-10"
        },
        "totalHours": 44.5,
        "totalAmount": 890.00,
        "dailyEntries": [
            {
            "date": "2025-08-08",
            "jobAddress": "Rua das Flores, 123",
            "hoursWorked": 8.0,
            "amountEarned": 160.00
            },
            {
            "date": "2025-08-09",
            "jobAddress": "Avenida Paulista, 1000",
            "hoursWorked": 8.5,
            "amountEarned": 170.00
            }
        ]
    }
    ```

---
**GERAR RELATÓRIO POR CUSTO DE SERVIÇO**
- **Endpoint:** GET */api/v1/reports/jobs/{jobId}
- **Descrição:** Gera o relatório de custos detalhado para um serviço específico, usando o valor de venda (faturamento). Permite filtrar por um período.
- **Exemplo de Uso:** *GET /api/v1/reports/jobs/{jobId}?startDate=2025-09-01&endDate=2025-09-30*
- **Autenticação:** Requer token de *MANAGER*
- **Responsta de Sucesso (200 OK):**
    ```json
    {
        "jobInfo": {
            "id": "uuid-do-trabalho-1",
            "address": "Rua das Flores, 123",
            "billingRate": 55.00
        },
        "period": {
            "start": "2025-09-01",
            "end": "2025-09-30"
        },
        "totalManHours": 160.0,
        "totalBillingAmount": 8800.00,
        "dailyBreakdown": [
            {
            "date": "2025-09-15",
            "employeesCount": 2,
            "manHours": 16.0,
            "dailyBillingAmount": 880.00
            }
        ]
    }
    ```

---
**GERAR RELTÓRIO DE FOLHA DE PAGAMENTO**
- **Endpoint:** GET */api/v1/reports/payroll
- **Descrição:** Gera a folha de pagamento de todos os funcionários para um determinado período, usando o valor/hora individual de cada um (custo).
- **Exemplo de Uso:** *GET /api/v1/reports/payroll?startDate=2025-08-01&endDate=2025-08-31*
- **Autenticação:** Requer token de *MANAGER*
- **Responsta de Sucesso (200 OK):**
    ```json
    {
        "period": {
            "start": "2025-08-01",
            "end": "2025-08-31"
        },
        "totalCompanyHours": 320.5,
        "totalPayrollAmount": 6410.00,
        "employeePayrolls": [
            {
            "employeeId": "uuid-do-funcionario-1",
            "employeeName": "João Silva",
            "totalHours": 160.0,
            "totalAmountToPay": 3200.00,
            "details": [
                {
                "date": "2025-08-15",
                "jobAddress": "Rua das Flores, 123",
                "hoursWorked": 8.0,
                "effectiveHourlyRate": 20.00,
                "amountEarned": 160.00
                }
            ]
            }
        ]
    }
    ```

---
**CRIAR CLIENTE**
- **Endpoint:** POST */api/v1/admin/companies
- **Descrição:** Cadastra uma nova empresa (cliente) na plataforma e cria seu usuário gestor principal.
- **Autenticação:** Requer token de *SAAS_OWNER*
- **Payload:** JSON
    ```json
    {
        "companyName": "Nova Construtora Exemplo",
        "managerName": "Nome do Gestor",
        "managerEmail": "gestor@novaconstrutora.com"
    }
    ```
- **Responsta de Sucesso (204 No Content):**
    > Ação do Backend: Cria a empresa, cria o usuário gestor com status "pendente" e dispara o e-mail de boas-vindas com o link para definir a senha.

---
**LISTAR CLIENTES**
- **Endpoint:** GET */api/v1/admin/companies
- **Descrição:** Lista todas as empresas/clientes cadastradas no SaaS.
- **Autenticação:** Requer token de *SAAS_OWNER*
- **Responsta de Sucesso (200 OK):**
    ```json
    [
        { "id": "uuid-da-empresa-1", "name": "Construtora A", "status": "Active" },
        { "id": "uuid-da-empresa-2", "name": "Reformas B", "status": "Inactive" }
    ]
    ```

---
**ATUALIZAR STATUS DE UM CLIENTES**
- **Endpoint:** PATCH */api/v1/admin/companies/{companyId}
- **Descrição:** Permite ativar ou desativar o acesso de um cliente.
- **Autenticação:** Requer token de *SAAS_OWNER*
- **Payload:** JSON
    ```json
    {
        "status": "Inactive"
    }
    ```
- **Responsta de Sucesso (200 OK):** Retorna o objeto da empresa com o status atualizado.
    ```json
    [
        { "id": "uuid-da-empresa-2", "name": "Reformas B", "status": "Inactive" }
    ]
    ```

---
**DEFINIR SENHA INICIAL**
- **Endpoint:** POST */api/v1/auth/set-initial-password
- **Descrição:** Recebe o token do link do e-mail e a nova senha escolhida pelo usuário.
- **Autenticação:** Pública (a segurança está no token).
- **Payload:** JSON
    ```json
    {
        "token": "TOKEN_SEGURO_RECEBIDO_NO_EMAIL",
        "newPassword": "a_senha_escolhida_pelo_usuario"
    }
    ```
    > Ação do Backend: Valida o token, define a senha para o usuário e ativa sua conta. Redireciona para a tela principal

---
**DEFINIR SENHA INICIAL**
- **Endpoint:** POST */api/v1/auth/set-initial-password
- **Descrição:** Recebe o token do link do e-mail e a nova senha escolhida pelo usuário.
- **Autenticação:** Pública (a segurança está no token).
- **Payload:** JSON
    ```json
    {
        "token": "TOKEN_SEGURO_RECEBIDO_NO_EMAIL",
        "newPassword": "a_senha_escolhida_pelo_usuario"
    }
    ```
    > Ação do Backend: Valida o token, define a senha para o usuário e ativa sua conta. Redireciona para a tela principal

---
**ALTERAR SENHA (LOGADO)**
- **Endpoint:** POST */api/v1/me/password
- **Descrição:** Permite que um usuário já autenticado altere sua própria senha.
- **Autenticação:** Requer token do usuário (qualquer perfil MANAGER, EMPLOYEE, SAAS_OWNER).
- **Payload:** JSON
    ```json
    {
        "currentPassword": "a_senha_antiga_dele",
        "newPassword": "a_nova_senha_que_ele_quer"
    }
    ```