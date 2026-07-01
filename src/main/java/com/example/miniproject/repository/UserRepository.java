package com.example.miniproject.repository;

import com.example.miniproject.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @EntityGraph: load User + Roles trong 1 câu SELECT duy nhất
     * Tránh N+1 Query khi Spring Security gọi loadUserByUsername (NFR-03)
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
