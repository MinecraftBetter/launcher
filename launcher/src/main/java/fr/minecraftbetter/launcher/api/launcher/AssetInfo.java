package fr.minecraftbetter.launcher.api.launcher;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class AssetInfo {
    private final String name;
    private final int size;
    private final int download_count;
    private final String url;

    public AssetInfo(String name, int size, int download_count, String url) {
        this.name = name;
        this.size = size;
        this.download_count = download_count;
        this.url = url;
    }

    public String name() {return name;}

    public int size() {return size;}

    public int download_count() {return download_count;}

    public String url() {return url;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AssetInfo) obj;
        return Objects.equals(this.name, that.name) &&
                this.size == that.size &&
                this.download_count == that.download_count &&
                Objects.equals(this.url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size, download_count, url);
    }

    @Override
    public String toString() {
        return "AssetInfo[" +
                "name=" + name + ", " +
                "size=" + size + ", " +
                "download_count=" + download_count + ", " +
                "url=" + url + ']';
    }
}
