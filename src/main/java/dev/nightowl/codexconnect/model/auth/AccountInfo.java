package dev.nightowl.codexconnect.model.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfo {
    private String accountId;
    private String email;
    private String planType;
}
