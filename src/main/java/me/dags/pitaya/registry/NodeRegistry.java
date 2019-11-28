package me.dags.pitaya.registry;

import me.dags.config.Config;
import me.dags.config.Node;
import org.spongepowered.api.CatalogType;

import java.util.Optional;

public abstract class NodeRegistry<T extends CatalogType> extends Registry<T> {

    private final Object[] root;
    private final Config storage;
    private final boolean registerDefaults;

    public NodeRegistry(Config storage, String... root) {
        this(storage, false, root);
    }

    public NodeRegistry(Config storage, boolean registerDefaults, String... root) {
        this.registerDefaults = registerDefaults;
        this.storage = storage;
        this.root = root;
    }

    @Override
    public void registerDefaults() {
        if (registerDefaults) {
            load();
        }
    }

    @Override
    public void load() {
        registry.clear();
        storage.reload();
        getRoot().iterate((name, data) -> deserialize(name.toString(), data).ifPresent(value -> registry.put(value.getId(), value)));
    }

    @Override
    public void register(T value) {
        super.register(value);
        Node data = getRoot().node(value.getId());
        serialize(data, value);
        storage.save();
    }

    protected Node getRoot() {
        if (root.length == 0) {
            return storage;
        }
        return storage.node(root);
    }

    protected abstract void serialize(Node node, T value);

    protected abstract Optional<T> deserialize(String name, Node node);
}
