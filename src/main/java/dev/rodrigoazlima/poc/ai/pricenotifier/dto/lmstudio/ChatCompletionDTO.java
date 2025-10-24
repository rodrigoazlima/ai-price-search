package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class ChatCompletionDTO {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChoiceDTO> choices;

}
