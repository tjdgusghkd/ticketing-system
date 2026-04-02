package com.ticketing.seat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketing.seat.entity.ScheduleSeat;
import com.ticketing.seat.entity.Seat;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<Seat, Long>{
	// 특정 회차의 좌석 60개를 가져오는 메서드
    // Fetch Join을 사용하면 연관된 Seat 엔티티 정보(행, 번호, 등급)까지 한방에 가져와서 N+1 문제를 방지합니다.
    @Query("SELECT ss FROM ScheduleSeat ss " +
           "JOIN FETCH ss.seat " + 
           "WHERE ss.schedule.scheduleNo = :scheduleNo " +
           "ORDER BY ss.seat.rowNum ASC, ss.seat.seatNumber ASC")
	List<ScheduleSeat> findByScheduleNo(@Param("scheduleNo")Long scheduleNo);

}
