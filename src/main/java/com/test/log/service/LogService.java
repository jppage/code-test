package com.test.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.log.domain.LogEvent;
import com.test.log.event.LogEventTO;
import com.test.log.exception.LogServiceException;
import com.test.log.repository.LogEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.*;

/**
 * This is the main service class that loads, processes and persists log event data. Whenever a pair of log events
 * are detected (started and finished) then the duration between them is calculated and if it exceeds the threshold
 * an alert flag is set. The event is then persisted and the log map entry for them is removed. Combining this
 * with Scanner to read the logs line by line means that this process will cope with large log files.
 *
 * The service uses @EventListener which causes the file load to automatically start once the application has fully
 * started.
 *
 * Any validation failures on the data being read are logged but don't prevent the rest of the file from loading.
 *
 * @author  Joe Page
 * @since   2021-04-04
 */

@Slf4j
@Service
public class LogService {

    private final LogEventRepository logEventRepository;
    private final String filePath;
    private final String fileName;
    private final Map<String, List<LogEventTO>> logEventMap = new HashMap<>();
    private final Integer durationMs;
    private Validator validator;

    public LogService(LogEventRepository logEventRepository, @Value("${file.path}") String filePath,
                      @Value("${file.name}") String fileName, @Value("${duration.ms}") Integer durationMs) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.logEventRepository = logEventRepository;
        this.durationMs = durationMs;
    }

    /**
     * Fires once when the application starts up and is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void processLogFile() throws IOException {
        log.info("Running log file processor.");
        String separator = FileSystems.getDefault().getSeparator();
        String fileToProcess = filePath + separator + fileName;
        try (FileInputStream inputStream = new FileInputStream(filePath + separator + fileName)) {
            log.info("Reading file: " + fileToProcess);
            Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8.name());
            while (sc.hasNextLine()) {
                processLogFileEntry(sc.nextLine());
            }
        }
    }

    private void processLogFileEntry(String logFileEntry) {
        log.debug("Processing log entry " + logFileEntry);
        ObjectMapper objectMapper = new ObjectMapper();
        LogEventTO logEventTO = null;
        try {
            logEventTO = objectMapper.readValue(logFileEntry, LogEventTO.class);
        } catch (JsonProcessingException e) {
            String message = "Unable to parse JSON for line: " + logFileEntry;
            throw new LogServiceException(message);
        }

        if (!validateLogEvent(logEventTO)) {
            return;
        }

        if (logEventMap.containsKey(logEventTO.getId())) {
            List<LogEventTO> logEvents = logEventMap.get(logEventTO.getId());
            logEvents.add(logEventTO);

            //If list size is 2 then we have both the STARTED and FINISHED events so we can persist and remove from the Map
            if (logEvents.size() == 2) {
                LogEvent logEventToPersist = createAndPersistLogEvent(logEvents);
                log.debug("Persistable entity created: " + logEventToPersist);
                logEventRepository.save(logEventToPersist);
                logEventMap.remove(logEventTO.getId());
            }
        } else {
            List<LogEventTO> logEventList = new ArrayList<>();
            logEventList.add(logEventTO);
            logEventMap.put(logEventTO.getId(), logEventList);
        }
    }

    private LogEvent createAndPersistLogEvent(List<LogEventTO> logEvents) {
        LogEventTO logEvent1 = logEvents.get(0);
        LogEventTO logEvent2 = logEvents.get(1);

        // Order of started and finished not guaranteed so get the absolute difference
        Long duration = Math.abs(logEvent1.getTimestamp() - logEvent2.getTimestamp());
        boolean isAlert = duration > durationMs;

        //Assumes type and host are the same for both events and may be null on one or both of them
        String type = logEvent1.getType() != null ? logEvent1.getType() : logEvent2.getType();
        String host = logEvent1.getHost() != null ? logEvent1.getHost() : logEvent2.getHost();
        return LogEvent.builder().eventId(logEvent1.getId()).host(host).type(type).eventDuration(duration).isAlert(isAlert).build();
    }

    // We don't want to stop the file loading in the event of a validation failure so we will just log the error instead.
    private boolean validateLogEvent(LogEventTO logEventTO) {
        if (logEventTO == null) {
            return false;
        }
        Set<ConstraintViolation<LogEventTO>> violations = getValidator().validate(logEventTO);
        violations.forEach(violation -> log.error("Validation error: " + violation.getMessage()+ ": " + logEventTO));
        return violations.size() == 0;
    }

    private Validator getValidator() {
        if (validator == null) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        }
        return validator;
    }

}
