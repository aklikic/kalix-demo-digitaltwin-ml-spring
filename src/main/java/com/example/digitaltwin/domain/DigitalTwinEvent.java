package com.example.digitaltwin.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Value;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = DigitalTwinEvent.Created.class, name = "created"),
                @JsonSubTypes.Type(value = DigitalTwinEvent.MaintenanceRequired.class, name = "maintenance-required"),
                @JsonSubTypes.Type(value = DigitalTwinEvent.MaintenancePerformed.class, name = "maintenance-performed"),
        })
public interface DigitalTwinEvent {
    @Value
    class Created implements DigitalTwinEvent{
        String dtId;
        String name;
        Instant timestamp;
    }
    @Value
    class MaintenanceRequired implements DigitalTwinEvent{
        String dtId;
        Instant timestamp;
    }
    @Value
    class MaintenancePerformed implements DigitalTwinEvent{
        String dtId;
        Instant timestamp;
    }
}
