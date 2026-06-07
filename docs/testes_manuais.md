# Rotas de Testes

## SaaS Owner - Gerenciamento de Empresas
1. [x] Login
2. [ ] Criar Empresa
    2.1 [x] Criar empresa com email já cadastrado - Error
    2.1 [x] Criar empresa com email não existente - Success
    2.2 [ ] Criar empresa sem nome - Error (Valor enviado " " e passou)
    2.3 [ ] Criar empresa sem email - Error (Passou com email " ", )
    2.4 [x] Criar empresa sem manager - Error
    2.5 [ ] Criar empresa com email inválido - Error (Valor enviado "abc" e passou)
    2.6 [ ] Criar empresa com email sem @ - Error (Passou com email "abc com")
    2.7 [x] Criar empresa com manager já existente - Success
    
3. [x] Listar Empresas
4. [ ] Atualizar Empresa
5. [x] Ativar / Desativar Empresa
6. [ ] Deletar Empresa

## Manager - Gerenciamento de Trabalhos
1. [ ] Login
2. [ ] Listar Trabalhos
3. [ ] Criar Trabalho
4. [ ] Deletar Trabalho

## Employee - Gerenciamento de ponto
1. [ ] Login
2. [ ] Listar Trabalhos
3. [ ] Bater Ponto
4. [ ] Listar Ponto
