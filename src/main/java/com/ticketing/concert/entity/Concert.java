package com.ticketing.concert.entity;


import java.util.ArrayList;
import java.util.List;

import com.ticketing.concert.enums.ConcertStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CONCERT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONCERT_NO")
    private Long concertNo;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "ARTIST", nullable = false, length = 100)
    private String artist;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "POSTER_URL", nullable = false, length = 500)
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ConcertStatus status;

    @OneToMany(mappedBy = "concert")
    private List<ConcertSchedule> schedules = new ArrayList<>();

    @Builder
    public Concert(String title, String artist, String description, String posterUrl, ConcertStatus status) {
        this.title = title;
        this.artist = artist;
        this.description = description;
        this.posterUrl = posterUrl;
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ConcertStatus.OPEN;
        }
    }
    
    public void addSchedule(ConcertSchedule schedule) {
    	if(schedule == null) {
    		return;
    	}
    	
    	if(!this.schedules.contains(schedule)) {
    		this.schedules.add(schedule);
    	}
    	
    	if(schedule.getConcert() != this) {
    		schedule.assignConcert(this);
    	}
    }
}