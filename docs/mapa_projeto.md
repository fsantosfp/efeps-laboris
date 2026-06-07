# Mapa do Projeto - Laboris

Laboris é um sistema SaaS multitenant projetado para gerenciar alocação de trabalhadores, controle de ponto com geolocalização por GPS, relatórios de custo de serviços e processamento de folha de pagamento. O sistema é voltado para empresas prestadoras de serviço (como construtoras, empresas de limpeza, reformas, etc.) onde o gestor aloca funcionários para locais de trabalho e os funcionários batem ponto pelo aplicativo móvel.

---

## 🛠️ Arquitetura do Sistema e Estrutura do Projeto

O repositório é composto por três frentes de desenvolvimento:

1. **`backend` (Java 21 + Spring Boot 3.5.4)**
   - Gerencia autenticação (JWT), regras de negócio de folha de pagamento (`Payroll`), horas trabalhadas (`TimeEntry`), e relatórios.
   - Banco de dados PostgreSQL configurado em contêiner Docker.
   - Integração com Google Maps para cálculo/validação geográfica das coordenadas dos pontos de batida.
2. **`frontend` (React 19 + Vite + Axios)**
   - Painel web do Administrador do SaaS (`SAAS_OWNER`) e dos Gestores (`MANAGER`).
   - Gerenciamento de clientes, criação de trabalhos (jobs), alocação de equipe e visualização de relatórios financeiros e de folhas de pagamento.
3. **`mobile` (React Native 0.80.2)**
   - Aplicativo para uso dos funcionários (`EMPLOYEE`).
   - Apresenta os trabalhos aos quais ele foi alocado, detecta a geolocalização e permite realizar a batida de ponto (entrada/saída).

---

## 🚀 Status das Features

Abaixo está o mapa detalhado de progresso das features de cada módulo:

### 1. Backend
- **Prontas (Implementadas):**
  - **Autenticação:** Login JWT (`/api/v1/auth/login`) e alteração de senha autenticada (`/api/v1/me/password`).
  - **Gestão de Trabalhos (Jobs):** Criação, listagem e detalhamento de trabalhos por parte de administradores (`MANAGER`).
  - **Cadastro de Funcionários:** Cadastro de novos funcionários com envio de convite por e-mail e criação de histórico salarial inicial (`/api/v1/employees`).
  - **Alocação de Funcionários:** Relação de associação de funcionários com jobs (`/api/v1/jobs/{jobId}/assignments`).
  - **Ponto (Time Entry):** Registro e listagem do histórico de batida de ponto por funcionário (`/api/v1/time-entries`).
  - **Relatórios:** Cálculo financeiro por custo de serviço (`/api/v1/reports/jobs/{jobId}`) e geração de relatório consolidade de folha de pagamento (`/api/v1/reports/payroll`).
  - **Administração SaaS:** Cadastro e listagem de empresas clientes com ativação/desativação de status (`/api/v1/admin/companies`).
- **Não Prontas / Pendentes / Em Progresso:**
  - **Definição de Senha Inicial:** Os endpoints `/api/v1/auth/set-initial-password` estão descritos na documentação, mas requerem a integração do serviço de e-mail e do token gerado para ativação real da conta do usuário.
  - **Ajuste Fino de Permissões:** Alguns endpoints administrativos e permissões JWT precisam de testes e validação rigorosa de rotas para garantir que perfis não acessem recursos indevidos.

### 2. Frontend (Painel Web - Gestor)
- **Prontas (Implementadas):**
  - **Autenticação:** Tela de login e guarda de rotas (`ProtectedRoute`, `AuthContext`).
  - **Dashboard:** Listagem simples de trabalhos cadastrados.
  - **Cadastro de Trabalho:** Formulário básico de criação de trabalho (`/jobs/new`) integrando o autocompletar de endereços do Google Maps.
  - **Detalhes do Trabalho:** Página para visualização e gerenciamento de alocação de funcionários ao serviço.
  - **Visualização de Relatórios:** Páginas prontas para relatório de folha de pagamento geral e custo de serviço específico.
- **Não Prontas / Pendentes / Em Progresso:**
  - **Gerenciamento e Criação de Contas SaaS:** Painel para o administrador principal (`SAAS_OWNER`) gerenciar e cadastrar empresas clientes.
  - **Visualização da Equipe e Edição de Salários:** Funcionalidades de listagem detalhada e alteração de taxas salariais de funcionários.

### 3. Mobile (App do Funcionário)
- **Prontas (Implementadas):**
  - **Autenticação:** Tela de login e persistência local do token de sessão.
  - **Detecção de Localização:** Integração com a API de Geolocation do dispositivo para checar se o funcionário está em um local designado.
  - **Bater Ponto:** Botão dinâmico na tela principal para fazer check-in (`IN`) ou check-out (`OUT`) com base no último ponto registrado.
- **Não Prontas / Pendentes / Em Progresso:**
  - **Tela de Histórico:** Implementação da tela (`HistoryScreen`) para o funcionário visualizar seu extrato de horas passadas e folhas de pagamento calculadas.
  - **Gestão de Erros de GPS:** Tratamento aprimorado de permissões de localização (Android/iOS) e fallback em caso de falhas na busca das coordenadas.

---

## 🧪 Situação de Testes e Cobertura (Unit & Integration Tests)

Para retomar o desenvolvimento de forma segura e atingir os **80% de cobertura mínimos**, mapeamos o estado dos testes abaixo:

### 1. Backend (`backend`)
- **Onde TEM Teste:**
  - `ReportServiceTest.java` (Testa geração de relatórios agrupados e com zero dados).
  - `PayrollServiceTest.java` (Testa o fluxo de fechamento de folha e cálculo de valores devidos).
  - `TimeEntryServiceTest.java` (Testa fluxo de batida de ponto e validações geográficas).
- **Onde PRECISA de Teste:**
  - **Controllers/Endpoints:** Ausência completa de testes de integração (como `MockMvc`) para validar segurança das rotas (`@PreAuthorize`), retorno de DTOs e tratamentos de exceção globais (`GlobalExceptionHandler.java`).
  - **Security Layer:** Autenticação JWT, geração de tokens e o filtro `SecurityFilter.java`.
  - **Services Restantes:** `CompanyService`, `EmployeeService`, `AuthService`, `GeoService`.
  - **Mapeamento de Cobertura:** Falta configurar o plugin do **Jacoco** no `pom.xml` para gerar relatórios automatizados de cobertura de código.

### 2. Frontend (`frontend`)
- **Onde TEM Teste:**
  - Nenhuma cobertura ou framework de testes configurado.
- **Onde PRECISA de Teste:**
  - Configuração do **Vitest** ou **Jest** + **React Testing Library**.
  - Testes unitários para utilitários de formatação (`formatters.js`).
  - Testes dos componentes principais (`ProtectedRoute`, `PlacesAutocompleteInput`, modais).
  - Testes das páginas principais, garantindo que usuários desautenticados sejam redirecionados.

### 3. Mobile (`mobile`)
- **Onde TEM Teste:**
  - Teste básico configurado para o componente `App.tsx` (`__tests__/App.test.tsx`).
- **Onde PRECISA de Teste:**
  - **Configuração do Jest:** A execução dos testes de cobertura (`npm test -- --coverage`) falha atualmente porque os módulos de terceiros (como `@react-navigation/native`) utilizam sintaxe ESM moderna que não é transformada por padrão. É preciso ajustar o arquivo `jest.config.js` com a propriedade `transformIgnorePatterns` para transpilar as dependências do React Native.
  - **Lógica de Geolocalização e Bater Ponto:** Criar mocks para `Geolocation` e testar se os fluxos de batida de ponto funcionam corretamente na tela `MainScreen.js` diante de coordenadas válidas e inválidas.
  - **Utilitários:** Cobertura do arquivo `calculateWorkedHours.js`.

---

## 📋 Próximos Passos e Planejamento Sugerido

1. **Ajuste de Infraestrutura de Testes:**
   - Adicionar e configurar o Jacoco no `backend/pom.xml` para obter as métricas exatas de cobertura do backend.
   - Ajustar as dependências e configuração do Jest no `mobile/jest.config.js` para destravar os testes do app React Native.
   - Implementar o Vitest no `frontend` para estabelecer uma base sólida de testes no painel Web.
2. **Desenvolvimento de Testes Unitários e Integração:**
   - Focar no desenvolvimento dos testes ausentes nas classes críticas (como segurança, autenticação e controllers de ponto e relatórios) para elevar a cobertura global para o patamar desejado (>= 80%).
3. **Resolução de Pendências de Negócio:**
   - Finalizar os fluxos de e-mail/ativação de senha inicial.
   - Concluir a tela de histórico no aplicativo móvel.
