---
name: excel-export-date-filter
description: Экспорт Excel с фильтром по датам — использование Thymeleaf inline для передачи дат из формы в JS
source: auto-skill
extracted_at: '2026-06-19T10:09:41.480Z'
---

При экспорте Excel с фильтром по датам через JS-функцию, нужно правильно передать выбранные даты в API.

**Проблема:** Thymeleaf-форма с `th:field="*{begin}"` создаёт `id="begin"`, но полагаться на ID ненадёжно (может конфликтовать, меняться). Кроме того, URL страницы может не отражать режим `date` (например, POST на `/task/info/date` не меняет адресную строку — остаётся `/task/info/week`).

**Решение:**
1. Использовать Thymeleaf inline `/*[[${dates.begin}]]*/` для прямой передачи дат из серверной модели в JS-переменные
2. Заменять `mode` в URL экспорта с `week/month/year` на `date` через regex `/mode=\w+/` → `mode=date`

**Шаблон JS-функции:**
```javascript
function exportExcel() {
    var path = window.location.pathname;
    var parts = path.split('/');
    var mode = (parts.length > 3) ? parts[3] : 'week';
    var baseUrl = '/api/report/excel?mode=' + mode + '&userId=-1';

    // Даты напрямую из серверной модели (Thymeleaf inline)
    var dateFrom = /*[[${dates.begin}]]*/ '';
    var dateTo = /*[[${dates.end}]]*/ '';
    if(dateFrom && dateTo){
        baseUrl += '&dateFrom=' + dateFrom + '&dateTo=' + dateTo;
        baseUrl = baseUrl.replace(/mode=\w+/, 'mode=date');
    }

    window.open(baseUrl, '_blank');
}
```

**Важно:** `<script>` должен иметь `th:inline="javascript"` для работы `/*[[...]]*/`.

**Where used:** `task-info-page.html`, `task-stat-page.html`.

**Why:** Thymeleaf inline надёжнее чем DOM-поиск по id (id могут не совпадать, форма может быть не в DOM при JS-рендеринге). Regex-замена mode гарантирует корректный режим независимо от текущего URL.
