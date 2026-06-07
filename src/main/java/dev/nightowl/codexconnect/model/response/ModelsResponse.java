package dev.nightowl.codexconnect.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelsResponse {
    private List<ModelInfo> models;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)

    public static class ModelInfo {

        private String slug;

        @JsonProperty("prefer_websockets")
        private boolean preferWebsockets;

        @JsonProperty("support_verbosity")
        private boolean supportVerbosity;

        @JsonProperty("default_verbosity")
        private String defaultVerbosity;

        @JsonProperty("apply_patch_tool_type")
        private String applyPatchToolType;

        @JsonProperty("web_search_tool_type")
        private String webSearchToolType;

        @JsonProperty("input_modalities")
        private List<String> inputModalities;

        @JsonProperty("supports_image_detail_original")
        private boolean supportsImageDetailOriginal;

        @JsonProperty("truncation_policy")
        private TruncationPolicy truncationPolicy;

        @JsonProperty("supports_parallel_tool_calls")
        private boolean supportsParallelToolCalls;

        @JsonProperty("tool_mode")
        private String toolMode;

        @JsonProperty("multi_agent_version")
        private String multiAgentVersion;

        @JsonProperty("use_responses_lite")
        private boolean useResponsesLite;

        @JsonProperty("auto_review_model_override")
        private String autoReviewModelOverride;

        @JsonProperty("context_window")
        private Integer contextWindow;

        @JsonProperty("max_context_window")
        private Integer maxContextWindow;

        @JsonProperty("auto_compact_token_limit")
        private Integer autoCompactTokenLimit;

        @JsonProperty("reasoning_summary_format")
        private String reasoningSummaryFormat;

        @JsonProperty("default_reasoning_summary")
        private String defaultReasoningSummary;

        @JsonProperty("display_name")
        private String displayName;

        private String description;

        @JsonProperty("default_reasoning_level")
        private String defaultReasoningLevel;

        @JsonProperty("supported_reasoning_levels")
        private List<ReasoningLevel> supportedReasoningLevels;

        @JsonProperty("shell_type")
        private String shellType;

        private String visibility;

        @JsonProperty("minimal_client_version")
        private String minimalClientVersion;

        @JsonProperty("supported_in_api")
        private boolean supportedInApi;

        @JsonProperty("availability_nux")
        private AvailabilityNux availabilityNux;

        private Object upgrade;

        private Integer priority;

        //   @JsonProperty("base_instructions") //its longgggg dont realy feel like storing it in ram yk
        // private String baseInstructions;

        @JsonProperty("experimental_supported_tools")
        private List<String> experimentalSupportedTools;

        @JsonProperty("available_in_plans")
        private List<String> availableInPlans;

        @JsonProperty("supports_search_tool")
        private boolean supportsSearchTool;

        @JsonProperty("default_service_tier")
        private String defaultServiceTier;

        @JsonProperty("service_tiers")
        private List<String> serviceTiers;

        @JsonProperty("additional_speed_tiers")
        private List<String> additionalSpeedTiers;

        @JsonProperty("supports_reasoning_summaries")
        private boolean supportsReasoningSummaries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TruncationPolicy {
        private String mode;
        private Integer limit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasoningLevel {
        private String effort;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityNux {
        private String message;
    }
}
