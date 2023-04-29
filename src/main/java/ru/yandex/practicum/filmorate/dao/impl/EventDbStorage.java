package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventStorage;
import ru.yandex.practicum.filmorate.enums.ActionType;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.enums.EventType;

import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("eventDbStorage")
@Slf4j
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Event> getById(int id) {
        String sqlQuery =
                "SELECT * " +
                "FROM event " +
                "WHERE event_id = ?";
        List<Event> events = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeEvent(rs), id);

        // обрабатываем результат выполнения запроса
        if (events.isEmpty()) {
            log.info("Событие с идентификатором {} не найдено.", id);
            return Optional.empty();
        }
        log.info("Найдено событие: {}", events.get(0));
        return Optional.of(events.get(0));
    }

    @Override
    public Collection<Event> getByUserId(int userId) {
        String sqlQuery =
                "SELECT * " +
                "FROM event " +
                "WHERE user_id = ?";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeEvent(rs), userId);
    }

    @Override
    public Event create(int userId, EventType eventType, ActionType actionType, long entityId) {
        String userSqlQuery =
                "INSERT INTO event (user_id, event_type, action_type, entity_id, event_dttm) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(userSqlQuery, new String[]{"event_id"});
            stmt.setInt(1, userId);
            stmt.setString(2, String.valueOf(eventType));
            stmt.setString(3, String.valueOf(actionType));
            stmt.setLong(4, entityId);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return stmt;
        }, keyHolder);

        if (updatedRowsCount == 0) {
            log.info("Произошла ошибка при добавлении события для пользователя {} в базу данных", userId);
            return null;
        }

        int eventId = (int) keyHolder.getKey().longValue();

        Event createdEvent = getById(eventId).orElse(null);

        log.info("Событие {} добавлен в базу данных. Присвоен идентификатор {}", createdEvent, eventId);
        return createdEvent;
    }

    private Event makeEvent(ResultSet rs) throws SQLException {
        Event event = Event.builder()
                .eventId(rs.getInt("event_id"))
                .userId(rs.getInt("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .actionType(ActionType.valueOf(rs.getString("action_type")))
                .entityId(rs.getLong("entity_id"))
                .eventDateTime(rs.getTimestamp("event_dttm").toInstant().toEpochMilli())
                .build();
        return event;
    }
}
