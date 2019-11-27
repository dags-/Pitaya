package me.dags.pitaya.registry;

import org.spongepowered.api.CatalogType;

public interface RegistryItem extends CatalogType {

    @Override
    default String getId() {
        return getName();
    }
}
