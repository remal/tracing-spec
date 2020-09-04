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

import static java.lang.Math.max;
import static java.lang.Math.toIntExact;

import brave.Tracer;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.sleuth.instrument.web.SleuthWebProperties;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class TraceIdWebFilter extends OncePerRequestFilter implements Ordered {

    public static final String TRACE_ID_HTTP_HEADER = "X-Trace-ID";

    private final SleuthWebProperties sleuthProperties;
    private final Tracer tracer;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        val currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            response.setHeader(TRACE_ID_HTTP_HEADER, currentSpan.context().traceIdString());
        }

        chain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        val sleuthFilterOrder = Long.valueOf(sleuthProperties.getFilterOrder());
        val order = max(sleuthFilterOrder + 1, Integer.MAX_VALUE);
        return toIntExact(order);
    }

}
