package com.test.log.repository;

import com.test.log.domain.LogEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogEventRepository extends CrudRepository<LogEvent, Integer> {

    Optional<LogEvent> findByEventId(String eventId);
}