package com.ticketing.concert.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketing.concert.entity.Concert;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long>{
	
	@Query("SELECT DISTINCT c FROM Concert c LEFT JOIN FETCH c.schedules")
	List<Concert> findAllWithdSchedules();

	@Query("""
            select distinct c
            from Concert c
            left join fetch c.schedules s
            where c.concertNo = :concertNo
            """)
    Optional<Concert> findDetailByConcertNo(@Param("concertNo") Long concertNo);

}
