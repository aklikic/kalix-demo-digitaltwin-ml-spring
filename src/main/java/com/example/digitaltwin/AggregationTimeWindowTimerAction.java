package com.example.digitaltwin;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Subscribe.EventSourcedEntity(value = DigitalTwinService.class,ignoreUnknown = true)
public class AggregationTimeWindowTimerAction extends Action {

    private KalixClient kalixClient;

    public AggregationTimeWindowTimerAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    private String getTimerName (String dtId){
        return  "TIME-WINDOW-TIMER/" + dtId;
    }
    private void createTimer(String dtId, Integer aggregationTimeWindowSeconds){
        timers().cancel(getTimerName(dtId));
        var deferredCall = kalixClient.post("/dt/"+dtId+"/aggregation-time-window-timer-trigger",DigitalTwinModel.EmptyResponse.class);
        timers().startSingleTimer(getTimerName(dtId), Duration.of(aggregationTimeWindowSeconds, ChronoUnit.SECONDS),deferredCall);
    }
    public Effect<DigitalTwinModel.Ack> onCreatedEvent(DigitalTwinModel.CreatedEvent event){
        createTimer(event.dtId, event.aggregationTimeWindowSeconds);
        return effects().reply(DigitalTwinModel.Ack.of());
    }

    public Effect<DigitalTwinModel.Ack> onAggregationFinishedEvent(DigitalTwinModel.AggregationFinishedEvent event){
        createTimer(event.dtId, event.aggregationTimeWindowSeconds);
        return effects().reply(DigitalTwinModel.Ack.of());
    }


}
