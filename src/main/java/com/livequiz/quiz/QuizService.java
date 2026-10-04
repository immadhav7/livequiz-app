package com.livequiz.quiz;

import com.livequiz.quiz.QuizDtos.CreateQuestion;
import com.livequiz.quiz.QuizDtos.CreateQuiz;
import com.livequiz.quiz.QuizDtos.LeaderboardEntry;
import com.livequiz.quiz.QuizDtos.QuestionView;
import com.livequiz.quiz.QuizDtos.QuizView;
import com.livequiz.quiz.QuizDtos.Result;
import com.livequiz.quiz.QuizDtos.Submit;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuizService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final QuizRepository quizzes;
    private final SubmissionRepository submissions;
    private final SecureRandom random = new SecureRandom();

    public QuizService(QuizRepository quizzes, SubmissionRepository submissions) {
        this.quizzes = quizzes;
        this.submissions = submissions;
    }

    @Transactional
    public QuizView create(CreateQuiz request) {
        Quiz quiz = new Quiz(request.title().trim(), newJoinCode());
        for (CreateQuestion q : request.questions()) {
            if (q.correctIndex() >= q.options().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "correctIndex is out of range for question: " + q.text());
            }
            quiz.addQuestion(new Question(q.text().trim(), q.options(), q.correctIndex()));
        }
        quizzes.save(quiz);
        return toView(quiz);
    }

    @Transactional(readOnly = true)
    public QuizView get(String joinCode) {
        return toView(find(joinCode));
    }

    @Transactional
    public Result submit(String joinCode, Submit request) {
        Quiz quiz = find(joinCode);
        List<Question> questions = quiz.getQuestions();
        if (request.answers().size() != questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected " + questions.size() + " answers but got " + request.answers().size());
        }
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer answer = request.answers().get(i);
            if (answer != null && answer == questions.get(i).getCorrectIndex()) {
                score++;
            }
        }
        String player = request.playerName().trim();
        submissions.save(new Submission(quiz, player, score, questions.size()));
        return new Result(player, score, questions.size());
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard(String joinCode) {
        Quiz quiz = find(joinCode);
        return submissions.findByQuizOrderByScoreDescSubmittedAtAsc(quiz).stream()
                .map(s -> new LeaderboardEntry(s.getPlayerName(), s.getScore(), s.getTotal()))
                .toList();
    }

    private Quiz find(String joinCode) {
        return quizzes.findByJoinCode(joinCode.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    private String newJoinCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (quizzes.existsByJoinCode(code));
        return code;
    }

    private QuizView toView(Quiz quiz) {
        List<QuestionView> questionViews = quiz.getQuestions().stream()
                .map(q -> new QuestionView(q.getId(), q.getText(), List.copyOf(q.getOptions())))
                .toList();
        return new QuizView(quiz.getJoinCode(), quiz.getTitle(), questionViews);
    }
}
