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

package apps.common.repository;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Locale.ENGLISH;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Unmodifiable;

public abstract class AbstractInMemoryRepository<ID, EntityType extends Entity<ID>>
    implements InMemoryRepository<ID, EntityType> {

    protected static final Faker FAKER = new Faker(ENGLISH);

    private final Map<ID, EntityType> entities = synchronizedMap(new LinkedHashMap<>());

    @Override
    @Unmodifiable
    public final List<EntityType> getAll() {
        return unmodifiableList(new ArrayList<>(entities.values()));
    }

    @Override
    public final Optional<EntityType> findById(ID id) {
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public final void save(EntityType entity) {
        entities.put(entity.getId(), entity);
    }

}
