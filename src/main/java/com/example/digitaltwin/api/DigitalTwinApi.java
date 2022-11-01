package com.example.digitaltwin.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public interface DigitalTwinApi {

    class CreateRequest implements DigitalTwinApi{
        public final String name;

        @JsonCreator
        public CreateRequest(@JsonProperty String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    class MetricRequest implements DigitalTwinApi{
        private final Double raw1;
        private final Double raw2;

        @JsonCreator
        public MetricRequest(@JsonProperty("raw1") Double raw1, @JsonProperty("raw2") Double raw2) {
            this.raw1 = raw1;
            this.raw2 = raw2;
        }

        public Double getRaw1() {
            return raw1;
        }

        public Double getRaw2() {
            return raw2;
        }
    }
    class EmptyResponse implements DigitalTwinApi{
        public final String note;
        @JsonCreator
        public EmptyResponse(@JsonProperty String note) {
            this.note = note;
        }
        @JsonCreator
        public EmptyResponse() {
            this.note = "OK";
        }

        public String getNote() {
            return note;
        }

        public static EmptyResponse of(){
            return new EmptyResponse("OK");
        }
    }


    class GetResponse implements DigitalTwinApi{
        public final String name;
        public final boolean maintenanceRequired;
        public final Instant lastUpdated;

        @JsonCreator
        public GetResponse(@JsonProperty("name") String name, @JsonProperty("maintenanceRequired") boolean maintenanceRequired, @JsonProperty("lastUpdated") Instant lastUpdated) {
            this.name = name;
            this.maintenanceRequired = maintenanceRequired;
            this.lastUpdated = lastUpdated;
        }
        public GetResponse(){
            this.name = null;
            this.maintenanceRequired = false;
            this.lastUpdated = null;
        }


        public String getName() {
            return name;
        }

        public boolean isMaintenanceRequired() {
            return maintenanceRequired;
        }

        public Instant getLastUpdated() {
            return lastUpdated;
        }
    }
}
