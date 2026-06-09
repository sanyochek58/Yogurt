# Yogurt VPN

Self-hosted VPN-сервис на базе протокола **VLESS + REALITY** (Xray-core).
Проект состоит из backend-сервера, Android-клиента и веб-админки для модерации заявок на доступ.

Пользователь регистрируется в приложении, отправляет заявку на доступ, администратор её подтверждает — после чего клиент автоматически получает персональную VPN-конфигурацию и подключается одним нажатием.

---

## Архитектура

```
┌──────────────┐      HTTPS       ┌──────────────────────────────────┐
│ Android-      │ ───────────────► │            Caddy (TLS)           │
│ клиент        │                  │           reverse proxy          │
└──────┬───────┘                  └────────────────┬─────────────────┘
       │                                           │
       │ VLESS + REALITY                           ▼
       │ (прямое VPN-соединение)         ┌──────────────────┐
       │                                 │  Backend (Ktor)  │
       │                                 └────────┬─────────┘
       ▼                                          │
┌──────────────┐                          ┌───────▼────────┐
│  Xray-core   │ ◄────────────────────────┤  PostgreSQL    │
│  (VPN-нода)  │                          └────────────────┘
└──────────────┘
                            ┌──────────────────┐
                            │  Web-админка     │  ── модерация заявок
                            └──────────────────┘
```

## Структура репозитория

| Каталог          | Описание                                                             |
|------------------|---------------------------------------------------------------------|
| `backend/`       | REST API на Ktor (Kotlin): аутентификация, заявки, выдача VPN-конфигов |
| `client_android/`| Android-приложение (Jetpack Compose) с встроенным VPN-сервисом       |
| `frontend/admin/`| Веб-админка для подтверждения/отклонения заявок (HTML/CSS/JS)        |
| `infra/`         | Docker Compose, Caddy, конфигурация Xray для деплоя                  |

---

## Технологии

**Backend**
- Kotlin + [Ktor](https://ktor.io) 3.x (Netty)
- PostgreSQL 16, [Exposed](https://github.com/JetBrains/Exposed) ORM, HikariCP
- Миграции — [Flyway](https://flywaydb.org)
- Аутентификация — JWT (`java-jwt`), пароли — bcrypt (`jbcrypt`)
- Отправка email — `simple-java-mail` (SMTP)

**Android-клиент**
- Kotlin, Jetpack Compose (Material 3)
- Ktor Client для сетевых запросов
- libv2ray (`libv2ray.aar`) — встроенное VPN-ядро (VLESS + REALITY)

**Инфраструктура**
- Docker / Docker Compose
- Caddy 2 (автоматический TLS)
- Xray-core (VPN-нода)

---

## API

Базовый префикс: `/api/v1`

### Публичные

| Метод | Путь                  | Описание                  |
|-------|-----------------------|---------------------------|
| GET   | `/health`             | Проверка состояния сервиса |
| POST  | `/api/v1/auth/register` | Регистрация               |
| POST  | `/api/v1/auth/login`    | Вход, выдача JWT          |

### Пользователь (`auth-user`)

| Метод | Путь                                  | Описание                       |
|-------|---------------------------------------|--------------------------------|
| POST  | `/api/v1/user/access-requests`        | Отправить заявку на доступ     |
| GET   | `/api/v1/user/access-requests/my`     | Статус своей заявки            |
| GET   | `/api/v1/user/vpn-config`             | Получить персональный VLESS-конфиг |

### Администратор (`auth-admin`)

| Метод | Путь                                            | Описание              |
|-------|-------------------------------------------------|-----------------------|
| GET   | `/api/v1/admin/access-requests`                 | Список всех заявок    |
| POST  | `/api/v1/admin/access-requests/{id}/approve`    | Подтвердить заявку    |
| POST  | `/api/v1/admin/access-requests/{id}/reject`     | Отклонить заявку      |

---

## Запуск (production / staging)

Развёртывание выполняется через Docker Compose в каталоге `infra/`.

1. Скопируйте файл с переменными окружения и заполните его:

   ```bash
   cd infra
   cp .env.example .env
   ```

2. Заполните `.env`:

   | Переменная | Назначение |
   |------------|------------|
   | `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Параметры БД |
   | `JWT_SECRET` | Секрет для подписи JWT (длинная случайная строка) |
   | `XRAY_SERVER_HOST` / `XRAY_SERVER_PORT` | Адрес и порт VPN-ноды |
   | `XRAY_PUBLIC_KEY` / `XRAY_SHORT_ID` / `XRAY_CLIENT_ID` | Параметры REALITY (должны совпадать с `xray/config/config.json`) |
   | `EMAIL_HOST` / `EMAIL_PORT` / `EMAIL_USERNAME` / `EMAIL_PASSWORD` / `EMAIL_FROM` | SMTP для уведомлений |

3. Запустите стек:

   ```bash
   docker compose up -d
   ```

Поднимутся сервисы: `postgres`, `backend`, `caddy` (TLS-прокси) и `xray` (VPN-нода).
Миграции БД (Flyway) применяются автоматически при старте backend.

---

## Локальная разработка

### Backend

```bash
cd backend
./gradlew run            # запуск сервера разработки
./gradlew buildFatJar    # сборка исполняемого JAR
./gradlew test           # тесты
```

Для локальной БД можно использовать `backend/docker-compose.dev.yml`.
Параметры приложения задаются в `src/main/resources/application.conf`.

### Android-клиент

```bash
cd client_android
./gradlew assembleDebug
```

Или откройте каталог `client_android/` в Android Studio.
Базовый URL API задаётся в `core/network/YogurtApi.kt`.

### Веб-админка

Статика в `frontend/admin/`. Базовый URL API — константа `API_BASE_URL` в `app.js`.
Достаточно отдать каталог любым статическим сервером.

---

## Безопасность

- Файл `infra/.env` содержит секреты и **не должен попадать в git**.
- Значения в `.env.example` приведены как образец — замените их перед деплоем.
- `JWT_SECRET`, пароли БД и SMTP обязательно меняются на собственные.

---

## Автор

**Бураев Александр**

