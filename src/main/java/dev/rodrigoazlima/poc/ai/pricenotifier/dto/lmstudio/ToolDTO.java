package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class ToolDTO {

    private String type;
    private String server_label;
    private String server_url;
    private List<String> allowed_tools;

}
