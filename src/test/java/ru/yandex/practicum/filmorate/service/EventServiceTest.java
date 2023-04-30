package ru.yandex.practicum.filmorate.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.dao.EventStorage;
import ru.yandex.practicum.filmorate.enums.ActionType;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EventServiceTest {

    private final EventStorage eventStorage = Mockito.mock(EventStorage.class);

    private final EventService eventService = new EventService(eventStorage);

    @Test
    public void testFindByUserId() {
        int userId = 1;
        Collection<Event> events = Arrays.asList(
                Event.builder().eventId(1).userId(userId).build(),
                Event.builder().eventId(2).userId(userId).build()
        );
        when(eventStorage.getByUserId(userId)).thenReturn(events);

        Collection<Event> result = eventService.findByUserId(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(Event.builder().eventId(1).userId(userId).build()));
        assertTrue(result.contains(Event.builder().eventId(2).userId(userId).build()));
    }

    @Test
    public void testCreateEvent() {
        int userId = 1;
        EventType eventType = EventType.LIKE;
        ActionType actionType = ActionType.ADD;
        int entityId = 1;
        Event event = Event.builder()
                .eventId(1)
                .userId(userId)
                .eventType(eventType)
                .actionType(actionType)
                .entityId(entityId)
                .build();

        when(eventStorage.create(userId, eventType, actionType, entityId)).thenReturn(event);

        Event result = eventService.createEvent(userId, actionType, eventType, entityId);

        assertEquals(event, result);
        verify(eventStorage, times(1)).create(userId, eventType, actionType, entityId);
    }
}