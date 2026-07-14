# 🔐 InfoSec

**Корпоративная платформа управления информационной безопасностью** — учёт задач, реестр информационных систем, справочник сотрудников, KPI-аналитика и интеграция с Active Directory.

---

## 📋 Стек технологий

| Категория | Технология |
|---|---|
| **Runtime** | Java 17 |
| **Framework** | Spring Boot 3.2.1 |
| **Reactive** | Spring WebFlux, Project Reactor, R2DBC |
| **Безопасность** | Spring Security, JWT (access + refresh), OAuth2 Client |
| **База данных** | PostgreSQL 16 (основная), Flyway (миграции) |
| **Шаблоны** | Thymeleaf (SSR) |
| **Отчёты** | Apache POI 5.2.2 (Excel) |
| **LDAP** | Spring LDAP (Active Directory) |
| **Сборка** | Maven, Docker (multi-stage) |
| **Коммуникации** | RSocket, WebSocket |

---

## 🏗️ Архитектура

```
┌──────────────────────────────────────────────────────┐
│                    Браузер (Thymeleaf SSR)            │
├──────────────────────────────────────────────────────┤
│                  Spring WebFlux (Netty)               │
│  ┌─────────────┐  ┌──────────────────────────────┐   │
│  │ Controllers │  │      REST API (/api/*)        │   │
│  │  (web/)     │  │                               │   │
│  └──────┬──────┘  └──────────────┬───────────────┘   │
│         │                        │                    │
│  ┌──────┴────────────────────────┴───────────────┐   │
│  │              Spring Security                   │   │
│  │    JWT Auth · CSRF · @PreAuthorize · Roles     │   │
│  └──────────────────────┬────────────────────────┘   │
│                         │                             │
│  ┌──────────────────────┴────────────────────────┐   │
│  │                 Services                       │   │
│  │  Implementer · Employee · Task · JobSystem     │   │
│  │  LdapSync · ExcelReport · TroubleTicket        │   │
│  └──────────────────────┬────────────────────────┘   │
│                         │                             │
│  ┌──────────────────────┴────────────────────────┐   │
│  │            R2DBC Repositories                  │   │
│  └──────────────────────┬────────────────────────┘   │
└─────────────────────────┼────────────────────────────┘
                          │
              ┌───────────┴───────────┐
              │    PostgreSQL 16      │
              │  (Flyway migrations)  │
              └───────────────────────┘
```

### Ключевые архитектурные решения

- **Реактивный non-blocking I/O** — все операции с БД асинхронны (Mono/Flux)
- **Два источника данных**: R2DBC (основной, реактивный) + JDBC (Flyway-миграции)
- **Блокирующий LDAP** изолирован через `Schedulers.boundedElastic()`
- **Единый шаблон** `template.html` — все страницы собираются из фрагментов `block/header · navbar · content/* · footer`

---

## 📁 Структура проекта

```
infosec/
├── src/main/java/net/security/infosec/
│   ├── InfosecApplication.java          # Точка входа + @EnableScheduling
│   ├── configurations/
│   │   ├── SecurityConfig.java          # WebFlux-безопасность + JWT-фильтр
│   │   ├── ApplicationConfig.java       # PasswordEncoder, Flyway DataSource
│   │   └── WebFluxConfig.java           # WebSocket, сессии
│   ├── controllers/
│   │   ├── rest/
│   │   │   └── ApiController.java       # 40+ REST-эндпоинтов
│   │   ├── web/
│   │   │   ├── HomeController.java      # Дашборд, логин, профиль
│   │   │   ├── TaskController.java      # Задачи + KPI-статистика
│   │   │   ├── GuideController.java     # Справочник (публичный)
│   │   │   ├── AdminController.java     # Админка (CRUD)
│   │   │   ├── InstructionController.java
│   │   │   └── XMLController*.java      # XML-экспорт
│   │   └── security/
│   │       └── SecurityControllerAdvice.java
│   ├── component/                       # JWT-компоненты
│   │   ├── JwtAuthenticationManager.java
│   │   ├── JwtAuthenticationConverter.java
│   │   ├── JwtAuthenticationSuccessHandler.java
│   │   └── JwtLogoutSuccessHandler.java
│   ├── models/
│   │   ├── entity/   (13 сущностей)
│   │   └── dto/      (20+ DTO)
│   ├── repositories/ (7 R2DBC-репозиториев)
│   ├── services/     (13 сервисов)
│   ├── utils/        (JWT, Translator, Checker, MonthConverter, NamingUtil)
│   ├── validations/  (PasswordValidation)
│   └── converters/   (RoleConverter)
├── src/main/resources/
│   ├── db/migration/                    # Flyway V1–V11
│   ├── templates/                       # 38 Thymeleaf-шаблонов
│   │   ├── template.html
│   │   ├── block/  (header, navbar, footer)
│   │   └── content/ (страницы)
│   ├── static/      (CSS, JS, изображения)
│   └── application.properties
├── docker-compose.yml
├── Dockerfile        (multi-stage: JDK build → JRE run)
└── pom.xml
```

---

## 🚀 Быстрый старт

### Предварительные требования

- Docker + Docker Compose
- Или: Java 17, Maven 3.8+, PostgreSQL 16

### Запуск через Docker

```bash
# 1. Клонировать репозиторий
git clone <repo-url> && cd infosec

# 2. Создать .env файл с переменными окружения
echo "DB_USERNAME=postgres" > .env
echo "DB_PASSWORD=your_password" >> .env
echo "JWT_SECRET=your-256-bit-secret-key-here-min-32-chars!!" >> .env

# 3. Запустить
docker compose up -d
```

Приложение доступно: **http://localhost:8001**

Проверка здоровья: **http://localhost:8001/api/health**

### Ручная сборка

```bash
# Настроить переменные в application.properties:
#   db.username, db.password, db.url, jwt.secret

./mvnw clean package -DskipTests
java -jar target/infosec-1.0.0.jar
```

---

## ⚙️ Переменные окружения

| Переменная | Назначение | По умолчанию |
|---|---|---|
| `DB_USERNAME` | Пользователь PostgreSQL | — |
| `DB_PASSWORD` | Пароль PostgreSQL | — |
| `DB_URL` | Хост PostgreSQL (формат: `host:port/db`) | — |
| `JWT_SECRET` | Секретный ключ для подписи JWT (≥256 бит) | — |
| `jwt.expiration.access` | Время жизни access-токена (мс) | `3600000` (1 час) |
| `jwt.expiration.refresh` | Время жизни refresh-токена (мс) | `86400000` (24 часа) |

---

## 🗄️ База данных

### Таблицы и их назначение

| Таблица | PK | Назначение |
|---|---|---|
| `implementer` | `id` (int) | Пользователи системы (Spring Security UserDetails) |
| `task` | `id` (int) | Задачи, создаваемые по проблемам |
| `category` | `id` (int) | Категории проблем (привязаны к department_role) |
| `trouble` | `id` (int) | Типы проблем внутри категорий |
| `department` | `id` (bigint) | Департаменты (оргструктура) |
| `division` | `id` (bigint) | Подразделения внутри департаментов |
| `employee` | `id` (bigint) | Сотрудники (справочник, синхронизация из AD) |
| `job_system` | `uuid` (UUID) | Реестр информационных систем |
| `employee_job_system` | `(employee_id, job_system_uuid)` | Связь сотрудник ↔ система |
| `app_settings` | `key` (text) | Key-value настройки приложения |

### ER-диаграмма связей

```
department ──1:N── division
    │                  │
    │                  │
    └──── 1:N ──── employee ──── N:M ──── job_system
                                          (employee_job_system)

category ──1:N── trouble ──1:N── task ──N:1── implementer
```

### Миграции (Flyway V1–V11)

| Версия | Описание |
|---|---|
| **V1** | Начальная схема (с массивами) |
| **V2** | Удаление массивов (division_ids, task_ids, trouble_ids) |
| **V3** | Индексы на FK-колонки и email |
| **V4** | start_time / end_time в task (KPI) |
| **V5** | work_day_end в implementer |
| **V6** | job_system — реестр информационных систем |
| **V7** | employee_job_system — M:N связь |
| **V8** | connect_date, disconnect_date, status, mchd, role_in_system |
| **V9** | foundation — основание МЧД |
| **V10** | mchd_basis, mchd_expiry, accesses |
| **V11** | app_settings — key-value хранилище (+ feature-флаг LDAP) |

---

## 👥 Ролевая модель

### Пользовательские роли (Implementer.role)

| Роль | Права |
|---|---|
| **WORKER** | Просмотр и выполнение своих задач |
| **MANAGER** | Просмотр задач своего отдела, статистика |
| **DIRECTOR** | Контроль и отчётность |
| **ADMIN** | Полный доступ: пользователи, категории, проблемы, системы, LDAP-синхронизация |
| **GUIDE_ADMIN** | Только справочник: сотрудники, департаменты, подразделения |

### Роли департаментов (DepartmentRole)

| Роль | Назначение |
|---|---|
| **IB** | Информационная безопасность (доступ к реестру систем) |
| **IT** | Техническая поддержка |
| **ALL** | Все |

> Роль `IB` имеет доступ к странице `/task/systems` — отчёт по информационным системам для службы безопасности.

---

## 🔌 API Endpoints

### Публичные (без аутентификации)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/api/health` | Health-check для Docker |
| `GET` | `/api/search/free/employees?query=` | Поиск свободных сотрудников (limit 10) |
| `GET` | `/api/search/employees?search=` | Поиск сотрудников по ФИО |
| `GET` | `/guide/list` | Справочник организации (публичная страница) |
| `GET` | `/login` | Страница входа |

### Справочник (ADMIN, GUIDE_ADMIN)

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/api/add/employee` | Добавить сотрудника |
| `GET` | `/api/pinout/employee?employeeId=` | Открепить сотрудника от структуры |
| `GET` | `/api/auth/employee/update` | Обновить данные сотрудника |
| `POST` | `/api/department/add` | Добавить департамент |
| `POST` | `/api/department/edit` | Редактировать департамент |
| `DELETE` | `/api/department/{id}` | Удалить департамент (каскадно) |
| `POST` | `/api/division/add` | Добавить подразделение |
| `POST` | `/api/division/edit` | Редактировать подразделение |
| `DELETE` | `/api/division/{id}` | Удалить подразделение |
| `POST` | `/api/department/reorder` | Переупорядочить департаменты (drag-and-drop) |
| `POST` | `/api/division/reorder` | Переупорядочить подразделения |
| `POST` | `/api/employee/reorder` | Переупорядочить сотрудников |

### Категории и проблемы (ADMIN)

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/api/category/add` | Добавить категорию |
| `POST` | `/api/category/edit/{id}` | Редактировать категорию |
| `DELETE` | `/api/category/{id}` | Удалить категорию |
| `POST` | `/api/trouble/add` | Добавить проблему в категорию |
| `POST` | `/api/trouble/edit/{id}` | Редактировать проблему |
| `DELETE` | `/api/trouble/{id}` | Удалить проблему |

### KPI и отчёты

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/api/search/implementers?role=` | Список исполнителей с фильтром по роли |
| `GET` | `/api/kpi/data?from=&to=&dept=&empId=` | KPI-данные за период |
| `GET` | `/api/report/excel` | Экспорт отчёта в Excel |
| `GET` | `/api/guide/pdf` | Генерация PDF справочника |

### Информационные системы (ADMIN + IB)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/api/systems?page=&size=&search=&type=` | Пагинированный список систем |
| `GET` | `/api/system/{uuid}` | Получить систему по UUID |
| `POST` | `/api/system` | Создать систему |
| `PUT` | `/api/system/{uuid}` | Обновить систему |
| `DELETE` | `/api/system/{uuid}` | Удалить систему |
| `GET` | `/api/system/{uuid}/employees` | Список привязанных сотрудников |
| `POST` | `/api/system/{uuid}/employees?employeeId=` | Привязать сотрудника |
| `DELETE` | `/api/system/{uuid}/employees/{employeeId}` | Отвязать сотрудника |
| `GET` | `/api/system/{uuid}/employees/{employeeId}` | Настройки связи (МЧД, роль) |
| `PUT` | `/api/system/{uuid}/employees/{employeeId}` | Обновить настройки связи |
| `GET` | `/api/report/systems` | Отчёт по системам для ИБ |
| `GET` | `/api/report/systems/excel` | Экспорт отчёта по системам |

### LDAP (ADMIN)

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/api/admin/sync-directory` | Ручной запуск синхронизации AD → БД |
| `GET` | `/api/admin/settings/ldap-sync` | Статус feature-флага LDAP |
| `PUT` | `/api/admin/settings/ldap-sync` | Включить/выключить автосинхронизацию |
| `GET` | `/api/admin/settings/ldap-config` | Текущие LDAP-настройки |
| `PUT` | `/api/admin/settings/ldap-config` | Сохранить LDAP-настройки |
| `POST` | `/api/admin/settings/ldap-test` | Проверить подключение к LDAP |

---

## 🔐 Безопасность

### JWT-аутентификация

- **Access-токен**: 1 час, используется для доступа к API
- **Refresh-токен**: 24 часа, для обновления access-токена
- **Алгоритм**: HMAC-SHA512
- **Хранение**: Secure HttpOnly cookies (`session-info-security`)
- **Цифровая подпись**: дополнительный claim `digital_signature` для проверки целостности

### Защита

- **CSRF**: включён, с поддержкой multipart-данных
- **Пароли**: `DelegatingPasswordEncoder` (bcrypt по умолчанию)
- **@PreAuthorize**: метод-level безопасность на всех защищённых эндпоинтах
- **Публичные ресурсы**: `/favicon.ico`, `/img/**`, `/css/**`, `/js/**`, `/fw/**`, `/login`, `/guide/list`, `/api/**`
- **LDAP-пароль**: шифруется перед сохранением в `app_settings`

---

## 🔄 LDAP-синхронизация

Синхронизация справочника сотрудников из Active Directory:

1. **Сохранение порядка**: номера департаментов/подразделений сохраняются перед сбросом
2. **Сброс структуры**: удаляются все Department + Division, открепляются Employee
3. **Загрузка из AD**: активные пользователи с `mail` и `telephoneNumber`
4. **Парсинг DN**: извлечение OU-цепочки → маппинг на Division + Department
5. **Создание/обновление**: Department → Division → Employee
6. **Очистка**: открепление сотрудников, отсутствующих в AD + INACTIVE в `employee_job_system`
7. **Восстановление порядка**: числа восстанавливаются из сохранённых

**Доступ**: ручная кнопка на `/profile` (ADMIN) + автоматический шедулер.

**Настройки LDAP** (все в БД через `/profile`):
- `ldap.sync.enabled` — feature-флаг
- `ldap.url`, `ldap.base`, `ldap.userDn`, `ldap.password` (зашифрован)

---

## 📊 Ключевые функции

### Тикет-система

```
Категория (category) → Проблема (trouble) → Задача (task) → Исполнитель (implementer)
```

- Категории и проблемы привязаны к `DepartmentRole` (IB/IT)
- Задачи содержат время выполнения (start_time/end_time) для KPI-учёта
- Статистика по дням/неделям/месяцам/году — графики ApexCharts на дашборде
- KPI-страница с фильтрацией по датам, отделам и сотрудникам

### Справочник сотрудников

- Древовидная структура: Департамент → Подразделение → Сотрудник
- Drag-and-drop сортировка (SortableJS)
- Поиск сотрудников, привязка/открепление
- Синхронизация с Active Directory
- Публичная страница `/guide/list`

### Информационные системы

- Реестр систем: название, описание, URL, тип (внутренняя/внешняя)
- Привязка сотрудников с настройками:
  - Дата подключения/отключения
  - Статус (активен/неактивен)
  - МЧД (машиночитаемая доверенность): основание, срок действия, доступы
  - Роль в системе
- Отчёт для ИБ: `/task/systems` с фильтрацией и экспортом в Excel

### Экспорт отчётов

- **Excel**: KPI-отчёт + отчёт по информационным системам
- **PDF**: справочник организации
- **XML**: экспорт контактов (`/contacts.xml`, `/contacts/group/list.xml`)

---

## 🐳 Развёртывание

### Docker Compose (рекомендуемый)

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: infosec
    volumes:
      - infosec:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready"]

  app:
    build: .
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8001:8080"
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/api/health"]
```

### Multi-stage Dockerfile

1. **builder** (eclipse-temurin:17-jdk-jammy): копирует исходники → `mvnw package`
2. **runner** (eclipse-temurin:17-jre-jammy): минимальный JRE-образ, запуск от `spring` пользователя

---

## 🛠️ Разработка

```bash
# Запуск в dev-режиме (с hot-reload через spring-boot-devtools)
./mvnw spring-boot:run

# Прогнать тесты
./mvnw test

# Собрать без тестов
./mvnw clean package -DskipTests

# Если миграции Flyway конфликтуют — исправить checksums
./mvnw flyway:repair
```

---

## 📝 Примечания

- Приложение использует **реактивный стек** — все операции с БД неблокирующие (Mono/Flux)
- LDAP-автоконфигурация Spring Boot **отключена** (`spring.autoconfigure.exclude`), LdapTemplate создаётся динамически из настроек в БД
- Spring Data LDAP и R2DBC конфликтуют — LDAP-репозитории отключены (`spring.data.ldap.repositories.enabled=false`)
- Для таблиц с составными ключами (employee_job_system) используется `DatabaseClient.fetch().rowsUpdated()` вместо стандартных R2DBC-репозиториев
- Схема БД управляется **только Flyway**, `spring.sql.init.mode=never`

---

**Лицензия**: проприетарная · **Версия**: 1.0.0
