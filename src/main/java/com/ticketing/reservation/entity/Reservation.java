package com.ticketing.reservation.entity;

import java.time.LocalDateTime;

import com.ticketing.member.entity.Member;
import com.ticketing.reservation.enums.ReservationStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RESERVATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_NO")
    private Long reservationNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESERVATION_STATUS", nullable = false, length = 20)
    private ReservationStatus reservationStatus;

    @Column(name = "RESERVED_AT", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "CANCELED_AT")
    private LocalDateTime canceledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_SEAT_NO", nullable = false)
    private ScheduleSeat scheduleSeat;

    @Builder
    public Reservation(ReservationStatus reservationStatus, LocalDateTime reservedAt,
                       LocalDateTime canceledAt, Member member, ScheduleSeat scheduleSeat) {
        this.reservationStatus = reservationStatus;
        this.reservedAt = reservedAt;
        this.canceledAt = canceledAt;
        this.member = member;
        this.scheduleSeat = scheduleSeat;
    }

    @PrePersist
    public void prePersist() {
        if (this.reservationStatus == null) {
            this.reservationStatus = ReservationStatus.RESERVED;
        }
        if (this.reservedAt == null) {
            this.reservedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        this.reservationStatus = ReservationStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
    
    public void assignMember(Member member) {
    	this.member = member;
    }
    
    public void assignScheduleSeat(ScheduleSeat scheduleSeat) {
    	this.scheduleSeat = scheduleSeat;
    }
}