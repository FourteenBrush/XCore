package me.fourteendoggo.xcore.user

import org.bukkit.Location
import java.util.*

data class Home(val name: String, val owner: UUID, val location: Location)
