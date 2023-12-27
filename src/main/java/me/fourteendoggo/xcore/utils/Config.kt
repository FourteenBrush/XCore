package me.fourteendoggo.xcore.utils

import me.fourteendoggo.xcore.XCore
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.FileConfigurationOptions
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.logging.Level

// FIXME: delegation
class Config(private val core: XCore, private val fileName: String, private val copyDefaults: Boolean) : Reloadable {
    private val file: File = File(core.dataFolder, fileName)
    private lateinit var configuration: FileConfiguration
    private var saveRequired = false

    init {
        if (!file.exists()) {
            core.saveResource(fileName, false)
        }
        // won't save the file yet so this won't block
        reload()
    }

    override fun reload() {
        if (saveRequired) {
            saveBlocking()
        }
        configuration = YamlConfiguration.loadConfiguration(file)
        if (!copyDefaults) return
        val defaults = core.getResource(fileName)
        if (defaults == null) {
            core.logger.warning("Tried to copy defaults for $fileName but it doesn't exist")
            return
        }
        val reader: Reader = InputStreamReader(defaults)
        configuration.setDefaults(YamlConfiguration.loadConfiguration(reader))
        configuration.options().copyDefaults(true)
    }

    fun saveBlocking() {
        try {
            configuration.save(file)
            saveRequired = false
        } catch (e: IOException) {
            core.logger.log(Level.SEVERE, "Couldn't write to $fileName", e)
        }
    }

    fun options(): FileConfigurationOptions {
        return configuration.options()
    }

    fun addDefault(path: String, value: Any?) {
        configuration.addDefault(path, value)
    }

    fun addDefaults(defaults: Map<String?, Any?>) {
        configuration.addDefaults(defaults)
    }

    fun addDefaults(defaults: Configuration) {
        configuration.addDefaults(defaults)
    }

    fun setDefaults(defaults: Configuration) {
        configuration.setDefaults(defaults)
    }

    val defaults: Configuration?
        get() = configuration.defaults
    val parent: ConfigurationSection?
        get() = configuration.parent

    fun getKeys(deep: Boolean): Set<String> {
        return configuration.getKeys(deep)
    }

    fun getValues(deep: Boolean): Map<String, Any> {
        return configuration.getValues(deep)
    }

    operator fun contains(path: String): Boolean {
        return configuration.contains(path)
    }

    fun contains(path: String, ignoreDefault: Boolean): Boolean {
        return configuration.contains(path, ignoreDefault)
    }

    fun isSet(path: String): Boolean {
        return configuration.isSet(path)
    }

    val currentPath: String
        get() = configuration.currentPath
    val name: String
        get() = configuration.name
    val root: Configuration?
        get() = configuration.root
    val defaultSection: ConfigurationSection?
        get() = configuration.defaultSection

    operator fun set(path: String, value: Any?) {
        configuration[path] = value
        saveRequired = true
    }

    operator fun get(path: String): Any? {
        return configuration[path]
    }

    @Contract("_, !null -> !null")
    operator fun get(path: String, def: Any?): Any? {
        return configuration[path, def]
    }

    fun createSection(path: String): ConfigurationSection {
        return configuration.createSection(path)
    }

    fun createSection(path: String, map: Map<*, *>): ConfigurationSection {
        return configuration.createSection(path, map)
    }

    fun getString(path: String): String? {
        return configuration.getString(path)
    }

    @Contract("_, !null -> !null")
    fun getString(path: String, def: String?): String? {
        return configuration.getString(path, def)
    }

    fun isString(path: String): Boolean {
        return configuration.isString(path)
    }

    fun getInt(path: String): Int {
        return configuration.getInt(path)
    }

    fun getInt(path: String, def: Int): Int {
        return configuration.getInt(path, def)
    }

    fun isInt(path: String): Boolean {
        return configuration.isInt(path)
    }

    fun getBoolean(path: String): Boolean {
        return configuration.getBoolean(path)
    }

    fun getBoolean(path: String, def: Boolean): Boolean {
        return configuration.getBoolean(path, def)
    }

    fun isBoolean(path: String): Boolean {
        return configuration.isBoolean(path)
    }

    fun getDouble(path: String): Double {
        return configuration.getDouble(path)
    }

    fun getDouble(path: String, def: Double): Double {
        return configuration.getDouble(path, def)
    }

    fun isDouble(path: String): Boolean {
        return configuration.isDouble(path)
    }

    fun getLong(path: String): Long {
        return configuration.getLong(path)
    }

    fun getLong(path: String, def: Long): Long {
        return configuration.getLong(path, def)
    }

    fun isLong(path: String): Boolean {
        return configuration.isLong(path)
    }

    fun getList(path: String): List<*>? {
        return configuration.getList(path)
    }

    @Contract("_, !null -> !null")
    fun getList(path: String, def: List<*>?): List<*>? {
        return configuration.getList(path, def)
    }

    fun isList(path: String): Boolean {
        return configuration.isList(path)
    }

    fun getStringList(path: String): List<String> {
        return configuration.getStringList(path)
    }

    fun getIntegerList(path: String): List<Int> {
        return configuration.getIntegerList(path)
    }

    fun getBooleanList(path: String): List<Boolean> {
        return configuration.getBooleanList(path)
    }

    fun getDoubleList(path: String): List<Double> {
        return configuration.getDoubleList(path)
    }

    fun getFloatList(path: String): List<Float> {
        return configuration.getFloatList(path)
    }

    fun getLongList(path: String): List<Long> {
        return configuration.getLongList(path)
    }

    fun getByteList(path: String): List<Byte> {
        return configuration.getByteList(path)
    }

    fun getCharacterList(path: String): List<Char> {
        return configuration.getCharacterList(path)
    }

    fun getShortList(path: String): List<Short> {
        return configuration.getShortList(path)
    }

    fun getMapList(path: String): List<Map<*, *>> {
        return configuration.getMapList(path)
    }

    fun <T> getObject(path: String, clazz: Class<T>): T? {
        return configuration.getObject(path, clazz)
    }

    @Contract("_, _, !null -> !null")
    fun <T> getObject(path: String, clazz: Class<T>, def: T?): T? {
        return configuration.getObject(path, clazz, def)
    }

    fun <T : ConfigurationSerializable?> getSerializable(path: String, clazz: Class<T>): T? {
        return configuration.getSerializable(path, clazz)
    }

    @Contract("_, _, !null -> !null")
    fun <T : ConfigurationSerializable?> getSerializable(path: String, clazz: Class<T>, def: T?): T? {
        return configuration.getSerializable(path, clazz, def)
    }

    fun getVector(path: String): Vector? {
        return configuration.getVector(path)
    }

    @Contract("_, !null -> !null")
    fun getVector(path: String, def: Vector?): Vector? {
        return configuration.getVector(path, def)
    }

    fun isVector(path: String): Boolean {
        return configuration.isVector(path)
    }

    fun getOfflinePlayer(path: String): OfflinePlayer? {
        return configuration.getOfflinePlayer(path)
    }

    @Contract("_, !null -> !null")
    fun getOfflinePlayer(path: String, def: OfflinePlayer?): OfflinePlayer? {
        return configuration.getOfflinePlayer(path, def)
    }

    fun isOfflinePlayer(path: String): Boolean {
        return configuration.isOfflinePlayer(path)
    }

    fun getItemStack(path: String): ItemStack? {
        return configuration.getItemStack(path)
    }

    @Contract("_, !null -> !null")
    fun getItemStack(path: String, def: ItemStack?): ItemStack? {
        return configuration.getItemStack(path, def)
    }

    fun isItemStack(path: String): Boolean {
        return configuration.isItemStack(path)
    }

    fun getColor(path: String): Color? {
        return configuration.getColor(path)
    }

    @Contract("_, !null -> !null")
    fun getColor(path: String, def: Color?): Color? {
        return configuration.getColor(path, def)
    }

    fun isColor(path: String): Boolean {
        return configuration.isColor(path)
    }

    fun getLocation(path: String): Location? {
        return configuration.getLocation(path)
    }

    @Contract("_, !null -> !null")
    fun getLocation(path: String, def: Location?): Location? {
        return configuration.getLocation(path, def)
    }

    fun isLocation(path: String): Boolean {
        return configuration.isLocation(path)
    }

    fun getConfigurationSection(path: String): ConfigurationSection? {
        return configuration.getConfigurationSection(path)
    }

    fun isConfigurationSection(path: String): Boolean {
        return configuration.isConfigurationSection(path)
    }
}
