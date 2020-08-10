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

package apps.dictionaries;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import name.remal.tracingspec.spring.sleuth.SpecSpan;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DictionariesController implements DictionariesApi {

    private final CounterpartyRepository repository;

    @Override
    @SpecSpan(description = "Get all counterparties")
    public Map<Long, Counterparty> getCounterparties() {
        return repository.getAll();
    }

    @Override
    @SpecSpan(description = "Get counterparty by id")
    public Optional<Counterparty> getCounterparty(long id) {
        return repository.findById(id);
    }

}
