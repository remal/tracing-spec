package utils.test.container;

import static java.lang.String.format;

public interface WithZipkinCollectorUrl {

    int getZipkinPort();

    default String getZipkinCollectorUrl() {
        return format("http://localhost:%d/api/v2/spans", getZipkinPort());
    }

}
