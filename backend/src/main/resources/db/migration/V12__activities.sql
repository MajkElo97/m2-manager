CREATE TABLE activities (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    planning_type VARCHAR(20) NOT NULL,
    default_period VARCHAR(50),
    duration_minutes INTEGER,
    priority VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_activities_code UNIQUE (code),
    CONSTRAINT chk_activities_planning_type CHECK (planning_type IN ('CYCLIC', 'PERIODIC', 'ON_DEMAND')),
    CONSTRAINT chk_activities_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
    CONSTRAINT chk_activities_duration_minutes CHECK (duration_minutes IS NULL OR duration_minutes >= 0)
);

CREATE INDEX idx_activities_category ON activities (category);
CREATE INDEX idx_activities_planning_type ON activities (planning_type);
CREATE INDEX idx_activities_active ON activities (active);
