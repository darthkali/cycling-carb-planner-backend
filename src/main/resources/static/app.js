let elevationChart = null;
const COLORS = {
    'blue': '#4dc9f6',
    'oronge': '#f67019',
    'magenta': '#f53794',
    'darkblue': '#537bc4',
    'grasgreen': '#acc236',
    'petrol': '#166a8f',
    'green': '#00a950',
    'gray': '#58595b',
    'violet': '#8549ba'
};

document.getElementById('uploadBtn').addEventListener('click', () => {
    const fileInput = document.getElementById('gpxFile');
    const file = fileInput.files[0];

    if (!file) {
        alert("Bitte wählen Sie eine GPX-Datei aus.");
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    fetch('http://localhost:8080/api/gpx/process', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            const tdeList = data.points; // Annahme: das Backend gibt direkt die tdeList zurück
            const dataForChart = tdeList.map(point => ({x: point.distance, y: point.elevation}));
            const ctx = document.getElementById('elevationChart').getContext('2d');

            // // Zerstören der bestehenden Chart-Instanz, falls vorhanden
            // if (elevationChart !== null) {
            //     elevationChart.destroy();
            // }


            // Berechnung der stündlichen Distanzen für die vertikalen Linien
            const hourlyMarkers = [];
            let previousHour = 0;

            tdeList.forEach(point => {
                const currentHour = Math.floor(point.time);
                if (currentHour > previousHour) {
                    hourlyMarkers.push({distance: point.distance, time: point.time});
                    previousHour = currentHour;
                }
            });

            console.log("dataForChart")
            console.log(data.points)
            const maxDistance = dataForChart[dataForChart.length - 1].x


            elevationChart = new Chart(ctx, {
                type: 'line',
                data: {
                    datasets: [{
                        label: 'Höhenprofil',
                        data: dataForChart,
                        borderColor: 'blue',
                        fill: true,
                        pointRadius: 0,
                        backgroundColor: COLORS.blue
                    }]
                },
                options: {
                    elements: {
                        line: {
                            tension: 0.4
                        }
                    },
                    scales: {
                        x: {
                            type: 'linear',
                            position: 'bottom',
                            title: {
                                display: true,
                                text: 'Distanz (km)'
                            },
                            max: maxDistance
                        },
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Höhe (m)'
                            }
                        }
                    },
                    plugins: {
                        annotation: {
                            annotations: hourlyMarkers.map((marker, index) => ({
                                type: 'line',
                                mode: 'vertical',
                                scaleID: 'x',
                                value: marker.distance,
                                borderColor: 'black',
                                borderWidth: 2,
                                label: {
                                    content: `Stunde ${index + 1}`,
                                    enabled: true,
                                    position: 'top'
                                }
                            }))
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Fehler beim Hochladen der Datei:', error);
        });
});
