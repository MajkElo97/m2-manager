ALTER TABLE users
    ADD COLUMN employee_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees (id);

CREATE INDEX idx_users_employee ON users (employee_id);
