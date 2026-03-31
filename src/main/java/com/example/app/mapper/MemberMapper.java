package com.example.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberMapper {

    Map<String, Object> findById(@Param("id") Long id);

    Map<String, Object> findByEmail(@Param("email") String email);

    void insert(Map<String, Object> member);

    void saveRefreshToken(@Param("memberId") Long memberId, @Param("refreshToken") String refreshToken);

    Map<String, Object> findRefreshToken(@Param("refreshToken") String refreshToken);

    void deleteRefreshToken(@Param("memberId") Long memberId);
}
