package com.example.demo.repository;

import com.example.demo.model.HistoriqueConnexion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoriqueConnexionRepository extends JpaRepository<HistoriqueConnexion, Long> {
    List<HistoriqueConnexion> findByUtilisateurId(Long utilisateurId);
}
