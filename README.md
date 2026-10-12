# Currency Converter — USD → PLN (NBP)

Aplikacja przelicza koszt zakupu komputerów z USD na PLN według średniego kursu NBP,
zapisuje wynik do bazy danych i pliku XML oraz pozwala wyszukiwać i sortować zapisane pozycje.

Zadanie rekrutacyjne — w trakcie realizacji.

## Stos technologiczny

- Java 21
- Spring Boot 4.1
- PostgreSQL 17 (Docker Compose)
- Flyway
- Maven (Maven Wrapper)

## Wymagania

- JDK 21
- Docker z Docker Compose

## Uruchomienie

```bash
./mvnw spring-boot:run
```

Spring Boot sam uruchamia kontener PostgreSQL z pliku `compose.yaml`.

Zatrzymanie bazy:

```bash
docker compose down
```

## Weryfikacja

- http://localhost:8080/actuator/health — `"status": "UP"`
- http://localhost:8080/swagger-ui.html — Swagger UI