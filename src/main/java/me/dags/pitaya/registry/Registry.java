package me.dags.pitaya.registry;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Registry<T extends CatalogType> implements CatalogRegistryModule<T> {

    protected final Map<String, T> registry = new LinkedHashMap<>();

    @Override
    public Optional<T> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<T> getAll() {
        return registry.values();
    }

    public void load() {

    }

    public void register(T value) {
        registry.put(value.getId(), value);
    }

    public void delete(String id) {
        registry.remove(id);
    }
}
