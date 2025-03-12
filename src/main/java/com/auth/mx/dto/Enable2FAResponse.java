package com.auth.mx.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Enable2FAResponse {
    private String totpUrl;
    private String secret;
}
