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

        for( TimeEntry entry : entries) {

            switch (entry.getEntryType()) {
                case IN -> {
                    startWork = entry.getEntryTimestamp();
                }
                case OUT -> {
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
        double oneHourInSecond = 3600.0;
        return BigDecimal.valueOf(seconds / oneHourInSecond).setScale(2, RoundingMode.HALF_UP);
    }
}
