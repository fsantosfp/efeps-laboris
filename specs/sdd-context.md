# SDD - Contexto Operacional da Tarefa Atual

## 🎯 Tarefa Atual
* **Título:** Tarefa 4.1: Endpoint PUT /api/v1/me/password
* **Objetivo:** Criar DTO `ChangePasswordRequestDto` (com validações de tamanho mínimo da senha) e o endpoint `PUT /api/v1/me/password` em `MeController`. Esse endpoint deve criptografar a nova senha informada, atualizar o usuário logado, definir `passwordResetRequired = false` e persistir as alterações.

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  * [MeController.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/controller/MeController.java) [NEW]
  * [ChangePasswordRequestDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/request/ChangePasswordRequestDto.java) [NEW]
* **Criar:**
  * [MeControllerTest.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/test/java/br/com/effies/laboris/backend/presentation/controller/MeControllerTest.java) [NEW]

---

## 🛡️ Diretrizes de Qualidade Mandatórias

1. **Testes de Integração:** Criar testes em `MeControllerTest` utilizando `MockMvc` para validar que:
   - Requisições autenticadas para `PUT /api/v1/me/password` com uma senha válida redefinem com sucesso a senha do usuário e alteram a flag `passwordResetRequired` para falso.
   - O endpoint exige autenticação.
2. **Code-Review:** Revisar a segurança do fluxo e a criptografia com `PasswordEncoder`.
3. **Validação de Prontidão (Quality Gate):**
   - Compilação limpa.
   - Sucesso em toda a suíte de testes (`mvn test`).
   - Obter aprovação humana.

---

## 💾 Instruções de Commit
* Commit somente após validação 100% com sucesso e aprovação humana.
* Mensagem sugerida para o commit: `feat(me): adiciona endpoint PUT /api/v1/me/password para redefinicao de senha`
