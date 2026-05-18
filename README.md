# Семестровая работа по дисциплине «Введение в Web-разработку»

## Автор — Слесарев Никита Дмитриевич, ФИТ-231

## Тема №10: Трекер расходов с совместным доступом

# План-отчёт по курсовой работе: «Трекер расходов с совместным доступом»

**Дата:** май 2026

---

## Текущее состояние

Реализован **рабочий MVP** семейного трекера расходов: backend (Kotlin + Spring Boot), frontend (Vue 3 + TypeScript), PostgreSQL, Redis, Docker. Пользователи регистрируются, создают общие кошельки, ведут доходы и расходы по категориям, делят чеки, задают бюджет на месяц и настраивают автоплатежи. Подробная инструкция по запуску и API — в файле [`docs/instrukcia.md`](docs/instrukcia.md).

---

## Что есть в программе (функциональность)

### Для всех пользователей

*   Регистрация и вход (JWT), профиль по нику.
*   Список своих кошельков с ролью: **владелец** или **участник**.
*   Просмотр баланса (доход / расход / разница) — баланс **считается из операций**, не хранится вручную.
*   Добавление, редактирование и удаление своих операций; владелец может управлять чужими.
*   Удаление собственного аккаунта (с подтверждением паролем).

### Владелец кошелька (WALLET_OWNER)

*   Создание и удаление кошелька, переименование.
*   Приглашение участников **по нику** (ник задаётся при регистрации).
*   Удаление участника из кошелька.
*   Включение/отключение показа бюджета для каждого участника (`canSeeBudget`).
*   Общий и покатегорийный бюджет на текущий месяц.
*   Свои категории доходов и расходов (иконка, цвет).
*   Автоплатежи «раз в месяц» (день месяца + сумма + категория).
*   Панель настроек (⚙) на странице кошелька.

### Участник (WALLET_MEMBER)

*   Добавление операций в разрешённый кошелёк.
*   Split — разделение одного расхода между участниками (фиксированные доли).
*   Просмотр бюджета — **только если владелец разрешил** (на главной и внутри кошелька блок скрыт).
*   Выход из кошелька без его удаления.

### Отчёты

*   Отчёт за выбранный период (доходы, расходы, разбивка по категориям).
*   Расходы по участникам за период.

### Демо-данные

| Ник  | Email          | Пароль     | Кошелёк «Семья» |
|------|----------------|------------|-----------------|
| papa | papa@test.ru   | password   | владелец        |
| mama | mama@test.ru   | password   | участник        |

---

## Структура проекта

```
semka_html6sem/
├── backend/                    # API, бизнес-логика, БД
│   ├── src/main/kotlin/ru/semka/
│   │   ├── controller/         # REST-эндпоинты
│   │   ├── service/            # правила, расчёты, права
│   │   ├── repository/         # JPA → PostgreSQL
│   │   ├── domain/entity/      # сущности (Wallet, Transaction, …)
│   │   ├── dto/                # запросы и ответы API
│   │   ├── security/           # JWT, Spring Security
│   │   ├── config/             # Redis, async, scheduled
│   │   └── exception/          # единый формат ошибок
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/       # Flyway V1–V6
│   ├── src/test/kotlin/        # unit + Testcontainers (CI)
│   ├── Dockerfile
│   └── build.gradle.kts
│
├── frontend/                   # интерфейс в браузере
│   ├── src/
│   │   ├── pages/              # Login, Register, Koshelki, Koshelek
│   │   ├── components/wallet/  # OwnerPanel, IconPicker, …
│   │   ├── api/                # axios + вызовы backend / mock
│   │   ├── stores/             # Pinia: auth, theme
│   │   ├── router/
│   │   ├── types/
│   │   └── utils/
│   ├── .env.development        # VITE_USE_MOCK=false, API URL
│   └── Dockerfile
│
├── docs/                       # документация курсовой
│   ├── instrukcia.md           # подробный запуск и API
│   ├── er-diagram.md
│   ├── openapi.yaml
│   ├── scenarios.md
│   └── …
│
├── docker-compose.yml          # postgres, redis, backend, frontend
├── .github/workflows/ci.yml    # сборка backend + frontend
└── README.md                   # этот файл
```

### Сущности БД (по ТЗ)

| Сущность        | Назначение                                      |
|-----------------|-------------------------------------------------|
| User            | Пользователь (email, nick, пароль-хеш)          |
| Wallet          | Кошелёк (название, владелец)                    |
| WalletMember    | Связь пользователь ↔ кошелёк, роль, вид бюджета |
| Category        | Категория дохода/расхода                        |
| Transaction     | Операция (тип, сумма, дата, категория)          |
| SplitExpense    | Доля участника в общем чеке                     |
| Budget          | Лимит на месяц (общий или по категории)         |
| RecurringRule   | Правило автоплатежа раз в месяц                 |

---

## Что выполнено (по этапам)

### Проектирование

*   ER-диаграмма и описание сущностей (`docs/er-diagram.md`).
*   Сценарии использования, акторы (`docs/use-case-actors.md`, `docs/scenarios.md`).
*   OpenAPI-черновик (`docs/openapi.yaml`).

### Backend

*   Spring Boot 3.3, Kotlin, слои Controller → Service → Repository.
*   PostgreSQL + Flyway (миграции V1–V6, демо-данные, очистка кошельков).
*   JWT-авторизация, роли кошелька в `WalletAccessService`.
*   CRUD: кошельки, категории, операции, бюджеты, автоплатежи.
*   Split, отчёты, кэш баланса в Redis (`@Cacheable`).
*   `@Scheduled` — ежедневный запуск автоплатежей; `@Async` — аудит split.
*   Actuator (health), SpringDoc (Swagger).
*   Тесты: unit + Testcontainers в CI.

### Frontend

*   Vue 3 + TypeScript, Pinia, Vue Router, Axios.
*   Страницы: вход, регистрация, список кошельков, страница кошелька.
*   Форма операций, split, отчёты, панель владельца.
*   Режим mock (`VITE_USE_MOCK`) для разработки без backend.
*   Светлая/тёмная тема.

### Инфраструктура

*   `docker-compose.yml` — postgres, redis, backend, frontend.
*   Dockerfile для backend и frontend.
*   GitHub Actions: сборка и тесты.

---

## Быстрый запуск (для проверки)

**Требования:** JDK 21, Node 20, Docker Desktop.

```powershell
cd semka_html6sem
docker compose up -d postgres redis

cd backend
.\gradlew.bat bootRun

# новый терминал
cd frontend
npm install
npm run dev
```

В `frontend/.env.development`:

```
VITE_USE_MOCK=false
VITE_API_URL=http://localhost:8080/api
```

Сайт: http://localhost:5173  
API / Swagger: http://localhost:8080/api/swagger-ui.html

**Два пользователя на одном ПК:** обычное окно + режим инкогнито (papa и mama).

