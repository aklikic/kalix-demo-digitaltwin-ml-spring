package com.example.digitaltwin.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class DigitalTwinState {
    private final String name;
    private final boolean maintenanceRequired;
    private final Instant lastUpdated;

    @JsonCreator
    public DigitalTwinState(@JsonProperty String name, @JsonProperty boolean maintenanceRequired, @JsonProperty Instant lastUpdated) {
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

    public static DigitalTwinState empty(){
        return new DigitalTwinState(null,false,null);
    }
    public boolean isEmpty(){
        return this.getLastUpdated()==null;
    }
    public DigitalTwinState onCreated(DigitalTwinEvent.Created event){
        return new DigitalTwinState(event.getName(), false, event.getTimestamp());
    }
    public DigitalTwinState onMaintenanceRequired(DigitalTwinEvent.MaintenanceRequired event){
        return new DigitalTwinState(this.getName(), true, event.getTimestamp());
    }
    public DigitalTwinState onMaintenancePerformed(DigitalTwinEvent.MaintenancePerformed event){
        return new DigitalTwinState(this.getName(), false, event.getTimestamp());
    }



}
