package com.example.digitaltwin;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = DigitalTwinService.class,ignoreUnknown = true)
public class AggregationTimeWindowTimerAction extends Action {

    private KalixClient kalixClient;

    public AggregationTimeWindowTimerAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    private String getTimerName (String dtId){
        return  "TIME-WINDOW-TIMER/" + dtId;
    }
    private CompletionStage<String> createTimer(String dtId, Integer aggregationTimeWindowSeconds){
        timers().cancel(getTimerName(dtId));
        var deferredCall = kalixClient.post("/dt/"+dtId+"/aggregation-time-window-timer-trigger",DigitalTwinModel.EmptyResponse.class);
        return timers().startSingleTimer(getTimerName(dtId), Duration.of(aggregationTimeWindowSeconds, ChronoUnit.SECONDS),deferredCall)
                .thenApply(d -> "OK");
    }
    public Effect<String> onCreatedEvent(DigitalTwinModel.CreatedEvent event){
        var timerCreate = createTimer(event.dtId, event.aggregationTimeWindowSeconds);
        return effects().asyncReply(timerCreate);
    }

    public Effect<String> onAggregationFinishedEvent(DigitalTwinModel.AggregationFinishedEvent event){
        var timerCreate = createTimer(event.dtId, event.aggregationTimeWindowSeconds);
        return effects().asyncReply(timerCreate);
    }


}
