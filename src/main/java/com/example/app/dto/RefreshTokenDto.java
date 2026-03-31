package com.example.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenDto {

    private Long id;
    private Long memberId;
    private String token;
    private String email;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
