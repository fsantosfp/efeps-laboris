package br.com.effies.laboris.backend.domain.helper;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TimeEntryCalculationHelper {

    public static BigDecimal calculateHoursWorked(List<TimeEntry> entries){
        long totalSeconds = 0;
        Instant startWork = null;
        Instant startBreak = null;

        for( TimeEntry entry : entries) {
            switch (entry.getEntryType()) {
                case CLOCK_IN -> {
                    startWork = entry.getEntryTimestamp();
                }
                case START_BREAK -> {
                    if (startWork != null) {
                        totalSeconds += Duration.between(startWork, entry.getEntryTimestamp()).getSeconds();
                        startWork = null;
                    }
                    startBreak = entry.getEntryTimestamp();
                }
                case END_BREAK -> {
                    if (startBreak != null) {
                        startWork = entry.getEntryTimestamp();
                        startBreak = null;
                    }
                }
                case CLOCK_OUT -> {
                    if (startWork != null) {
                        totalSeconds += Duration.between(startWork, entry.getEntryTimestamp()).getSeconds();
                        startWork = null;
                    }
                }
            }
        }

        return convertToHours(totalSeconds);
    }

    private static BigDecimal convertToHours(long seconds){
        long oneHourInSecond = 3600;
        return BigDecimal.valueOf(seconds / oneHourInSecond).setScale(2, RoundingMode.HALF_UP);
    }
}
