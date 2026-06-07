package dev.nightowl.codexconnect.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageResponse {


    @JsonProperty("user_id")
    private String userId;

    private String email;
    @JsonProperty("plan_type")
    private String planType;

    @JsonProperty("rate_limit")
    private RateLimit rateLimit;

    @JsonProperty("code_review_rate_limit")
    private RateLimit codeReviewRateLimit;

//    @JsonProperty("additional_rate_limits")
//    private Object additionalRateLimits;

    private Credits credits;

    @JsonProperty("spend_control")
    private SpendControl spendControl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendControl {
        private Boolean reached;
        @JsonProperty("individual_limit")
        private Integer individualLimit;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credits {
        @JsonProperty("has_credits")
        private Boolean hasCredits;
        private Boolean unlimited;
        @JsonProperty("overage_limit_reached")
        private Boolean overageLimitReached;
        private Integer balance;
        @JsonProperty("approx_local_messages")
        private Integer approxLocalMessages;
        @JsonProperty("approx_cloud_messages")
        private Integer approxCloudMessages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimit {
        private Boolean allowed;
        @JsonProperty("limit_reached")
        private Boolean limitReached;

        @JsonProperty("primary_window")
        private Window primaryWindow;
        @JsonProperty("secondary_window")
        private Window secondaryWindow;


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Window {
            @JsonProperty("used_percent")
            private Integer usedPercent;
            @JsonProperty("limit_window_seconds")
            private Integer limitWindowSeconds;
            @JsonProperty("reset_after_seconds")
            private Integer resetAfterSeconds;
            @JsonProperty("reset_at")
            private Long resetAt;
        }
    }
}
