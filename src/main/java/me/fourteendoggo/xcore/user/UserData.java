package me.fourteendoggo.xcore.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserData {
    private final Map<String, Home> homes = new HashMap<>();

    public int getHomesAmount() {
        return homes.size();
    }

    public Home removeHome(String name) {
        return homes.remove(name);
    }

    public boolean addHome(Home home) {
        return homes.putIfAbsent(home.name(), home) == null;
    }

    public Collection<Home> getHomes() {
        return Collections.unmodifiableCollection(homes.values());
    }
}
