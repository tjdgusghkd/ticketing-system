package com.ticketing.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketing.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByLoginId(String loginId);

	Optional<Member> findByLoginId(String loginId);

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);
}
