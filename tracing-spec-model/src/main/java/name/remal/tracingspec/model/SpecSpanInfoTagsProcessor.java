package name.remal.tracingspec.model;

import static name.remal.tracingspec.model.SpecSpanKind.parseSpecSpanKind;

import java.util.Map;
import javax.annotation.Nullable;
import lombok.val;

@SuppressWarnings({"java:S1157"})
abstract class SpecSpanInfoTagsProcessor {

    @SuppressWarnings({"java:S131", "checkstyle:MissingSwitchDefault"})
    public static void processTag(SpecSpanInfo<?> info, @Nullable String key, @Nullable String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return;
        }

        switch (key) {
            case "spec.hidden":
                if (!info.isHidden()) {
                    info.setHidden(parseBoolean(value));
                }
                break;
            case "spec.kind":
                if (info.getKind() == null) {
                    info.setKind(parseSpecSpanKind(value));
                }
                break;
            case "spec.async":
                if (!info.isAsync()) {
                    info.setAsync(parseBoolean(value));
                }
                break;
            case "spec.serviceName":
                if (info.getServiceName() == null) {
                    info.setServiceName(value);
                }
                break;
            case "spec.remoteServiceName":
                if (info.getRemoteServiceName() == null) {
                    info.setRemoteServiceName(value);
                }
                break;
            case "spec.description":
                if (info.getDescription() == null) {
                    info.setDescription(value);
                }
                break;
        }
    }

    public static void processTags(SpecSpanInfo<?> info, Map<String, String> map) {
        for (val entry : map.entrySet()) {
            processTag(info, entry.getKey(), entry.getValue());
        }
    }


    private static boolean parseBoolean(String value) {
        return "1".equals(value) || "true".equals(value.toLowerCase());
    }


    private SpecSpanInfoTagsProcessor() {
    }

}
