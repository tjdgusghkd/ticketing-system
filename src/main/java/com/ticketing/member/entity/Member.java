package com.ticketing.member.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ticketing.member.enums.Gender;
import com.ticketing.member.enums.MemberStatus;
import com.ticketing.reservation.entity.Reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MEMBER_NO")
	private Long memberNo;
	
	@Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
	private String loginId;
	
	@Column(name="PASSWORD", nullable = false, length = 255)
	private String password;
	
	@Column(name = "EMAIL", nullable = false, unique = true, length = 100)
	private String email;
	
	@Column(name = "PHONE", nullable = false, unique = true, length = 20)
	private String phone;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "GENDER", nullable = false, length = 10)
	private Gender gender;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 10)
	private MemberStatus status;
	
	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;
	
	@OneToMany(mappedBy = "member", fetch=FetchType.LAZY)
	private List<Reservation> reservations = new ArrayList<>();
	
	 @Builder
	    public Member(String loginId, String password, String email, String phone,
	                  Gender gender) {
	        this.loginId = loginId;
	        this.password = password;
	        this.email = email;
	        this.phone = phone;
	        this.gender = gender;
	    }
	
	@PrePersist
	public void prePersist() {
		if(this.status == null) {
			this.status = MemberStatus.ACTIVE;
		}
		if(this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
	
	public void addReservation(Reservation reservation) {
		if(reservation == null) {
			return;
		}
		
		if(!this.reservations.contains(reservation)) {
			this.reservations.add(reservation);
		}
		
		reservation.assignMember(this);
	}
}
