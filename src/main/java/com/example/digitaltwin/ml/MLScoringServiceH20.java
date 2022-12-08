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

@Component
public class MLScoringServiceH20 implements MLScoringService{

    private static Logger logger = LoggerFactory.getLogger(MLScoringServiceH20.class);
    private final MojoPipeline model;

    public MLScoringServiceH20() throws IOException, LicenseException {
        model = MojoPipelineService.loadPipeline(getFile("pipeline.mojo"));
//        String path = getFile("pipeline.mojo").getPath();
//        System.out.println("path: "+path);
//        byte [] data = Files.readAllBytes(Paths.get(path));
//        System.out.println("mojo: "+data.length);
//
//        model = MojoPipelineService.loadPipeline(new ByteArrayReaderBackend(data));
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
    public boolean scoreIfMaintenanceRequired(Double raw1, Double raw2) {
        double[] rowValues = new double[2];
        rowValues [0] = raw1;
        rowValues [1] = raw2;
        boolean res = score(model,rowValues);
        logger.info("Scoring {}/{}: {}",raw1,raw2,res);
        return res;
    }

    // score line
    private boolean score(MojoPipeline model, final double[] rowValues) {

        // setup mojoFrame
        final MojoFrameBuilder frameBuilder = model.getInputFrameBuilder();
        final MojoRowBuilder rowBuilder = frameBuilder.getMojoRowBuilder();

        for (int i=0; i<rowValues.length; i++) {
            rowBuilder.setValue(model.getInputMeta().getColumnName(i), rowValues[i]+"");
        }

        // create a row
        frameBuilder.addRow(rowBuilder);
        // Create a frame which can be transformed by MOJO pipeline
        final MojoFrame iframe = frameBuilder.toMojoFrame();
        // Transform input frame by MOJO pipeline
        final MojoFrame oframe = model.transform(iframe);

        // display predicted class
        double[] output = new double[2];
        output[0] = Double.parseDouble(oframe.getColumn(0).getDataAsStrings()[0]);
        output[1] = Double.parseDouble(oframe.getColumn(1).getDataAsStrings()[0]);

        int index = argMax(output);

        return index != 0;
    }

    // argmax function to return the predicted class based on predicted probabilities
    private static int argMax(final double[] values) {
        double curr_max = Double.NEGATIVE_INFINITY;
        int pos = 0;
        for (int i=0; i<values.length; i++) {
            if (values[i] > curr_max) {
                curr_max = values[i];
                pos = i;
            }
        }
        return pos;
    }
}
