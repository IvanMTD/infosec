---
name: bootstrap-multiselect-dropdown
description: Мультиселект-дропдаун с чекбоксами на Bootstrap — загрузка, фильтрация, отображение выбранного
source: auto-skill
extracted_at: '2026-06-08T11:00:00.000Z'
---

# Bootstrap Multi-select Dropdown with Checkboxes

Замена стандартному `<select multiple>` — кнопка-дропдаун с чекбоксами внутри, AJAX-загрузка списка, клиентская фильтрация.

## HTML

```html
<div class="dropdown w-100">
    <button class="btn btn-outline-secondary w-100 text-start dropdown-toggle d-flex justify-content-between align-items-center"
            type="button" id="dropdownBtn" data-bs-toggle="dropdown"
            style="border:1px solid #dee2e6; border-radius:0.375rem;">
        <span class="text-truncate">Все</span>
    </button>
    <ul class="dropdown-menu px-2" id="checkList"
        style="min-width:350px;max-height:250px;overflow-y:auto;"
        onclick="event.stopPropagation()">
        <li class="text-muted text-center py-1">Загрузка...</li>
    </ul>
</div>
```

Ключевые моменты:
- `onclick="event.stopPropagation()"` — чтобы клик по чекбоксу не закрывал дропдаун
- `min-width:350px` — достаточно широко, без горизонтальной прокрутки
- `max-height + overflow-y:auto` — вертикальная прокрутка при большом списке

## JS — загрузка списка

```javascript
function loadList() {
    fetch('/api/items?filter=' + filterValue)
    .then(r => r.json())
    .then(data => {
        var html = '';
        data.forEach(item => {
            html += `<li class="dropdown-item" style="cursor:default;">
                <label class="w-100 mb-0" style="cursor:pointer;">
                    <input type="checkbox" value="${item.name}" onchange="onCheckChange()" class="me-2">
                    ${item.name}
                </label></li>`;
        });
        document.getElementById('checkList').innerHTML = html || '<li class="text-muted text-center py-1">Нет данных</li>';
        onCheckChange();
    });
}
```

## JS — обновление текста кнопки

```javascript
function onCheckChange() {
    var checked = [];
    document.querySelectorAll('#checkList input[type="checkbox"]:checked')
        .forEach(cb => checked.push(cb.value));
    var span = document.querySelector('#dropdownBtn span');
    if (checked.length === 0) span.textContent = 'Все';
    else if (checked.length <= 2) span.textContent = checked.join(', ');
    else span.textContent = 'Выбрано: ' + checked.length;
    filterData(); // обновить отображаемые данные
}
```

## JS — фильтрация данных на клиенте

```javascript
function getSelectedNames() {
    return [...document.querySelectorAll('#checkList input[type="checkbox"]:checked')]
        .map(cb => cb.value);
}

function filterData() {
    var selected = getSelectedNames();
    if (selected.length > 0) {
        data = data.filter(item => selected.includes(item.name));
    }
    renderData(data);
}
```

## Когда использовать

- Нужен выбор нескольких элементов из динамического списка
- Список зависит от другого фильтра (каскадная загрузка)
- Стандартный `<select multiple>` выглядит недостаточно стильно
