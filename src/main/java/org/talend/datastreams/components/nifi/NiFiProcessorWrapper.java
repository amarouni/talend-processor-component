package org.talend.datastreams.components.nifi;

import org.apache.nifi.processors.standard.CountText;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.BeforeGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;

@Version(1)
@Icon(Icon.IconType.STAR)
@Processor(name = "NiFiProcessorWrapper")
@Documentation("NiFiProcessorWrapper")
public class NiFiProcessorWrapper implements Serializable {

    private final NiFiProcessorWrapperConfiguration configuration;
    // NiFi Test Runner setup
    private TestRunner nifiTestRunner;

    @Service
    private final RecordBuilderFactory recordBuilderFactory;

    public NiFiProcessorWrapper(@Option("configuration") final NiFiProcessorWrapperConfiguration configuration,
                                RecordBuilderFactory recordBuilderFactory) {
        this.recordBuilderFactory = recordBuilderFactory;
        this.configuration = configuration;
    }

    @PostConstruct
    public void init() {
        // Setup Testrunner
        nifiTestRunner = TestRunners.newTestRunner(CountText.class);

        // Set NiFi config
        nifiTestRunner.setProperty(CountText.TEXT_LINE_COUNT_PD, "true");
        nifiTestRunner.setProperty(CountText.TEXT_LINE_NONEMPTY_COUNT_PD, "true");
        nifiTestRunner.setProperty(CountText.TEXT_WORD_COUNT_PD, "true");
        nifiTestRunner.setProperty(CountText.TEXT_CHARACTER_COUNT_PD, "true");
    }

    @BeforeGroup
    public void beforeGroup() {
        // if the environment supports chunking this method is called at the beginning if a chunk
        // it can be used to start a local transaction specific to the backend you use
        // Note: if you don't need it you can delete it
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput,
            @Output final OutputEmitter<Record> defaultOutput) {

        // NiFi Input
        byte[] input = defaultInput.getString("col0").getBytes();

        // Enqueue bytes in NiFi runner
        nifiTestRunner.enqueue(input);
        // Run on NiFi runner
        nifiTestRunner.run();
        // Get the first output from NiFi runner
        MockFlowFile flowFile = nifiTestRunner.getFlowFilesForRelationship(CountText.REL_SUCCESS).get(0);

        // Build Talend record from Nifi FlowFile
        Record outputRecord = NiFiUtils.BuildRecordFromFlowFile(flowFile, recordBuilderFactory);

        // Identity mapping
        defaultOutput
                .emit(outputRecord);
    }

    @AfterGroup
    public void afterGroup() {
        // symmetric method of the beforeGroup() executed after the chunk processing
        // Note: if you don't need it you can delete it
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
    }

    private static void println(Object str) {
        System.out.println(str);
    }


}