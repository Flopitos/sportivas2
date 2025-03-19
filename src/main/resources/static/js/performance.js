// Composant pour le suivi de performances
const performanceModule = {
    activities: [],
    summary: null,

    async fetchActivities() {
        try {
            // Utiliser l'API simplifiée
            const response = await fetch('/api/simple/activities', {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to fetch activities');
            }

            const data = await response.json();
            
            if (data.success && data.activities) {
                this.activities = data.activities;
                return this.activities;
            } else {
                throw new Error(data.message || 'Failed to get activities');
            }
        } catch (error) {
            console.error('Fetch activities error:', error);
            return [];
        }
    },

    async fetchSummary() {
        try {
            // Utiliser l'API simplifiée
            const response = await fetch('/api/simple/activities-summary', {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to fetch summary');
            }

            const data = await response.json();
            
            if (data.success && data.summary) {
                this.summary = data.summary;
                return this.summary;
            } else {
                throw new Error(data.message || 'Failed to get activities summary');
            }
        } catch (error) {
            console.error('Fetch summary error:', error);
            return null;
        }
    },

    renderActivityCard(activity) {
        console.log("Rendering activity:", activity);
        
        // Formatage de la date et de l'heure avec vérification
        let activityDate = "Date inconnue";
        let activityTime = "";
        
        if (activity.startDate) {
            try {
                // Formater la date SQL (YYYY-MM-DD HH:MM:SS)
                const date = new Date(activity.startDate.replace(' ', 'T'));
                if (!isNaN(date.getTime())) {
                    activityDate = date.toLocaleDateString();
                    activityTime = date.toLocaleTimeString();
                }
            } catch (e) {
                console.warn("Error parsing date:", e);
            }
        }

        // Conversion de la distance de mètres en kilomètres avec vérification
        const distance = parseFloat(activity.distance) || 0;
        const distanceKm = (distance / 1000).toFixed(2);

        // Calcul de la durée en minutes avec vérification
        const movingTime = parseInt(activity.movingTime) || 0;
        const durationMinutes = Math.floor(movingTime / 60);

        // Vérification des vitesses
        const avgSpeed = parseFloat(activity.averageSpeed) || 0;
        const maxSpeed = parseFloat(activity.maxSpeed) || 0;
        
        // Calculer les vitesses en km/h
        const avgSpeedKmh = (avgSpeed * 3.6).toFixed(2);
        const maxSpeedKmh = (maxSpeed * 3.6).toFixed(2);
        
        // Vérification de la fréquence cardiaque
        const hasHeartrate = activity.hasHeartrate === true;
        const avgHeartrate = parseFloat(activity.averageHeartrate) || 0;

        return `
            <div class="card mb-3">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">${activity.name || 'Activité sans nom'}</h5>
                    <span class="badge bg-primary">${activity.type || 'Type inconnu'}</span>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Date:</strong> ${activityDate}${activityTime ? ` à ${activityTime}` : ''}</p>
                            <p><strong>Durée:</strong> ${durationMinutes} min</p>
                            <p><strong>Distance:</strong> ${distanceKm} km</p>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Vitesse moyenne:</strong> 
                                    <span class="text-primary fw-bold">${avgSpeedKmh} km/h</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-primary" role="progressbar" 
                                        style="width: ${Math.min(100, avgSpeed * 3.6 * 5)}%"></div>
                                </div>
                            </div>
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Vitesse maximale:</strong> 
                                    <span class="text-success fw-bold">${maxSpeedKmh} km/h</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-success" role="progressbar" 
                                        style="width: ${Math.min(100, maxSpeed * 3.6 * 3)}%"></div>
                                </div>
                            </div>
                            ${hasHeartrate ? `
                            <div class="mt-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong>Fréquence cardiaque moy.:</strong> 
                                    <span class="text-danger fw-bold">${avgHeartrate.toFixed(0)} bpm</span>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar bg-danger" role="progressbar" 
                                        style="width: ${Math.min(100, avgHeartrate / 2)}%"></div>
                                </div>
                            </div>` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    },

    async fetchTotalPoints() {
        try {
            // Utiliser l'API simplifiée
            const response = await fetch('/api/simple/performance-points', {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to fetch total points');
            }

            const data = await response.json();
            
            if (data.success) {
                return data.totalPoints;
            } else {
                throw new Error(data.message || 'Failed to get total points');
            }
        } catch (error) {
            console.error('Fetch total points error:', error);
            return 0;
        }
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

        // Calculer les points basés sur la distance (16 points/km)
        const distanceInKm = summary.totalDistance / 1000;
        const pointsFromDistance = Math.round(distanceInKm * 16);

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
                                        <h3 class="text-primary">${distanceInKm.toFixed(1)} km</h3>
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
                                        <h3 class="text-warning">${pointsFromDistance}</h3>
                                        <p class="mb-0 text-muted">Points (16pts/km)</p>
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
                <div class="card mb-4">
                    <div class="card-header bg-dark text-white">
                        <h5 class="mb-0">Points de Performance</h5>
                    </div>
                    <div class="card-body text-center">
                        <div class="row align-items-center">
                            <div class="col-md-6">
                                <div class="display-4 fw-bold text-primary" id="total-points-display">...</div>
                                <p class="lead">points totaux</p>
                            </div>
                            <div class="col-md-6">
                                <div class="alert alert-info">
                                    <i class="fas fa-info-circle"></i> 1 kilomètre parcouru = 16 points
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
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
            // Charger les données en parallèle
            const [activitiesPromise, summaryPromise, pointsPromise] = [
                this.fetchActivities(),
                this.fetchSummary(),
                this.fetchTotalPoints()
            ];
            
            // Afficher les points totaux dès qu'ils sont disponibles
            const totalPoints = await pointsPromise;
            document.getElementById('total-points-display').textContent = totalPoints;
            document.getElementById('performance-points').textContent = totalPoints;
            
            // Attendre que les autres données soient chargées
            this.activities = await activitiesPromise;
            this.summary = await summaryPromise;

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
                    // Calculer les points pour cette activité (16 points par km)
                    const distanceKm = activity.distance / 1000;
                    const pointsEarned = Math.round(distanceKm * 16);
                    
                    // Ajouter les points gagnés à la carte d'activité
                    const activityCard = this.renderActivityCard(activity);
                    const cardWithPoints = activityCard.replace(
                        '<div class="card-header d-flex justify-content-between align-items-center">',
                        `<div class="card-header d-flex justify-content-between align-items-center">
                         <div class="badge bg-warning text-dark me-2">+${pointsEarned} pts</div>`
                    );
                    activitiesHtml += cardWithPoints;
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