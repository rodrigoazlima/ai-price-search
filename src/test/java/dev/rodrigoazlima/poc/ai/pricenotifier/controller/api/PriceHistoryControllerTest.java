package dev.rodrigoazlima.poc.ai.pricenotifier.controller.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PriceHistoryController.class)
class PriceHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/v1/products/{id}/history - success cases")
    class GetSuccess {
        @Test
        @DisplayName("valid request returns 200 with empty history and product_id")
        void validRequest_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("from", "2023-01-01T00:00:00Z")
                            .param("to", "2023-12-31T23:59:59Z")
                            .param("limit", "50")
                            .param("sort", "asc")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", is("prod123")))
                    .andExpect(jsonPath("$.history", isA(Iterable.class)))
                    .andExpect(jsonPath("$.history", hasSize(0)));
        }

        @Test
        @DisplayName("default params (no query) returns 200 with empty history")
        void defaultParams_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", is("prod123")))
                    .andExpect(jsonPath("$.history", isA(Iterable.class)))
                    .andExpect(jsonPath("$.history", hasSize(0)));
        }

        @Test
        @DisplayName("sort is case-insensitive (DESC) and returns 200")
        void sortCaseInsensitive_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("sort", "DESC")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", is("prod123")));
        }

        @Test
        @DisplayName("sort with whitespace and case ( ' Asc ' ) returns 200")
        void sortWithWhitespace_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("sort", " Asc ")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", is("prod123")));
        }

        @Test
        @DisplayName("blank from/to are treated as absent and return 200")
        void blankFromTo_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("from", "")
                            .param("to", "")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", is("prod123")))
                    .andExpect(jsonPath("$.history", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}/history - validation errors")
    class GetValidationErrors {
        @Test
        @DisplayName("invalid sort returns 400 with error message")
        void invalidSort_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("sort", "sideways")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("invalid sort")))
                    .andExpect(jsonPath("$.detail", containsStringIgnoringCase("asc")));
        }

        @Test
        @DisplayName("invalid 'from' timestamp returns 400 with error message")
        void invalidFromTimestamp_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("from", "not-a-timestamp")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("invalid timestamp")))
                    .andExpect(jsonPath("$.detail", containsStringIgnoringCase("ISO-8601")));
        }

        @Test
        @DisplayName("invalid 'to' timestamp returns 400 with error message")
        void invalidToTimestamp_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/products/{product_id}/history", "prod123")
                            .param("to", "bad-timestamp")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", containsStringIgnoringCase("invalid timestamp")))
                    .andExpect(jsonPath("$.detail", containsStringIgnoringCase("ISO-8601")));
        }
    }
}
