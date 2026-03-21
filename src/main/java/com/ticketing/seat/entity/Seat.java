package com.ticketing.seat.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SEAT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEAT_NO")
    private Long seatNo;

    @Column(name = "SEAT_NUMBER", nullable = false)
    private Integer seatNumber;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "SECTION", nullable = false, length = 50)
    private String section;

    @Column(name = "ROW_NUMBER", nullable = false, length = 20)
    private String rowNumber;


    @OneToMany(mappedBy = "seat")
    private List<ScheduleSeat> scheduleSeats = new ArrayList<>();

    @Builder
    public Seat(Integer seatNumber, Integer price, String section, String rowNumber) {
        this.seatNumber = seatNumber;
        this.price = price;
        this.section = section;
        this.rowNumber = rowNumber;
    }
    
    public void addScheduleSeat(ScheduleSeat scheduleSeat) {
    	if(scheduleSeat == null) {
    		return;
    	}
    	
    	if(!this.scheduleSeats.contains(scheduleSeat)) {
    		this.getScheduleSeats().add(scheduleSeat);
    	}
    	
    	if(scheduleSeat.getSeat() != this) {
    		scheduleSeat.assignSeat(this);
    	}
    }
    
}