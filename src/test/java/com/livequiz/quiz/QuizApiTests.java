package com.livequiz.quiz;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class QuizApiTests {

    private static final String QUIZ_JSON = """
            {"title":"Linux basics","questions":[
              {"text":"Which command lists files?","options":["ls","cd","rm"],"correctIndex":0},
              {"text":"Which command prints the current directory?","options":["mkdir","pwd","cat"],"correctIndex":1}
            ]}
            """;

    @Autowired
    MockMvc mockMvc;

    private String createQuizAndGetCode() throws Exception {
        String body = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUIZ_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"joinCode\"\\s*:\\s*\"([A-Z0-9]+)\"").matcher(body);
        if (!m.find()) {
            throw new AssertionError("No joinCode in response: " + body);
        }
        return m.group(1);
    }

    private void submit(String code, String player, String answersJson) throws Exception {
        mockMvc.perform(post("/api/quizzes/" + code + "/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"" + player + "\",\"answers\":" + answersJson + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void createQuiz_returnsJoinCode_andHidesCorrectAnswers() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUIZ_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.joinCode").isNotEmpty())
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(content().string(not(containsString("correctIndex"))));
    }

    @Test
    void fullFlow_scoresSubmissions_andRanksLeaderboard() throws Exception {
        String code = createQuizAndGetCode();

        mockMvc.perform(post("/api/quizzes/" + code + "/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Asha\",\"answers\":[0,1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(2))
                .andExpect(jsonPath("$.total").value(2));

        submit(code, "Ravi", "[0,0]");

        mockMvc.perform(get("/api/quizzes/" + code + "/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerName").value("Asha"))
                .andExpect(jsonPath("$[0].score").value(2))
                .andExpect(jsonPath("$[1].playerName").value("Ravi"))
                .andExpect(jsonPath("$[1].score").value(1));
    }

    @Test
    void unknownJoinCode_returns404() throws Exception {
        mockMvc.perform(get("/api/quizzes/ZZZZZZ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongNumberOfAnswers_returns400() throws Exception {
        String code = createQuizAndGetCode();
        mockMvc.perform(post("/api/quizzes/" + code + "/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Asha\",\"answers\":[0]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankTitle_isRejectedByValidation() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"questions\":[{\"text\":\"Q\",\"options\":[\"a\",\"b\"],\"correctIndex\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void correctIndexOutOfRange_returns400() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"questions\":[{\"text\":\"Q\",\"options\":[\"a\",\"b\"],\"correctIndex\":5}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void joinCode_isCaseInsensitive() throws Exception {
        String code = createQuizAndGetCode();
        mockMvc.perform(get("/api/quizzes/" + code.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joinCode").value(code));
    }
}

