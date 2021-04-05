package com.test.log.service;

import com.test.log.domain.LogEvent;
import com.test.log.repository.LogEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that loads and processes a test log file. Uses in memory hsql database.
 * Automatically loads the file defined in the test application.properties when the spring boot application starts.
 */

@SpringBootTest
class LogServiceTest {

    @Autowired
    private LogEventRepository logEventRepository;

    @Test
    void durationGreaterThanThreshold_AlertExpected() {
        // Duration > threshold
        Optional<LogEvent>  logEvent =  logEventRepository.findByEventId("scsmbstgra");
        assertTrue(logEvent.isPresent(), "Log event not found for id scsmbstgra");
        assertEquals(true, logEvent.get().isAlert(), "Alert should be true");
        assertEquals("APPLICATION_LOG", logEvent.get().getType(), "Type should be APPLICATION_LOG");
        assertEquals("12345", logEvent.get().getHost(), "Host should be 12345");
    }

    @Test
    void durationLessThanThreshold_NoAlertExpected() {
        // Duration < threshold
        Optional<LogEvent> logEvent =  logEventRepository.findByEventId("scsmbstgrb");
        assertTrue(logEvent.isPresent(), "Log event not found for id scsmbstgrb");
        assertEquals(false, logEvent.get().isAlert(), "Alert should be false");
        assertNull(logEvent.get().getType(), "Type should be null");
        assertNull(logEvent.get().getHost(), "Host should be null");
    }

    /**
     * In this test the finished log event comes in before the start one.
     */
    @Test
    void durationEqualToThreshold_NoAlertExpected_OrderReversed() {
        // Duration > threshold
        Optional<LogEvent> logEvent  =  logEventRepository.findByEventId("scsmbstgrc");
        assertTrue(logEvent.isPresent(), "Log event not found for id scsmbstgrc");
        assertEquals(false, logEvent.get().isAlert(), "Alert should be false");
        assertNull(logEvent.get().getType(), "Type should be null");
        assertNull(logEvent.get().getHost(), "Host should be null");
    }

    /**
     * In this test the finished log event comes in before the start one but with an alert expected
     */
    @Test
    void durationGreaterThanThreshold_AlertExpected_OrderReversed() {
        // Duration == threshold
        Optional<LogEvent> logEvent  =  logEventRepository.findByEventId("scsmbstgre");
        assertTrue(logEvent.isPresent(), "Log event not found for id scsmbstgre");
        assertEquals(true, logEvent.get().isAlert(), "Alert should be true");
        assertNull(logEvent.get().getType(), "Type should be null");
        assertNull(logEvent.get().getHost(), "Host should be null");
    }

    /**
     * This test fails validation due to a null type field.
     */
    @Test
    void validationFailureNoRecordExpected() {
        Optional<LogEvent> logEvent =  logEventRepository.findByEventId("scsmbstgrd");
        assertTrue(logEvent.isEmpty(), "Log event should not found for id scsmbstgrd");
    }
}