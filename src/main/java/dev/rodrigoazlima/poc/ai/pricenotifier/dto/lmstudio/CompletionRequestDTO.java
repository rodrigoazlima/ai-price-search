package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.Map;


@Data
@Builder
public class CompletionRequestDTO {

    private String model;
    private String prompt;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;
    private String stop;
    private Double presence_penalty;
    private Double frequency_penalty;
    private Map<String, Integer> logit_bias;
    private Double repeat_penalty;
    private Integer seed;
    private Integer top_p;
    private Integer top_k;

}
