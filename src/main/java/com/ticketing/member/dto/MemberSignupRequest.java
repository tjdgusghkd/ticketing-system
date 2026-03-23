package com.ticketing.member.dto;

import com.ticketing.member.enums.Gender;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignupRequest {
	private String loginId;
    private String password;
    private String email;
    private String phone;
    private Gender gender;
}
