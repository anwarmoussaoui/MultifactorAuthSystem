package com.example.auth.repositories;

import com.example.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
Optional<User> findUtilisateurByEmail(String email);

}
