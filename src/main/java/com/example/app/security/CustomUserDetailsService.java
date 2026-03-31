package com.example.app.security;

import com.example.app.exception.CustomException;
import com.example.app.exception.ErrorCode;
import com.example.app.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("[loadUserByUsername] email={}", email);
        Map<String, Object> member = memberMapper.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("회원을 찾을 수 없습니다: " + email);
        }
        return toUserDetails(member);
    }

    public UserDetails loadUserById(Long id) {
        log.debug("[loadUserById] id={}", id);
        Map<String, Object> member = memberMapper.findById(id);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return toUserDetails(member);
    }

    private CustomUserDetails toUserDetails(Map<String, Object> member) {
        return new CustomUserDetails(
                ((Number) member.get("id")).longValue(),
                (String) member.get("email"),
                (String) member.get("password")
        );
    }
}
