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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class PostCreate {
        @Test
        @DisplayName("POST /api/v1/products with valid body returns 201 and product_id")
        void create_valid_returns201() throws Exception {
            String body = "{\n" +
                    "  \"url\": \"https://example.com/product/123\",\n" +
                    "  \"title\": \"Smartphone XYZ\",\n" +
                    "  \"desired_price\": 99.99,\n" +
                    "  \"currency\": \"USD\"\n" +
                    "}";

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", not(emptyString())));
        }
    }

    @Nested
    class GetList {
        @Test
        @DisplayName("GET /api/v1/products with params returns 200 and empty products array")
        void list_withParams_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("limit", "50")
                            .param("offset", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.products", isA(Iterable.class)))
                    .andExpect(jsonPath("$.products", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/v1/products without params returns 200 and empty products array (defaults)")
        void list_withoutParams_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.products", isA(Iterable.class)))
                    .andExpect(jsonPath("$.products", hasSize(0)));
        }
    }

    @Nested
    class PutUpdate {
        @Test
        @DisplayName("PUT /api/v1/products/{id} returns 200 and echoes product_id")
        void update_returns200_andEchoesId() throws Exception {
            String productId = "prod123";
            String body = "{\n" +
                    "  \"desired_price\": 89.99,\n" +
                    "  \"active_flag\": true\n" +
                    "}";

            mockMvc.perform(put("/api/v1/products/{product_id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.product_id", is(productId)));
        }
    }

    @Nested
    class DeleteRemove {
        @Test
        @DisplayName("DELETE /api/v1/products/{id} returns 204 No Content")
        void delete_returns204() throws Exception {
            mockMvc.perform(delete("/api/v1/products/{product_id}", "prod123"))
                    .andExpect(status().isNoContent());
        }
    }
}
