package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ModelDTO {

    private String id;
    private String object;
    private long created;
    private String ownedBy;

}
