package dev.nightowl.codexconnect.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeviceAuthResponse {
    @JsonProperty("device_auth_id")
    private String deviceAuthId;

    @JsonProperty("user_code")
    private String userCode;

    @JsonProperty("interval")
    private String interval;

    @JsonProperty("verification_uri")
    private String verificationUri;
    @JsonProperty("expires_at")
    private String expiresAt;
}
