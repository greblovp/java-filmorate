package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.ActionType;
import ru.yandex.practicum.filmorate.enums.EventType;

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
