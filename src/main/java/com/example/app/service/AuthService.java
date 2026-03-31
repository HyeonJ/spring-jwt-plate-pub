package com.example.app.service;

import com.example.app.dto.LoginRequest;
import com.example.app.dto.SignupRequest;
import com.example.app.dto.TokenResponse;
import com.example.app.exception.CustomException;
import com.example.app.exception.ErrorCode;
import com.example.app.mapper.MemberMapper;
import com.example.app.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberMapper memberMapper;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        log.info("[signup] email={}", request.getEmail());

        Map<String, Object> existing = memberMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Map<String, Object> member = new HashMap<>();
        member.put("email", request.getEmail());
        member.put("password", passwordEncoder.encode(request.getPassword()));
        member.put("name", request.getName());
        memberMapper.insert(member);

        log.info("[signup] 회원가입 완료 email={}", request.getEmail());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("[login] email={}", request.getEmail());

        Map<String, Object> member = memberMapper.findByEmail(request.getEmail());
        if (member == null || !passwordEncoder.matches(request.getPassword(), (String) member.get("password"))) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        Long memberId = ((Number) member.get("id")).longValue();
        String email = (String) member.get("email");

        String accessToken = jwtProvider.createAccessToken(memberId, email);
        String refreshToken = jwtProvider.createRefreshToken(memberId, email);

        memberMapper.saveRefreshToken(memberId, refreshToken);

        log.info("[login] 로그인 성공 memberId={}", memberId);
        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getAccessTokenExpiration());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        log.info("[refresh] 토큰 갱신 요청");

        Map<String, Object> tokenInfo = memberMapper.findRefreshToken(refreshToken);
        if (tokenInfo == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        Long memberId = ((Number) tokenInfo.get("member_id")).longValue();
        String email = (String) tokenInfo.get("email");

        String newAccessToken = jwtProvider.createAccessToken(memberId, email);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId, email);

        memberMapper.saveRefreshToken(memberId, newRefreshToken);

        log.info("[refresh] 토큰 갱신 완료 memberId={}", memberId);
        return TokenResponse.of(newAccessToken, newRefreshToken, jwtProvider.getAccessTokenExpiration());
    }

    @Transactional
    public void logout(Long memberId) {
        log.info("[logout] memberId={}", memberId);
        memberMapper.deleteRefreshToken(memberId);
    }
}
