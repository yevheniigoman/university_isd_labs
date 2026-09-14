import java.time.Duration;
import java.time.LocalDate;

public class Training {
    private LocalDate date;
    private double distanceKm;
    private Duration duration;

    public Training() {
    }

    public Training(LocalDate date, double distanceKm, Duration duration) {
        this.date = date;
        this.distanceKm = distanceKm;
        this.duration = duration;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
                "date=" + date +
                ", distanceKm=" + distanceKm +
                ", duration=" + duration +
                '}';
    }
}
