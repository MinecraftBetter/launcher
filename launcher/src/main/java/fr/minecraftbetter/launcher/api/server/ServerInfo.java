package fr.minecraftbetter.launcher.api.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class ServerInfo {
    public static final String API_URL = "https://api.justbetter.fr/minecraftbetter/server/info";

    private final String version;
    private final String description;
    private final int players_max;
    private final int players_online;
    private final String icon;
    private final List<Player> players;

    public ServerInfo(String version, String description, int players_max, int players_online, String icon,
                      List<Player> players) {
        this.version = version;
        this.description = description;
        this.players_max = players_max;
        this.players_online = players_online;
        this.icon = icon;
        this.players = players;
    }

    public static ServerInfo tryGet() {
        JsonObject response = HTTP.getAsJSONObject(API_URL);
        if (response == null) return null;
        JsonObject results = response.getAsJsonObject("results");
        if (results == null) return null;
        return new Gson().fromJson(results, ServerInfo.class);
    }

    public String version() {return version;}

    public String description() {return description;}

    public int players_max() {return players_max;}

    public int players_online() {return players_online;}

    public String icon() {return icon;}

    public List<Player> players() {return players;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ServerInfo) obj;
        return Objects.equals(this.version, that.version) &&
                Objects.equals(this.description, that.description) &&
                this.players_max == that.players_max &&
                this.players_online == that.players_online &&
                Objects.equals(this.icon, that.icon) &&
                Objects.equals(this.players, that.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, description, players_max, players_online, icon, players);
    }

    @Override
    public String toString() {
        return "ServerInfo[" +
                "version=" + version + ", " +
                "description=" + description + ", " +
                "players_max=" + players_max + ", " +
                "players_online=" + players_online + ", " +
                "icon=" + icon + ", " +
                "players=" + players + ']';
    }


}

