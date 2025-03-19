// Constantes pour les URLs des API
const API_BASE_URL = '/api';
const AUTH_URL = `${API_BASE_URL}/auth`;
const SPORTS_URL = `${API_BASE_URL}/sports`;
const FEELINGS_URL = `${API_BASE_URL}/feelings`;
const INJURIES_URL = `${API_BASE_URL}/injuries`;
const PERFORMANCES_URL = `${API_BASE_URL}/performances`;
const SESSIONS_URL = `${API_BASE_URL}/sessions`;

// Gestion des utilisateurs et de l'authentification
const userService = {
    currentUser: null,
    token: null,
    isAuthenticated: false,
    passwordChanged: false,

    async login(insuranceNumber, password) {
        try {
            // Pour la démo, accepter l'identifiant et mot de passe hard-codés
            if (insuranceNumber === '123456789' && password === 'password123') {
                console.log("Using demo credentials");
                // Créer un utilisateur fictif pour la démo
                const demoUser = {
                    id: 1,
                    insuranceNumber: '123456789',
                    firstName: 'Demo',
                    lastName: 'User',
                    passwordChanged: true
                };

                this.currentUser = demoUser;
                this.isAuthenticated = true;
                this.passwordChanged = true;
                localStorage.setItem('user', JSON.stringify(demoUser));
                return demoUser;
            }

            // Si ce n'est pas l'utilisateur démo, essayer l'API
            const response = await fetch(`${AUTH_URL}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ insuranceNumber, password })
            });

            if (!response.ok) {
                throw new Error('Login failed');
            }

            const data = await response.json();
            this.currentUser = data;
            this.isAuthenticated = true;
            this.passwordChanged = data.passwordChanged;
            localStorage.setItem('user', JSON.stringify(data));
            return data;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    },

    async getCurrentUser() {
        try {
            const response = await fetch(`${AUTH_URL}/me`, {
                method: 'GET',
                headers: this.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get current user');
            }

            const data = await response.json();
            this.currentUser = data;
            return data;
        } catch (error) {
            console.error('Get current user error:', error);
            throw error;
        }
    },

    async changePassword(oldPassword, newPassword) {
        try {
            const response = await fetch(`${AUTH_URL}/change-password`, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify({ oldPassword, newPassword })
            });

            if (!response.ok) {
                throw new Error('Failed to change password');
            }

            this.passwordChanged = true;
            return true;
        } catch (error) {
            console.error('Change password error:', error);
            throw error;
        }
    },

    logout() {
        this.currentUser = null;
        this.isAuthenticated = false;
        this.passwordChanged = false;
        localStorage.removeItem('user');
    },

    checkAuth() {
        const user = localStorage.getItem('user');
        if (user) {
            this.currentUser = JSON.parse(user);
            this.isAuthenticated = true;
            this.passwordChanged = this.currentUser.passwordChanged;
            return true;
        }
        return false;
    },

    getHeaders() {
        return {
            'Content-Type': 'application/json',
            // Pour la démo, ne pas utiliser d'en-tête d'authentification
            // car notre sécurité Spring est configurée pour permettre
            // toutes les requêtes sans authentification
        };
    }
};

// Service pour les ressentis quotidiens
const feelingService = {
    async recordFeeling(feeling) {
        try {
            // Pour la démo, simuler une réponse réussie sans appeler l'API
            console.log("Enregistrement du ressenti (simulé):", feeling);
            return { success: true, feeling: feeling };

            /* Commenté pour la démo
            const response = await fetch(`${FEELINGS_URL}?feeling=${feeling}`, {
                method: 'POST',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to record feeling');
            }

            return await response.json();
            */
        } catch (error) {
            console.error('Record feeling error:', error);
            throw error;
        }
    },

    async getTodayFeeling() {
        try {
            // Pour la démo, simuler une réponse
            console.log("Récupération du ressenti du jour (simulé)");
            return { feeling: "HAPPY", timestamp: new Date().toISOString() };

            /* Commenté pour la démo
            const response = await fetch(`${FEELINGS_URL}/today`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (response.status === 404) {
                return null;
            }

            if (!response.ok) {
                throw new Error('Failed to get today feeling');
            }

            return await response.json();
            */
        } catch (error) {
            console.error('Get today feeling error:', error);
            return null;
        }
    }
};
// Service pour les sports
const sportService = {
    async getAllSports() {
        try {
            const response = await fetch(SPORTS_URL, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get sports');
            }

            return await response.json();
        } catch (error) {
            console.error('Get all sports error:', error);
            throw error;
        }
    },

    async getUserSports() {
        try {
            const response = await fetch(`${SPORTS_URL}/user`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get user sports');
            }

            return await response.json();
        } catch (error) {
            console.error('Get user sports error:', error);
            throw error;
        }
    },

    async addSportToUser(sportId, frequency, level) {
        try {
            console.log(`Adding sport with ID: ${sportId}, frequency: ${frequency}, level: ${level}`);

            // Utiliser l'API simplifiée au lieu de l'endpoint debug-add
            const response = await fetch(`/api/simple/add-sport/${sportId}?frequency=${encodeURIComponent(frequency || 'Occasionnel')}&level=${encodeURIComponent(level || 'Débutant')}`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                console.error('Server responded with status:', response.status);
                const errorText = await response.text();
                console.error('Error response:', errorText);
                throw new Error(`Failed to add sport: ${response.status}`);
            }

            const result = await response.json();
            console.log('API response:', result);
            return result;
        } catch (error) {
            console.error('Add sport error:', error);
            throw error;
        }
    },

    async removeSportFromUser(userSportId) {
        try {
            // Utiliser l'API simplifiée
            const response = await fetch(`/api/simple/remove-sport/${userSportId}`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to remove sport');
            }

            return await response.json();
        } catch (error) {
            console.error('Remove sport error:', error);
            throw error;
        }
    }
};

// Service pour les blessures
const injuryService = {
    async addInjury(injury) {
        try {
            const response = await fetch(INJURIES_URL, {
                method: 'POST',
                headers: userService.getHeaders(),
                body: JSON.stringify(injury)
            });

            if (!response.ok) {
                throw new Error('Failed to add injury');
            }

            return await response.json();
        } catch (error) {
            console.error('Add injury error:', error);
            throw error;
        }
    },

    async getUserInjuries() {
        try {
            const response = await fetch(INJURIES_URL, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get injuries');
            }

            return await response.json();
        } catch (error) {
            console.error('Get user injuries error:', error);
            throw error;
        }
    },

    async updateInjury(id, injury) {
        try {
            const response = await fetch(`${INJURIES_URL}/${id}`, {
                method: 'PUT',
                headers: userService.getHeaders(),
                body: JSON.stringify(injury)
            });

            if (!response.ok) {
                throw new Error('Failed to update injury');
            }

            return await response.json();
        } catch (error) {
            console.error('Update injury error:', error);
            throw error;
        }
    },

    async deleteInjury(id) {
        try {
            const response = await fetch(`${INJURIES_URL}/${id}`, {
                method: 'DELETE',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to delete injury');
            }

            return true;
        } catch (error) {
            console.error('Delete injury error:', error);
            throw error;
        }
    }
};

// Service pour les performances
const performanceService = {
    async addPerformance(performance) {
        try {
            const response = await fetch(PERFORMANCES_URL, {
                method: 'POST',
                headers: userService.getHeaders(),
                body: JSON.stringify(performance)
            });

            if (!response.ok) {
                throw new Error('Failed to add performance');
            }

            return await response.json();
        } catch (error) {
            console.error('Add performance error:', error);
            throw error;
        }
    },

    async getUserPerformances() {
        try {
            const response = await fetch(PERFORMANCES_URL, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get performances');
            }

            return await response.json();
        } catch (error) {
            console.error('Get user performances error:', error);
            throw error;
        }
    },

    async getTotalPoints() {
        try {
            const response = await fetch(`${PERFORMANCES_URL}/points`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get total points');
            }

            return await response.json();
        } catch (error) {
            console.error('Get total points error:', error);
            throw error;
        }
    }
};

// Service pour les séances sportives
const sessionService = {
    async addSession(session) {
        try {
            const response = await fetch(SESSIONS_URL, {
                method: 'POST',
                headers: userService.getHeaders(),
                body: JSON.stringify(session)
            });

            if (!response.ok) {
                throw new Error('Failed to add session');
            }

            return await response.json();
        } catch (error) {
            console.error('Add session error:', error);
            throw error;
        }
    },

    async getUserSessions() {
        try {
            const response = await fetch(SESSIONS_URL, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get sessions');
            }

            return await response.json();
        } catch (error) {
            console.error('Get user sessions error:', error);
            throw error;
        }
    },

    async getUpcomingSessions() {
        try {
            const response = await fetch(`${SESSIONS_URL}/upcoming`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get upcoming sessions');
            }

            return await response.json();
        } catch (error) {
            console.error('Get upcoming sessions error:', error);
            throw error;
        }
    },

    async markSessionCompleted(id) {
        try {
            const response = await fetch(`${SESSIONS_URL}/${id}/complete`, {
                method: 'POST',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to mark session as completed');
            }

            return await response.json();
        } catch (error) {
            console.error('Mark session completed error:', error);
            throw error;
        }
    },

    async getSportSuggestion() {
        try {
            const response = await fetch(`${SESSIONS_URL}/suggestion`, {
                method: 'GET',
                headers: userService.getHeaders()
            });

            if (!response.ok) {
                throw new Error('Failed to get sport suggestion');
            }

            return await response.json();
        } catch (error) {
            console.error('Get sport suggestion error:', error);
            throw error;
        }
    }
};

// Gestionnaire de l'interface utilisateur
const uiManager = {
    init() {
        console.log('Initializing UI manager');
        // Vérifier l'authentification au chargement
        if (userService.checkAuth()) {
            this.showDashboard();
        } else {
            this.showLoginPage();
        }

        // Initialiser les écouteurs d'événements
        this.setupEventListeners();
    },

    setupEventListeners() {
        console.log('Setting up event listeners');
        // Écouteur pour le formulaire de connexion
        document.addEventListener('submit', (e) => {
            if (e.target.id === 'login-form') {
                e.preventDefault();
                const insuranceNumber = document.getElementById('insurance-number').value;
                const password = document.getElementById('password').value;

                this.showLoading();
                userService.login(insuranceNumber, password)
                    .then(() => {
                        this.hideLoading();
                        this.showDashboard();

                        // Forcer l'affichage du quiz après connexion
                        console.log('Login successful, showing quiz');
                        localStorage.removeItem('quizCompletedToday');

                        // S'assurer que le module quiz est chargé avant de l'appeler
                        if (typeof window.quizModule !== 'undefined') {
                            window.quizModule.showQuiz();
                        } else {
                            console.log('Quiz module not loaded yet, waiting...');
                            // Attendre que le module soit chargé
                            const checkQuizModule = setInterval(() => {
                                if (typeof window.quizModule !== 'undefined') {
                                    clearInterval(checkQuizModule);
                                    window.quizModule.showQuiz();
                                    console.log('Quiz module loaded, showing quiz');
                                }
                            }, 100);
                        }
                    })
                    .catch(error => {
                        this.hideLoading();
                        // Remplace l'alerte par un message dans l'interface
                        document.getElementById('login-error-message').style.display = 'block';
                        console.error('Login error:', error);
                    });
            }

            if (e.target.id === 'change-password-form') {
                e.preventDefault();
                const oldPassword = document.getElementById('old-password').value;
                const newPassword = document.getElementById('new-password').value;
                const confirmPassword = document.getElementById('confirm-password').value;

                if (newPassword !== confirmPassword) {
                    // Afficher l'erreur dans l'interface plutôt que dans une alerte
                    document.getElementById('password-error-message').textContent = 'Les mots de passe ne correspondent pas';
                    document.getElementById('password-error-message').style.display = 'block';
                    document.getElementById('password-success-message').style.display = 'none';
                    return;
                }

                this.showLoading();
                userService.changePassword(oldPassword, newPassword)
                    .then(() => {
                        this.hideLoading();
                        // Afficher le succès dans l'interface
                        document.getElementById('password-success-message').style.display = 'block';
                        document.getElementById('password-error-message').style.display = 'none';
                        // Masquer la modal après un court délai
                        setTimeout(() => {
                            document.getElementById('change-password-modal').classList.add('hidden');
                        }, 2000);
                    })
                    .catch(error => {
                        this.hideLoading();
                        // Afficher l'erreur dans l'interface
                        document.getElementById('password-error-message').textContent = 'Erreur lors du changement de mot de passe';
                        document.getElementById('password-error-message').style.display = 'block';
                        document.getElementById('password-success-message').style.display = 'none';
                        console.error('Change password error:', error);
                    });
            }

            // Formulaire d'ajout de sport
            if (e.target.id === 'add-sport-form') {
                e.preventDefault();
                const sportId = document.getElementById('sport-select').value;
                const frequency = document.getElementById('sport-frequency').value;
                const level = document.getElementById('sport-level').value;

                console.log("Form submitted with values:", { sportId, frequency, level });

                if (!sportId || !frequency || !level) {
                    // Feedback pour les champs obligatoires
                    const feedbackEl = document.createElement('div');
                    feedbackEl.className = 'alert alert-danger mt-3';
                    feedbackEl.innerHTML = 'Veuillez remplir tous les champs.';
                    e.target.appendChild(feedbackEl);
                    setTimeout(() => {
                        feedbackEl.remove();
                    }, 3000);
                    return;
                }

                this.showLoading();
                console.log("Calling sportService.addSportToUser");

                // Appel avec tous les paramètres
                sportService.addSportToUser(sportId, frequency, level)
                    .then((result) => {
                        console.log("Sport added successfully:", result);
                        this.hideLoading();

                        // Réinitialiser le formulaire
                        document.getElementById('add-sport-form').reset();

                        // Feedback de succès
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-success mt-3';
                        feedbackEl.innerHTML = 'Sport ajouté avec succès !';
                        document.getElementById('add-sport-form').appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 3000);

                        // Rafraîchir les listes de sports
                        this.loadSportsTab();

                        // Rafraîchir le dashboard
                        this.loadDashboardData();
                    })
                    .catch(error => {
                        console.error("Failed to add sport:", error);
                        this.hideLoading();

                        // Feedback d'erreur
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-danger mt-3';
                        feedbackEl.innerHTML = 'Erreur lors de l\'ajout du sport: ' + error.message;
                        document.getElementById('add-sport-form').appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 5000);
                    });
            }
        });

        // Écouteur pour le bouton de déconnexion et la navigation
        document.addEventListener('click', (e) => {
            // Gestion du bouton de déconnexion
            if (e.target.id === 'logout-btn') {
                userService.logout();
                this.showLoginPage();
            }

            // Navigation entre les sections
            if (e.target.classList.contains('nav-link')) {
                e.preventDefault();
                const targetId = e.target.getAttribute('href');
                if (targetId) {
                    // Masquer toutes les sections
                    document.querySelectorAll('section').forEach(section => {
                        section.classList.add('hidden');
                    });

                    // Afficher la section cible
                    document.querySelector(targetId).classList.remove('hidden');

                    // Mettre à jour la classe active dans la navbar
                    document.querySelectorAll('.nav-link').forEach(link => {
                        link.classList.remove('active');
                    });
                    e.target.classList.add('active');

                    // Charger les données spécifiques pour certains onglets
                    if (targetId === '#performances-section') {
                        if (window.performanceModule && typeof window.performanceModule.renderPerformanceTab === 'function') {
                            window.performanceModule.renderPerformanceTab();
                        }
                    }
                }
            }

            // Écouteurs pour les boutons de ressenti
            if (e.target.classList.contains('feeling-btn')) {
                const feeling = e.target.dataset.feeling;
                this.showLoading();
                feelingService.recordFeeling(feeling)
                    .then(() => {
                        this.hideLoading();
                        document.getElementById('mood-popup').style.display = 'none';
                        // Au lieu d'une alerte, ajouter un toast ou un message temporaire
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-success position-fixed top-0 start-50 translate-middle-x mt-3';
                        feedbackEl.style.zIndex = '9999';
                        feedbackEl.innerHTML = 'Votre ressenti a été enregistré !';
                        document.body.appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 3000);
                    })
                    .catch(error => {
                        this.hideLoading();
                        // Même approche pour les erreurs
                        const feedbackEl = document.createElement('div');
                        feedbackEl.className = 'alert alert-danger position-fixed top-0 start-50 translate-middle-x mt-3';
                        feedbackEl.style.zIndex = '9999';
                        feedbackEl.innerHTML = 'Erreur lors de l\'enregistrement du ressenti';
                        document.body.appendChild(feedbackEl);
                        setTimeout(() => {
                            feedbackEl.remove();
                        }, 3000);
                        console.error('Record feeling error:', error);
                    });
            }
        });
    },

    showLoginPage() {
        console.log('Showing login page');
        document.getElementById('login-page').classList.remove('hidden');
        document.getElementById('dashboard-page').classList.add('hidden');
        document.getElementById('mood-popup').style.display = 'none';
        // Masquer le message d'erreur au retour à la page de connexion
        document.getElementById('login-error-message').style.display = 'none';
    },

    showDashboard() {
        console.log('Showing dashboard');
        document.getElementById('login-page').classList.add('hidden');
        document.getElementById('dashboard-page').classList.remove('hidden');

        // Charger les données du dashboard
        this.loadDashboardData();
    },

    showMoodPopup() {
        document.getElementById('mood-popup').style.display = 'block';
    },

    showLoading() {
        // Fonction pour afficher un indicateur de chargement
        console.log('Loading...');
        // Créer et ajouter un spinner comme dans le premier fichier
        const loadingEl = document.createElement('div');
        loadingEl.classList.add('loading');
        loadingEl.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';
        document.body.appendChild(loadingEl);
    },

    hideLoading() {
        // Fonction pour masquer l'indicateur de chargement
        console.log('Loading complete');
        const loadingEl = document.querySelector('.loading');
        if (loadingEl) {
            loadingEl.remove();
        }
    },

    loadDashboardData() {
        this.showLoading();

        // Charger les points de performance
        performanceService.getTotalPoints()
            .then(points => {
                document.getElementById('performance-points').textContent = points;
            })
            .catch(error => {
                console.error('Get total points error:', error);
            });

        // Charger les sports de l'utilisateur pour le dashboard avec l'API simplifiée
        fetch('/api/simple/list-sports')
            .then(response => response.json())
            .then(data => {
                const sportsContainer = document.getElementById('user-sports');
                sportsContainer.innerHTML = '';

                if (!data.success || !data.userSports || data.userSports.length === 0) {
                    sportsContainer.innerHTML = '<p>Vous n\'avez pas encore ajouté de sports. Allez dans la section Sports pour en ajouter.</p>';
                } else {
                    const userSports = data.userSports;
                    userSports.forEach(userSport => {
                        const sportItem = document.createElement('div');
                        sportItem.classList.add('sport-item', 'mb-2');
                        sportItem.innerHTML = `<span><strong>${userSport.name}</strong> - ${userSport.frequency}, Niveau: ${userSport.level}</span>`;
                        sportsContainer.appendChild(sportItem);
                    });
                }
            })
            .catch(error => {
                console.error('Get user sports error:', error);
                const sportsContainer = document.getElementById('user-sports');
                sportsContainer.innerHTML = '<p>Erreur lors du chargement des sports.</p>';
            });

        // Charger les prochaines séances
        sessionService.getUpcomingSessions()
            .then(sessions => {
                const sessionsContainer = document.getElementById('upcoming-sessions');
                sessionsContainer.innerHTML = '';

                if (sessions.length === 0) {
                    sessionsContainer.innerHTML = '<p>Vous n\'avez pas de séances programmées. Allez dans la section Planning pour en ajouter.</p>';
                } else {
                    sessions.forEach(session => {
                        const sessionItem = document.createElement('div');
                        sessionItem.classList.add('session-item', 'mb-2');
                        const startDate = new Date(session.scheduledStart).toLocaleString();
                        sessionItem.innerHTML = `<span>${session.sportName} - ${startDate}</span>`;
                        sessionsContainer.appendChild(sessionItem);
                    });
                }
            })
            .catch(error => {
                console.error('Get upcoming sessions error:', error);
            });

        // Suggestion de sport
        sessionService.getSportSuggestion()
            .then(suggestion => {
                document.getElementById('sport-suggestion').textContent = suggestion;
            })
            .catch(error => {
                console.error('Get sport suggestion error:', error);
            });

        // Charger les sports pour le formulaire et la liste des sports utilisateur
        // dans l'onglet sports
        this.loadSportsTab();

        this.hideLoading();
    },

    loadSportsTab() {
        // Charger tous les sports disponibles pour affichage
        sportService.getAllSports()
            .then(sports => {
                const sportsList = document.getElementById('sports-list');
                sportsList.innerHTML = '<h5 class="mb-3">Sports disponibles</h5>';

                if (sports.length === 0) {
                    sportsList.innerHTML += '<p>Aucun sport disponible.</p>';
                } else {
                    const sportsContainer = document.createElement('div');
                    sportsContainer.className = 'row';

                    sports.forEach(sport => {
                        const sportCard = document.createElement('div');
                        sportCard.className = 'col-md-4 mb-3';
                        sportCard.innerHTML = `
                            <div class="card">
                                <div class="card-body">
                                    <h5 class="card-title">${sport.name}</h5>
                                    <p class="card-text">${sport.description || 'Aucune description'}</p>
                                    <button class="btn btn-primary" 
                                            onclick="openSportModal(${sport.id}, '${sport.name.replace(/'/g, "\\'")}')">
                                        Ajouter
                                    </button>
                                </div>
                            </div>
                        `;
                        sportsContainer.appendChild(sportCard);
                    });

                    sportsList.appendChild(sportsContainer);
                }
            })
            .catch(error => {
                console.error('Get all sports error:', error);
                const sportsList = document.getElementById('sports-list');
                sportsList.innerHTML = '<div class="alert alert-danger">Erreur lors du chargement des sports</div>';
            });

        // Charger la liste des sports de l'utilisateur
        this.loadUserSportsList();
    },

    loadUserSportsList() {
        // Utiliser l'API simplifiée pour charger les sports de l'utilisateur
        fetch('/api/simple/list-sports')
            .then(response => response.json())
            .then(data => {
                console.log('List sports response:', data);

                const mySportsList = document.getElementById('my-sports-list');
                mySportsList.innerHTML = '';

                if (!data.success || !data.userSports || data.userSports.length === 0) {
                    mySportsList.innerHTML = '<p>Vous n\'avez pas encore ajouté de sports.</p>';
                } else {
                    const userSports = data.userSports;

                    const table = document.createElement('table');
                    table.className = 'table table-striped';

                    // En-tête du tableau
                    const thead = document.createElement('thead');
                    thead.innerHTML = `
                        <tr>
                            <th>Sport</th>
                            <th>Fréquence</th>
                            <th>Niveau</th>
                            <th>Action</th>
                        </tr>
                    `;
                    table.appendChild(thead);

                    // Corps du tableau
                    const tbody = document.createElement('tbody');
                    userSports.forEach(userSport => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>${userSport.name}</td>
                            <td>${userSport.frequency}</td>
                            <td>${userSport.level}</td>
                            <td>
                              <button class="btn btn-sm btn-danger remove-sport-btn" data-id="${userSport.id}">
                                    <i class="fas fa-trash"></i> Supprimer
                                </button>
                            </td>
                        `;
                        tbody.appendChild(tr);
                    });
                    table.appendChild(tbody);

                    mySportsList.appendChild(table);

                    // Utiliser une délégation d'événements pour le tableau entier
                    table.addEventListener('click', function(e) {
                        // Vérifier si le clic était sur un bouton de suppression ou un de ses enfants
                        let target = e.target;
                        while (target != this) {
                            if (target.classList.contains('remove-sport-btn')) {
                                e.preventDefault(); // Empêcher toute action par défaut

                                const userSportId = target.dataset.id;

                                // Stocker la référence à la ligne avant d'appeler l'API
                                const row = target.closest('tr');

                                uiManager.showLoading();

                                // Utiliser l'API simplifiée pour supprimer
                                fetch(`/api/simple/remove-sport/${userSportId}`)
                                    .then(response => response.json())
                                    .then(data => {
                                        uiManager.hideLoading();
                                        console.log('Remove sport response:', data);

                                        if (data.success) {
                                            // Feedback visuel
                                            const feedbackEl = document.createElement('div');
                                            feedbackEl.className = 'alert alert-success';
                                            feedbackEl.innerHTML = data.message || 'Sport supprimé avec succès !';
                                            feedbackEl.style.marginTop = '10px';
                                            mySportsList.prepend(feedbackEl);

                                            // Faire disparaître visuellement la ligne
                                            row.style.backgroundColor = '#ffdddd';
                                            row.style.transition = 'background-color 0.5s, opacity 0.5s';
                                            row.style.opacity = '0';

                                            // Attendre un moment puis actualiser complètement la liste
                                            setTimeout(() => {
                                                // Rafraîchir complètement la liste
                                                uiManager.loadUserSportsList();
                                                uiManager.loadDashboardData();

                                                // Supprimer le message de succès après un délai
                                                setTimeout(() => {
                                                    if (feedbackEl && feedbackEl.parentNode) {
                                                        feedbackEl.remove();
                                                    }
                                                }, 1500);
                                            }, 500);
                                        } else {
                                            throw new Error(data.message || 'Erreur lors de la suppression du sport');
                                        }
                                    })
                                    .catch(error => {
                                        uiManager.hideLoading();
                                        console.error('Remove sport error:', error);

                                        // Feedback visuel en cas d'erreur
                                        const feedbackEl = document.createElement('div');
                                        feedbackEl.className = 'alert alert-danger';
                                        feedbackEl.innerHTML = 'Erreur lors de la suppression du sport: ' + error.message;
                                        mySportsList.prepend(feedbackEl);

                                        setTimeout(() => {
                                            if (feedbackEl && feedbackEl.parentNode) {
                                                feedbackEl.remove();
                                            }
                                        }, 3000);
                                    });

                                return; // Sortir de la boucle
                            }
                            target = target.parentNode;
                            if (!target) break;
                        }
                    });
                }
            })
            .catch(error => {
                console.error('Get user sports error:', error);

                const mySportsList = document.getElementById('my-sports-list');
                mySportsList.innerHTML = `
                    <div class="alert alert-danger">
                        Erreur lors du chargement des sports: ${error.message}
                    </div>
                `;
            });
    }
};

// Fonction globale pour ouvrir la modal d'ajout de sport
// Cette fonction doit être dans le scope global pour être appelée par le onclick
window.openSportModal = function(sportId, sportName) {
    console.log(`Ouverture de la modal pour le sport ID: ${sportId}, Nom: ${sportName}`);

    // Définir les valeurs dans la modal
    document.getElementById('modal-sport-id').value = sportId;
    document.getElementById('modal-sport-name').textContent = sportName;

    // Réinitialiser les champs de formulaire
    document.getElementById('modal-sport-frequency').value = '';
    document.getElementById('modal-sport-level').value = '';

    // Ouvrir la modal avec Bootstrap
    const modalEl = document.getElementById('sportDetailsModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
};

// Initialiser l'application quand le DOM est chargé
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM content loaded, initializing application');

    // Gestion du bouton d'ajout de sport dans la modal
    const submitBtn = document.getElementById('submit-sport-details');
    if (submitBtn) {
        submitBtn.addEventListener('click', function() {
            const sportId = document.getElementById('modal-sport-id').value;
            const frequency = document.getElementById('modal-sport-frequency').value;
            const level = document.getElementById('modal-sport-level').value;

            console.log("submit-sport-details clicked with values:", { sportId, frequency, level });

            if (!frequency || !level) {
                // Afficher un message d'erreur
                const feedback = document.getElementById('sport-modal-feedback');
                feedback.innerHTML = '<div class="alert alert-danger">Veuillez remplir tous les champs.</div>';
                setTimeout(() => {
                    feedback.innerHTML = '';
                }, 3000);
                return;
            }

            // Fermer la modal
            const modalEl = document.getElementById('sportDetailsModal');
            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();

            // Afficher le chargement
            uiManager.showLoading();

            // Appeler l'API simplifiée
            fetch(`/api/simple/add-sport/${sportId}?frequency=${encodeURIComponent(frequency)}&level=${encodeURIComponent(level)}`)
                .then(response => response.json())
                .then(data => {
                    uiManager.hideLoading();

                    // Afficher message de succès
                    const feedback = document.getElementById('add-sport-feedback');
                    if (data.success) {
                        feedback.innerHTML = `
                            <div class="alert alert-success">
                                Sport ajouté avec succès !
                            </div>
                        `;

                        // Rafraîchir les données
                        uiManager.loadUserSportsList();
                        uiManager.loadDashboardData();
                    } else {
                        feedback.innerHTML = `
                            <div class="alert alert-danger">
                                Erreur: ${data.message || 'Erreur inconnue'}
                            </div>
                        `;
                    }

                    // Masquer le message après 3 secondes
                    setTimeout(() => {
                        feedback.innerHTML = '';
                    }, 3000);
                })
                .catch(error => {
                    uiManager.hideLoading();
                    const feedback = document.getElementById('add-sport-feedback');
                    feedback.innerHTML = `
                        <div class="alert alert-danger">
                            Erreur: ${error.message}
                        </div>
                    `;
                    setTimeout(() => {
                        feedback.innerHTML = '';
                    }, 3000);
                });
        });
    } else {
        console.warn("Le bouton submit-sport-details n'a pas été trouvé dans le DOM");
    }

    // Exposer uiManager globalement pour pouvoir l'utiliser depuis d'autres scripts
    window.uiManager = uiManager;

    // Initialiser l'application
    uiManager.init();
});