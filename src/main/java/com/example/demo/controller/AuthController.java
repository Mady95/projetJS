package com.example.demo.controller;

import com.example.demo.model.BankAccount;
import com.example.demo.model.HistoriqueConnexion;
import com.example.demo.model.Transaction;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.HistoriqueConnexionRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;


import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private HistoriqueConnexionRepository historiqueConnexionRepository;

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

    // Gestion de la connexion classique
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

        // Enregistrer l'historique de connexion
        HistoriqueConnexion historique = new HistoriqueConnexion();
        historique.setUtilisateur(user);
        historique.setDateConnexion(LocalDateTime.now());
        historique.setAdresseIp("127.0.0.1"); // Remplacez par une méthode pour récupérer l'adresse IP réelle
        historiqueConnexionRepository.save(historique);

        model.addAttribute("username", user.getUsername());
        return "welcome";
    }

    @GetMapping("/welcome")
    public String showWelcomePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                model.addAttribute("username", user.getUsername());
            }
        }
        return "welcome";
    }

    // Afficher la liste des comptes bancaires
    @GetMapping("/accounts")
    public String getAccounts(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login"; // Redirige vers la page de connexion si l'utilisateur n'est pas connecté
        }

        // Récupérer la liste des comptes bancaires associés à cet utilisateur
        List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
        if (accounts == null || accounts.isEmpty()) {
            model.addAttribute("error", "Aucun compte trouvé !");
        } else {
            model.addAttribute("accounts", accounts);
        }

        return "accounts"; // Assurez-vous que `accounts.html` existe dans `/templates`
    }

    // Afficher les transactions pour un compte donné
    @GetMapping("/transactions/{accountId}")
    public String getTransactions(@PathVariable Long accountId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login"; // Redirige vers la page de connexion si l'utilisateur n'est pas connecté
        }

        BankAccount account = bankAccountRepository.findById(accountId).orElse(null);
        if (account == null || !account.getUser().getId().equals(userId)) {
            model.addAttribute("errorMessage", "Aucun compte trouvé pour cet utilisateur ou cet ID.");
            return "error"; // Vue pour afficher une erreur générique
        }

        model.addAttribute("transactions", account.getTransactions());
        model.addAttribute("accountName", account.getAccountName());
        return "transactions"; // Assurez-vous que `transactions.html` existe dans `/templates`
    }

    @PostMapping("/accounts/add")
    public String addAccount(@RequestParam String accountName,
                             @RequestParam Double balance,
                             HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login"; // Redirige vers la connexion si l'utilisateur n'est pas connecté
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            model.addAttribute("errorMessage", "Utilisateur non trouvé !");
            return "accounts";
        }

        BankAccount newAccount = new BankAccount();
        newAccount.setAccountName(accountName);
        newAccount.setBalance(balance);
        newAccount.setUser(user);

        bankAccountRepository.save(newAccount); // Sauvegarde du nouveau compte

        return "redirect:/accounts"; // Redirige vers la liste des comptes
    }

    @PostMapping("/accounts/delete/{accountId}")
    public String deleteAccount(@PathVariable Long accountId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login"; // Redirige vers la page de connexion si l'utilisateur n'est pas connecté
        }

        // Vérifie que le compte appartient à l'utilisateur connecté
        BankAccount account = bankAccountRepository.findById(accountId).orElse(null);
        if (account == null || !account.getUser().getId().equals(userId)) {
            model.addAttribute("errorMessage", "Aucun compte trouvé ou vous n'avez pas les droits pour le supprimer.");
            return "accounts";
        }

        bankAccountRepository.delete(account); // Supprime le compte de la base de données
        return "redirect:/accounts"; // Redirige vers la liste des comptes
    }

    @PostMapping("/transactions/add/{accountId}")
    public String addTransaction(
            @PathVariable("accountId") Long accountId,
            @RequestParam("amount") Double amount,
            @RequestParam("description") String description,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login";
        }

        BankAccount account = bankAccountRepository.findById(accountId).orElse(null);
        if (account == null || !account.getUser().getId().equals(userId)) {
            model.addAttribute("errorMessage", "Compte non trouvé ou accès non autorisé.");
            return "transactions";
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBankAccount(account);
        transaction.setTransactionDate(LocalDateTime.now()); // Ajout de la date actuelle

        transactionRepository.save(transaction);

        return "redirect:/transactions/" + accountId;
    }




    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<String> handleLoginAjax(@RequestBody LoginRequest loginRequest, HttpSession session) {
        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body("Identifiants incorrects !");
        }

        // Enregistrer l'utilisateur connecté dans la session
        session.setAttribute("loggedInUserId", user.getId());

        // Enregistrer l'historique de connexion
        HistoriqueConnexion historique = new HistoriqueConnexion();
        historique.setUtilisateur(user);
        historique.setDateConnexion(LocalDateTime.now());
        historique.setAdresseIp("127.0.0.1"); // Remplacez par une méthode pour récupérer l'adresse IP réelle
        historiqueConnexionRepository.save(historique);

        return ResponseEntity.ok("Connexion réussie !");
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login"; // Redirige vers la page de connexion si l'utilisateur n'est pas connecté
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
        } else {
            model.addAttribute("errorMessage", "Utilisateur non trouvé !");
        }

        return "profile"; // Assurez-vous que `profile.html` existe dans `/templates`
    }



    // Afficher l'historique des connexions
    @GetMapping("/historique")
    public String afficherHistorique(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId != null) {
            List<HistoriqueConnexion> historique = historiqueConnexionRepository.findByUtilisateurId(userId);
            model.addAttribute("historique", historique);
        }
        return "historique";
    }

    // Déconnexion
    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Supprimer toutes les données de la session
        return "redirect:/login";
    }
}

// DTO pour la requête AJAX
class LoginRequest {
    private String email;
    private String password;

    // Getters et setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
