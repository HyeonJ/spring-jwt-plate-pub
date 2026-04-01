package com.example.app.service;

import com.example.app.dto.LoginRequest;
import com.example.app.dto.MemberDto;
import com.example.app.dto.RefreshTokenDto;
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

import java.time.LocalDateTime;

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

        MemberDto existing = memberMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        MemberDto member = new MemberDto();
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setName(request.getName());
        memberMapper.insert(member);

        log.info("[signup] 회원가입 완료 email={}", request.getEmail());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("[login] email={}", request.getEmail());

        MemberDto member = memberMapper.findByEmail(request.getEmail());
        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getEmail());

        memberMapper.deleteRefreshToken(member.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpiration() / 1000);
        memberMapper.saveRefreshToken(member.getId(), refreshToken, expiresAt);

        log.info("[login] 로그인 성공 memberId={}", member.getId());
        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getAccessTokenExpiration());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        log.info("[refresh] 토큰 갱신 요청");

        RefreshTokenDto tokenInfo = memberMapper.findRefreshToken(refreshToken);
        if (tokenInfo == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        String newAccessToken = jwtProvider.createAccessToken(tokenInfo.getMemberId(), tokenInfo.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(tokenInfo.getMemberId(), tokenInfo.getEmail());

        memberMapper.deleteRefreshToken(tokenInfo.getMemberId());
        LocalDateTime refreshExpiresAt = LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpiration() / 1000);
        memberMapper.saveRefreshToken(tokenInfo.getMemberId(), newRefreshToken, refreshExpiresAt);

        log.info("[refresh] 토큰 갱신 완료 memberId={}", tokenInfo.getMemberId());
        return TokenResponse.of(newAccessToken, newRefreshToken, jwtProvider.getAccessTokenExpiration());
    }

    @Transactional
    public void logout(Long memberId) {
        log.info("[logout] memberId={}", memberId);
        memberMapper.deleteRefreshToken(memberId);
    }
}
