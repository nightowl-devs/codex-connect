package dev.nightowl.codexconnect.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReasoningConfig {
    private String effort;
    private String summary;

    public static ReasoningConfig none() {
        return ReasoningConfig.builder().effort("none").summary("auto").build();
    }

    public static ReasoningConfig low() {
        return ReasoningConfig.builder().effort("low").summary("auto").build();
    }

    public static ReasoningConfig medium() {
        return ReasoningConfig.builder().effort("medium").summary("auto").build();
    }

    public static ReasoningConfig high() {
        return ReasoningConfig.builder().effort("high").summary("auto").build();
    }

    public static ReasoningConfig xhigh() {
        return ReasoningConfig.builder().effort("xhigh").summary("auto").build();
    }
}
