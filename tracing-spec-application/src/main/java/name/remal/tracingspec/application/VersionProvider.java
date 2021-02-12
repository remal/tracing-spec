package name.remal.tracingspec.application;

import static name.remal.gradle_plugins.api.BuildTimeConstants.getStringProperty;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.stereotype.Component;
import picocli.CommandLine.IVersionProvider;

@Internal
@Component
class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[]{
            getStringProperty("version")
        };
    }

}
