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

import static java.lang.String.format;

import apps.common.NotFoundException;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

public interface InMemoryRepository<ID, EntityType extends Entity<ID>> {

    @Unmodifiable
    List<EntityType> getAll();

    Optional<EntityType> findById(ID id);

    default EntityType getById(ID id) {
        return findById(id)
            .orElseThrow(() -> {
                val repositoryType = (ParameterizedType) TypeToken.of(getClass())
                    .getSupertype(InMemoryRepository.class)
                    .getType();
                val entityType = repositoryType.getActualTypeArguments()[1];
                throw new NotFoundException(format(
                    "%s can't be found by ID=%s",
                    TypeToken.of(entityType).getRawType().getSimpleName(),
                    id
                ));
            });
    }

    void save(EntityType entity);

}
