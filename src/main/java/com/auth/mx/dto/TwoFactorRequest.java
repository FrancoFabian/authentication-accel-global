package com.auth.mx.dto;
import lombok.Data;

@Data
public class TwoFactorRequest {
    private String email;
    private String code;
}
