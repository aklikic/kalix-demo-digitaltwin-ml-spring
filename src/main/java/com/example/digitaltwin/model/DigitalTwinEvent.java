package com.example.digitaltwin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

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

    class MetricRaw1Received extends MetricRawReceived{
        @JsonCreator
        public MetricRaw1Received(@JsonProperty String dtId, @JsonProperty Double raw, @JsonProperty Instant timestamp) {
            super(dtId, raw, timestamp);
        }
    }

    class MetricRaw2Received extends MetricRawReceived{
        @JsonCreator
        public MetricRaw2Received(@JsonProperty String dtId, @JsonProperty Double raw, @JsonProperty Instant timestamp) {
            super(dtId, raw, timestamp);
        }
    }

    class MetricRawReceived implements DigitalTwinEvent{
        private final String dtId;
        private final Double raw;
        private final Instant timestamp;

        public MetricRawReceived(String dtId,Double raw,Instant timestamp) {
            this.dtId = dtId;
            this.raw = raw;
            this.timestamp = timestamp;
        }

        public String getDtId() {
            return dtId;
        }

        public Double getRaw() {
            return raw;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    class MaintenanceNotRequired implements DigitalTwinEvent{
        private final String dtId;
        private final Instant timestamp;

        @JsonCreator
        public MaintenanceNotRequired(@JsonProperty String dtId, @JsonProperty Instant timestamp) {
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
    class MaintenanceRequired implements DigitalTwinEvent{
        private final String dtId;
        private final Double raw1;
        private final Double raw2;
        private final Instant timestamp;

        @JsonCreator
        public MaintenanceRequired(@JsonProperty String dtId, @JsonProperty Double raw1, @JsonProperty Double raw2, @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.raw1 = raw1;
            this.raw2 = raw2;
            this.timestamp = timestamp;
        }

        public String getDtId() {
            return dtId;
        }

        public Double getRaw1() {
            return raw1;
        }

        public Double getRaw2() {
            return raw2;
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
