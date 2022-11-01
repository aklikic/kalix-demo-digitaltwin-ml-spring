package com.example.digitaltwin.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = DigitalTwinEvent.Created.class, name = "created"),
                @JsonSubTypes.Type(value = DigitalTwinEvent.MaintenanceRequired.class, name = "maintenance-required"),
                @JsonSubTypes.Type(value = DigitalTwinEvent.MaintenancePerformed.class, name = "maintenance-performed"),
        })
public interface DigitalTwinEvent {
    class Created implements DigitalTwinEvent{
        private final String dtId;
        private final String name;
        private final Instant timestamp;

        @JsonCreator
        public Created(@JsonProperty String dtId, @JsonProperty String name, @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.name = name;
            this.timestamp = timestamp;
        }

        public String getDtId() {
            return dtId;
        }

        public String getName() {
            return name;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    class MaintenanceRequired implements DigitalTwinEvent{
        private final String dtId;
        private final Instant timestamp;

        @JsonCreator
        public MaintenanceRequired(@JsonProperty String dtId, @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.timestamp = timestamp;
        }

        public String getDtId() {
            return dtId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    class MaintenancePerformed implements DigitalTwinEvent{
        private final String dtId;
        private final Instant timestamp;

        @JsonCreator
        public MaintenancePerformed(@JsonProperty String dtId, @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.timestamp = timestamp;
        }

        public String getDtId() {
            return dtId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
