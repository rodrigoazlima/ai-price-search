package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ChoiceDTO {

    private Integer index;
    private ChatMessageDTO message;
    private String finishReason;

}
