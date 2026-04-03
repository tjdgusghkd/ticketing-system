package com.ticketing.concert.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketing.concert.entity.ConcertSchedule;

@Repository
public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long>{
	@Query("""
			select s
			from ConcertSchedule s
			join fetch s.concert
			where s.scheduleNo = :scheduleNo
			"""
			)
	Optional<ConcertSchedule> findDetailByScheduleNo(@Param("scheduleNo") Long scheduleNo);

}
