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
