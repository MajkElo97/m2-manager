# M2 Manager 2.0 — Technical Blueprint

## Produkt

M2 Manager jest systemem SaaS do zarządzania firmami sprzątającymi.

MVP jest przeznaczone dla jednej organizacji, ale architektura jest multi-tenant od pierwszego dnia.

## Stack

- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- React
- TypeScript
- Vite
- Docker
- GitHub Actions
- AWS
- Google Calendar API

## Zasady

1. PostgreSQL jest źródłem prawdy.
2. Backend jest odpowiedzialny za bezpieczeństwo i logikę biznesową.
3. Frontend nigdy nie jest mechanizmem autoryzacji.
4. Każda encja biznesowa jest ograniczona do organizacji.
5. Każda zmiana schematu bazy idzie przez Flyway.
6. API używa DTO.
7. Controller nie zawiera logiki biznesowej.
8. Każda ważna operacja jest testowalna.
9. UI zachowuje obecny wygląd M2 Managera.
10. UI dostaje dark mode i pełną optymalizację mobilną.
11. Generator harmonogramu jest projektowany jako moduł rozszerzalny.
12. Google Calendar jest integracją, nie źródłem prawdy.

## MVP

- logowanie
- użytkownicy
- role
- uprawnienia
- pełny CRUD modułów
- wspólnoty
- klatki
- zarządcy
- opiekunowie
- kontakty
- pracownicy
- katalog czynności
- zakresy prac
- prosty harmonogram
- prosty generator
- Google Calendar sync
- podstawowe finanse/przychody
- magazyn CRUD
- flota CRUD
- dashboard
- audit log
- dark mode
- mobile UI

## Kolejne sprinty

- panel pracownika
- zdjęcia
- checklisty
- raporty pracowników
- rozpoczęcie/zakończenie pracy
- urlopy i absencje
- zastępstwa
- zaawansowany generator
- trasy i optymalizacja
- pełne finanse
- dokumenty i pliki
- rozliczenia SaaS
- billing/subskrypcje

## Development

Najpierw uruchamiamy lokalnie:
- backend
- frontend
- PostgreSQL

przez Docker Compose.

Dopiero po działającym MVP:
- GitHub Actions
- AWS
- produkcyjna baza
- monitoring
- backupy

## Następny krok

Utworzyć repozytorium i przygotować:
- backend Spring Boot,
- frontend React + TypeScript,
- PostgreSQL,
- Docker Compose,
- pierwszy Flyway migration,
- konfigurację środowisk,
- podstawowe health checki,
- strukturę modułów.
