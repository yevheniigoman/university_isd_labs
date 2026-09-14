import java.time.Duration;
import java.time.LocalTime;

public class Training {
    private LocalTime time;
    private double distanceKm;
    private Duration duration;

    public Training() {
    }

    public Training(LocalTime time, double distanceKm, Duration duration) {
        this.time = time;
        this.distanceKm = distanceKm;
        this.duration = duration;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Training{" +
                "time=" + time +
                ", distanceKm=" + distanceKm +
                ", duration=" + duration +
                '}';
    }
}