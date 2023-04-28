package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Event {
    private int eventId;
    private int userId;
    private EventType eventType;
    @JsonProperty("operation")
    private ActionType actionType;
    private long entityId;
    @JsonProperty("timestamp")
    private long eventDateTime;
}
