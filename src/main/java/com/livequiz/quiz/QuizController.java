package com.livequiz.quiz;

import com.livequiz.quiz.QuizDtos.CreateQuiz;
import com.livequiz.quiz.QuizDtos.LeaderboardEntry;
import com.livequiz.quiz.QuizDtos.QuizView;
import com.livequiz.quiz.QuizDtos.Result;
import com.livequiz.quiz.QuizDtos.Submit;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService service;

    public QuizController(QuizService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizView create(@Valid @RequestBody CreateQuiz body) {
        return service.create(body);
    }

    @GetMapping("/{joinCode}")
    public QuizView get(@PathVariable("joinCode") String joinCode) {
        return service.get(joinCode);
    }

    @PostMapping("/{joinCode}/submissions")
    public Result submit(@PathVariable("joinCode") String joinCode, @Valid @RequestBody Submit body) {
        return service.submit(joinCode, body);
    }

    @GetMapping("/{joinCode}/leaderboard")
    public List<LeaderboardEntry> leaderboard(@PathVariable("joinCode") String joinCode) {
        return service.leaderboard(joinCode);
    }
}
