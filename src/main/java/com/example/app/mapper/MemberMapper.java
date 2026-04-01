package com.example.app.mapper;

import com.example.app.dto.MemberDto;
import com.example.app.dto.RefreshTokenDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    MemberDto findById(@Param("id") Long id);

    MemberDto findByEmail(@Param("email") String email);

    void insert(MemberDto member);

    void saveRefreshToken(@Param("memberId") Long memberId, @Param("refreshToken") String refreshToken, @Param("expiresAt") java.time.LocalDateTime expiresAt);

    void deleteRefreshToken(@Param("memberId") Long memberId);

    RefreshTokenDto findRefreshToken(@Param("refreshToken") String refreshToken);
}
