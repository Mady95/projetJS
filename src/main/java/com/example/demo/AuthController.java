package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Afficher le formulaire d'inscription
    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    // Gestion de l'inscription
    @PostMapping("/signup")
    public String handleSignup(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password, Model model) {
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Cet email est déjà utilisé !");
            return "signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);

        return "redirect:/login";
    }

    // Afficher le formulaire de connexion
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // Gestion de la connexion
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session, Model model) {
        User user = userRepository.findByEmail(email);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Identifiants incorrects !");
            return "login";
        }

        // Enregistrer l'utilisateur connecté dans la session
        session.setAttribute("loggedInUserId", user.getId());
        model.addAttribute("username", user.getUsername());
        return "welcome";
    }

    // Afficher la page de profil
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId"); // Corrigé en Long
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
            }
        }
        return "profile";
    }

    // Mise à jour des informations personnelles
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String email,
                                HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId"); // Corrigé en Long
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setUsername(username);
                user.setEmail(email);
                userRepository.save(user); // Mettre à jour en base de données

                model.addAttribute("confirmationMessage", "Les modifications ont été enregistrées avec succès.");
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
            }
        }
        return "profile";
    }

    // Déconnexion
    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Supprimer toutes les données de la session
        return "redirect:/login";
    }
}
