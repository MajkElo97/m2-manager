ALTER TABLE buildings
    ADD COLUMN manager_id UUID,
    ADD COLUMN supervisor_id UUID,
    ADD COLUMN employee_id UUID;

ALTER TABLE buildings
    ADD CONSTRAINT fk_buildings_manager FOREIGN KEY (manager_id) REFERENCES managers (id),
    ADD CONSTRAINT fk_buildings_supervisor FOREIGN KEY (supervisor_id) REFERENCES supervisors (id),
    ADD CONSTRAINT fk_buildings_employee FOREIGN KEY (employee_id) REFERENCES employees (id);

CREATE INDEX idx_buildings_manager ON buildings (manager_id);
CREATE INDEX idx_buildings_supervisor ON buildings (supervisor_id);
CREATE INDEX idx_buildings_employee ON buildings (employee_id);
