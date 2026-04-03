package com.ticketing.member.dto;

import com.ticketing.member.enums.Gender;
import com.ticketing.member.enums.MemberStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberSignupRequest {
	private String loginId;
    private String password;
    private String email;
    private String phone;
    private Gender gender;
    private MemberStatus status;
}
