-- Tabela para os clientes do SaaS
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Active', -- Ativo/Inativo
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tabela de usuários do sistema (Dono, Gestores, Funcionários)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(id), -- NULL para o Dono do SaaS
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    password_reset_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT check_user_role CHECK (role IN ('SAAS_OWNER', 'MANAGER', 'EMPLOYEE')),
    CONSTRAINT check_user_status CHECK ( status IN ('ACTIVE', 'INACTIVE'))
);

-- Tabela para o histórico de salários, permitindo aumentos com data de vigência
CREATE TABLE salary_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hourly_rate NUMERIC(10, 2) NOT NULL,
    effective_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tabela para os trabalhos/projetos dos clientes
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    client_name VARCHAR(255), -- Nome do contratante do serviço
    budget NUMERIC(10, 2) NOT NULL,
    billing_rate NUMERIC(10, 2) NOT NULL, -- Valor/hora de venda
    responsible_name VARCHAR(255) NOT NULL,
    responsible_phone VARCHAR(50) NOT NULL,
    responsible_email VARCHAR(255),
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT check_job_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELED', 'DELETED'))
);

-- Tabela de ligação (muitos-para-muitos) entre funcionários e trabalhos
CREATE TABLE job_assignments (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, job_id) -- Garante que um usuário só pode ser designado uma vez para o mesmo trabalho
);

-- Tabela para armazenar as folhas de pagamentos.
CREATE TABLE payrolls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id),
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    settled_by_user_id UUID NOT NULL REFERENCES users(id), -- Quem executou o pagamento
    settled_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payroll_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payroll_id UUID NOT NULL REFERENCES payrolls(id),
    employee_id UUID NOT NULL REFERENCES users(id),
    total_hours NUMERIC(10, 2) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL
);

-- Tabela para armazenar todas as batidas de ponto
CREATE TABLE time_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    entry_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    entry_type VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_manual BOOLEAN NOT NULL DEFAULT FALSE,
    justification TEXT, -- Justificativa para entradas manuais
    payroll_id UUID,
    CONSTRAINT check_time_entry_type CHECK (entry_type IN ('IN', 'OUT')),
    CONSTRAINT fk_time_entries_payroll FOREIGN KEY (payroll_id) REFERENCES payrolls(id)
);

-- Criando um índice para otimizar a busca por salário em uma data específica, analisar primeiro o desempenho
-- CREATE INDEX idx_salary_history_user_date ON salary_history (user_id, effective_date DESC);

INSERT INTO users (name, email, password_hash, "role")
VALUES ('Admin SaaS', 'admin@laboris.com', '$2a$10$p8s27fACTFqP4fz3iD0z0.c8zHyCxEEEN/BuLNGzKAUgtNXs0SqcS', 'SAAS_OWNER')
ON CONFLICT (email) DO NOTHING;

-- Tabela para armazenar deslocamentos (translados) entre trabalhos
CREATE TABLE displacements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_timestamp TIMESTAMPTZ NOT NULL,
    end_timestamp TIMESTAMPTZ,
    start_latitude DOUBLE PRECISION NOT NULL,
    start_longitude DOUBLE PRECISION NOT NULL,
    end_latitude DOUBLE PRECISION,
    end_longitude DOUBLE PRECISION,
    start_address TEXT,
    destination_job_id UUID REFERENCES jobs(id) ON DELETE CASCADE
);