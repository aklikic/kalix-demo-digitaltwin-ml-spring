package com.example.digitaltwin.ml;

import ai.h2o.mojos.runtime.MojoPipeline;
import ai.h2o.mojos.runtime.api.MojoPipelineService;
import ai.h2o.mojos.runtime.frame.MojoFrame;
import ai.h2o.mojos.runtime.frame.MojoFrameBuilder;
import ai.h2o.mojos.runtime.frame.MojoRowBuilder;
import ai.h2o.mojos.runtime.lic.LicenseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class MLScoringServiceH20 implements MLScoringService{

    private static Logger logger = LoggerFactory.getLogger(MLScoringServiceH20.class);
    private final MojoPipeline model;

    public MLScoringServiceH20() throws IOException, LicenseException {
        model = MojoPipelineService.loadPipeline(getFile("pipeline.mojo"));
    }

    private File getFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);

        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }

    @Override
    public boolean scoreAndReturnIfMaintenanceRequired(List<Data> dataList) {
       return score(model,dataList);
    }

    private boolean score(MojoPipeline model, final List<Data> dataList) {

        // setup mojoFrame
        final MojoFrameBuilder frameBuilder = model.getInputFrameBuilder();


        dataList.stream().map(d -> {
            final MojoRowBuilder rowBuilder = frameBuilder.getMojoRowBuilder();
            rowBuilder.setValue(model.getInputMeta().getColumnName(0), d.raw1+"");
            rowBuilder.setValue(model.getInputMeta().getColumnName(1), d.raw2+"");
            return rowBuilder;
        }).forEach(rb -> frameBuilder.addRow(rb));
        // Create a frame which can be transformed by MOJO pipeline
        final MojoFrame iframe = frameBuilder.toMojoFrame();
        // Transform input frame by MOJO pipeline
        final MojoFrame oframe = model.transform(iframe);

        var resColumn0 = Double.parseDouble(oframe.getColumn(0).getDataAsStrings()[0]);
        var resColumn1 = Double.parseDouble(oframe.getColumn(1).getDataAsStrings()[0]);

        return resColumn1 > resColumn0;
    }

}
