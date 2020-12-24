/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package apps.common;

import static org.apache.logging.log4j.Level.INFO;
import static org.apache.logging.log4j.Level.WARN;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.tracingspec.renderer.RenderingOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@Component
@RequiredArgsConstructor
public class LogSpanReporter implements Reporter<Span> {

    private static final Logger logger = LogManager.getLogger(LogSpanReporter.class);

    private final RenderingOptions renderingOptions;

    @Override
    public void report(Span span) {
        final Level level;
        {
            val error = span.tags().get("error");
            if (error == null || error.isEmpty()) {
                level = INFO;
            } else {
                level = WARN;
            }
        }

        logger.log(level, () -> {
            val sb = new StringBuilder();
            sb.append("Span: ").append(span.localServiceName()).append(": ").append(span.name());
            span.tags().forEach((key, value) -> {
                if (renderingOptions.getTagsToDisplay().contains(key)) {
                    sb.append(", ").append(key).append('=').append(value);
                }
            });
            return sb.toString();
        });
    }

    @Override
    public String toString() {
        return LogSpanReporter.class.getSimpleName();
    }

}
