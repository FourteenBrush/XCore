package me.fourteendoggo.xcore.storage;

import me.fourteendoggo.xcore.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class UserManager implements Iterable<User> {
    private final Storage storage;
    private final Map<UUID, User> users;

    public UserManager(Storage storage) {
        this.storage = storage;
        this.users = new HashMap<>();
    }

    @NotNull
    @Override
    public Iterator<User> iterator() {
        return users.values().iterator();
    }

    public User getUser(UUID id) {
        return users.get(id);
    }

    public boolean loadIfAbsent(UUID id) {
        return users.computeIfAbsent(id, storage::loadUserBlocking) != null;
    }

    public User unloadUser(UUID id) {
        User user = users.remove(id);
        storage.saveUser(user);
        return user;
    }

    public void saveAllBlocking() {
        users.values().forEach(storage::saveUserBlocking);
    }
}
