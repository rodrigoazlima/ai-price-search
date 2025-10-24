package dev.rodrigoazlima.poc.ai.pricenotifier.feign;

import dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "lm-studio",
        url = "${lmstudio.api.url:http://localhost:1234}"
)
public interface LmStudioClient {

    @GetMapping("/v1/models")
    List<ModelDTO> listModels();

    @PostMapping("/v1/responses")
    ResponseDTO createResponse(
            @RequestBody
            ResponseRequestDTO request);

    @PostMapping("/v1/chat/completions")
    ChatCompletionDTO createChatCompletion(
            @RequestBody
            ChatCompletionRequestDTO request);

    @PostMapping("/v1/embeddings")
    EmbeddingDTO createEmbedding(
            @RequestBody
            EmbeddingRequestDTO request);

    @PostMapping("/v1/completions")
    CompletionDTO createCompletion(
            @RequestBody
            CompletionRequestDTO request);

}