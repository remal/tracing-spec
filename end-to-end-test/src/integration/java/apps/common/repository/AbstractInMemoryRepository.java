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
import lombok.val;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
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
        val prevEntity = entities.put(entity.getId(), entity);

        onEntitySaved(entity);
        if (prevEntity == null) {
            onEntityCreated(entity);
        } else {
            onEntityChanged(entity);
        }
    }


    @OverrideOnly
    protected void onEntitySaved(EntityType entity) {
    }

    @OverrideOnly
    protected void onEntityCreated(EntityType entity) {
    }

    @OverrideOnly
    protected void onEntityChanged(EntityType entity) {
    }

}
