package org.talend.datastreams.components.nifi;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@GridLayout({
        @GridLayout.Row({ "generateNiFiOutputAttributes" })
})
@Documentation("NiFiProcessorWrapperConfiguration")
public class NiFiProcessorWrapperConfiguration implements Serializable {

    @Option
    @Documentation("Generate NiFi Output Attributes")
    private boolean generateNiFiOutputAttributes = true;

    public boolean getGenerateNiFiOutputAttributes() {
        return generateNiFiOutputAttributes;
    }

    public NiFiProcessorWrapperConfiguration setGenerateNiFiOutputAttributes(boolean generateNiFiOutputAttributes) {
        this.generateNiFiOutputAttributes = generateNiFiOutputAttributes;
        return this;
    }
}