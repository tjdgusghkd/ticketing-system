package com.ticketing.member.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.concert.entity.Concert;
import com.ticketing.concert.entity.ConcertSchedule;
import com.ticketing.member.dto.MemberSignupRequest;
import com.ticketing.member.dto.MyReservationResponseDto;
import com.ticketing.member.entity.Member;
import com.ticketing.member.enums.MemberStatus;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.reservation.entity.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.seat.entity.ScheduleSeat;
import com.ticketing.seat.entity.Seat;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationRepository reservationRepository;
    
	public boolean isIdAvailable(String loginId) {
		return !memberRepository.existsByLoginId(loginId);
	}
	
	@Transactional
	public void signup(MemberSignupRequest dto) {
		if(memberRepository.existsByLoginId(dto.getLoginId())) {
			throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
		}
		
		if(memberRepository.existsByEmail(dto.getEmail())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
		
		if(memberRepository.existsByPhone(dto.getPhone())) {
			throw new IllegalArgumentException("이미 존재하는 휴대폰 번호입니다.");
		}
		
		String rawPassword = dto.getPassword();
		
		String encodePassword = passwordEncoder.encode(rawPassword);
		
		Member member = Member.builder()
						.loginId(dto.getLoginId())
						.password(encodePassword)
						.email(dto.getEmail())
						.phone(dto.getPhone())
						.gender(dto.getGender())
						.status(MemberStatus.ACTIVE)
						.build();
		try {
			memberRepository.save(member);
		} catch(DataIntegrityViolationException e){
			throw new IllegalArgumentException("이미 사용중인 회원정보입니다.");
		}
		
	}

	public List<MyReservationResponseDto> getMyReservations(String loginId) {
	    Member member = memberRepository.findByLoginId(loginId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

	    List<Reservation> reservations = reservationRepository.findByMemberMemberNo(member.getMemberNo());

	    Map<Long, List<Reservation>> grouped = reservations.stream()
					.collect(Collectors.groupingBy(r -> r.getSchedule().getScheduleNo()));
	    
	    return grouped.values().stream()
	            .map(this::toMyReservationResponseDto)
	            .sorted(Comparator.comparing(
	            		(MyReservationResponseDto dto) -> dto.getReservationNo()).reversed())
	            .toList();
	}
	
	private MyReservationResponseDto toMyReservationResponseDto(List<Reservation> reservationGroup) {
		Reservation first = reservationGroup.get(0);
		
		Concert concert = first.getScheduleSeat().getSchedule().getConcert();
		ConcertSchedule schedule = first.getScheduleSeat().getSchedule();
		
		List<ScheduleSeat> scheduleSeats = reservationGroup.stream()
											.map(Reservation::getScheduleSeat)
											.toList();
		
		String scheduleDateTime = schedule.getStartTime()
									.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		
		String seatSummary = makeSeatSummary(scheduleSeats);
		
		long totalPrice = scheduleSeats.stream()
							.mapToLong(seat -> seat.getPrice().longValue())
							.sum();
		
		return MyReservationResponseDto.builder()
	            .reservationNo(first.getReservationNo())
	            .concertTitle(concert.getTitle())
	            .artist(concert.getArtist())
	            .posterUrl(concert.getPosterUrl())
	            .scheduleDateTime(scheduleDateTime)
	            .seatSummary(seatSummary)
	            .seatCount(scheduleSeats.size())
	            .totalPrice(totalPrice)
	            .build();
	}
	
	private String makeSeatSummary(List<ScheduleSeat> scheduleSeats) {
		if(scheduleSeats.isEmpty()) {
			return "";
		}
		
		ScheduleSeat firstSeat = scheduleSeats.get(0);
		Seat seat = firstSeat.getSeat();
		
		String firstSeatText = seat.getSection() + "구역 " + seat.getRowNum() + "열 " + seat.getSeatNumber() + "번";
		
		if(scheduleSeats.size() == 1) {
			return firstSeatText;
		} 
		
		return firstSeatText + "외 " + (scheduleSeats.size() - 1) + "석";
		
		
	}
}