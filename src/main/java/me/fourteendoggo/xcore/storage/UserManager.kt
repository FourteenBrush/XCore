package me.fourteendoggo.xcore.storage

import me.fourteendoggo.xcore.user.User
import org.jetbrains.annotations.Blocking
import java.util.UUID

class UserManager(private val storage: Storage) : Iterable<User> {
    private val users = HashMap<UUID, User>()

    override fun iterator() = users.values.iterator()

    fun getUser(id: UUID) = users[id]!!

    @Blocking // potentially
    fun loadIfAbsent(id: UUID): Boolean {
        @Suppress("UNCHECKED_CAST")
        val uncheckedMap = users as MutableMap<UUID, User?>
        return uncheckedMap.computeIfAbsent(id, storage::loadUserBlocking) != null
    }

    fun unloadUser(id: UUID) = users.remove(id)!!.also {
        storage.saveUser(it)
    }

    @Blocking
    fun saveAllBlocking() = forEach(storage::saveUserBlocking)
}