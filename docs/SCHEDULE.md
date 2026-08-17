# M2 Manager — SCHEDULE.md

## 1. Cel

Generator ma na podstawie zakresów prac tworzyć konkretne zadania w harmonogramie i synchronizować je z Google Calendar.

Architektura musi umożliwić późniejsze dodanie:
- urlopów,
- nieobecności,
- zastępstw,
- godzin pracy,
- limitów godzin,
- lokalizacji,
- czasu przejazdu,
- kolejności obiektów,
- optymalizacji tras.

## 2. Rozdzielenie reguły od zadania

Nigdy nie traktujemy pojedynczego eventu Google jako źródła prawdy.

Źródłem prawdy jest `schedule_task`.

Reguła:
`work_scope -> schedule_rule`

Wygenerowane wystąpienie:
`schedule_rule -> schedule_task`

Google Calendar:
`schedule_task -> google_calendar_event`

## 3. MVP generatora

Generator przyjmuje:
- zakres prac,
- pracownika,
- częstotliwość,
- dni tygodnia,
- datę początku,
- opcjonalną datę końca,
- godzinę rozpoczęcia,
- godzinę zakończenia/czas trwania.

Przykład:

Kasprzaka
- czynność: Tereny zewnętrzne
- częstotliwość: 2x tygodniowo
- dni: poniedziałek, piątek
- pracownik: Jan
- godzina: 08:00
- czas: 60 min

Generator tworzy konkretne zadania dla zadanego zakresu dat.

## 4. Idempotencja

Ponowne uruchomienie generatora nie może tworzyć duplikatów.

Dla danego `schedule_rule` + daty + wystąpienia należy rozpoznać istniejące zadanie.

Generator powinien mieć tryby:
- GENERATE_NEW
- REGENERATE
- PREVIEW

`PREVIEW` pokazuje wynik bez zapisu.

## 5. Ręczna zmiana

Użytkownik z odpowiednim uprawnieniem może zmienić pojedyncze `schedule_task` bez zmiany reguły.

Przykład:
Reguła mówi Jan, ale 18 sierpnia zadanie zostaje ręcznie przypisane Piotrowi.

Reguła pozostaje bez zmian.

## 6. MVP bez optymalizacji

Pierwsza wersja nie uwzględnia automatycznie:
- tras,
- czasu przejazdu,
- urlopów,
- nieobecności,
- limitów godzin,
- automatycznych zastępstw.

Architektura ma jednak przewidywać te rozszerzenia.

## 7. Kolejne wersje

### Generator 2
- grafik pracy pracowników,
- dni wolne,
- urlopy,
- absencje,
- zastępstwa,
- limity godzin.

### Generator 3
- geolokalizacja obiektów,
- czas przejazdu,
- kolejność obiektów,
- konflikt godzin,
- obciążenie pracownika.

### Generator 4
- automatyczna optymalizacja tras,
- sugestie zmian,
- scoring harmonogramu,
- symulacja kosztu pracy.

## 8. Google Calendar

Każdy pracownik może mieć własne konto Google i kalendarz.

Po połączeniu:
- zapisujemy identyfikator kalendarza,
- przechowujemy bezpiecznie dane OAuth,
- mapujemy `schedule_task` na `google_calendar_event`.

Synchronizacja musi być idempotentna.

### Utworzenie
Nowe zadanie -> utworzenie eventu.

### Aktualizacja
Zmiana zadania -> aktualizacja istniejącego eventu.

### Usunięcie/anulowanie
Anulowane zadanie -> usunięcie lub odpowiednia aktualizacja eventu.

### Zmiana pracownika
Zadanie może zostać zsynchronizowane do kalendarza nowego pracownika, a stare powiązanie powinno zostać poprawnie obsłużone.

## 9. Status synchronizacji

- PENDING
- SYNCED
- ERROR

Błąd synchronizacji nie może powodować utraty zadania w M2 Managerze.

Użytkownik administracyjny powinien widzieć błąd i móc ponowić synchronizację.

## 10. Strefy czasowe

Organizacja posiada strefę czasową. Dla obecnego produktu domyślnie:
`Europe/Warsaw`.

Godziny harmonogramu są interpretowane w strefie organizacji.

## 11. API generatora

Docelowo:

`POST /api/schedule/generate`
`POST /api/schedule/preview`
`POST /api/schedule/regenerate`
`POST /api/schedule/tasks/{id}/sync`
`POST /api/schedule/sync`

Dokładne request/response DTO powstaną przy implementacji.

## 12. UI

Desktop:
- kalendarz,
- filtry,
- widok tygodnia,
- widok miesiąca,
- lista zadań,
- generator.

Mobile:
- przede wszystkim lista zadań pracownika,
- dzień/tydzień,
- szybki dostęp do szczegółów.

Rozbudowany panel pracownika jest kolejnym sprintem.
