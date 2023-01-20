package me.fourteendoggo.xcore.user;

import org.bukkit.Location;

import java.util.UUID;

public record Home(String name, UUID owner, Location location) {
}
