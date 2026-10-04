package com.livequiz.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class QuizDtos {

    private QuizDtos() {
    }

    public record CreateQuestion(
            @NotBlank @Size(max = 500) String text,
            @NotNull @Size(min = 2, max = 6) List<@NotBlank String> options,
            @Min(0) int correctIndex) {
    }

    public record CreateQuiz(
            @NotBlank @Size(max = 120) String title,
            @NotNull @Size(min = 1, max = 50) List<@Valid CreateQuestion> questions) {
    }

    public record QuestionView(Long id, String text, List<String> options) {
    }

    public record QuizView(String joinCode, String title, List<QuestionView> questions) {
    }

    public record Submit(
            @NotBlank @Size(max = 60) String playerName,
            @NotNull List<Integer> answers) {
    }

    public record Result(String playerName, int score, int total) {
    }

    public record LeaderboardEntry(String playerName, int score, int total) {
    }
}
