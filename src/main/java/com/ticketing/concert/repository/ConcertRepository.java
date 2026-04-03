package com.ticketing.concert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ticketing.concert.entity.Concert;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long>{
	
	@Query("SELECT DISTINCT c FROM Concert c LEFT JOIN FETCH c.schedules")
	List<Concert> findAllWithdSchedules();

}
