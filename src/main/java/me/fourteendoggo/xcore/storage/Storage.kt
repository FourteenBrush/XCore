package me.fourteendoggo.xcore.storage

import me.fourteendoggo.xcore.user.User
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

// TODO: use coroutines or smth

class Storage(private val logger: Logger) {
    private val database = Database()

    fun disconnect() = database.close()

    fun loadUserBlocking(id: UUID): User? {
        return try {
            database.loadUserBlocking(id)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to load user $id", e)
            null
        }
    }

    fun saveUserBlocking(user: User) {
        try {
            database.saveUser(user)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to save user ${user.player.name} (${user.player.uniqueId})", e)
        }
    }

    fun saveUser(user: User): CompletableFuture<Void> = CompletableFuture.runAsync {
        database.saveUser(user)
    }
}