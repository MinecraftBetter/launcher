package fr.minecraftbetter.launcher.utils.http;

import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.Settings;
import fr.minecraftbetter.launcher.utils.installer.Progress;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ConcurrentDownloader {
    public ConcurrentDownloader() {
        int concurrentTasks = Settings.getSettings().concurrentDownloads;
        currentTasks = new DownloadThread[concurrentTasks];
    }

    private final List<DownloadTask> tasks = new ArrayList<>();
    List<DownloadTask> remainingTasks = new ArrayList<>();
    DownloadThread[] currentTasks;

    public void addTask(DownloadTask task) {
        tasks.add(task);
        remainingTasks.add(task);
    }

    Consumer<Progress> progress;

    public void onProgress(Consumer<Progress> progress) {this.progress = progress;}

    public Thread thread() {
        return new Thread(() -> {
            for (int i = 0; i < currentTasks.length; i++) {
                int finalI = i;
                currentTasks[finalI] = new DownloadThread(() -> {
                    if (remainingTasks.isEmpty()) {
                        currentTasks[finalI] = null;
                        return null;
                    }
                    DownloadTask nextTask = remainingTasks.get(0);
                    remainingTasks.remove(nextTask);
                    return nextTask;
                }, finalI);
                currentTasks[finalI].start();
            }
            synchronized (this) {
                try {
                    while (Arrays.stream(currentTasks).anyMatch(Objects::nonNull)) {
                        double threadProgress = 0;
                        for (var thread : currentTasks)
                            if (thread != null) threadProgress += thread.getProgress();

                        int completedTasks = tasks.size() - remainingTasks.size();
                        if (progress != null) progress.accept(new Progress((completedTasks + threadProgress) / tasks.size(), completedTasks + " / " + tasks.size()));
                        wait(1000);
                    }
                } catch (InterruptedException ignored) {Thread.currentThread().interrupt();}
            }
        });
    }

    static final class DownloadThread {
        private final int index;
        private final Callable<DownloadTask> getNext;

        DownloadThread(Callable<DownloadTask> getNext, int index) {
            this.getNext = getNext;
            this.index = index;
        }

        DownloadTask task;

        public double getProgress() {return task == null ? 0 : task.getProgress();}

        public void start() {
            try {
                task = getNext.call();
                if (task == null) return;
                Main.logger.fine(() -> MessageFormat.format("START DOWNLOAD THREAD {0} {1}", index, task.getTaskName()));
                task.setDone(success -> {
                    if (Boolean.TRUE.equals(success))
                        Main.logger.fine(() -> MessageFormat.format("DOWNLOAD THREAD {0} SUCCEED {1}", index, task == null ? "" : task.getTaskName()));
                    else Main.logger.warning(() -> MessageFormat.format("DOWNLOAD THREAD {0} ERRORED {1}", index, task == null ? "" : task.getTaskName()));
                    start();
                });
                new Thread(task::start).start();
            } catch (Exception e) {Main.logger.log(Level.WARNING, "Failed to get next download task (thread " + index + ")", e);}
        }
    }
}