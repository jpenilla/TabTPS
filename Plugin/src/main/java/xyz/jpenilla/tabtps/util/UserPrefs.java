package xyz.jpenilla.tabtps.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public class UserPrefs {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser jsonParser = new JsonParser();

    @Getter private final ArrayList<UUID> tabEnabled = new ArrayList<>();
    @Getter private final ArrayList<UUID> actionBarEnabled = new ArrayList<>();

    public static UserPrefs deserialize(File json) throws FileNotFoundException {
        JsonObject jsonObject = jsonParser.parse(new FileReader(json)).getAsJsonObject();
        return gson.fromJson(jsonObject, UserPrefs.class);
    }

    public void serialize(FileWriter writer) throws IOException {
        writer.write(gson.toJson(this));
        writer.flush();
        writer.close();
    }
}
