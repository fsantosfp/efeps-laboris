package br.com.effies.laboris.backend.domain.helper;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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

    public static BigDecimal calculateIntervalHoursForJob(List<TimeEntry> allEmployeeDailyEntries, UUID jobId) {
        if (allEmployeeDailyEntries == null || allEmployeeDailyEntries.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        List<TimeEntry> sorted = allEmployeeDailyEntries.stream()
                .sorted(Comparator.comparing(TimeEntry::getEntryTimestamp))
                .toList();

        long totalSeconds = 0;
        TimeEntry lastOut = null;

        for (TimeEntry entry : sorted) {
            if (entry.getEntryType() == TimeEntryType.OUT) {
                lastOut = entry;
            } else if (entry.getEntryType() == TimeEntryType.IN) {
                if (lastOut != null) {
                    if (lastOut.getJob().getId().equals(jobId) && entry.getJob().getId().equals(jobId)) {
                        totalSeconds += Duration.between(lastOut.getEntryTimestamp(), entry.getEntryTimestamp()).getSeconds();
                    }
                    lastOut = null;
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
