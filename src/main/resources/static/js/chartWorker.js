onmessage = function(event) {
    const { chartTable, chartTitle } = event.data;

    const categories = chartTable.map(entry => entry.localDate);
    const data = chartTitle.map((item, index) => ({
        name: item.name,
        data: chartTable.map(entry => entry.taskOnTrouble[index])
    }));

    postMessage({ categories, data });
};