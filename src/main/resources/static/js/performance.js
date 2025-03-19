// Composant pour le suivi de performances
const performanceModule = {
    activities: [],
    summary: null,

    async fetchActivities() {
        try {
            const response = await fetch(`${API_BASE_URL}/activities`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to fetch activities');
            }

            this.activities = await response.json();
            return this.activities;
        } catch (error) {
            console.error('Fetch activities error:', error);
            return [];
        }
    },

    async fetchSummary() {
        try {
            const response = await fetch(`${API_BASE_URL}/activities/summary`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to fetch summary');
            }

            this.summary = await response.json();
            return this.summary;
        } catch (error) {
            console.error('Fetch summary error:', error);
            return null;
        }
    },

    renderActivityCard(activity) {
        // Formatage de la date et de l'heure
        const activityDate = new Date(activity.startDate).toLocaleDateString();
        const activityTime = new Date(activity.startDate).toLocaleTimeString();

        // Conversion de la distance de mètres en kilomètres
        const distanceKm = (activity.distance / 1000).toFixed(2);

        // Calcul de la durée en minutes
        const durationMinutes = Math.floor(activity.movingTime / 60);

        return `
            <div class="card mb-3">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">${activity.name}</h5>
                    <span class="badge bg-primary">${activity.type}</span>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Date:</strong> ${activityDate} à ${activityTime}</p>
                            <p><strong>Durée:</strong> ${durationMinutes} min</p>
                            <p><strong>Distance:</strong> ${distanceKm} km</p>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Vitesse moyenne:</strong> 
                                    <span class="text-primary fw-bold">${(activity.averageSpeed * 3.6).toFixed(2)} km/h</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-primary" role="progressbar" 
                                        style="width: ${Math.min(100, (activity.averageSpeed * 3.6) * 5)}%"></div>
                                </div>
                            </div>
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Vitesse maximale:</strong> 
                                    <span class="text-success fw-bold">${(activity.maxSpeed * 3.6).toFixed(2)} km/h</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-success" role="progressbar" 
                                        style="width: ${Math.min(100, (activity.maxSpeed * 3.6) * 3)}%"></div>
                                </div>
                            </div>
                            ${activity.hasHeartrate ? `
                            <div class="mt-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Fréquence cardiaque moy.:</strong> 
                                    <span class="text-danger fw-bold">${activity.averageHeartrate?.toFixed(0) || 'N/A'} bpm</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-danger" role="progressbar" 
                                        style="width: ${Math.min(100, (activity.averageHeartrate || 0) / 2)}%"></div>
                                </div>
                            </div>` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    },

    renderSummaryDashboard(summary) {
        if (!summary) return '';

        // Calculer le nombre total d'activités par type
        let activityTypesList = '';
        if (summary.activityTypes) {
            Object.entries(summary.activityTypes).forEach(([type, count]) => {
                activityTypesList += `
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        ${type}
                        <span class="badge bg-primary rounded-pill">${count}</span>
                    </li>`;
            });
        }

        return `
            <div class="row mb-4">
                <div class="col-md-6 mb-3">
                    <div class="card h-100">
                        <div class="card-header bg-primary text-white">
                            <h5 class="mb-0">Résumé des Activités</h5>
                        </div>
                        <div class="card-body">
                            <div class="row text-center">
                                <div class="col-6 mb-3">
                                    <div class="border rounded p-3">
                                        <h3 class="text-primary">${(summary.totalDistance / 1000).toFixed(1)} km</h3>
                                        <p class="mb-0 text-muted">Distance totale</p>
                                    </div>
                                </div>
                                <div class="col-6 mb-3">
                                    <div class="border rounded p-3">
                                        <h3 class="text-success">${summary.activityCount || 0}</h3>
                                        <p class="mb-0 text-muted">Activités</p>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="border rounded p-3">
                                        <h3 class="text-info">${Math.floor(summary.totalDuration / 60)} min</h3>
                                        <p class="mb-0 text-muted">Temps total</p>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="border rounded p-3">
                                        <h3 class="text-danger">${summary.totalElevation?.toFixed(0) || 0} m</h3>
                                        <p class="mb-0 text-muted">Dénivelé total</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <div class="card h-100">
                        <div class="card-header bg-success text-white">
                            <h5 class="mb-0">Types d'Activités</h5>
                        </div>
                        <div class="card-body">
                            <ul class="list-group">
                                ${activityTypesList || '<li class="list-group-item">Aucune activité trouvée</li>'}
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        `;
    },

    async renderPerformanceTab() {
        const contentArea = document.getElementById('content');
        contentArea.innerHTML = `
            <div class="container my-4">
                <h2 class="text-center mb-4">Suivi de Performance</h2>
                <div id="summary-dashboard"></div>
                <div id="activities-container">
                    <div class="d-flex justify-content-center">
                        <div class="spinner-border" role="status">
                            <span class="visually-hidden">Chargement...</span>
                        </div>
                    </div>
                </div>
            </div>
        `;

        try {
            // Charger les données
            await Promise.all([this.fetchActivities(), this.fetchSummary()]);

            // Préparer les données du résumé si elles n'existent pas
            if (!this.summary) {
                this.summary = this.calculateSummary(this.activities);
            }

            // Afficher le tableau de bord
            const summaryDashboard = document.getElementById('summary-dashboard');
            summaryDashboard.innerHTML = this.renderSummaryDashboard(this.summary);

            // Afficher la liste des activités
            const activitiesContainer = document.getElementById('activities-container');
            if (this.activities && this.activities.length > 0) {
                let activitiesHtml = '<h4 class="mb-3 mt-4">Vos Activités Récentes</h4>';

                // Trier les activités par date (plus récentes en premier)
                const sortedActivities = [...this.activities].sort((a, b) =>
                    new Date(b.startDate) - new Date(a.startDate)
                );

                sortedActivities.forEach(activity => {
                    activitiesHtml += this.renderActivityCard(activity);
                });
                activitiesContainer.innerHTML = activitiesHtml;
            } else {
                activitiesContainer.innerHTML = '<div class="alert alert-info mt-3">Aucune activité trouvée.</div>';
            }
        } catch (error) {
            console.error('Error rendering performance tab:', error);
            const activitiesContainer = document.getElementById('activities-container');
            activitiesContainer.innerHTML = '<div class="alert alert-danger mt-3">Erreur lors du chargement des données.</div>';
        }
    },

    // Méthode pour calculer le résumé à partir des activités si l'API ne le fournit pas
    calculateSummary(activities) {
        if (!activities || activities.length === 0) {
            return {
                activityCount: 0,
                totalDistance: 0,
                totalDuration: 0,
                totalElevation: 0,
                activityTypes: {}
            };
        }

        const summary = {
            activityCount: activities.length,
            totalDistance: 0,
            totalDuration: 0,
            totalElevation: 0,
            activityTypes: {}
        };

        activities.forEach(activity => {
            summary.totalDistance += activity.distance || 0;
            summary.totalDuration += activity.movingTime || 0;
            summary.totalElevation += activity.totalElevationGain || 0;

            // Compter les types d'activités
            const type = activity.type || 'Unknown';
            summary.activityTypes[type] = (summary.activityTypes[type] || 0) + 1;
        });

        return summary;
    }
};

// Exposer le module globalement
window.performanceModule = performanceModule;