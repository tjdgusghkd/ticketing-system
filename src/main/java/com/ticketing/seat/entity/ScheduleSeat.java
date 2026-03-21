package com.ticketing.seat.entity;


import java.util.ArrayList;
import java.util.List;

import com.ticketing.concert.entity.ConcertSchedule;
import com.ticketing.reservation.entity.Reservation;
import com.ticketing.seat.enums.ScheduleSeatStatus;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "SCHEDULE_SEAT",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_SCHEDULE_SEAT_SCHEDULE_NO_SEAT_NO",
            columnNames = {"SCHEDULE_NO", "SEAT_NO"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_SEAT_NO")
    private Long scheduleSeatNo;

    @Column(name = "PRICE")
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ScheduleSeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEAT_NO", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_NO", nullable = false)
    private ConcertSchedule schedule;

    @OneToMany(mappedBy = "scheduleSeat")
    private List<Reservation> reservations = new ArrayList<>();

    @Builder
    public ScheduleSeat(Integer price, ScheduleSeatStatus status, Seat seat, ConcertSchedule schedule) {
        this.price = price;
        this.status = status;
        this.seat = seat;
        this.schedule = schedule;
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ScheduleSeatStatus.AVAILABLE;
        }
    }

    public void booked() {
        this.status = ScheduleSeatStatus.BOOKED;
    }

    public void cancel() {
        this.status = ScheduleSeatStatus.AVAILABLE;
    }
    
    public void addReservation(Reservation reservation) {
    	if(reservation == null) {
    		return;
    	}
    	
    	if(!this.reservations.contains(reservation)) {
    		this.reservations.add(reservation);
    	}
    	
    	if(reservation.getScheduleSeat() != this) {
    		reservation.assignScheduleSeat(this);
    	}
    }
    
   public void assignSeat(Seat seat) {
    	if(seat == null) {
    		return;
    	}
    	
    	this.seat = seat;
    }
    
    public void assignSchedule(ConcertSchedule schedule) {
    	if(schedule == null) {
    		return;
    	}
    	
    	
    	this.schedule = schedule;
    }
}