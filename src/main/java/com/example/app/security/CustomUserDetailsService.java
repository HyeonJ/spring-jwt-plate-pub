package com.example.app.security;

import com.example.app.dto.MemberDto;
import com.example.app.exception.CustomException;
import com.example.app.exception.ErrorCode;
import com.example.app.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("[loadUserByUsername] email={}", email);
        MemberDto member = memberMapper.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("회원을 찾을 수 없습니다: " + email);
        }
        return new CustomUserDetails(member.getId(), member.getEmail(), member.getPassword());
    }

    public UserDetails loadUserById(Long id) {
        log.debug("[loadUserById] id={}", id);
        MemberDto member = memberMapper.findById(id);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return new CustomUserDetails(member.getId(), member.getEmail(), member.getPassword());
    }
}
