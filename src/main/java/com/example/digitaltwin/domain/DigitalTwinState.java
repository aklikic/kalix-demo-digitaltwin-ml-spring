package com.example.digitaltwin.domain;

import lombok.Value;

import java.time.Instant;

@Value
public class DigitalTwinState {
    String name;
    boolean maintenanceRequired;
    Instant lastUpdated;

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
