package com.example.digitaltwin.api;

import lombok.Value;

import java.time.Instant;

public interface DigitalTwinApi {

    @Value
    class CreateRequest implements DigitalTwinApi{
        String name;
    }
    @Value
    class MetricRequest implements DigitalTwinApi{
        Double raw1;
        Double raw2;
    }
    class SetMaintenancePerformedRequest implements DigitalTwinApi{}
    class EmptyResponse implements DigitalTwinApi{
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    class GetRequest implements DigitalTwinApi{}

    @Value
    class GetResponse implements DigitalTwinApi{
        String name;
        boolean maintenanceRequired;
        Instant lastUpdated;
    }
}
