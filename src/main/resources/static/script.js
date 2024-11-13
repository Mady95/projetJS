document.addEventListener('DOMContentLoaded', () => {
    // Animation de focus pour les champs
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('focus', () => {
            input.style.borderColor = '#457b9d';
        });

        input.addEventListener('blur', () => {
            input.style.borderColor = '#ccc';
        });
    });

    // Validation en temps réel
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const form = document.querySelector('form');

    emailInput?.addEventListener('input', () => {
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        emailInput.style.borderColor = emailPattern.test(emailInput.value) ? 'green' : 'red';
    });

    passwordInput?.addEventListener('input', () => {
        passwordInput.style.borderColor = passwordInput.value.length >= 8 ? 'green' : 'red';
    });

    form?.addEventListener('submit', (event) => {
        if (!emailInput.value.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/) || passwordInput.value.length < 8) {
            alert("Veuillez vérifier vos informations !");
            event.preventDefault();
        }
    });

    // Alerte avant de quitter la page
    let formModified = false;
    inputs.forEach(input => {
        input.addEventListener('input', () => {
            formModified = true;
        });
    });

    document.addEventListener('DOMContentLoaded', () => {
        const confirmationMessage = document.getElementById('confirmationMessage');
        if (confirmationMessage && confirmationMessage.textContent.trim() !== '') {
            confirmationMessage.classList.add('show'); // Affiche le message de confirmation
        }
    });


    // Afficher/Masquer le mot de passe
    const togglePassword = document.getElementById('togglePassword');
    if (togglePassword) {
        togglePassword.addEventListener('click', () => {
            const passwordInput = document.getElementById('password');
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                togglePassword.textContent = '🙈';
            } else {
                passwordInput.type = 'password';
                togglePassword.textContent = '👁';
            }
        });
    }

    // Transition douce pour les messages d'erreur
    const errorElement = document.querySelector('.error');
    if (errorElement && errorElement.textContent.trim() !== '') {
        errorElement.classList.add('show');
    }

});
