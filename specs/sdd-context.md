# SDD - Contexto Operacional da Tarefa Atual

## 🎯 Tarefa Atual
* **Título:** Tarefa 1.1: Criação dos DTOs de Histórico Salarial e Tarefa 1.2: Métodos de Serviço no EmployeeService
* **Objetivo:** 
  1. Criar os DTOs `CreateSalaryHistoryRequestDto.java` e `SalaryHistoryResponseDto.java`.
  2. Implementar os métodos `findSalariesByEmployee` e `addSalaryHistory` em `EmployeeService.java`.

---

## 📂 Escopo dos Arquivos
* **Modificar:**
  * [EmployeeService.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/domain/service/EmployeeService.java)
* **Criar:**
  * [CreateSalaryHistoryRequestDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/request/CreateSalaryHistoryRequestDto.java)
  * [SalaryHistoryResponseDto.java](file:///Users/fsantos/Documents/workspace/effies-laboris/backend/src/main/java/br/com/effies/laboris/backend/presentation/dto/response/SalaryHistoryResponseDto.java)

---

## 🛡️ Diretrizes de Qualidade Mandatórias

1. **Segurança de Dados:** Os métodos do serviço devem validar se a empresa do funcionário a ser alterado coincide com a empresa do gestor (`manager.getCompany().getId()`), impedindo acesso cruzado.
2. **Quality Gate:**
   - Compilação limpa do projeto.
