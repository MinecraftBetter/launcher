package fr.minecraftbetter.launcher.api.server;

public final class Player {
    public final String name;
    public final String head;

    public Player() {
        name = "";
        head = "";
    }

    public Player(String name, String head) {
        this.name = name;
        this.head = head;
    }
}
