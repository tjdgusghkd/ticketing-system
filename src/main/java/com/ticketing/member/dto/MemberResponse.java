package com.ticketing.member.dto;

import com.ticketing.member.entity.Member;

import lombok.Getter;

@Getter
public class MemberResponse {
	private Long memberNo;
	private String loginId;
	private String email;
	private String phone;
	
	public MemberResponse(Member member) {
		this.memberNo = member.getMemberNo();
		this.loginId = member.getLoginId();
		this.email = member.getEmail();
		this.phone = member.getPhone();
	}
}
