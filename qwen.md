# InfoSec Project - Session Summary

## Overview
**Project Name:** infosec  
**Group ID:** net.security  
**Version:** 1.0.0  
**Framework:** Spring Boot 3.2.1  
**Java Version:** 17  
**Build Tool:** Maven  

## Architecture
Реактивное веб-приложение безопасности (InfoSec) построенное на **Spring WebFlux** с использованием:
- Reactive Programming (Project Reactor)
- R2DBC для реактивного доступа к базе данных
- Spring Security с JWT аутентификацией
- Thymeleaf для серверного рендеринга шаблонов

## Technology Stack

### Основные зависимости:
- `spring-boot-starter-webflux` - реактивный веб-фреймворк
- `spring-boot-starter-data-r2dbc` - реактивный доступ к БД
- `spring-boot-starter-security` + OAuth2 Client - безопасность
- `spring-boot-starter-thymeleaf` - шаблонизатор
- `spring-boot-starter-validation` - валидация данных
- `spring-boot-starter-mail` - отправка почты
- `spring-boot-starter-rsocket` - RSocket поддержка
- `jjwt` (0.11.2) - работа с JWT токенами
- `Apache POI` (5.2.2) - работа с Office документами
- `PostgreSQL` + `r2dbc-postgresql` - база данных
- `Lombok` - уменьшение бойлерплейт кода

## Структура проекта

### Пакеты:
```
net.security.infosec/
├── InfosecApplication.java          # Точка входа
├── configurations/                  # Конфигурации безопасности и приложения
│   ├── ApplicationConfig.java
│   ├── SecurityConfig.java         # Настройки Spring Security + JWT
│   └── WebFluxConfig.java
├── controllers/
│   ├── rest/                       # REST API контроллеры
│   │   ├── ApiController.java
│   │   └── ServiceRestController.java
│   ├── web/                        # Web контроллеры (Thymeleaf)
│   │   ├── AdminController.java
│   │   ├── GuideController.java
│   │   ├── HomeController.java
│   │   ├── InstructionController.java
│   │   ├── TaskController.java
│   │   ├── XMLController.java
│   │   └── XMLController2.java
│   └── security/                   # Обработчики ошибок безопасности
│       └── SecurityControllerAdvice.java
├── services/                       # Бизнес-логика
│   ├── DepartmentService.java
│   ├── DivisionService.java
│   ├── EmployeeService.java
│   ├── ImplementerService.java    # Управление исполнителями (пользователями)
│   ├── TaskService.java
│   ├── TroubleTicketService.java
│   └── WebFluxWebSocketHandler.java
├── repositories/                   # R2DBC репозитории
│   ├── CategoryRepository.java
│   ├── DepartmentRepository.java
│   ├── DivisionRepository.java
│   ├── EmployeeRepository.java
│   ├── ImplementerRepository.java
│   ├── TaskRepository.java
│   └── TroubleRepository.java
├── models/
│   ├── dto/                       # Data Transfer Objects
│   │   ├── CategoryDTO.java
│   │   ├── ChartDTO.java
│   │   ├── ConstructDTO.java
│   │   ├── DateDTO.java
│   │   ├── DepartmentDTO.java
│   │   ├── DivisionDTO.java
│   │   ├── DivisionNode.java
│   │   ├── EmployeeDTO.java
│   │   ├── GuideDTO.java
│   │   ├── ImplementerDataTransferObject.java
│   │   ├── PasswordDTO.java
│   │   ├── PersonDTO.java
│   │   ├── PersonNode.java
│   │   ├── StatDataTransferObject.java
│   │   ├── TaskDataTransferObject.java
│   │   ├── TaskDTO.java
│   │   ├── TicketDataTransferObject.java
│   │   ├── TroubleDTO.java
│   │   └── TroubleTicketDataTransferObject.java
│   └── entity/                    # Сущности базы данных
│       ├── Category.java
│       ├── Department.java
│       ├── DepartmentRole.java
│       ├── Division.java
│       ├── Employee.java
│       ├── Implementer.java
│       ├── Role.java
│       ├── Task.java
│       └── Trouble.java
├── component/                      # JWT компоненты безопасности
│   ├── JwtAuthenticationConverter.java
│   ├── JwtAuthenticationManager.java
│   ├── JwtAuthenticationSuccessHandler.java
│   └── JwtLogoutSuccessHandler.java
├── utils/                          # Утилиты
│   ├── Checker.java
│   ├── JWT.java                   # Работа с JWT токенами
│   ├── MonthConverter.java
│   ├── NamingUtil.java
│   └── Translator.java
├── validations/                    # Валидаторы
│   └── PasswordValidation.java
└── converters/                     # Конвертеры
    └── RoleConverter.java
```

## База данных (schema.sql)

### Таблицы:
1. **implementer** - пользователи системы (исполнители)
   - id, email, password, firstname, middle_name, lastname
   - office_position, role, departmentRole
   - task_ids (массив ID задач)

2. **task** - задачи
   - id, title, description, execute_date
   - trouble_id, implementer_id

3. **category** - категории проблем
   - id, name, description, department_role
   - trouble_ids (массив ID проблем)

4. **trouble** - проблемы/инциденты
   - id, name, description, department_role
   - category_id

5. **department** - департаменты
   - id, number, title, description
   - division_ids (массив ID подразделений)

6. **division** - подразделения
   - id, number, department_id, title, description

7. **employee** - сотрудники
   - id, number, name, middle_name, lastname, position
   - department_id, division_id
   - cabinet, address, phone, personal_phone, email

## Конфигурация (application.properties)

```properties
server.port=8080

# Переменные окружения (требуется настройка)
db.username=
db.password=
db.url=
jwt.secret=
jwt.expiration.access=3600000      # 1 час
jwt.expiration.refresh=86400000    # 24 часа

# R2DBC конфигурация
spring.r2dbc.url=${db.url}
spring.r2dbc.username=${db.username}
spring.r2dbc.password=${db.password}
spring.sql.init.mode=always
```

## Безопасность

### SecurityConfig:
- Реактивная безопасность с `EnableWebFluxSecurity`
- JWT аутентификация через кастомные компоненты
- CSRF защита с поддержкой multipart данных
- Path matchers для публичных ресурсов:
  - `/favicon.ico`, `/img/**`, `/css/**`, `/js/**`, `/fw/**`
  - `/login`, `/guide/list`, `/api/**`
- Форма логина с кастомной страницей `/login`
- Ролевая модель: `ADMIN`, `GUIDE_ADMIN`

### JWT Компоненты:
- `JwtAuthenticationManager` - менеджер аутентификации
- `JwtAuthenticationConverter` - конвертер токенов
- `JwtAuthenticationSuccessHandler` - обработчик успешной аутентификации
- `JwtLogoutSuccessHandler` - обработчик выхода

## API Endpoints (ApiController)

### Публичные:
- `GET /api/search/free/employees?query=` - поиск свободных сотрудников
- `GET /api/search/employees?search=` - поиск сотрудников
- `GET /api/guide/pdf` - генерация PDF руководства

### Защищенные (ADMIN, GUIDE_ADMIN):
- `POST /api/add/employee` - добавление сотрудника
- `GET /api/pinout/employee?employeeId=` - отвязка сотрудника
- `GET /api/auth/employee/update` - обновление данных сотрудника

## Основные функции системы

1. **Управление сотрудниками (Employee)**
   - Добавление, редактирование, поиск
   - Привязка к департаментам и подразделениям
   - Отслеживание статуса (свободен/занят)

2. **Управление задачами (Task)**
   - Создание задач из проблем (Trouble)
   - Назначение исполнителей (Implementer)
   - Отслеживание сроков выполнения

3. **Управление проблемами (Trouble/Ticket)**
   - Категоризация проблем
   - Привязка к ролям департаментов
   - Создание задач на основе проблем

4. **Справочник организации (Guide)**
   - Структура департаментов и подразделений
   - Информация о сотрудниках
   - Генерация PDF отчетов

5. **Аутентификация и авторизация**
   - JWT токены (access + refresh)
   - Ролевая модель
   - Интеграция с OAuth2

## Примечания для разработки

- Проект использует реактивный стек (Mono/Flux)
- Требуется настройка переменных окружения для БД и JWT секрета
- Схема БД создается автоматически при старте (`spring.sql.init.mode=always`)
- Статические ресурсы находятся в `/src/main/resources/static/`
- HTML шаблоны в `/src/main/resources/templates/`

---
**Дата создания документа:** 2024
**Статус:** Активная разработка
