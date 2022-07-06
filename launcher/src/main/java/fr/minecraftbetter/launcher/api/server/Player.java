package fr.minecraftbetter.launcher.api.server;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class Player {
    private final String name;
    private final String head;

    public Player(String name, String head) {
        this.name = name;
        this.head = head;
    }

    public String name() {return name;}

    public String head() {return head;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Player) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.head, that.head);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, head);
    }

    @Override
    public String toString() {
        return "Player[" +
                "name=" + name + ", " +
                "head=" + head + ']';
    }
}
