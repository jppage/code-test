package com.test.log.domain;

import lombok.*;

import javax.persistence.*;

@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log_event")
public class LogEvent {

    private Long id;
    private String eventId;
    private Long eventDuration;
    private String type;
    private String host;
    private boolean isAlert;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    public Long getId() {
        return id;
    }

    @Column(name = "event_id", nullable = false)
    public String getEventId() {
        return eventId;
    }

    @Column(name = "event_duration", nullable = false)
    public Long getEventDuration() {
        return eventDuration;
    }

    @Column(name = "type", nullable = true)
    public String getType() {
        return type;
    }

    @Column(name = "host", nullable = true)
    public String getHost() {
        return host;
    }

    @Column(name = "alert", nullable = false)
    public boolean isAlert() {
        return isAlert;
    }
}
