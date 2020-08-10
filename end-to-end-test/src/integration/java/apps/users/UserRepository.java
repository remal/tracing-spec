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

import apps.common.repository.AbstractInMemoryRepository;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("java:S1171")
public class CounterpartyRepository extends AbstractInMemoryRepository<Counterparty> {

    {
        for (long id = 1; id <= 9; ++id) {
            val entity = ImmutableCounterparty.builder()
                .title(FAKER.company().name())
                .address(FAKER.address().fullAddress())
                .build();
            save(id, entity);
        }
    }

}
