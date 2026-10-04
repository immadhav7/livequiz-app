package com.livequiz.quiz;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false, length = 60)
    private String playerName;

    private int score;
    private int total;

    @Column(nullable = false)
    private Instant submittedAt;

    protected Submission() {
    }

    public Submission(Quiz quiz, String playerName, int score, int total) {
        this.quiz = quiz;
        this.playerName = playerName;
        this.score = score;
        this.total = total;
        this.submittedAt = Instant.now();
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public Instant getSubmittedAt() { return submittedAt; }
}
