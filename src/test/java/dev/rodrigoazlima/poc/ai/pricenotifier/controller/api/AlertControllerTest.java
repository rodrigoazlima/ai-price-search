package dev.rodrigoazlima.poc.ai.pricenotifier.controller.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class PostCreate {
        @Test
        @DisplayName("POST /api/v1/alerts with valid body returns 201 and alert_id")
        void create_valid_returns201() throws Exception {
            String body = "{\n" +
                    "  \"product_id\": \"prod123\",\n" +
                    "  \"desired_price\": 89.99,\n" +
                    "  \"min_interval_hours\": 24\n" +
                    "}";

            mockMvc.perform(post("/api/v1/alerts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.alert_id", not(emptyString())));
        }

        @Test
        @DisplayName("POST /api/v1/alerts with negative desired_price returns 400")
        void create_negativePrice_returns400() throws Exception {
            String body = "{\n" +
                    "  \"product_id\": \"prod123\",\n" +
                    "  \"desired_price\": -1,\n" +
                    "  \"min_interval_hours\": 24\n" +
                    "}";

            mockMvc.perform(post("/api/v1/alerts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("desired_price")));
        }

        @Test
        @DisplayName("POST /api/v1/alerts with missing product_id returns 400")
        void create_missingProductId_returns400() throws Exception {
            String body = "{\n" +
                    "  \"desired_price\": 10,\n" +
                    "  \"min_interval_hours\": 24\n" +
                    "}";

            mockMvc.perform(post("/api/v1/alerts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("product_id")));
        }

        @Test
        @DisplayName("POST /api/v1/alerts with negative min_interval_hours returns 400")
        void create_negativeInterval_returns400() throws Exception {
            String body = "{\n" +
                    "  \"product_id\": \"prod123\",\n" +
                    "  \"desired_price\": 10,\n" +
                    "  \"min_interval_hours\": -1\n" +
                    "}";

            mockMvc.perform(post("/api/v1/alerts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("min_interval_hours")));
        }
    }

    @Nested
    class GetList {
        @Test
        @DisplayName("GET /api/v1/alerts without params returns 200 and empty alerts array")
        void list_default_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/alerts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.alerts", isA(Iterable.class)))
                    .andExpect(jsonPath("$.alerts", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/v1/alerts with invalid limit returns 400")
        void list_invalidLimit_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/alerts").param("limit", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("limit")));

            mockMvc.perform(get("/api/v1/alerts").param("limit", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("limit")));
        }

        @Test
        @DisplayName("GET /api/v1/alerts with invalid offset returns 400")
        void list_invalidOffset_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/alerts").param("offset", "-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("offset")));
        }

        @Test
        @DisplayName("GET /api/v1/alerts filtered by product returns 200 with empty array (placeholder)")
        void list_byProduct_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/alerts").param("product_id", "prod123").param("limit", "50").param("offset", "0"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.alerts", isA(Iterable.class)))
                    .andExpect(jsonPath("$.alerts", hasSize(0)));
        }
    }
}
