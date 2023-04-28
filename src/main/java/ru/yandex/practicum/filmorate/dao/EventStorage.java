package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.ActionType;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.Collection;
import java.util.Optional;

public interface EventStorage {

    public Optional<Event> getById(int id);

    public Collection<Event> getByUserId(int userId);

    public Event create(int userId, EventType eventType, ActionType actionType, long entityId);
}
