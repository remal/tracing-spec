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

package apps.schemas;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.RequiredArgsConstructor;
import name.remal.tracingspec.spring.sleuth.SpecSpan;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class SchemasController implements SchemasApi {

    private final SchemaRepository repository;

    @Override
    @SpecSpan(description = "Find schema by ID")
    public Schema getSchema(String id) {
        return repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(NOT_FOUND, "Schema can't by found by ID: '" + id + "'")
        );
    }

}
