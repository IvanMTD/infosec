google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
    let year = [[${year}]];
    let chartTitle = [[${categoryTitleMonth}]];
    console.log(chartTitle);
    let chartTable = [[${categoryChartMonth}]];
    console.log(chartTable);

    let dataTable = new google.visualization.DataTable();
    dataTable.addColumn('string', 'Неделя')
    for (let i = 0; i < chartTitle.length; i++) {
        dataTable.addColumn('number', chartTitle[i].name);
        dataTable.addColumn({type: 'string', role: 'tooltip'});
    }
    for (let i = 0; i < chartTable.length; i++) {
        let row = [];
        row[0] = chartTable[i].title;
        let chartList = chartTable[i].charts;
        for (let j = 0; j < chartList.length; j++) {
            row[(j + 1) + j] = chartList[j].taskCount;
            row[(j + 2) + j] = chartTitle[j].name + '\n' + chartTitle[j].description + '\n' + 'Всего: ' + chartList[j].taskCount;
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