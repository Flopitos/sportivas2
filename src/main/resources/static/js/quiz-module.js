// Module pour gérer le quiz quotidien
window.quizModule = {
    currentStep: 0,
    answers: {},

    // Initialisation du quiz
    init() {
        console.log('Quiz module initialized');
        // Création des écouteurs d'événements pour les boutons du quiz
        this.setupEventListeners();
    },

    // Configuration des écouteurs d'événements
    setupEventListeners() {
        // Écouteur pour les boutons de ressenti
        document.addEventListener('click', (e) => {

            // Gestion des boutons de type de fatigue
            if (e.target.classList.contains('fatigue-btn')) {
                const fatigue = e.target.dataset.fatigue;
                this.answers.fatigue = fatigue;
                this.nextStep();
            }

            // Gestion des boutons de ressenti
            if (e.target.classList.contains('feeling-btn')) {
                const feeling = e.target.dataset.feeling;
                this.answers.feeling = feeling;
                this.nextStep();
            }

            // Gestion des boutons de type de fatigue
            if (e.target.classList.contains('fatigue-btn')) {
                const fatigue = e.target.dataset.fatigue;
                this.answers.fatigue = fatigue;
                this.nextStep();
            }

            // Gestion des boutons de douleurs morales
            if (e.target.classList.contains('moral-douleurs-btn')) {
                const moralDouleurs = e.target.dataset.moralDouleurs;
                this.answers.moralDouleurs = moralDouleurs;
                this.nextStep();
            }

            // Gestion des boutons de blessure
            if (e.target.classList.contains('blessure-btn')) {
                const blessure = e.target.dataset.blessure;
                this.answers.blessure = blessure;
                this.nextStep();
            }

            // Gestion des boutons de zone de douleurs
            if (e.target.classList.contains('zone-douleurs-btn')) {
                const zoneDouleurs = e.target.dataset.zoneDouleurs;
                this.answers.zoneDouleurs = zoneDouleurs;
                this.completeQuiz();
            }

            // Bouton pour fermer le quiz (ajouté dans la nouvelle interface)
            if (e.target.id === 'close-quiz-btn') {
                this.hideQuiz();
                // Garder trace que l'utilisateur a répondu au quiz aujourd'hui
                localStorage.setItem('quizCompletedToday', new Date().toDateString());
            }
        });
    },

    // Afficher le quiz avec fond transparent
    showQuiz() {
        console.log('Showing quiz...');
        // S'assurer que l'entrée localStorage est supprimée pour pouvoir afficher le quiz
        localStorage.removeItem('quizCompletedToday');

        const overlay = document.getElementById('quiz-overlay');
        if (!overlay) {
            // Si l'overlay n'existe pas, le créer
            const newOverlay = document.createElement('div');
            newOverlay.id = 'quiz-overlay';
            newOverlay.className = 'quiz-overlay';
            document.body.appendChild(newOverlay);

            // Ajouter le conteneur du quiz
            const quizContainer = document.createElement('div');
            quizContainer.id = 'quiz-container';
            quizContainer.className = 'quiz-container';
            quizContainer.innerHTML = `
                <div class="card shadow">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Quiz quotidien</h5>
                        <button id="close-quiz-btn" class="btn-close btn-close-white" aria-label="Close"></button>
                    </div>
                    <div id="quiz-content" class="card-body">
                        <!-- Le contenu du quiz sera chargé dynamiquement -->
                    </div>
                </div>
            `;
            newOverlay.appendChild(quizContainer);

            // Afficher la première question
            this.showStep(0);
        } else {
            overlay.style.display = 'flex';
            this.showStep(0);
        }
    },

    // Cacher le quiz
    hideQuiz() {
        const overlay = document.getElementById('quiz-overlay');
        if (overlay) {
            overlay.style.display = 'none';
        }

        // Réinitialiser l'état du quiz
        this.currentStep = 0;
        this.answers = {};
    },

    // Afficher une étape spécifique du quiz
    showStep(step) {
        this.currentStep = step;
        const quizContent = document.getElementById('quiz-content');
        if (!quizContent) {
            console.error('Quiz content element not found');
            return;
        }

        // Contenu des différentes étapes
        const steps = [
            // Étape 0: Comment vous sentez-vous aujourd'hui?
            `
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">Comment vous sentez-vous aujourd'hui ?</h5>
            </div>
            <div class="card-body text-center">
                <div class="d-flex justify-content-center mb-3">
                    <button class="btn btn-light emoji-btn feeling-btn mx-2" data-feeling="Bien">En pleine forme 😊</button>
                    <button class="btn btn-light emoji-btn feeling-btn mx-2" data-feeling="Normal">Normal 😐</button>
                    <button class="btn btn-light emoji-btn feeling-btn mx-2" data-feeling="Fatiguer">Fatigué 😞</button>
                    <button class="btn btn-light emoji-btn feeling-btn mx-2" data-feeling="Malade">Malade 🤒</button>
                </div>
            </div>
            `,
            // Étape 1: C'est une fatigue morale ou physique?
            `
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">C'est une fatigue morale ou physique ?</h5>
            </div>
            <div class="card-body text-center">
                <div class="d-flex justify-content-center mb-3">
                    <button class="btn btn-light emoji-btn fatigue-btn mx-2" data-fatigue="physique">Physique</button>
                    <button class="btn btn-light emoji-btn fatigue-btn mx-2" data-fatigue="moral">Morale</button>
                </div>
            </div>
            `,
            // Étape 2: En plus de cela, est-ce que tu as des douleurs?
            `
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">En plus de cela, avez-vous des douleurs ?</h5>
            </div>
            <div class="card-body text-center">
                <div class="d-flex justify-content-center mb-3">
                    <button class="btn btn-light emoji-btn moral-douleurs-btn mx-2" data-moral-douleurs="Oui">Oui</button>
                    <button class="btn btn-light emoji-btn moral-douleurs-btn mx-2" data-moral-douleurs="Courbatures">Oui, type courbatures</button>
                    <button class="btn btn-light emoji-btn moral-douleurs-btn mx-2" data-moral-douleurs="Non">Non</button>
                </div>
            </div>
            `,
            // Étape 3: Avez-vous des douleurs?
            `
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">Avez-vous des douleurs ?</h5>
            </div>
            <div class="card-body text-center">
                <div class="d-flex justify-content-center mb-3">
                    <button class="btn btn-light emoji-btn blessure-btn mx-2" data-blessure="Non">Non</button>
                    <button class="btn btn-light emoji-btn blessure-btn mx-2" data-blessure="Courbature">Oui, type courbature</button>
                    <button class="btn btn-light emoji-btn blessure-btn mx-2" data-blessure="Oui">Oui</button>
                </div>
            </div>
            `,
            // Étape 4: Où ressentez vous ces douleurs?
            `
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">Où ressentez-vous ces douleurs ?</h5>
            </div>
            <div class="card-body text-center">
                <div class="d-flex flex-wrap justify-content-center mb-3">
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="BrasG">Bras Gauche</button>
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="BrasD">Bras Droit</button>
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="JambeG">Jambe Gauche</button>
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="JambeD">Jambe Droite</button>
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="Épaules">Épaules</button>
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="Torse">Torse</button>
                    <button class="btn btn-light emoji-btn zone-douleurs-btn m-1" data-zone-douleurs="Dos">Dos</button>
                </div>
            </div>
            `
        ];

        // Afficher le contenu de l'étape actuelle
        if (step < steps.length) {
            quizContent.innerHTML = steps[step];
        }
    },

    // Passer à l'étape suivante du quiz
    nextStep() {
        switch (this.currentStep) {
            case 0:
                switch (this.answers.feeling) {
                    case 'Fatiguer':
                        this.showStep(1); // Aller à la question sur le type de fatigue
                        break;
                    case 'Malade':
                        this.completeQuiz(); // Terminer le quiz si malade
                        break;
                    default:
                        this.showStep(3) // Aller à blessure le quiz pour "Bien" ou "Normal"
                        break;
                }
                break;

            case 1:
                switch (this.answers.fatigue) {
                    case 'physique':
                        this.completeQuiz(); // terminer sur les douleurs physiques
                        break;
                    case 'moral':
                        this.showStep(2); // Aller à la question sur les douleurs morales
                        break;
                }
                break;

            case 2:
                switch (this.answers.moralDouleurs) {
                    case 'Oui':
                        this.completeQuiz();
                        break;
                    case 'Courbatures':
                        this.completeQuiz(); // Aller à la question sur la zone des douleurs
                        break;
                    case 'Non':
                        this.completeQuiz(); // Terminer le quiz si pas de douleurs morales
                        break;
                }
                break;

            case 3:
                switch (this.answers.blessure) {
                    case 'Non':
                        this.completeQuiz(); // Terminer le quiz si pas de douleurs physiques
                        break;
                    case 'Courbature':
                    case 'Oui':
                        this.showStep(4); // Aller à la question sur la zone des douleurs
                        break;
                }
                break;

            default:
                this.showStep(this.currentStep + 1);
                break;
        }
    },

    // Compléter le quiz
    completeQuiz() {
        const quizContent = document.getElementById('quiz-content');

        // Afficher un message de remerciement
        quizContent.innerHTML = `
            <div class="card-header bg-success text-white">
                <h5 class="mb-0">Merci pour vos réponses!</h5>
            </div>
            <div class="card-body text-center">
                <p>Vos réponses ont été enregistrées avec succès.</p>
                <button id="close-quiz-btn" class="btn btn-primary">Continuer vers l'application</button>
            </div>
        `;

        // Enregistrer les réponses (simulé ici, à remplacer par une vraie API)
        console.log('Quiz answers:', this.answers);

        // Enregistrer localement que le quiz a été complété aujourd'hui
        localStorage.setItem('quizCompletedToday', new Date().toDateString());

        // Si nécessaire, envoi des données à l'API
        this.sendAnswersToAPI();
    },

    // Envoyer les réponses à l'API (à implémenter)
    sendAnswersToAPI() {
        // TODO: Implémentez cette méthode pour envoyer les données du quiz à votre API
        // Exemple:
        fetch('/api/quiz/submit', {
            method: 'POST',
            headers: userService.getHeaders(),
            body: JSON.stringify(this.answers)
        })
        .then(response => response.json())
        .then(data => console.log('Quiz submitted:', data))
        .catch(error => console.error('Error submitting quiz:', error));
    },

    // Vérifier si le quiz a déjà été complété aujourd'hui
    checkIfCompletedToday() {
        const quizCompletedToday = localStorage.getItem('quizCompletedToday');
        return quizCompletedToday === new Date().toDateString();
    }
};

// Initialiser le module de quiz au chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
    // Initialiser le module de quiz
    window.quizModule.init();
});