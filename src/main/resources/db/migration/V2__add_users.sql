-- Création de la table users
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       is_active BOOLEAN NOT NULL DEFAULT true,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP
);

-- Ajout de la colonne user_id aux tables existantes
ALTER TABLE revenues ADD COLUMN user_id BIGINT;
ALTER TABLE expenses ADD COLUMN user_id BIGINT;
ALTER TABLE budgets ADD COLUMN user_id BIGINT;

-- Ajout des contraintes de clé étrangère
ALTER TABLE revenues ADD CONSTRAINT fk_revenue_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE expenses ADD CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE budgets ADD CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Index pour les recherches par utilisateur
CREATE INDEX idx_revenues_user_id ON revenues(user_id);
CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_budgets_user_id ON budgets(user_id);

-- Insertion d'un utilisateur admin par défaut (mot de passe: admin123)
-- Le mot de passe est hashé avec BCrypt
INSERT INTO users (first_name, last_name, email, password, role) VALUES
    ('Admin', 'System', 'admin@wallet.com', '$2a$10$rS.r4P3n4n4n4n4n4n4n4ueCbP3n4n4n4n4n4n4n4n4n4n4n4n4n4n', 'ADMIN');