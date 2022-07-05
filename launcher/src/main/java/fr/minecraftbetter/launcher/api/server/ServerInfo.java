package fr.minecraftbetter.launcher.api.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.util.ArrayList;
import java.util.List;

public class ServerInfo {
    public static final String API_URL = "https://api.minecraftbetter.com/minecraftbetter/server/info";

    public ServerInfo() {
        JsonObject response = HTTP.getAsJSONObject(API_URL);
        assert response != null;
        JsonObject results = response.getAsJsonObject("results");
        if (results == null) {
            version = null;
            description = null;
            playersMax = -1;
            playersOnline = -1;
            icon = null;
            players = new ArrayList<>();
            return;
        }

        version = results.get("version").getAsString();
        description = results.get("description").getAsString();
        playersMax = results.get("players_max").getAsInt();
        playersOnline = results.get("players_online").getAsInt();
        icon = results.get("icon").getAsString();

        JsonArray playersA = results.get("players").getAsJsonArray();
        players = new ArrayList<>();
        for (JsonElement playerE : playersA) {
            JsonObject player = playerE.getAsJsonObject();
            players.add(new Player(player.get("name").getAsString(), player.get("head").getAsString()));
        }
    }

    public final String version;
    public final String description;
    public final int playersMax;
    public final int playersOnline;
    public final String icon;
    public final List<Player> players;
}

