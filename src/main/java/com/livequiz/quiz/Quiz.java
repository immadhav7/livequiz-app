package com.livequiz.quiz;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true, length = 8)
    private String joinCode;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Question> questions = new ArrayList<>();

    protected Quiz() {
    }

    public Quiz(String title, String joinCode) {
        this.title = title;
        this.joinCode = joinCode;
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getJoinCode() { return joinCode; }
    public List<Question> getQuestions() { return questions; }
}
