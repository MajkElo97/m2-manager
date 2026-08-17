# M2 Manager — DATABASE.md

## 1. Cel

PostgreSQL jest źródłem prawdy dla aplikacji. Google Sheets nie będzie używany jako baza danych.

System jest od początku multi-tenant: każda organizacja posiada własne dane, a wszystkie zapytania biznesowe muszą być ograniczone do `organization_id`.

## 2. Standardy

- PostgreSQL
- UUID jako klucze główne
- `created_at`, `updated_at` dla encji biznesowych
- `deleted_at` dla encji wspierających soft delete
- kwoty finansowe: `NUMERIC`, nigdy `float`
- daty/czas: `TIMESTAMP WITH TIME ZONE` tam, gdzie istotna jest chwila w czasie
- migracje wyłącznie przez Flyway
- indeksy na `organization_id` i kluczach obcych
- unikalność definiowana w kontekście organizacji

## 3. Organizacja i dostęp

### organizations
- id
- name
- nip
- email
- phone
- active
- created_at
- updated_at

### users
- id
- organization_id
- email
- password_hash
- first_name
- last_name
- active
- last_login_at
- created_at
- updated_at

### roles
- id
- organization_id
- name
- description
- system_role
- active
- created_at
- updated_at

### permissions
- id
- code
- module
- action
- description

### user_roles
- user_id
- role_id

### role_permissions
- role_id
- permission_id

## 4. Wspólnoty

### buildings
- id
- organization_id
- code
- name
- address
- city
- nip
- phone
- email
- manager_id
- supervisor_id
- default_employee_id
- contract_date
- start_date
- notice_period_months
- status
- notes
- created_at
- updated_at
- deleted_at

### staircases
- id
- organization_id
- building_id
- name
- intercom_code
- key_required
- elevator
- floors
- notes
- created_at
- updated_at
- deleted_at

### managers
- id
- organization_id
- company
- phone
- email
- address
- active
- notes
- created_at
- updated_at
- deleted_at

### supervisors
- id
- organization_id
- manager_id
- first_name
- last_name
- phone
- email
- active
- notes
- created_at
- updated_at
- deleted_at

### contacts
- id
- organization_id
- building_id
- first_name
- last_name
- role
- phone
- email
- active
- notes
- created_at
- updated_at
- deleted_at

## 5. Pracownicy

### employees
- id
- organization_id
- user_id (nullable)
- first_name
- last_name
- phone
- email
- google_email
- position
- employment_type
- hire_date
- salary
- calendar_color
- active
- notes
- created_at
- updated_at
- deleted_at

Pracownik i użytkownik systemu są osobnymi pojęciami. Pracownik może istnieć bez konta.

## 6. Katalog czynności

### activities
- id
- organization_id
- name
- category
- type
- period
- default_duration_minutes
- priority
- icon
- color
- description
- active
- created_at
- updated_at
- deleted_at

## 7. Zakresy prac

### work_scopes
- id
- organization_id
- building_id
- activity_id
- frequency
- execution_count
- assigned_employee_id
- start_time
- end_time
- active
- notes
- created_at
- updated_at
- deleted_at

### work_scope_weekdays
- work_scope_id
- weekday

Zakres może mieć wiele dni tygodnia.

## 8. Harmonogram

### schedule_rules
- id
- organization_id
- work_scope_id
- employee_id
- frequency
- start_date
- end_date
- start_time
- end_time
- active
- created_at
- updated_at

### schedule_tasks
- id
- organization_id
- schedule_rule_id
- building_id
- activity_id
- employee_id
- scheduled_date
- start_time
- end_time
- status
- notes
- created_at
- updated_at

Statusy MVP:
- TODO
- IN_PROGRESS
- DONE
- CANCELLED

## 9. Google Calendar

### google_calendar_accounts
- id
- organization_id
- employee_id
- google_email
- calendar_id
- encrypted_access_token
- encrypted_refresh_token
- token_expires_at
- active
- created_at
- updated_at

### google_calendar_events
- id
- schedule_task_id
- calendar_account_id
- google_event_id
- sync_status
- last_synced_at
- error_message
- created_at
- updated_at

Statusy:
- PENDING
- SYNCED
- ERROR

Tokeny Google nie mogą być przechowywane w kodzie ani logach.

## 10. Finanse

### revenues
- id
- organization_id
- building_id
- date
- description
- gross_amount
- vat_rate
- net_amount
- vat_amount
- category
- invoice_id
- notes
- created_at
- updated_at

### expenses
- id
- organization_id
- building_id
- date
- description
- category
- gross_amount
- vat_rate
- net_amount
- vat_amount
- employee_id
- vehicle_id
- warehouse_item_id
- invoice_id
- notes
- created_at
- updated_at

### invoices
- id
- organization_id
- number
- type
- contractor_name
- contractor_nip
- issue_date
- due_date
- gross_amount
- net_amount
- vat_amount
- status
- notes
- created_at
- updated_at

## 11. Magazyn

### warehouse_items
- id
- organization_id
- name
- category
- unit
- quantity
- minimum_quantity
- unit_price
- active
- notes
- created_at
- updated_at
- deleted_at

## 12. Flota

### vehicles
- id
- organization_id
- registration_number
- brand
- model
- vin
- production_year
- mileage
- fuel_type
- insurance_date
- inspection_date
- assigned_employee_id
- active
- notes
- created_at
- updated_at
- deleted_at

## 13. Audyt

### audit_logs
- id
- organization_id
- user_id
- action
- entity_type
- entity_id
- old_data
- new_data
- created_at

`old_data` i `new_data` mogą być przechowywane jako JSONB.

## 14. Relacje główne

Organization 1:N Users
Organization 1:N Employees
Organization 1:N Buildings
Building 1:N Staircases
Building 1:N Contacts
Manager 1:N Supervisors
Building N:1 Manager
Building N:1 Supervisor
Building 1:N WorkScopes
Activity 1:N WorkScopes
WorkScope 1:N ScheduleRules
ScheduleRule 1:N ScheduleTasks
Employee 1:N ScheduleTasks
Employee 1:N GoogleCalendarAccounts
ScheduleTask 1:0..1 GoogleCalendarEvent

## 15. Zasady bezpieczeństwa danych

Każdy endpoint biznesowy musi ustalić organizację z uwierzytelnionego użytkownika. Nie wolno ufać `organization_id` przesłanemu przez frontend.

Przykład:
`GET /api/buildings/{id}` musi sprawdzić jednocześnie:
- użytkownik jest uwierzytelniony,
- posiada uprawnienie BUILDINGS_VIEW,
- budynek należy do jego organizacji,
- budynek nie jest logicznie usunięty.

## 16. Indeksy MVP

Minimum:
- users(organization_id, email)
- buildings(organization_id, code)
- buildings(organization_id, status)
- employees(organization_id, active)
- work_scopes(organization_id, building_id)
- schedule_tasks(organization_id, scheduled_date)
- schedule_tasks(organization_id, employee_id, scheduled_date)
- revenues(organization_id, date)
- expenses(organization_id, date)
- audit_logs(organization_id, created_at)

Dokładne constrainty i indeksy zostaną zapisane w migracjach Flyway.
