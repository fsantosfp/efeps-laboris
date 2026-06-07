# Lista de Tarefas - Feature de Boas-vindas com Senha Temporária

Este documento contém o fatiamento e decomposição das tarefas para a implementação da feature, seguindo os princípios de **Spec-Driven Development (SDD)**. Cada tarefa deve ser realizada de forma incremental, testada e revisada antes do commit.

---

## 🛠️ Slice 1: Infraestrutura de Banco e Serviço de E-mail (Backend)

- [x] **Tarefa 1.1: Alteração do Schema de Banco e Entidade User**
  - **Ações:** 
    - Adicionar propriedade `passwordResetRequired` no arquivo [User.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/entity/User.java) mapeando para a coluna `password_reset_required`.
    - Atualizar a definição do schema no arquivo SQL de inicialização [laboris_database.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/laboris_database.sql) adicionando a coluna na tabela `users`.
  - **Critério de Aceitação:** O projeto compila com sucesso e o banco de dados reflete o novo campo.

- [x] **Tarefa 1.2: Gerador de Senhas Temporárias**
  - **Ações:**
    - Criar a classe utilitária `PasswordGenerator.java` sob o pacote `br.com.effies.laboris.backend.domain.utils`.
    - Implementar a geração de string alfanumérica aleatória segura com exatamente 10 caracteres usando `SecureRandom`.
  - **Critério de Aceitação:** Teste unitário demonstra que as senhas têm 10 caracteres, variam de forma segura e contêm letras maiúsculas, minúsculas e números.

- [x] **Tarefa 1.3: Serviço de Envio de E-mail (Resend & Fallback Console)**
  - **Ações:**
    - Criar a interface [EmailService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/EmailService.java).
    - Criar a classe `ResendEmailService.java` que implementa `EmailService`.
    - Adicionar propriedade `api.resend.key` em `application.properties` e `application-local.properties`.
    - No `ResendEmailService`, usar `java.net.http.HttpClient` para fazer requisição POST para a API do Resend se a chave estiver presente.
    - Se a chave for vazia ou `"mock"`, logar o conteúdo do e-mail no console de forma estruturada.
  - **Critério de Aceitação:** O serviço funciona sem dependências adicionais externas complexas e loga no console caso a API Key não seja informada.

---

## 🔐 Slice 2: Fluxo de Criação e Envio de E-mail (Backend)

- [x] **Tarefa 2.1: Geração de Senha e E-mail no Cadastro de Empresas (Gestores)**
  - **Ações:**
    - Injetar o `EmailService` em `CompanyService.java`.
    - Modificar o método `managerCreate` para gerar a senha temporária usando `PasswordGenerator`, aplicar o hash e salvar o gestor com `passwordResetRequired = true`.
    - No método `create`, disparar o envio do e-mail de boas-vindas informando a senha temporária legível.
  - **Critério de Aceitação:** Ao criar uma empresa, o log do terminal exibe o e-mail de boas-vindas com a senha alfanumérica gerada.

- [x] **Tarefa 2.2: Geração de Senha e E-mail no Cadastro de Funcionários**
  - **Ações:**
    - Injetar o `EmailService` em `EmployeeService.java`.
    - No método `create`, gerar a senha temporária, aplicar o hash, marcar `passwordResetRequired = true` no usuário do funcionário e salvar.
    - Disparar o e-mail de boas-vindas com a senha temporária.
  - **Critério de Aceitação:** Ao cadastrar um funcionário, o e-mail mock de boas-vindas é impresso com a senha correspondente de 10 caracteres.

---

## 🚪 Slice 3: Bloqueio de Acesso e Login (Backend)

- [x] **Tarefa 3.1: JWT Claim de Alteração de Senha Pendente**
  - **Ações:**
    - Modificar `TokenService.java` para adicionar o claim `"passwordResetRequired"` ao payload do token JWT se `user.isPasswordResetRequired()` for verdadeiro.
  - **Critério de Aceitação:** Ao decodificar um JWT gerado para um usuário novo, a propriedade `passwordResetRequired` está presente e possui valor `true`.

- [x] **Tarefa 3.2: Retorno do Status no Login**
  - **Ações:**
    - Atualizar `LoginResponseDto.java` adicionando a propriedade `passwordResetRequired`.
    - Modificar a resposta do login em `AuthController.java` / `AuthService.java` para retornar o valor boolean correto.
  - **Critério de Aceitação:** O payload JSON retornado pela chamada HTTP de login exibe `passwordResetRequired: true` para usuários que ainda não alteraram a senha inicial.

- [x] **Tarefa 3.3: Filtro de Segurança Interceptador (Security Gate)**
  - **Ações:**
    - Atualizar o `SecurityFilter.java`.
    - Se o token conter o claim `passwordResetRequired = true` e o endpoint solicitado não for `/api/v1/me/password`, bloquear a requisição e retornar HTTP `403 Forbidden` com corpo detalhando a necessidade de alteração de senha.
  - **Critério de Aceitação:** Chamadas para qualquer outro endpoint protegido usando o token temporário retornam `403 Forbidden`.

---

## ✏️ Slice 4: Endpoint de Redefinição de Senha (Backend)

- [x] **Tarefa 4.1: Endpoint PUT /api/v1/me/password**
  - **Ações:**
    - Criar `ChangePasswordRequestDto.java` recebendo `newPassword` (mínimo de 8 caracteres).
    - Criar o controller `MeController.java` anotado com `@RestController` e `@RequestMapping("/api/v1/me")`.
    - Implementar a rota `PUT /password` que extrai o usuário autenticado do contexto de segurança, atualiza sua senha criptografada, define `passwordResetRequired` como `false` e salva no banco.
  - **Critério de Aceitação:** Enviar requisição com a nova senha para `/api/v1/me/password` remove o bloqueio e permite logins futuros normais sem flag ativada.

---

## 🌐 Slice 5: Painel Web React (Frontend)

- [x] **Tarefa 5.1: Redirecionamento no AuthContext**
  - **Ações:**
    - Atualizar a função `login` no [AuthContext.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/context/AuthContext.jsx).
    - Capturar o campo `passwordResetRequired` da resposta e, se `true`, navegar o usuário para `/change-password` ao invés de `/dashboard`.
  - **Critério de Aceitação:** Usuários comuns vão para `/dashboard`, novos usuários com senha temporária vão para `/change-password`.

- [x] **Tarefa 5.2: Criação da Página ChangePasswordPage.jsx**
  - **Ações:**
    - Criar a página de redefinição com interface premium e amigável.
    - Exibir campos para nova senha e confirmação com validações.
    - Realizar a chamada HTTP `PUT /api/v1/me/password` com o token.
    - Em caso de sucesso, limpar a sessão (logout) e mandar de volta ao login com mensagem de sucesso.
  - **Critério de Aceitação:** O fluxo de troca funciona visualmente e impede a saída da página sem a conclusão.

- [x] **Tarefa 5.3: Configuração de Rotas no App.jsx**
  - **Ações:**
    - Adicionar a rota `/change-password` ligada ao `ChangePasswordPage.jsx` dentro das rotas do [App.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/App.jsx).
  - **Critério de Aceitação:** A rota está registrada e protegida.

---

## 📱 Slice 6: Aplicativo Mobile React Native (Mobile)

- [x] **Tarefa 6.1: Fluxo de Login no Mobile**
  - **Ações:**
    - Adaptar `LoginScreen.js` para ler `passwordResetRequired` do payload HTTP.
    - Se `true`, redirecionar para a nova tela de redefinição.
  - **Critério de Aceitação:** O aplicativo identifica novos funcionários e direciona para a tela de alteração obrigatória.

- [x] **Tarefa 6.2: Criação da Tela ChangePasswordScreen.js**
  - **Ações:**
    - Criar tela nativa de redefinição de senha.
    - Bloquear o botão de voltar físico do Android e gestos de navegação.
    - Chamar o endpoint `PUT /api/v1/me/password` e retornar ao login com sucesso.
  - **Critério de Aceitação:** O funcionário não consegue usar o app sem antes definir uma senha definitiva.
