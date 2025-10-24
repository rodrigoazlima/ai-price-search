package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionRequestDTO {

    private String model;
    private List<ChatMessageDTO> messages;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    private String stop;
    private Double presencePenalty;
    private Double frequencyPenalty;
    private Map<String, Integer> logitBias;
    private Double repeatPenalty;
    private Integer seed;
    private Integer topP;
    private Integer topK;

}
