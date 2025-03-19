// Composant pour le suivi de performances
const performanceModule = {
    activities: [],
    summary: null,

    async fetchActivities() {
        try {
            const response = await fetch(`${PERFORMANCES_URL}/activities`, {
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
            const response = await fetch(`${PERFORMANCES_URL}/summary`, {
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
        const activityDate = new Date(activity.start_date).toLocaleDateString();
        const activityTime = new Date(activity.start_date).toLocaleTimeString();
        
        return `
            <div class="card mb-3">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">${activity.name}</h5>
                    <span class="badge bg-primary">${activity.sport_type}</span>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Date:</strong> ${activityDate} à ${activityTime}</p>
                            <p><strong>Durée:</strong> ${activity.duration_minutes || 0} min</p>
                            <p><strong>Distance:</strong> ${(activity.distance_km || 0).toFixed(2)} km</p>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Calories brûlées:</strong> 
                                    <span class="text-danger fw-bold">${activity.calories || 0}</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-danger" role="progressbar" style="width: ${Math.min(100, (activity.calories || 0) / 10)}%"></div>
                                </div>
                            </div>
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Pas:</strong> 
                                    <span class="text-success fw-bold">${activity.steps || 0}</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-success" role="progressbar" style="width: ${Math.min(100, (activity.steps || 0) / 100)}%"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    },

    renderSummaryDashboard(summary) {
        if (!summary) return '';
        
        // Créer la liste des types d'activités
        let activityTypesList = '';
        if (summary.activityTypes) {
            for (const [type, count] of Object.entries(summary.activityTypes)) {
                activityTypesList += `<li class="list-group-item d-flex justify-content-between align-items-center">
                    ${type}
                    <span class="badge bg-primary rounded-pill">${count}</span>
                </li>`;
            }
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
                                        <h3 class="text-danger">${summary.totalCalories || 0}</h3>
                                        <p class="mb-0 text-muted">Calories brûlées</p>
                                    </div>
                                </div>
                                <div class="col-6 mb-3">
                                    <div class="border rounded p-3">
                                        <h3 class="text-success">${summary.totalSteps || 0}</h3>
                                        <p class="mb-0 text-muted">Pas totaux</p>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="border rounded p-3">
                                        <h3>${(summary.totalDistance || 0).toFixed(1)} km</h3>
                                        <p class="mb-0 text-muted">Distance totale</p>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="border rounded p-3">
                                        <h3>${summary.activityCount || 0}</h3>
                                        <p class="mb-0 text-muted">Activités</p>
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
                                ${activityTypesList}
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
            
            // Afficher le tableau de bord
            const summaryDashboard = document.getElementById('summary-dashboard');
            summaryDashboard.innerHTML = this.renderSummaryDashboard(this.summary);
            
            // Afficher la liste des activités
            const activitiesContainer = document.getElementById('activities-container');
            if (this.activities && this.activities.length > 0) {
                let activitiesHtml = '<h4 class="mb-3 mt-4">Vos Activités Récentes</h4>';
                this.activities.forEach(activity => {
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
    }
};