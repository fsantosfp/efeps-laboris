# Plano de Implementação - Feature de Boas-vindas com Senha Temporária (Resend Integration)

Este plano descreve a implementação da funcionalidade de boas-vindas. Toda vez que uma nova empresa (e seu respectivo gestor) ou funcionário for cadastrado na plataforma:
1. Uma senha temporária segura de 10 caracteres será gerada automaticamente.
2. O campo `password_reset_required` será definido como `true` no registro do usuário.
3. Um e-mail contendo a senha temporária e instruções para o primeiro acesso será enviado usando o serviço **Resend**.
4. No primeiro login, a API e as aplicações (Web/Mobile) detectarão que a senha temporária está ativa e exigirão que o usuário defina uma nova senha antes de acessar qualquer outra parte do sistema.

---

## Alinhamento de Escopo e Regras (Product Discovery)

### Regras de Negócio
1. **Geração de Senha Temporária:** Ao criar uma empresa/gestor ou funcionário, o sistema gerará uma senha alfanumérica aleatória legível de 10 caracteres (letras maiúsculas, minúsculas e números) para melhor usabilidade no copy/paste.
2. **Flag de Redefinição:** A tabela `users` possuirá a coluna `password_reset_required` (boolean), inicializada como `true` para novos usuários.
3. **Bloqueio de Acesso na API (Security Gate):** 
   - Usuários com `password_reset_required = true` receberão um token JWT contendo a claim `passwordResetRequired: true`.
   - O `SecurityFilter` interceptará requisições com esse token. Se a claim for verdadeira, qualquer chamada para endpoints diferentes de `PUT /api/v1/me/password` retornará status `403 Forbidden`.
4. **Atualização de Senha:** O endpoint `PUT /api/v1/me/password` receberá a nova senha, atualizará o hash no banco, definirá `password_reset_required` como `false` e invalidará o estado de alteração obrigatória.
5. **Integração com Resend:**
   - Criaremos a propriedade `api.resend.key` em `application.properties`.
   - Implementaremos um `ResendEmailService` que faz uma requisição HTTP POST para a API do Resend (`https://api.resend.com/emails`) usando o `java.net.http.HttpClient` padrão do Java 21 ou Spring `RestClient`.
   - Se a chave da API do Resend estiver vazia ou com valor padrão/fictício, o serviço fará o fallback imprimindo o e-mail no console (comportamento mock útil para testes locais).

### Fluxos com User Stories (Gherkin)

#### Cenário 1: Login com senha temporária pela primeira vez
```gherkin
Funcionalidade: Primeiro Login com Senha Temporária
  Como um novo funcionário ou gestor cadastrado
  Quero fazer o primeiro login usando a senha temporária recebida por e-mail
  Para que eu seja obrigado a cadastrar uma nova senha segura antes de acessar o sistema

  Cenário: Primeiro login bem-sucedido direciona para alteração de senha
    Dado que eu recebi o e-mail de boas-vindas com a senha temporária
    Quando eu insiro meu e-mail e a senha temporária na tela de login
    Então o login é realizado com sucesso
    E o sistema me redireciona obrigatoriamente para a tela de Alteração de Senha
    E eu não consigo navegar para o Dashboard ou outras telas sem alterar a senha
```

#### Cenário 2: Tentativa de burlar o fluxo de redefinição de senha
```gherkin
Funcionalidade: Segurança do Token Temporário
  Como o sistema de segurança do Laboris
  Quero impedir que tokens de usuários que não alteraram a senha temporária acessem outros recursos
  Para garantir a integridade e segurança dos dados da empresa

  Cenário: Tentativa de requisição de dados com senha pendente de alteração
    Dado que eu tenho um token JWT válido, mas com a claim passwordResetRequired ativa
    Quando eu tento acessar a rota GET "/api/v1/time-entries" ou qualquer outra rota protegida
    Então o servidor do backend recusa a requisição e retorna o código 403 Forbidden
```

#### Cenário 3: Alteração de senha concluída com sucesso
```gherkin
Funcionalidade: Alteração de Senha no Primeiro Login
  Como um novo usuário autenticado temporariamente
  Quero fornecer uma nova senha segura
  Para que meu acesso definitivo seja liberado

  Cenário: Alteração de senha bem-sucedida
    Dado que eu estou na tela de alteração de senha
    Quando eu informo uma nova senha válida e confirmo
    Então a API processa a atualização com sucesso
    E o campo password_reset_required do meu usuário é alterado para falso
    E eu sou desconectado para realizar o login com a minha nova senha (ou minha sessão é atualizada)
```

---

## Proposed Changes

### Database Layer

#### [MODIFY] [laboris_database.sql](file:///Users/fsantos/Documents/workspace/effies-laboris/docker/db/laboris_database.sql)
- Adicionar o campo `password_reset_required BOOLEAN NOT NULL DEFAULT FALSE` à tabela `users`.
- Atualizar a query do usuário padrão ('Admin SaaS') para ter `password_reset_required = FALSE`.

---

### Backend Layer

#### [MODIFY] [User.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/entity/User.java)
- Adicionar a propriedade `passwordResetRequired` com anotação `@Column(name = "password_reset_required", nullable = false)`.
- Inicializar por padrão como `false`.

#### [NEW] [EmailService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/EmailService.java)
- Interface genérica para envio de e-mails com o método `sendWelcomeEmail(String toEmail, String userName, String tempPassword, String roleName)`.

#### [NEW] [ResendEmailService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/ResendEmailService.java)
- Implementação de `EmailService` que envia e-mails reais usando a API do **Resend** se a chave estiver configurada, senão imprime formatado no console para fins de desenvolvimento.

#### [NEW] [PasswordGenerator.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/utils/PasswordGenerator.java)
- Utilitário estático para gerar senhas alfanuméricas seguras e fáceis de ler com 10 caracteres.

#### [MODIFY] [CompanyService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/CompanyService.java)
- Injetar o `EmailService`.
- No método `managerCreate`, gerar a senha temporária usando `PasswordGenerator.generate()`, salvar a senha com hash e marcar `manager.setPasswordResetRequired(true)`.
- No método `create`, disparar o e-mail usando o `EmailService.sendWelcomeEmail` passando a senha temporária original gerada.

#### [MODIFY] [EmployeeService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/EmployeeService.java)
- Injetar o `EmailService`.
- No método `create`, gerar a senha temporária usando `PasswordGenerator.generate()`, marcar `employee.setPasswordResetRequired(true)` e salvar.
- Chamar o `EmailService.sendWelcomeEmail` com a senha gerada antes de retornar.

#### [MODIFY] [TokenService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/TokenService.java)
- No método `generateToken`, adicionar a claim `passwordResetRequired` contendo o valor de `user.isPasswordResetRequired()`.

#### [MODIFY] [SecurityFilter.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/infra/security/SecurityFilter.java)
- Extrair a claim `passwordResetRequired` do token.
- Se a claim for `true` e a URL requisitada não for a de alteração de senha (`/api/v1/me/password`), responder imediatamente com status `403 Forbidden` e interromper a cadeia de execução.

#### [MODIFY] [LoginResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/LoginResponseDto.java)
- Adicionar o campo `private boolean passwordResetRequired;` para facilitar o tratamento direto nas aplicações frontend e mobile.

#### [MODIFY] [AuthService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/AuthService.java)
- Retornar um objeto ou DTO contendo o Token e o boolean `passwordResetRequired`.
- Modificar o controller `AuthController.java` para preencher essa informação na resposta do login.

#### [NEW] [ChangePasswordRequestDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/request/ChangePasswordRequestDto.java)
- DTO contendo o campo `newPassword` com validação de tamanho mínimo de 8 caracteres.

#### [NEW] [MeController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/MeController.java)
- Endpoint `PUT /api/v1/me/password` que recebe a nova senha, criptografa, atualiza o usuário autenticado, define `passwordResetRequired` como `false` e salva no banco de dados.

#### [MODIFY] [application.properties](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/resources/application.properties) / [application-local.properties](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/resources/application-local.properties)
- Adicionar a propriedade `api.resend.key` para a API Key do Resend.

---

### Frontend Layer (React Web)

#### [MODIFY] [AuthContext.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/context/AuthContext.jsx)
- Adaptar a função `login` para receber a flag `passwordResetRequired`.
- Se a flag for `true`, armazenar o token mas redirecionar o usuário para `/change-password` em vez de `/dashboard`.

#### [NEW] [ChangePasswordPage.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/pages/ChangePasswordPage.jsx)
- Tela com design premium alinhada ao visual do sistema para alteração da senha inicial.
- Exibir campos: "Nova Senha" e "Confirmar Nova Senha".
- Chamar o endpoint `PUT /api/v1/me/password` enviando a nova senha com cabeçalho de autorização.
- Após o sucesso, limpar o token, exibir mensagem amigável e redirecionar para a tela de login.

#### [MODIFY] [App.jsx](file:///Users/fsantos/Documents/workspace/effies-laboris/frontend/src/App.jsx)
- Registrar a rota protegida `/change-password`.

---

### Mobile Layer (React Native)

#### [MODIFY] [LoginScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/LoginScreen.js)
- No sucesso da chamada `/auth/login`, verificar a propriedade `passwordResetRequired` no JSON de resposta.
- Se for verdadeira, invocar um callback de navegação para a nova tela de alteração de senha.

#### [NEW] [ChangePasswordScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/ChangePasswordScreen.js)
- Tela nativa para digitação da nova senha com confirmação.
- Chamada ao endpoint `PUT /api/v1/me/password` com o token temporário salvo.
- Retorno ao fluxo de login inicial após sucesso.

---

## Verification Plan

### Automated Tests
- Criar testes unitários em `CompanyServiceTest.java` e `EmployeeServiceTest.java` validando que a senha é gerada e o e-mail de boas-vindas é enviado.
- Criar classe de teste de integração `AuthControllerTest.java` utilizando `MockMvc` para testar:
  1. Login bem-sucedido de usuário comum (retorna `passwordResetRequired = false`).
  2. Login com senha temporária (retorna `passwordResetRequired = true`).
- Criar `MeControllerTest.java` testando:
  1. Alteração de senha bem-sucedida atualiza a flag para `false`.
  2. Bloqueio de outros endpoints quando o token possui a claim ativa.

### Manual Verification
1. Subir os contêineres e a aplicação backend localmente.
2. Executar o comando SQL para adicionar o campo no banco Postgres.
3. Criar uma nova empresa/gestor pela API administrativa.
4. Verificar no log do console o e-mail gerado com a senha temporária.
5. Fazer login com o e-mail cadastrado e a senha temporária. Validar que o retorno JSON do login indica `passwordResetRequired: true`.
6. Tentar acessar outro endpoint restrito usando o token gerado (ex: `/api/v1/time-entries`) e validar o retorno `403 Forbidden`.
7. Fazer requisição para `PUT /api/v1/me/password` informando a nova senha.
8. Repetir o login com a nova senha e validar que `passwordResetRequired` agora é `false`.
