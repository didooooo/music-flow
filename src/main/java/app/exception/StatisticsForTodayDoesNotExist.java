package app.exception;

public class StatisticsForTodayDoesNotExist extends RuntimeException{
    public StatisticsForTodayDoesNotExist(String message) {
        super(message);
    }
}
