package com.sellerhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesPrioritizedOperationsQueue() throws Exception {
        mockMvc.perform(get("/api/operations/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("CRITICAL"));
    }

    @Test
    void answersPendingQuestion() throws Exception {
        mockMvc.perform(post("/api/questions/Q-202/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Sí, incluye switches rojos lineales.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANSWERED"));
    }
}
