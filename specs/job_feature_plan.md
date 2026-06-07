# Especificação de Funcionalidade - Gerenciamento e Controle de Jobs (Trabalhos)

Esta especificação detalha as melhorias no gerenciamento de trabalhos (Jobs) na plataforma Laboris. O objetivo é adicionar o gerenciamento de responsáveis, validações obrigatórias no cadastro, edição completa das propriedades do Job no painel administrativo e exclusão segura para evitar remoção acidental.

---

## 📋 Regras de Negócio e Requisitos

### 1. Dados do Responsável
Todo trabalho deve ter um responsável atribuído diretamente. Os campos do responsável são:
- **Nome do Responsável:** Obrigatório (texto, 2 a 255 caracteres).
- **Telefone do Responsável:** Obrigatório (formato de telefone válido).
- **E-mail do Responsável:** Opcional (se fornecido, deve ser um e-mail válido).

### 2. Validações Estritas na Criação
Não é permitido criar um trabalho sem os seguintes dados:
- **Endereço Completo:** Texto não vazio.
- **Geolocalização (Latitude e Longitude):** Coordenadas numéricas válidas obtidas do Google Places ou fornecidas.
- **Taxa Horária (Billing Rate):** Valor numérico estritamente maior que zero.
- **Orçamento (Budget):** Valor numérico estritamente maior que zero.
- **Responsável:** Nome e telefone preenchidos.

### 3. Edição do Trabalho
Um gestor (`MANAGER`) pode editar as seguintes informações de um Job ativo ou pendente:
- **Taxa de Faturamento (Billing Rate)**
- **Orçamento (Budget)**
- **Data de Início**
- **Data de Término (Estimada)**
- **Dados do Responsável** (Nome, E-mail, Telefone)

### 4. Deleção Segura com Verificação
- Apenas trabalhos com status `PENDING` podem ser deletados.
- Para evitar exclusão acidental, o gestor deve preencher uma caixa de texto contendo o endereço exato (`address`) do trabalho antes que a ação de deleção seja autorizada.

---

## 🛠️ Arquitetura Técnica e Contratos

### Banco de Dados (PostgreSQL)
Tabela `jobs`:
- `billing_rate` alterada para `NUMERIC(10, 2) NOT NULL` (remover nulidade).
- `responsible_name` VARCHAR(255) NOT NULL.
- `responsible_phone` VARCHAR(50) NOT NULL.
- `responsible_email` VARCHAR(255) NULL.

### Backend (Java)
- Modificar DTO de entrada `CreateJobRequestDto` e `UpdateJobRequestDto`.
- Atualizar a entidade `Job.java` e a lógica de persistência e validação no `JobService.java`.
- Atualizar `JobResponseDto` para incluir os campos do responsável.

### Frontend (React)
- Integrar `PlacesAutocompleteInput` para obter lat/lng do Place selecionado.
- Adicionar campos do responsável nos formulários de criação e edição.
- Exibir os novos campos nos detalhes do Job (`JobDetailPage`).
- Substituir o diálogo de deleção por um modal customizado de confirmação com input de texto.
