package me.fourteendoggo.xcore.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.fourteendoggo.xcore.user.Home;
import me.fourteendoggo.xcore.user.User;
import me.fourteendoggo.xcore.user.UserData;
import me.fourteendoggo.xcore.utils.Settings;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private final MongoClient client;
    private final MongoCollection<Document> users;

    public Database() {
        this.client = MongoClients.create(Settings.getMongoHost());
        MongoDatabase database = client.getDatabase("xcore");
        MongoCollection<Document> users = database.getCollection("users");
        if (users == null) {
            database.createCollection("users");
            users = database.getCollection("users");
        }
        this.users = users;
    }

    public void close() {
        client.close();
    }

    public User loadUserBlocking(UUID id) {
        Document document = users.find(Filters.eq("_id", id)).first();
        if (document == null) {
            return new User(id);
        }

        UserData data = new UserData();
        List<Document> homes = document.getList("homes", Document.class);
        for (Document homeDocument : homes) {
            String name = homeDocument.getString("name");
            UUID worldId = homeDocument.get("world", UUID.class);
            double x = homeDocument.getDouble("x");
            double y = homeDocument.getDouble("y");
            double z = homeDocument.getDouble("z");
            float yaw = homeDocument.getDouble("yaw").floatValue();
            float pitch = homeDocument.getDouble("pitch").floatValue();

            Location location = new Location(Bukkit.getWorld(worldId), x, y, z, yaw, pitch);

            data.addHome(new Home(name, id, location));
        }

        return new User(id, data);
    }

    public void saveUser(User user) {
        Document userDocument = new Document("_id", user.getUniqueId());
        List<Document> homes = new ArrayList<>();
        for (Home home : user.getData().getHomes()) {
            Location loc = home.location();
            // the world couldn't be unloaded on runtime, meaning this is an invalid home loaded from the database, let the user delete it
            // let's not save these again because calling World::getUID gives a NullPointerException
            if (!loc.isWorldLoaded()) continue;

            Document homeDocument = new Document()
                    .append("name", home.name())
                    .append("world", loc.getWorld().getUID())
                    .append("x", loc.getX())
                    .append("y", loc.getY())
                    .append("z", loc.getZ())
                    .append("yaw", loc.getYaw())
                    .append("pitch", loc.getPitch());

            homes.add(homeDocument);
        }
        userDocument.put("homes", homes);
        Bson filter = Filters.eq("_id", user.getUniqueId());
        users.replaceOne(filter, userDocument, new ReplaceOptions().upsert(true));
    }

    public void deleteHome(Home home) {
        users.updateOne(Filters.eq("_id", home.owner()), new Document("$pull", new Document("homes", new Document("name", home.name()))));
    }
}
