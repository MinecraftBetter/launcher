package fr.minecraftbetter.launcher.utils.news;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class News {

    public News(String title, String description, Date date) {
        _title = title;
        _description = description;
        _date = date;
    }

    String _title;
    Date _date;
    String _description;

    public String getTitle() {return _title;}

    public String getDescription() {return _description;}

    public Date getDate() {return _date;}


    public static List<News> getNews(String apiUrl) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JsonObject response = HTTP.getAsJSONObject(apiUrl);
        assert response != null;
        JsonArray results = response.getAsJsonArray("results");

        ArrayList<News> news = new ArrayList<>();
        for (JsonElement resultE : results) {
            JsonObject result = resultE.getAsJsonObject();
            try {
                news.add(new News(result.get("title").getAsString(), result.get("description").getAsString(), dateFormat.parse(result.get("date").getAsString())));
            } catch (Exception e) {Main.logger.log(Level.SEVERE, "Error parsing the news", e);}
        }
        return news.stream().sorted(Comparator.comparing(n -> ((News)n).getDate()).reversed()).toList();
    }
}
