package dev.rodrigoazlima.poc.ai.pricenotifier.service.impl;

import dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio.ChatCompletionDTO;
import dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio.ChatCompletionRequestDTO;
import dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio.ChatMessageDTO;
import dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio.ChoiceDTO;
import dev.rodrigoazlima.poc.ai.pricenotifier.feign.LmStudioClient;
import dev.rodrigoazlima.poc.ai.pricenotifier.service.LmStudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LmStudioServiceImpl implements LmStudioService {

    @Autowired
    private LmStudioClient lmStudioClient;

    @Value("${lmstudio.api.api-key:}")
    private String apiKey;
    @Value("${lmstudio.model:default-model}")
    private String defaultModel;

    @Override
    public String prompt(String text) {
        // Create a chat completion request with the input text
        ChatCompletionRequestDTO request = new ChatCompletionRequestDTO();
        request.setModel(defaultModel);

        // Create messages list with user input
        List<ChatMessageDTO> messages = new ArrayList<>();
        ChatMessageDTO userMessage = new ChatMessageDTO();
        userMessage.setRole("user");
        userMessage.setContent(text);
        messages.add(userMessage);

        request.setMessages(messages);

        // Set default parameters
        request.setTemperature(0.7);
        request.setMaxTokens(150);

        // Call the LM Studio client to get the completion
        ChatCompletionDTO response = lmStudioClient.createChatCompletion(request);

        // Extract and return the response text
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            List<ChoiceDTO> choices = response.getChoices();
            if (choices.get(0) != null && choices.get(0).getMessage() != null) {
                return choices.get(0).getMessage().getContent();
            }
        }

        return "";
    }
}
