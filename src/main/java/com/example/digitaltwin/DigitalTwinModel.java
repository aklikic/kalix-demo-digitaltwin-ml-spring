package com.example.digitaltwin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import kalix.springsdk.annotations.TypeName;

import java.time.Instant;

public interface DigitalTwinModel {

    class MetricData implements DigitalTwinModel {
        public final String requestId;
        public final Double raw1;
        public final Double raw2;
        public final Instant timestamp;

        @JsonCreator
        public MetricData(
                          @JsonProperty String requestId,
                          @JsonProperty Double raw1,
                          @JsonProperty Double raw2,
                          @JsonProperty Instant timestamp) {
            this.requestId = requestId;
            this.raw1 = raw1;
            this.raw2 = raw2;
            this.timestamp = timestamp;
        }
    }

     class State implements DigitalTwinModel {
        public final String name;
        public final Integer aggregationLimit;
        public final Integer aggregationTimeWindowSeconds;
        public final String aggregationId;
        public final ImmutableList<MetricData> aggregation;
        public final boolean maintenanceRequired;
        public final Instant lastUpdated;

        @JsonCreator
        public State(@JsonProperty String name,
                     @JsonProperty Integer aggregationLimit,
                     @JsonProperty Integer aggregationTimeWindowSeconds,
                     @JsonProperty String aggregationId,
                     @JsonProperty ImmutableList<MetricData> aggregation,
                     @JsonProperty boolean maintenanceRequired,
                     @JsonProperty Instant lastUpdated) {
            this.name = name;
            this.aggregationLimit = aggregationLimit;
            this.aggregationTimeWindowSeconds = aggregationTimeWindowSeconds;
            this.aggregationId = aggregationId;
            this.aggregation = aggregation;
            this.maintenanceRequired = maintenanceRequired;
            this.lastUpdated = lastUpdated;
        }

        public static State empty(){
            return new State(null,null,null,null,null,false,null);
        }
        @JsonIgnore
        public boolean isEmpty(){
            return lastUpdated==null;
        }
        @JsonIgnore
        public boolean isDuplicate(String requestId){
            return aggregation.stream().filter(a -> a.requestId.equals(requestId)).findFirst().isPresent();
        }
        @JsonIgnore
        public boolean isAggregationFinished(){
            if(aggregation.isEmpty())
                return false;
            if(aggregation.size() == aggregationLimit)
                return true;
            if(!aggregation.isEmpty() && aggregation.get(0).timestamp.plusSeconds(aggregationTimeWindowSeconds).isBefore(Instant.now()))
                return true;
            return false;
        }
        @JsonIgnore
        public State onCreatedEvent(CreatedEvent event){
            return new State(event.name, event.aggregationLimit, event.aggregationTimeWindowSeconds, event.nextAggregationId, ImmutableList.of(), false, event.timestamp);
        }
        @JsonIgnore
        public State onMetricAggregatedEvent(MetricAggregatedEvent event){
            ImmutableList<MetricData> newList = ImmutableList.<MetricData>builder().addAll(this.aggregation).add(new MetricData(event.requestId, event.raw1, event.raw2, event.timestamp)).build();
            return new State(this.name, this.aggregationLimit, this.aggregationTimeWindowSeconds, event.aggregationId, newList, false, event.timestamp);
        }
        @JsonIgnore
        public State onAggregationFinishedEvent(AggregationFinishedEvent event){
            return new State(this.name, this.aggregationLimit, this.aggregationTimeWindowSeconds, event.nextAggregationId, ImmutableList.of(), event.maintenanceRequired, event.timestamp);
        }
         @JsonIgnore
        public State onMaintenancePerformedEvent(MaintenancePerformedEvent event){
            return new State(this.name, this.aggregationLimit, this.aggregationTimeWindowSeconds, this.aggregationId, ImmutableList.of(), false, event.timestamp);
        }

    }


    /**
     * Events
     */

    @TypeName("created")
    class CreatedEvent implements DigitalTwinModel{
        public final String dtId;
        public final String name;
        public final Integer aggregationLimit;
        public final Integer aggregationTimeWindowSeconds;
        public final String nextAggregationId;
        public final Instant timestamp;

        @JsonCreator
        public CreatedEvent(@JsonProperty String dtId,
                            @JsonProperty String name,
                            @JsonProperty Integer aggregationLimit,
                            @JsonProperty Integer aggregationTimeWindowSeconds,
                            @JsonProperty String nextAggregationId,
                            @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.name = name;
            this.aggregationLimit = aggregationLimit;
            this.aggregationTimeWindowSeconds = aggregationTimeWindowSeconds;
            this.nextAggregationId = nextAggregationId;
            this.timestamp = timestamp;
        }

    }

    @TypeName("metric_aggregated")
    class MetricAggregatedEvent implements DigitalTwinModel{
        public final String dtId;
        public final String aggregationId;
        public final String requestId;
        public final Double raw1;
        public final Double raw2;
        public final Instant timestamp;

        @JsonCreator
        public MetricAggregatedEvent(@JsonProperty String dtId,
                                     @JsonProperty String aggregationId,
                                     @JsonProperty String requestId,
                                     @JsonProperty Double raw1,
                                     @JsonProperty Double raw2,
                                     @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.requestId =  requestId;
            this.raw1 = raw1;
            this.raw2 = raw2;
            this.aggregationId = aggregationId;
            this.timestamp = timestamp;
        }
    }

    @TypeName("aggregation_finished")
    class AggregationFinishedEvent implements DigitalTwinModel{
        public final String dtId;
        public final Integer aggregationTimeWindowSeconds;
        public final String nextAggregationId;
        public final boolean maintenanceRequired;
        public final Instant timestamp;

        @JsonCreator
        public AggregationFinishedEvent(@JsonProperty String dtId,
                                        @JsonProperty Integer aggregationTimeWindowSeconds,
                                        @JsonProperty String nextAggregationId,
                                        @JsonProperty boolean maintenanceRequired,
                                        @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.aggregationTimeWindowSeconds = aggregationTimeWindowSeconds;
            this.nextAggregationId = nextAggregationId;
            this.maintenanceRequired = maintenanceRequired;
            this.timestamp = timestamp;
        }
    }

    @TypeName("maintenance_performed")
    class MaintenancePerformedEvent implements DigitalTwinModel{
        public final String dtId;
        public final Integer aggregationTimeWindowSeconds;
        public final String nextAggregationId;
        public final Instant timestamp;

        @JsonCreator
        public MaintenancePerformedEvent(@JsonProperty String dtId,
                                         @JsonProperty Integer aggregationTimeWindowSeconds,
                                         @JsonProperty String nextAggregationId,
                                         @JsonProperty Instant timestamp) {
            this.dtId = dtId;
            this.aggregationTimeWindowSeconds = aggregationTimeWindowSeconds;
            this.nextAggregationId = nextAggregationId;
            this.timestamp = timestamp;
        }
    }

    /**
     * Endpoint API
     */

    class CreateRequest implements DigitalTwinModel {
        public final String name;
        public final Integer aggregationLimit;
        public final Integer aggregationTimeWindowSeconds;

        @JsonCreator
        public CreateRequest(@JsonProperty String name,
                             @JsonProperty Integer aggregationLimit,
                             @JsonProperty Integer aggregationTimeWindowSeconds) {
            this.name = name;
            this.aggregationLimit = aggregationLimit;
            this.aggregationTimeWindowSeconds = aggregationTimeWindowSeconds;
        }
    }
    class MetricRequest implements DigitalTwinModel {
        public final String requestId;
        public final Double raw1;
        public final Double raw2;

        @JsonCreator
        public MetricRequest(@JsonProperty String requestId,
                             @JsonProperty Double raw1,
                             @JsonProperty Double raw2) {
            this.requestId = requestId;
            this.raw1 = raw1;
            this.raw2 = raw2;
        }
    }

    class AggregationTimeWindowDoneRequest implements DigitalTwinModel {
        public final String aggregationId;

        @JsonCreator
        public AggregationTimeWindowDoneRequest(@JsonProperty String aggregationId) {
            this.aggregationId = aggregationId;
        }
    }

    class GetResponse implements DigitalTwinModel {
        private String name;
        private Integer aggregationSize;
        private Boolean maintenanceRequired;
        @JsonCreator
        public GetResponse(@JsonProperty("name") String name,
                           @JsonProperty("aggregationSize") Integer aggregationSize,
                           @JsonProperty("maintenanceRequired") Boolean maintenanceRequired) {
            this.name = name;
            this.aggregationSize = aggregationSize;
            this.maintenanceRequired = maintenanceRequired;
        }
        public String getName() {
            return name;
        }
        public int getAggregationSize() {
            return aggregationSize;
        }
        public boolean isMaintenanceRequired() {
            return maintenanceRequired;
        }
    }

    public static String OK_RESPONSE = "OK";
}
