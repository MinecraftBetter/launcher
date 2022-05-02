package fr.minecraftbetter.launcher.utils.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.MessageFormat;
import java.util.function.Consumer;
import java.util.logging.Level;

/** A utility class to make HTTP requests **/
public class HTTP {
    private HTTP() {throw new IllegalStateException("Utility class");}

    public static Response get(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Main.logger.fine(() -> MessageFormat.format("GET request to {0}", request.url()));
        try {return client.newCall(request).execute();} catch (IOException e) {
            Main.logger.log(Level.SEVERE, "GET request error", e);
            return null;
        }
    }

    public static JsonElement getAsJSON(String url) {
        Response response = get(url);
        if (response == null) return null;
        ResponseBody body = response.body();
        if (body == null) return null;
        else return JsonParser.parseReader(body.charStream());
    }
    public static JsonObject getAsJSONObject(String url) {
        JsonElement json = getAsJSON(url);
        return json == null ? null : json.getAsJsonObject();
    }
    public static JsonArray getAsJSONArray(String url) {
        JsonElement json = getAsJSON(url);
        return json == null ? null : json.getAsJsonArray();
    }

    public static final int CHUNK_SIZE = 1024;

    public static void getFile(String url, OutputStream outputStream, Consumer<DownloadProgress> progress) throws IOException {
        Response response = get(url);
        assert response != null;
        ResponseBody responseBody = response.body();
        if (responseBody == null) throw new IllegalStateException("Response doesn't contain a file");

        try (BufferedInputStream input = new BufferedInputStream(responseBody.byteStream())) {
            byte[] dataBuffer = new byte[CHUNK_SIZE];
            int readBytes;
            long totalBytes = 0;
            while ((readBytes = input.read(dataBuffer)) != -1) {
                totalBytes += readBytes;
                outputStream.write(dataBuffer, 0, readBytes);
                if (progress != null) progress.accept(new DownloadProgress(totalBytes, responseBody.contentLength()));
            }
        }
    }

    @NotNull
    public static Boolean downloadFile(String url, File outputFile, Consumer<DownloadProgress> progress) {
        try {
            getFile(url, new FileOutputStream(outputFile), progress);
            Main.logger.fine(() -> MessageFormat.format("Saved to {0}", outputFile));
            return true;
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while downloading {0}", outputFile));
            return false;
        }
    }
}

