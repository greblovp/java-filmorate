package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Event {
    private int eventId;
    private int userId;
    private EventType eventType;
    private ActionType actionType;
    private int entityId;
    private LocalDateTime eventDateTime;
}
