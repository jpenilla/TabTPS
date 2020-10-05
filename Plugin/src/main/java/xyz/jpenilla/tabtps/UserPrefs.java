package xyz.jpenilla.tabtps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class UserPrefs {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser jsonParser = new JsonParser();

    @Getter private final Collection<UUID> tabEnabled = new HashSet<>();
    @Getter private final Collection<UUID> actionBarEnabled = new HashSet<>();

    public void serialize(FileWriter writer) throws IOException {
        writer.write(gson.toJson(this));
        writer.flush();
        writer.close();
    }

    public static UserPrefs deserialize(File json) throws FileNotFoundException {
        JsonObject jsonObject = jsonParser.parse(new FileReader(json)).getAsJsonObject();
        return gson.fromJson(jsonObject, UserPrefs.class);
    }
}
