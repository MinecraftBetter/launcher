package fr.minecraftbetter.launcher.utils.http;

import java.text.MessageFormat;

public class DownloadProgress {
    private final long downloaded;
    private final long total;
    private final double percentage;

    public DownloadProgress(long downloaded, long total) {
        this.downloaded = downloaded;
        this.total = total;
        percentage = total != 0 ? downloaded / (double) total : -1;
    }

    public DownloadProgress() {
        downloaded = -1;
        total = -1;
        percentage = -1;
    }

    public double getDownloaded() {return downloaded;}

    public double getTotal() {return total;}

    public double getPercentage() {return percentage;}

    public String getPercentageToString() {return percentage < 0 ? "~%" : percentage + "%";}

    public String toString() {
        if (total < 0 && downloaded < 0) return "~";
        if (total < 0) return humanReadableSize(downloaded);
        return MessageFormat.format("{0} / {1}", humanReadableSize(downloaded), humanReadableSize(total));
    }

    private String humanReadableSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        //noinspection SpellCheckingInspection
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
}
