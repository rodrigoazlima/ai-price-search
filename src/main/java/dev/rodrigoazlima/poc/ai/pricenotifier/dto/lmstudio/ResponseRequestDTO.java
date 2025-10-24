package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class ResponseRequestDTO {

    private String model;
    private String input;
    private ReasoningDTO reasoning;
    private String previousResponseId;
    private Boolean stream;
    private List<ToolDTO> tools;

}
