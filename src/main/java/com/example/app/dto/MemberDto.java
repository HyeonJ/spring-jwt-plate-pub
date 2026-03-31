package com.example.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MemberDto {

    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime createdAt;
}
