package fr.minecraftbetter.launcher.utils.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.installer.Utils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/** A utility class to make HTTP requests **/
public final class HTTP {
    private HTTP() {throw new IllegalStateException("Utility class");}

    private static Response get(String url, int retry) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(url).build();
        Main.logger.fine(() -> MessageFormat.format("GET request to {0}", request.url()));
        try {return client.newCall(request).execute();} catch (IOException e) {
            Main.logger.log(Level.SEVERE, "GET request error", e);
            return retry <= 1 ? null : get(url, retry - 1);
        }
    }

    public static JsonElement getAsJSON(String url) {
        Response response = get(url, 3);
        if (response == null) return null;
        ResponseBody body = response.body();
        if (body == null) return null;
        else return JsonParser.parseReader(body.charStream());
    }

    public static JsonObject getAsJSONObject(String url) {
        JsonElement json = getAsJSON(url);
        if (json == null || !json.isJsonObject()) return null;
        return json.getAsJsonObject();
    }

    public static JsonArray getAsJSONArray(String url) {
        JsonElement json = getAsJSON(url);
        if (json == null || !json.isJsonArray()) return null;
        return json.getAsJsonArray();
    }

    public static final int CHUNK_SIZE = 1024;

    public static void getFile(String url, OutputStream outputStream, Consumer<DownloadProgress> progress) throws IOException {
        Response response = get(url, 3);
        assert response != null;
        if(!response.isSuccessful()) throw new RemoteException("Response wasn't successful");
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
            Utils.tryCreateFolder(outputFile.toPath().getParent());
            getFile(url, new FileOutputStream(outputFile), progress);
            Main.logger.fine(() -> MessageFormat.format("Saved to {0}", outputFile));
            return true;
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while downloading {0} to {1}", url, outputFile));
            return false;
        }
    }
}

