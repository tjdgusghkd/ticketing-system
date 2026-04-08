package com.ticketing.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketing.reservation.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

	List<Reservation> findByMemberMemberNo(Long memberNo);

	List<Reservation> findByMember_LoginIdAndSchedule_ScheduleNo(String loginId, Long scheduleNo);

}
