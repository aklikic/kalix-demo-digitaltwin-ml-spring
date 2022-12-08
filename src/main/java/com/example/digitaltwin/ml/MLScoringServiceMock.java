package com.example.digitaltwin.ml;

import com.example.digitaltwin.model.DigitalTwinApi;
import org.springframework.stereotype.Component;

//@Component
public class MLScoringServiceMock implements MLScoringService {

    public static final DigitalTwinApi.MetricRequest metricOKRequest = new DigitalTwinApi.MetricRequest(0.92d,0.328501935d);
    public static final DigitalTwinApi.MetricRequest metricFailRequest = new DigitalTwinApi.MetricRequest(0.72d,0.002764904d);

    public static final DigitalTwinApi.MetricRawRequest metricRaw1OKRequest = new DigitalTwinApi.MetricRawRequest(0.92d);
    public static final DigitalTwinApi.MetricRawRequest metricRaw2OKRequest = new DigitalTwinApi.MetricRawRequest(0.328501935d);

    public static final DigitalTwinApi.MetricRawRequest metricRaw1FailRequest = new DigitalTwinApi.MetricRawRequest(0.72d);
    public static final DigitalTwinApi.MetricRawRequest metricRaw2FailRequest = new DigitalTwinApi.MetricRawRequest(0.002764904d);

    @Override
    public boolean scoreIfMaintenanceRequired(Double raw1, Double raw2) {
        if(raw1.doubleValue() == metricOKRequest.getRaw1().doubleValue() && raw2.doubleValue() == metricOKRequest.getRaw2().doubleValue())
            return false;

        return true;

    }
}
