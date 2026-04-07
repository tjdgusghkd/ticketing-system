package com.ticketing.seat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketing.seat.entity.ScheduleSeat;
import com.ticketing.seat.enums.ScheduleSeatStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long>{
	// 특정 회차의 좌석 60개를 가져오는 메서드
    // Fetch Join을 사용하면 연관된 Seat 엔티티 정보(행, 번호, 등급)까지 한방에 가져와서 N+1 문제를 방지합니다.
    @Query("SELECT ss FROM ScheduleSeat ss " +
           "JOIN FETCH ss.seat " + 
           "WHERE ss.schedule.scheduleNo = :scheduleNo " +
           "ORDER BY ss.seat.rowNum ASC, ss.seat.seatNumber ASC")
	List<ScheduleSeat> findByScheduleNo(@Param("scheduleNo")Long scheduleNo);
    
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ss
        from ScheduleSeat ss
        join fetch ss.seat
        where ss.schedule.scheduleNo = :scheduleNo
        and ss.scheduleSeatNo = :scheduleSeatNo
    """)
    Optional<ScheduleSeat> findByScheduleNoAndScheduleSeatNoForUpdate(
        @Param("scheduleNo") Long scheduleNo,
        @Param("scheduleSeatNo") Long scheduleSeatNo
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ScheduleSeat s SET s.status = 'BOOKED' " +
    		"WHERE s.scheduleSeatNo IN :seatIds AND s.schedule.scheduleNo = :scheduleNo")
	int updateStatusToBooked(@Param("scheduleNo") Long scheduleNo, @Param("seatIds") List<Long> seatIds);
    
 // ReservationRepository.java
    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.member.loginId = :loginId " +
           "AND r.schedule.scheduleNo = :scheduleNo " +
           "AND r.reservationStatus = 'RESERVED'")
    int countByMemberAndSchedule(@Param("loginId") String loginId, @Param("scheduleNo") Long scheduleNo);
    
}
