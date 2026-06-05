---
name: frontend-stats-from-data
description: Подсчёт индикаторов/статистики на фронте из загруженных данных вместо отдельного API-запроса
source: auto-skill
extracted_at: '2026-06-08T11:00:00.000Z'
---

# Frontend Statistics from Loaded Data

Вместо отдельного API-запроса для статистики (количество систем, активных/неактивных и т.д.) — считать из данных, которые уже загружены для отображения. Это гарантирует, что индикаторы всегда отражают то, что реально показано пользователю.

## JS — функция подсчёта

```javascript
function updateStats(data) {
    var systems = data ? data.length : 0;
    var total = 0, active = 0, inactive = 0;
    if (data) {
        for (var i = 0; i < data.length; i++) {
            var items = data[i].employees || data[i].items || [];
            total += items.length;
            for (var j = 0; j < items.length; j++) {
                if (items[j].status === 'ACTIVE') active++;
                else inactive++;
            }
        }
    }
    document.getElementById('statSystems').textContent = systems;
    document.getElementById('statTotal').textContent = total;
    document.getElementById('statActive').textContent = active;
    document.getElementById('statInactive').textContent = inactive;
}
```

## HTML — индикаторы (Smoke Theme)

```html
<div class="d-flex gap-3 mt-3 flex-wrap px-3 pb-3" id="statsIndicators">
    <div class="stat-summary-card flex-grow-1" style="min-width:140px;">
        <div class="stat-summary-card__header">Количество систем</div>
        <div class="stat-summary-card__body"><div class="stat-total" id="statSystems">—</div></div>
    </div>
    <!-- ещё 3 карточки аналогично -->
</div>
```

## Интеграция с loadData()

```javascript
function loadData() {
    fetch(url).then(r => r.json()).then(data => {
        // Сначала фильтрация (чекбоксы, etc)
        data = applyFilters(data);
        // Затем статистика ИЗ ТЕХ ЖЕ ДАННЫХ
        updateStats(data);
        // Затем рендеринг
        renderGrid(data);
    });
}
```

## Преимущества

- **Всегда точные** — индикаторы отражают реально показанные данные
- **Без лишних запросов** — нет отдельного API `/stats`
- **Учитывают все фильтры** — тип, поиск, чекбоксы, статус — автоматически
- **Не требует синхронизации** — один источник данных

## Когда НЕ использовать

- Когда данных очень много (тысячи записей) и API-статистика с `COUNT(*)` эффективнее
- Когда индикаторы должны показывать общее количество (не фильтрованное)
