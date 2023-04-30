package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import ru.yandex.practicum.filmorate.enums.ActionType;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.model.*;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(scripts = "classpath:schema.sql", config = @SqlConfig(encoding = "UTF-8"), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:create_test_data.sql", config = @SqlConfig(encoding = "UTF-8"), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventDbStorageTest {

    private final EventDbStorage eventDbStorage;

    @Test
    public void testGetEventById() {
        Optional<Event> eventOptional = eventDbStorage.getById(1);

        assertTrue(eventOptional.isPresent());
        Event event = eventOptional.get();
        assertEquals(1, event.getEventId());
        assertEquals(1, event.getUserId());
        assertEquals(EventType.LIKE, event.getEventType());
        assertEquals(ActionType.ADD, event.getActionType());
        assertEquals(1, event.getEntityId());

    }

    @Test
    public void testGetGenreByIdNotFound() {
        Optional<Event> eventOptional = eventDbStorage.getById(999);

        assertTrue(eventOptional.isEmpty());
    }

    @Test
    public void testGetEventByUserId() {
        Collection<Event> events = eventDbStorage.getByUserId(1);

        assertEquals(2, events.size());
    }


    @Test
    public void testCreateEvent() {
        int userId = 1;
        EventType eventType = EventType.LIKE;
        ActionType actionType = ActionType.ADD;
        int entityId = 1;

        Event createdEvent = eventDbStorage.create(userId, eventType, actionType, entityId);

        assertNotNull(createdEvent);
        assertTrue(createdEvent.getEventId() > 0);
        assertEquals(userId, createdEvent.getUserId());
        assertEquals(eventType, createdEvent.getEventType());
        assertEquals(actionType, createdEvent.getActionType());
        assertEquals(entityId, createdEvent.getEntityId());
    }

}