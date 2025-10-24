package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CompletionChoiceDTO {

    private Integer index;
    private String text;
    private String finishReason;

}
