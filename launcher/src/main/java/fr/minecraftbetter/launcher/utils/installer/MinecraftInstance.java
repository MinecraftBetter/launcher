package fr.minecraftbetter.launcher.utils.installer;

import javafx.application.Platform;
import java.util.function.BiConsumer;

public class MinecraftInstance {

    public enum StartStatus {STARTED, ERROR, INCOMPLETE_INSTALL, EXITED, UNKNOWN}

    StartStatus status;
    public StartStatus getStatus() { return status; }

    Process process;
    public Process getProcess() { return process; }

    public void onExit(BiConsumer<Boolean, Process> onexit) { process.onExit().thenAccept(p -> Platform.runLater(() -> onexit.accept(p.exitValue() != 0, p))); }

    public MinecraftInstance(StartStatus status){ this.status = status; }
    public MinecraftInstance(StartStatus status, Process process){ this.status = status; this.process = process; }

}
