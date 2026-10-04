package com.livequiz.quiz;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);
}
