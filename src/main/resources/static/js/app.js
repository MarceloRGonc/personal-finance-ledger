(function () {
  const palette = ['#047857', '#2563eb', '#d97706', '#9333ea', '#dc2626', '#0891b2', '#65a30d', '#db2777', '#4b5563', '#ca8a04', '#0f766e'];
  const euro = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' });

  const builders = {
    monthly: (data) => ({
      type: 'bar',
      data: {
        labels: data.labels,
        datasets: [
          { label: 'Net revenue', data: data.netRevenue, backgroundColor: palette[0] },
          { label: 'Expenses', data: data.expenses, backgroundColor: palette[4] },
          { label: 'Investments', data: data.investments, backgroundColor: palette[1] },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { ticks: { callback: (v) => euro.format(v) } } },
        plugins: { tooltip: { callbacks: { label: (c) => `${c.dataset.label}: ${euro.format(c.parsed.y)}` } } },
      },
    }),
    comparison: (rows) => ({
      type: 'bar',
      data: {
        labels: rows.map((r) => String(r.year)),
        datasets: [
          { label: 'Net Value', data: rows.map((r) => r.values.NET_VALUE ?? null), backgroundColor: palette[0] },
          { label: 'Invested', data: rows.map((r) => r.values.INVESTED ?? null), backgroundColor: palette[1] },
          { label: 'Net Annual Income', data: rows.map((r) => r.values.NET_ANNUAL_INCOME ?? null), backgroundColor: palette[2] },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { ticks: { callback: (v) => euro.format(v) } } },
        plugins: { tooltip: { callbacks: { label: (c) => `${c.dataset.label}: ${euro.format(c.parsed.y)}` } } },
      },
    }),
    pie: (data) => ({
      type: 'pie',
      data: {
        labels: data.labels,
        datasets: [{ data: data.values, backgroundColor: data.labels.map((_, i) => palette[i % palette.length]) }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'right' },
          tooltip: { callbacks: { label: (c) => `${c.label}: ${euro.format(c.parsed)}` } },
        },
      },
    }),
  };

  async function renderChart(canvas) {
    const build = builders[canvas.dataset.chartType];
    if (!build) {
      return;
    }
    const response = await fetch(canvas.dataset.chartUrl);
    if (!response.ok) {
      return;
    }
    new Chart(canvas, build(await response.json()));
  }

  document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('canvas[data-chart-url]').forEach(renderChart);
  });

  document.addEventListener('htmx:responseError', (event) => {
    window.alert(event.detail.xhr.responseText || 'The request failed');
  });
})();
