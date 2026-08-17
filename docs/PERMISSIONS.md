# M2 Manager — PERMISSIONS.md

## 1. Model

RBAC: Role Based Access Control.

Użytkownik posiada jedną lub więcej ról. Rola posiada zestaw uprawnień. Uprawnienie składa się z modułu i operacji.

Operacje:
- VIEW
- CREATE
- EDIT
- DELETE
- ADMIN

`ADMIN` oznacza pełny dostęp administracyjny do danego modułu i obejmuje operacje niższych poziomów.

## 2. Moduły

- DASHBOARD
- BUILDINGS
- STAIRCASES
- SCOPES
- ACTIVITIES
- EMPLOYEES
- MANAGERS
- SUPERVISORS
- CONTACTS
- SCHEDULE
- FINANCES
- WAREHOUSE
- FLEET
- REPORTS
- USERS
- ROLES
- SETTINGS

Uprawnienie ma kod np.:
`BUILDINGS_VIEW`
`BUILDINGS_CREATE`
`BUILDINGS_EDIT`
`BUILDINGS_DELETE`
`BUILDINGS_ADMIN`

## 3. Role systemowe

### SUPER_ADMIN

Pełny dostęp do całej organizacji oraz funkcji administracyjnych.

Docelowo może być także oddzielona rola platformowa do obsługi wszystkich tenantów SaaS.

### ADMIN

Pełny dostęp do modułów organizacji, użytkowników, ról i ustawień.

### KIEROWNIK

Domyślnie:
- Dashboard: VIEW
- Wspólnoty: ADMIN
- Klatki: ADMIN
- Zakresy prac: ADMIN
- Katalog czynności: ADMIN
- Pracownicy: ADMIN
- Zarządcy: ADMIN
- Opiekunowie: ADMIN
- Kontakty: ADMIN
- Harmonogram: ADMIN
- Finanse: VIEW
- Magazyn: ADMIN
- Flota: ADMIN
- Raporty: VIEW
- Użytkownicy: VIEW
- Role: VIEW
- Ustawienia: VIEW

### BIURO

Domyślnie:
- Dashboard: VIEW
- Wspólnoty: EDIT
- Klatki: EDIT
- Zakresy prac: VIEW
- Katalog czynności: VIEW
- Pracownicy: VIEW
- Zarządcy: EDIT
- Opiekunowie: EDIT
- Kontakty: EDIT
- Harmonogram: EDIT
- Finanse: ADMIN
- Magazyn: EDIT
- Flota: VIEW
- Raporty: VIEW
- Użytkownicy: NONE
- Role: NONE
- Ustawienia: NONE

### BRYGADZISTA / OPIEKUN

Domyślnie:
- Dashboard: VIEW
- Wspólnoty: VIEW
- Klatki: VIEW
- Zakresy prac: VIEW
- Katalog czynności: VIEW
- Pracownicy: VIEW
- Zarządcy: VIEW
- Opiekunowie: VIEW
- Kontakty: VIEW
- Harmonogram: EDIT
- Finanse: NONE
- Magazyn: VIEW
- Flota: VIEW
- Raporty: VIEW
- Użytkownicy: NONE
- Role: NONE
- Ustawienia: NONE

### PRACOWNIK

MVP:
- Dashboard: VIEW
- Harmonogram: VIEW
- Pozostałe moduły: NONE

W kolejnym sprincie można dodać:
- zadania własne,
- rozpoczęcie/zakończenie pracy,
- checklisty,
- zdjęcia,
- raporty,
- zgłoszenia problemów.

## 4. Role własne

Administrator organizacji może tworzyć role własne i przypisywać dowolne uprawnienia dostępne dla organizacji.

Nie wolno jednak nadać roli uprawnienia wyższego niż posiada użytkownik tworzący rolę.

## 5. Backend

Ukrycie przycisku w React nie jest zabezpieczeniem.

Każdy endpoint musi sprawdzać permission na backendzie.

Przykład:
`POST /api/buildings`
wymaga `BUILDINGS_CREATE`.

Brak uprawnienia:
HTTP 403.

Brak uwierzytelnienia:
HTTP 401.

## 6. Multi-tenant

Uprawnienia nie mogą pozwolić użytkownikowi na dostęp do danych innej organizacji.

`organization_id` nigdy nie może być uznane za wiarygodne tylko dlatego, że przyszło z frontendu.

## 7. Audyt

Zmiany administracyjne i biznesowo istotne powinny tworzyć `audit_logs`, szczególnie:
- zmiana danych użytkownika,
- zmiana roli/uprawnień,
- usunięcie/archiwizacja danych,
- zmiana pracownika,
- zmiana zakresu prac,
- wygenerowanie harmonogramu,
- ręczna zmiana zadania,
- operacje synchronizacji kalendarza,
- zmiany finansowe.
