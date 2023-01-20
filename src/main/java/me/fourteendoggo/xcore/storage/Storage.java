package me.fourteendoggo.xcore.storage;

import me.fourteendoggo.xcore.user.Home;
import me.fourteendoggo.xcore.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
    private final Database database = new Database();
    private final Logger logger;

    public Storage(Logger logger) {
        this.logger = logger;
    }

    public void disconnect() {
        database.close();
    }

    public User loadUserBlocking(UUID id) {
        try {
            User user = database.loadUserBlocking(id);
            logger.info("Loaded the user with uuid " + id);
            return user;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load the user with uuid " + id, e);
        }
        return null;
    }

    public void saveUserBlocking(User user) {
        try {
            database.saveUser(user);
            logger.info("Saved the user with name " + user.getPlayer().getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save the user with name " + user.getPlayer().getName(), e);
        }
    }

    public CompletableFuture<Void> saveUser(User user) {
        return makeFuture(() -> database.saveUser(user),
                "Saved the user with uuid " + user.getUniqueId(),
                "Failed to save the user with uuid " + user.getUniqueId());
    }

    public CompletableFuture<Void> deleteHome(Home home) {
        return makeFuture(() -> database.deleteHome(home),
                "Deleted the home with name " + home.name() + " for user with uuid " + home.owner(),
                "Failed to delete the home with name " + home.name() + " for user with uuid " + home.owner());
    }

    private CompletableFuture<Void> makeFuture(Runnable runnable, String logOnSuccess, String logOnFailure) {
        return CompletableFuture.runAsync(runnable).whenComplete((v, throwable) -> {
            if (throwable != null) {
                logger.log(Level.SEVERE, logOnFailure, throwable);
            } else {
                logger.info(logOnSuccess);
            }
        });
    }

    private <T> CompletableFuture<T> makeFuture(Supplier<T> supplier, String logOnSuccess, String logOnFailure) {
        return CompletableFuture.supplyAsync(supplier).whenComplete((result, exception) -> {
            if (exception != null) {
                logger.log(Level.SEVERE, logOnFailure, exception);
            } else {
                logger.info(logOnSuccess);
            }
        });
    }
}
