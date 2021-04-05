package com.test.log.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class LogEventTO implements Serializable {

    @NotNull(message = "id cannot be null")
    private String id;

    @NotNull(message = "state cannot be null")
    private String state;

    @NotNull(message = "timestamp cannot be null")
    private Long timestamp;

    private String type;

    private String host;

}
