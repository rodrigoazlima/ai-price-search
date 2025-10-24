package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class ToolDTO {

    private String type;
    private String serverLabel;
    private String serverUrl;
    private List<String> allowedTools;

}
