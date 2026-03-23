package com.ticketing.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketing.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	// 로그인용
    Optional<Member> findByLoginId(String loginId);

    // 중복 체크용
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);
}
