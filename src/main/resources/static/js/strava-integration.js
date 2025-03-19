// Service pour l'intégration avec Strava
const stravaService = {
    // Vérifier le statut de connexion à Strava
    async checkConnectionStatus() {
        try {
            const response = await fetch('/api/strava/auth/status', {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to check Strava connection status');
            }

            return await response.json();
        } catch (error) {
            console.error('Check Strava connection status error:', error);
            return { connected: false };
        }
    },

    // Rediriger vers la page d'autorisation Strava
    connectToStrava() {
        window.location.href = '/api/strava/auth/authorize';
    },

    // Déconnecter de Strava
    async disconnectFromStrava() {
        try {
            // Afficher un indicateur de chargement
            if (window.uiManager) window.uiManager.showLoading();

            const response = await fetch('/api/strava/auth/disconnect', {
                method: 'POST',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to disconnect from Strava');
            }

            if (window.uiManager) window.uiManager.hideLoading();

            // Afficher un message de confirmation
            this.showNotification('success', 'Déconnecté de Strava avec succès');

            return await response.json();
        } catch (error) {
            console.error('Disconnect from Strava error:', error);
            if (window.uiManager) window.uiManager.hideLoading();
            this.showNotification('error', 'Erreur lors de la déconnexion de Strava');
            throw error;
        }
    },

    // Récupérer les activités Strava
    async getStravaActivities(page = 1, perPage = 30) {
        try {
            // Afficher un indicateur de chargement
            if (window.uiManager) window.uiManager.showLoading();

            const response = await fetch(`/api/strava/activities?page=${page}&per_page=${perPage}`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get Strava activities');
            }

            if (window.uiManager) window.uiManager.hideLoading();

            return await response.json();
        } catch (error) {
            console.error('Get Strava activities error:', error);
            if (window.uiManager) window.uiManager.hideLoading();
            this.showNotification('error', 'Erreur lors de la récupération des activités Strava');
            throw error;
        }
    },

    // Forcer une synchronisation des activités Strava
    async synchronizeActivities() {
        try {
            // Afficher un indicateur de chargement
            if (window.uiManager) window.uiManager.showLoading();

            // Mettre à jour le statut dans l'interface si disponible
            const syncStatusEl = document.getElementById('strava-sync-status');
            if (syncStatusEl) {
                syncStatusEl.textContent = 'Synchronisation en cours...';
            }

            const response = await fetch('/api/strava/activities/sync', {
                method: 'POST',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to synchronize Strava activities');
            }

            const result = await response.json();

            // Mettre à jour le statut dans l'interface si disponible
            if (syncStatusEl) {
                syncStatusEl.textContent = `Synchronisé (${result.count} activités)`;
            }

            // Rafraîchir l'affichage des activités
            if (window.performanceModule &&
                typeof window.performanceModule.renderPerformanceTab === 'function') {
                window.performanceModule.renderPerformanceTab();
            }

            if (window.uiManager) window.uiManager.hideLoading();

            // Afficher un message de succès
            this.showNotification('success', `Synchronisation réussie ! ${result.count} activités importées.`);

            return result;
        } catch (error) {
            console.error('Synchronize Strava activities error:', error);

            // Mettre à jour le statut dans l'interface si disponible
            const syncStatusEl = document.getElementById('strava-sync-status');
            if (syncStatusEl) {
                syncStatusEl.textContent = 'Échec de la synchronisation';
            }

            if (window.uiManager) window.uiManager.hideLoading();
            this.showNotification('error', 'Erreur lors de la synchronisation des activités Strava');
            throw error;
        }
    },

    // Afficher une notification
    showNotification(type, message) {
        const notificationEl = document.createElement('div');
        notificationEl.className = `alert alert-${type === 'success' ? 'success' : 'danger'} position-fixed top-0 start-50 translate-middle-x mt-3`;
        notificationEl.style.zIndex = '9999';
        notificationEl.innerHTML = message;
        document.body.appendChild(notificationEl);

        setTimeout(() => {
            notificationEl.remove();
        }, 3000);
    }
};

// Ajouter au contexte global de la fenêtre
window.stravaService = stravaService;

// Code d'initialisation pour l'interface Strava
document.addEventListener('DOMContentLoaded', () => {
    // Initialiser les éléments de l'interface Strava
    initStravaUI();
});

// Fonction pour initialiser l'interface Strava
function initStravaUI() {
    const stravaConnectBtn = document.getElementById('strava-connect-btn');
    const stravaDisconnectBtn = document.getElementById('strava-disconnect-btn');
    const stravaSyncBtn = document.getElementById('strava-sync-btn');
    const stravaStatusEl = document.getElementById('strava-connection-status');

    // Si les éléments n'existent pas, rien à faire
    if (!stravaConnectBtn && !stravaDisconnectBtn && !stravaSyncBtn && !stravaStatusEl) {
        return;
    }

    // Fonction pour mettre à jour l'interface en fonction du statut de connexion
    async function updateStravaUI() {
        try {
            const status = await stravaService.checkConnectionStatus();

            if (status.connected) {
                if (stravaConnectBtn) stravaConnectBtn.style.display = 'none';
                if (stravaDisconnectBtn) stravaDisconnectBtn.style.display = 'inline-block';
                if (stravaSyncBtn) stravaSyncBtn.style.display = 'inline-block';
                if (stravaStatusEl) {
                    stravaStatusEl.textContent = 'Connecté à Strava';
                    stravaStatusEl.classList.remove('text-danger', 'badge-danger');
                    stravaStatusEl.classList.add('text-success', 'badge-success');
                }

                // Afficher le nombre d'activités importées si disponible
                if (status.activitiesCount) {
                    const activitiesCountEl = document.getElementById('strava-activities-count');
                    if (activitiesCountEl) {
                        activitiesCountEl.textContent = status.activitiesCount;
                    }
                }
            } else {
                if (stravaConnectBtn) stravaConnectBtn.style.display = 'inline-block';
                if (stravaDisconnectBtn) stravaDisconnectBtn.style.display = 'none';
                if (stravaSyncBtn) stravaSyncBtn.style.display = 'none';
                if (stravaStatusEl) {
                    stravaStatusEl.textContent = 'Non connecté à Strava';
                    stravaStatusEl.classList.remove('text-success', 'badge-success');
                    stravaStatusEl.classList.add('text-danger', 'badge-danger');
                }
            }
        } catch (error) {
            console.error('Error updating Strava UI:', error);
        }
    }

    // Ajouter les gestionnaires d'événements
    if (stravaConnectBtn) {
        stravaConnectBtn.addEventListener('click', () => {
            stravaService.connectToStrava();
        });
    }

    if (stravaDisconnectBtn) {
        stravaDisconnectBtn.addEventListener('click', async () => {
            try {
                await stravaService.disconnectFromStrava();
                updateStravaUI();
            } catch (error) {
                console.error('Error disconnecting from Strava:', error);
            }
        });
    }

    if (stravaSyncBtn) {
        stravaSyncBtn.addEventListener('click', async () => {
            try {
                await stravaService.synchronizeActivities();
                updateStravaUI();
            } catch (error) {
                console.error('Error synchronizing Strava activities:', error);
            }
        });
    }

    // Mettre à jour l'interface au chargement si l'utilisateur est authentifié
    if (window.userService && userService.isAuthenticated) {
        updateStravaUI();
    }
}