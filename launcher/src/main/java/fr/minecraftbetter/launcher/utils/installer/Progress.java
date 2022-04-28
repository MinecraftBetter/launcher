package fr.minecraftbetter.launcher.utils.installer;

public class Progress {
    private final double percentage;
    private final String status;

    public Progress(double percentage, String status) {
        this.percentage = percentage;
        this.status = status;
    }

    public Progress(double percentage, int action, int total, String status) {
        this.percentage = (action + percentage) / total;
        this.status = status;
    }

    public double getPercentage() {return percentage;}

    public String getStatus() {return status;}
}
