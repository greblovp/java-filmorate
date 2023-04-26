package ru.yandex.practicum.filmorate.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventStorage;
import ru.yandex.practicum.filmorate.model.ActionType;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    @Qualifier("eventDbStorage")
    @NonNull
    private final EventStorage eventStorage;

    public Collection<Event> findByUserId(int userId) {
        return eventStorage.getByUserId(userId);
    }

    public Event createEvent(int userId, EventType eventType, ActionType actionType, int entityId) {
        return eventStorage.create(userId, eventType, actionType, entityId);
    }
}
