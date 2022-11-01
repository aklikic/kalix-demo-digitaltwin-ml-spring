package com.example.digitaltwin.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public interface DigitalTwinApi {

//    class CreateRequest implements DigitalTwinApi{
//        public final String name;
//
//        @JsonCreator
//        public CreateRequest(@JsonProperty String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//    }
    class MetricRequest implements DigitalTwinApi{
        private final Double raw1;
        private final Double raw2;

        @JsonCreator
        public MetricRequest(@JsonProperty Double raw1, @JsonProperty Double raw2) {
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
    class SetMaintenancePerformedRequest implements DigitalTwinApi{}
    class EmptyResponse implements DigitalTwinApi{
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    class GetRequest implements DigitalTwinApi{}

    class GetResponse implements DigitalTwinApi{
        private final String name;
        private final boolean maintenanceRequired;
        private final Instant lastUpdated;

        @JsonCreator
        public GetResponse(@JsonProperty String name, @JsonProperty boolean maintenanceRequired, @JsonProperty Instant lastUpdated) {
            this.name = name;
            this.maintenanceRequired = maintenanceRequired;
            this.lastUpdated = lastUpdated;
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
