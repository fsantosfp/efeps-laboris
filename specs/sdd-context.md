# SDD - Contexto Operacional da Tarefa Atual

## 🎯 Tarefa Atual
* **Título:** Slice 6: Aplicativo Mobile React Native (Mobile)
* **Objetivo:** 
  1. Adaptar o fluxo de login no aplicativo mobile (`LoginScreen.js`) para capturar a propriedade `passwordResetRequired`. Se for verdadeira, navegar o usuário para a tela de redefinição de senha.
  2. Criar a tela `ChangePasswordScreen.js` para digitação e confirmação da nova senha, enviando-a para `PUT /api/v1/me/password` e retornando ao login.

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  * [LoginScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/LoginScreen.js)
* **Criar:**
  * [ChangePasswordScreen.js](file:///Users/fsantos/Documents/workspace/effies-laboris/mobile/src/screens/ChangePasswordScreen.js) [NEW]

---

## 🛡️ Diretrizes de Qualidade Mandatórias

1. **Validação Visual:** Verificar se o fluxo de login redireciona corretamente no mobile e se a tela de redefinição de senha impede navegações de volta (bloqueando botão físico de voltar no Android e gestos de swipe no iOS).
2. **Quality Gate:**
   - Obter aprovação humana após apresentação da implementação.
