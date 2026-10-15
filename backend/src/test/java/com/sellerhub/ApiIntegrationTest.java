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
import static org.hamcrest.Matchers.oneOf;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private String sellerSession() throws Exception {
        return mockMvc.perform(post("/api/demo/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SELLER\",\"sellerId\":\"seller-01\"}"))
                .andReturn().getResponse().getContentAsString().replaceAll(".*\\\"token\\\":\\\"([^\\\"]+).*$", "$1");
    }

    private String operationsSession() throws Exception {
        return mockMvc.perform(post("/api/demo/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"OPERATIONS\"}"))
                .andReturn().getResponse().getContentAsString().replaceAll(".*\\\"token\\\":\\\"([^\\\"]+).*$", "$1");
    }

    @Test
    void exposesPrioritizedOperationsQueue() throws Exception {
        mockMvc.perform(get("/api/operations/questions").header("X-Demo-Session", operationsSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].priority").value("CRITICAL"));
    }

    @Test
    void answersPendingQuestion() throws Exception {
        mockMvc.perform(post("/api/questions/Q-202/answer").header("X-Demo-Session", sellerSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Sí, incluye switches rojos lineales.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANSWERED"));
    }

    @Test
    void rejectsRequestsWithoutSession() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sellerCannotReadAnotherSeller() throws Exception {
        mockMvc.perform(get("/api/orders?sellerId=seller-02")
                        .header("X-Demo-Session", sellerSession()))
                .andExpect(status().isForbidden());
    }

    @Test
    void orderSummaryDoesNotExposeBuyerPii() throws Exception {
        mockMvc.perform(get("/api/orders").header("X-Demo-Session", sellerSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].buyerEmail").doesNotExist())
                .andExpect(jsonPath("$.items[0].shippingAddress").doesNotExist());
    }

    @Test
    void appliesQuestionStatusFilterOnBackend() throws Exception {
        mockMvc.perform(get("/api/questions?status=PENDING&page=0&pageSize=2")
                        .header("X-Demo-Session", sellerSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void appliesOrderSearchAndPaginationOnBackend() throws Exception {
        mockMvc.perform(get("/api/orders?search=teclado&page=0&pageSize=1")
                        .header("X-Demo-Session", sellerSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value("ORD-1047"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void preventsStartingDuplicateGroupAnalysisForSameSeller() throws Exception {
        String token = sellerSession();
        mockMvc.perform(post("/api/questions/groups/analyze").header("X-Demo-Session", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(oneOf("RUNNING", "COMPLETED")));
        mockMvc.perform(post("/api/questions/groups/analyze").header("X-Demo-Session", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(oneOf("RUNNING", "COMPLETED")));
    }

    @Test
    void createsQuestionAndTransitionsOrder() throws Exception {
        String token = sellerSession();
        mockMvc.perform(post("/api/orders/ORD-1047/questions").header("X-Demo-Session", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buyerName\":\"Ana\",\"text\":\"¿Tiene garantía?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
        mockMvc.perform(post("/api/orders/ORD-1047/status").header("X-Demo-Session", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
