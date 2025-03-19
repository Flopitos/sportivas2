/**
 * Sportivas - Module de Planning
 * Ce module gère les fonctionnalités liées au planning et aux séances sportives
 * Utilisation des API backend directement pour les opérations CRUD
 */

// Fonction pour charger les sports de l'utilisateur dans le sélecteur de sessions
function loadUserSportsForSessionForm() {
    const sportSelect = document.getElementById('session-sport');
    if (!sportSelect) return;

    // Vider d'abord le sélecteur sauf la première option
    while (sportSelect.options.length > 1) {
        sportSelect.remove(1);
    }

    // Afficher un indicateur de chargement
    const loadingOption = new Option('Chargement des sports...', '');
    sportSelect.add(loadingOption);

    // Utiliser l'API simplifiée qui fonctionne
    fetch('/api/simple/list-sports', {
        method: 'GET',
        headers: userService.getHeaders()
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur serveur: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Sports de l\'utilisateur reçus:', data);

            // Supprimer l'option de chargement
            sportSelect.remove(sportSelect.options.length - 1);

            // Vérifier si des sports sont disponibles
            if (!data.success || !data.userSports || data.userSports.length === 0) {
                const noSportsOption = new Option('Aucun sport disponible', '');
                noSportsOption.disabled = true;
                sportSelect.add(noSportsOption);
                return;
            }

            // Ajouter chaque sport comme option
            data.userSports.forEach(userSport => {
                const option = new Option(userSport.name, userSport.id);
                sportSelect.add(option);
            });
        })
        .catch(error => {
            console.error('Failed to load user sports:', error);
            // Supprimer l'option de chargement et afficher une erreur
            sportSelect.remove(sportSelect.options.length - 1);
            const errorOption = new Option('Erreur: ' + error.message, '');
            errorOption.disabled = true;
            sportSelect.add(errorOption);
        });
}

// Fonction pour charger les séances de l'utilisateur
function loadUserSessions() {
    const sessionsContainer = document.getElementById('sessions-list');
    if (!sessionsContainer) return;

    // Afficher un indicateur de chargement
    sessionsContainer.innerHTML = '<p>Chargement de vos séances...</p>';

    // Utiliser directement l'API pour récupérer les séances à venir
    fetch('/api/sessions/upcoming', {
        method: 'GET',
        headers: userService.getHeaders()
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur lors de la récupération des séances: ${response.status}`);
            }
            return response.json();
        })
        .then(sessions => {
            if (!sessions || sessions.length === 0) {
                sessionsContainer.innerHTML = '<p>Vous n\'avez pas de séances programmées.</p>';
                return;
            }

            // Créer un tableau pour afficher les séances
            const table = document.createElement('table');
            table.className = 'table table-striped';

            // En-tête du tableau
            const thead = document.createElement('thead');
            thead.innerHTML = `
            <tr>
                <th>Sport</th>
                <th>Date</th>
                <th>Heure</th>
                <th>Notes</th>
                <th>Actions</th>
            </tr>
        `;
            table.appendChild(thead);

            // Corps du tableau
            const tbody = document.createElement('tbody');
            sessions.forEach(session => {
                // Formater les dates
                const startDate = new Date(session.scheduledStart);
                const endDate = new Date(session.scheduledEnd);

                const tr = document.createElement('tr');
                tr.innerHTML = `
                <td>${session.sportName}</td>
                <td>${startDate.toLocaleDateString()}</td>
                <td>${startDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - ${endDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
                <td>${session.notes || '-'}</td>
                <td>
                    <button class="btn btn-sm btn-success mark-complete-btn ${session.completed ? 'disabled' : ''}" data-id="${session.id}" ${session.completed ? 'disabled' : ''}>
                        <i class="fas fa-check"></i> ${session.completed ? 'Complétée' : 'Marquer comme complétée'}
                    </button>
                    <button class="btn btn-sm btn-danger delete-session-btn" data-id="${session.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
                tbody.appendChild(tr);
            });
            table.appendChild(tbody);

            // Vider et ajouter le tableau
            sessionsContainer.innerHTML = '';
            sessionsContainer.appendChild(table);

            // Ajouter les écouteurs d'événements pour les boutons d'actions
            setupSessionActionButtons();
        })
        .catch(error => {
            console.error('Failed to load sessions:', error);
            sessionsContainer.innerHTML = `<div class="alert alert-danger">Erreur lors du chargement des séances: ${error.message}</div>`;
        });
}

// Configuration des boutons d'action pour les séances
function setupSessionActionButtons() {
    // Marquer comme complétée
    document.querySelectorAll('.mark-complete-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const sessionId = this.dataset.id;

            uiManager.showLoading();

            // Appel direct à l'API pour marquer comme complétée
            fetch(`/api/sessions/${sessionId}/complete`, {
                method: 'POST',
                headers: userService.getHeaders()
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Erreur lors du marquage comme complété: ${response.status}`);
                    }
                    return response.json();
                })
                .then(updatedSession => {
                    uiManager.hideLoading();

                    // Mettre à jour l'interface
                    this.classList.add('disabled');
                    this.disabled = true;
                    this.innerHTML = '<i class="fas fa-check"></i> Complétée';

                    // Rafraîchir le dashboard
                    if (uiManager && typeof uiManager.loadDashboardData === 'function') {
                        uiManager.loadDashboardData();
                    }

                    // Afficher un toast de confirmation
                    const feedbackEl = document.createElement('div');
                    feedbackEl.className = 'alert alert-success position-fixed top-0 start-50 translate-middle-x mt-3';
                    feedbackEl.style.zIndex = '9999';
                    feedbackEl.innerHTML = 'Séance marquée comme complétée !';
                    document.body.appendChild(feedbackEl);
                    setTimeout(() => {
                        feedbackEl.remove();
                    }, 3000);
                })
                .catch(error => {
                    uiManager.hideLoading();
                    console.error('Failed to mark session as completed:', error);

                    // Afficher un message d'erreur
                    const feedbackEl = document.createElement('div');
                    feedbackEl.className = 'alert alert-danger position-fixed top-0 start-50 translate-middle-x mt-3';
                    feedbackEl.style.zIndex = '9999';
                    feedbackEl.innerHTML = 'Erreur: ' + error.message;
                    document.body.appendChild(feedbackEl);
                    setTimeout(() => {
                        feedbackEl.remove();
                    }, 3000);
                });
        });
    });

    // Supprimer une séance
    document.querySelectorAll('.delete-session-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const sessionId = this.dataset.id;
            if (confirm('Êtes-vous sûr de vouloir supprimer cette séance ?')) {
                uiManager.showLoading();

                // Appel direct à l'API pour supprimer
                fetch(`/api/sessions/${sessionId}`, {
                    method: 'DELETE',
                    headers: userService.getHeaders()
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(`Erreur lors de la suppression: ${response.status}`);
                        }
                        return response;
                    })
                    .then(() => {
                        uiManager.hideLoading();

                        // Supprimer la ligne du tableau
                        this.closest('tr').remove();

                        // Si le tableau est vide, afficher un message
                        const tbody = document.querySelector('#sessions-list table tbody');
                        if (tbody && tbody.children.length === 0) {
                            document.getElementById('sessions-list').innerHTML = '<p>Vous n\'avez pas de séances programmées.</p>';
                        }

                        // Rafraîchir le dashboard
                        if (uiManager && typeof uiManager.loadDashboardData === 'function') {
                            uiManager.loadDashboardData();
                        }

                        // Afficher un toast de confirmation
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-success position-fixed top-0 start-50 translate-middle-x mt-3';
                        feedbackEl.style.zIndex = '9999';
                        feedbackEl.innerHTML = 'Séance supprimée avec succès !';
                        document.body.appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 3000);
                    })
                    .catch(error => {
                        uiManager.hideLoading();
                        console.error('Failed to delete session:', error);

                        // Afficher un message d'erreur
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-danger position-fixed top-0 start-50 translate-middle-x mt-3';
                        feedbackEl.style.zIndex = '9999';
                        feedbackEl.innerHTML = 'Erreur: ' + error.message;
                        document.body.appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 3000);
                    });
            }
        });
    });
}

// Gérer la soumission du formulaire d'ajout de séance
function setupSessionFormHandler() {
    const addSessionForm = document.getElementById('add-session-form');
    if (addSessionForm) {
        addSessionForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // Récupérer les valeurs du formulaire
            const sportId = document.getElementById('session-sport').value;
            const sessionDate = document.getElementById('session-date').value;
            const startTime = document.getElementById('start-time').value;
            const endTime = document.getElementById('end-time').value;
            const notes = document.getElementById('session-notes').value;

            // Vérifier que tous les champs obligatoires sont remplis
            if (!sportId || !sessionDate || !startTime || !endTime) {
                // Afficher un message d'erreur
                const feedbackEl = document.createElement('div');
                feedbackEl.className = 'alert alert-danger mt-3';
                feedbackEl.innerHTML = 'Veuillez remplir tous les champs obligatoires.';
                addSessionForm.appendChild(feedbackEl);
                setTimeout(() => {
                    feedbackEl.remove();
                }, 3000);
                return;
            }

            try {
                // Créer des objets Date
                const startDateTime = new Date(`${sessionDate}T${startTime}`);
                const endDateTime = new Date(`${sessionDate}T${endTime}`);

                // Vérifier que les dates sont valides
                if (isNaN(startDateTime.getTime()) || isNaN(endDateTime.getTime())) {
                    throw new Error("Dates ou heures invalides");
                }

                // Convertir au format ISO
                const scheduledStart = startDateTime.toISOString();
                const scheduledEnd = endDateTime.toISOString();

                // Créer l'objet de session
                const sessionDto = {
                    sportId: parseInt(sportId),
                    scheduledStart: scheduledStart,
                    scheduledEnd: scheduledEnd,
                    notes: notes,
                    completed: false
                };

                console.log('Données de session à envoyer:', sessionDto);

                // Afficher le chargement
                uiManager.showLoading();

                // Appel direct à l'API pour ajouter une séance
                fetch('/api/sessions', {
                    method: 'POST',
                    headers: userService.getHeaders(),
                    body: JSON.stringify(sessionDto)
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(`Erreur lors de l'ajout de la séance: ${response.status}`);
                        }
                        return response.json();
                    })
                    .then(result => {
                        uiManager.hideLoading();

                        // Réinitialiser le formulaire
                        addSessionForm.reset();

                        // Afficher un message de succès
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-success mt-3';
                        feedbackEl.innerHTML = 'Séance ajoutée avec succès !';
                        addSessionForm.appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 3000);

                        // Rafraîchir la liste des séances et le dashboard
                        loadUserSessions();
                        if (uiManager && typeof uiManager.loadDashboardData === 'function') {
                            uiManager.loadDashboardData();
                        }
                    })
                    .catch(error => {
                        uiManager.hideLoading();

                        // Afficher un message d'erreur
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-danger mt-3';
                        feedbackEl.innerHTML = 'Erreur lors de l\'ajout de la séance: ' + error.message;
                        addSessionForm.appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 5000);
                    });
            } catch (error) {
                // Erreur lors de la création des dates
                const feedbackEl = document.createElement('div');
                feedbackEl.className = 'alert alert-danger mt-3';
                feedbackEl.innerHTML = 'Erreur de format de date: ' + error.message;
                addSessionForm.appendChild(feedbackEl);
                setTimeout(() => {
                    feedbackEl.remove();
                }, 3000);
            }
        });
    }
}

// Initialisation du module de planning
function initPlanningModule() {
    // Vérifier si l'onglet planning est présent
    if (document.getElementById('planning-section')) {
        console.log('Initializing planning module...');
        loadUserSportsForSessionForm();
        loadUserSessions();
        setupSessionFormHandler();
    }

    // Ajouter des écouteurs d'événements pour la navigation
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('nav-link') && e.target.getAttribute('href') === '#planning-section') {
            console.log('Planning tab selected, refreshing data...');
            loadUserSportsForSessionForm();
            loadUserSessions();
        }
    });
}

// Exposer le module de planning globalement
window.planningModule = {
    init: initPlanningModule,
    loadUserSportsForSessionForm: loadUserSportsForSessionForm,
    loadUserSessions: loadUserSessions,
    setupSessionFormHandler: setupSessionFormHandler
};

// Initialiser le module lorsque le DOM est chargé
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing planning module...');
    setTimeout(initPlanningModule, 500); // Légère temporisation pour s'assurer que tout est bien chargé
});