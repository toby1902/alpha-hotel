package com.alpha.hotel.repository;

import com.alpha.hotel.model.Utilisateur;
import com.alpha.hotel.model.enums.RoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRoleInAndActifTrue(Collection<RoleUtilisateur> roles);
}
