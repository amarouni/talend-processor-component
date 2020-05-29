package org.talend.datastreams.components.nifi;

import org.apache.nifi.util.MockFlowFile;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import java.util.Map;

public class NiFiUtils {

    public static Record BuildRecordFromFlowFile(MockFlowFile flowFile, RecordBuilderFactory recordBuilderFactory){

        // NiFi Data Output
        byte[] output = flowFile.toByteArray();

        // Build Talend Record builder
        Record.Builder baseRecordBuilder =
                recordBuilderFactory.newRecordBuilder();
        // Put transformed data as output String column
        baseRecordBuilder.withString("output", new String(output));

        // Put all NifI attributes under 'attributes' key
        Record.Builder attributesRecordBuilder =
                recordBuilderFactory.newRecordBuilder();
        for (Map.Entry<String, String> pair : flowFile.getAttributes().entrySet()){
            attributesRecordBuilder.withString(pair.getKey(), pair.getValue());
        }

        baseRecordBuilder.withRecord("attributes", attributesRecordBuilder.build());

        return baseRecordBuilder.build();
    }
}
