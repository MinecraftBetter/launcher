package fr.minecraftbetter.launcher.utils.http;

import java.io.File;
import java.util.function.Consumer;

public class DownloadTask {
    private double progress;

    public double getProgress() {return progress;}

    private final String url;
    private final File outputFile;
    private final String taskName;

    public File getOutputFile() {return outputFile;}

    public String getUrl() {return url;}

    public String getTaskName() {return taskName;}


    public DownloadTask(String url, File outputFile, String taskName) {
        this.url = url;
        this.outputFile = outputFile;
        this.taskName = taskName;
        this.progress = 0;
    }

    private Consumer<Boolean> done;

    public void setDone(Consumer<Boolean> done) {this.done = done;}

    public void start() {
        boolean success = HTTP.downloadFile(url, outputFile, p -> progress = p.getPercentage());
        if (done != null) done.accept(success);
    }
}
