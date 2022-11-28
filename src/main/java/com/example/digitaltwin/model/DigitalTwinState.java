package com.example.digitaltwin.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class DigitalTwinState {
    private final String name;
    private final Double raw1;
    private final Double raw2;
    private final boolean maintenanceRequired;
    private final Instant lastUpdated;

    @JsonCreator
    public DigitalTwinState(@JsonProperty String name, @JsonProperty Double raw1, @JsonProperty Double raw2 , @JsonProperty boolean maintenanceRequired, @JsonProperty Instant lastUpdated) {
        this.name = name;
        this.raw1 = raw1;
        this.raw2 = raw2;
        this.maintenanceRequired = maintenanceRequired;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public Double getRaw1() {
        return raw1;
    }
    public Double getRaw2() {
        return raw2;
    }

    public boolean isMaintenanceRequired() {
        return maintenanceRequired;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public static DigitalTwinState empty(){
        return new DigitalTwinState(null,null,null,false,null);
    }

    public boolean isEmpty(){
        return this.getLastUpdated()==null;
    }

    public boolean isRaw1Received(){
        return raw1 !=null;
    }
    public boolean isRaw2Received(){
        return raw2 !=null;
    }

    public boolean allRawsReceived(){
        return raw1 !=null && raw2 !=null;
    }

    public DigitalTwinState onCreated(DigitalTwinEvent.Created event){
        return new DigitalTwinState(event.getName(), null, null,false, event.getTimestamp());
    }
    public DigitalTwinState onMetricRaw1Received(DigitalTwinEvent.MetricRaw1Received event){
        return new DigitalTwinState(this.getName(), event.getRaw(), this.getRaw2(), false, event.getTimestamp());
    }
    public DigitalTwinState onMetricRaw2Received(DigitalTwinEvent.MetricRaw2Received event){
        return new DigitalTwinState(this.getName(), this.getRaw1(), event.getRaw(), false, event.getTimestamp());
    }
    public DigitalTwinState onMaintenanceRequired(DigitalTwinEvent.MaintenanceRequired event){
        return new DigitalTwinState(this.getName(), event.getRaw1(),event.getRaw2(),true, event.getTimestamp());
    }
    public DigitalTwinState onMaintenanceNotRequired(DigitalTwinEvent.MaintenanceNotRequired event){
        return new DigitalTwinState(this.getName(), null,null,false, event.getTimestamp());
    }
    public DigitalTwinState onMaintenancePerformed(DigitalTwinEvent.MaintenancePerformed event){
        return new DigitalTwinState(this.getName(),null,null,false, event.getTimestamp());
    }



}
