-- Création de la table categories
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            description VARCHAR(100),
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP
);

-- Création de la table revenues
CREATE TABLE revenues (
                          id BIGSERIAL PRIMARY KEY,
                          description VARCHAR(200) NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          revenue_date DATE NOT NULL,
                          notes VARCHAR(500),
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
);

-- Création de la table expenses
CREATE TABLE expenses (
                          id BIGSERIAL PRIMARY KEY,
                          description VARCHAR(200) NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          expense_date DATE NOT NULL,
                          category_id BIGINT NOT NULL,
                          notes VARCHAR(500),
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Création de la table budgets
CREATE TABLE budgets (
                         id BIGSERIAL PRIMARY KEY,
                         amount DECIMAL(10,2) NOT NULL,
                         month INTEGER NOT NULL,
                         year INTEGER NOT NULL,
                         category_id BIGINT,
                         alert_threshold DECIMAL(5,2),
                         is_active BOOLEAN NOT NULL DEFAULT true,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP,
                         CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Création des index pour optimiser les recherches
CREATE INDEX idx_expenses_category ON expenses(category_id);
CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_revenues_date ON revenues(revenue_date);
CREATE INDEX idx_budgets_month_year ON budgets(month, year);

-- Insertion des catégories par défaut
INSERT INTO categories (name, description) VALUES
                                               ('Nourriture', 'Alimentation et restaurants'),
                                               ('Transport', 'Transport en commun, essence, etc.'),
                                               ('Logement', 'Loyer, électricité, eau, etc.'),
                                               ('Santé', 'Médicaments, consultations, etc.'),
                                               ('Loisirs', 'Divertissements et hobbies'),
                                               ('Autres', 'Dépenses diverses');