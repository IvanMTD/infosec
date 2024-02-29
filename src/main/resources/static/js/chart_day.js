google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
    let year = [[${year}]];
    let chartTitle = [[${categoryTitleDay}]];
    let chartTable = [[${categoryChartDay}]];

    let dataTable = new google.visualization.DataTable();
    dataTable.addColumn('string', 'дата')
    for (let i = 0; i < chartTitle.length; i++) {
        dataTable.addColumn('number', chartTitle[i].name);
        dataTable.addColumn({type: 'string', role: 'tooltip'});
    }
    for (let i = 0; i < chartTable.length; i++) {
        let row = [];
        row[0] = chartTable[i].date;
        let tasks = chartTable[i].taskOnTrouble;
        for (let j = 0; j < tasks.length; j++) {
            row[(j + 1) + j] = tasks[j];
            row[(j + 2) + j] = chartTitle[j].name + '\n' + chartTitle[j].description + '\n' + 'Всего: ' + tasks[j];
        }
        dataTable.addRows([row]);
    }

    let options = {
        title: 'Категории работ выполненные в ' + year + ' году',
        connectSteps: true,
        vAxis: {title: 'Общее количество выполненных задач'},
        isStacked: true,
        legend: {position: 'top'}
    };

    let chart = new google.visualization.ColumnChart(document.getElementById('category_chart'));
    chart.draw(dataTable, options);
}