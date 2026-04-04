# gel
Codul echipei gel pentru HackItAll 2026, proba jetbrains

## Backend (Ktor + PostgreSQL)

Serverul este in modulul `server` si expune API-ul REST pentru `pins` si `comments`.

### Config DB

Variabile recomandate:

- `JDBC_DATABASE_URL` (ex: `jdbc:postgresql://localhost:5432/hackitall`)
- `DB_USER`
- `DB_PASSWORD`

Daca variabilele lipsesc, se folosesc valorile din `server/src/main/resources/application.conf`.

### Run

```bash
./gradlew :server:run
```

La startup, `DatabaseFactory` ruleaza `SchemaUtils.create()` pentru tabelele:
`users`, `pins`, `comments`, `pin_images`.

