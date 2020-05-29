package org.talend.datastreams.components.nifi;

import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.Is;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.talend.datastreams.components.Records;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.JoinInputFactory;
import org.talend.sdk.component.junit.SimpleComponentRule;
import org.talend.sdk.component.junit.beam.Data;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.junit5.WithMavenServers;
import org.talend.sdk.component.runtime.beam.TalendFn;
import org.talend.sdk.component.runtime.output.Processor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

@WithComponents("org.talend.datastreams.components.nifi")
@WithMavenServers
public class NiFiProcessorWrapperTest implements Serializable {
    @ClassRule
    public static final SimpleComponentRule COMPONENT_FACTORY = new SimpleComponentRule(
            "com.uppercase.talend.components.processor");
    @Rule
    public transient final TestPipeline pipeline = TestPipeline.create();
    @Service
    protected RecordBuilderFactory recordBuilderFactory;
    @Test
    public void processor() {
        // Processor configuration
        // Setup your component configuration for the test here
        final NiFiProcessorWrapperConfiguration configuration =  new NiFiProcessorWrapperConfiguration();
        configuration.setGenerateNiFiOutputAttributes(true);

        final Processor processor = COMPONENT_FACTORY.createProcessor(NiFiProcessorWrapper.class, configuration);
        final JoinInputFactory joinInputFactory =  new JoinInputFactory()
                .withInput("__default__", asList(new Records("some test data for the branches you want to test",
                        22), new Records("camel case", 50)));
        final PCollection<Record> inputs =
                pipeline.apply(Data.of(processor.plugin(), joinInputFactory.asInputRecords()));
        final PCollection<Map<String, Record>> outputs = inputs.apply(TalendFn.asFn(processor))
                .apply(Data.map(processor.plugin(), Record.class));

        PAssert.that(outputs).satisfies((SerializableFunction<Iterable<Map<String, Record>>, Void>) input -> {
            final List<Map<String, Record>> result = StreamSupport.stream(input.spliterator(), false)
                    .collect(toList());
            result.forEach(r -> {
                MatcherAssert.assertThat(r.get("__default__").getString("name"), AnyOf.anyOf(Is.is("Aaa"),
                        Is.is("CamelCase")));
            });
            return null;
        });

        assertEquals(PipelineResult.State.DONE, pipeline.run().waitUntilFinish());
    }

    private static void println(Object str) {
        System.out.println(str);
    }


}