package com.livequiz.quiz;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "question_text", nullable = false, length = 500)
    private String text;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "position")
    @Column(name = "option_text", nullable = false)
    private List<String> options = new ArrayList<>();

    @Column(nullable = false)
    private int correctIndex;

    protected Question() {
    }

    public Question(String text, List<String> options, int correctIndex) {
        this.text = text;
        this.options = new ArrayList<>(options);
        this.correctIndex = correctIndex;
    }

    void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public Long getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
}
