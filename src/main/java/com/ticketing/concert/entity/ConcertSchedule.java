package com.ticketing.concert.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ticketing.concert.enums.ScheduleStatus;
import com.ticketing.seat.entity.ScheduleSeat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CONCERT_SCHEDULE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_NO")
    private Long scheduleNo;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ScheduleStatus status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCERT_NO", nullable = false)
    private Concert concert;

    @OneToMany(mappedBy = "schedule")
    private List<ScheduleSeat> scheduleSeats = new ArrayList<>();

    @Builder
    public ConcertSchedule(LocalDateTime startTime, LocalDateTime endTime,
                            Concert concert) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.concert = concert;
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ScheduleStatus.OPEN;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    public void assignConcert(Concert concert) {
    	this.concert = concert;
    }
}