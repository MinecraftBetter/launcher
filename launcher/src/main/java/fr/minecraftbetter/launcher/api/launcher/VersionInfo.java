package fr.minecraftbetter.launcher.api.launcher;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class VersionInfo {
    private final String version_number;
    private final Date date;
    private final String changelog;
    private final String url;
    private final List<AssetInfo>  assets;

    public VersionInfo(String version_number, Date date, String changelog, String url, List<AssetInfo> assets) {
        this.version_number = version_number;
        this.date = date;
        this.changelog = changelog;
        this.url = url;
        this.assets = assets;
    }

    public String version_number() {return version_number;}

    public Date date() {return date;}

    public String changelog() {return changelog;}

    public String url() {return url;}

    public List<AssetInfo> assets() {return assets;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VersionInfo) obj;
        return Objects.equals(this.version_number, that.version_number) &&
                Objects.equals(this.date, that.date) &&
                Objects.equals(this.changelog, that.changelog) &&
                Objects.equals(this.url, that.url) &&
                Objects.equals(this.assets, that.assets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version_number, date, changelog, url, assets);
    }

    @Override
    public String toString() {
        return "VersionInfo[" +
                "version_number=" + version_number + ", " +
                "date=" + date + ", " +
                "changelog=" + changelog + ", " +
                "url=" + url + ", " +
                "assets=" + assets + ']';
    }
}
