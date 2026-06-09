package br.com.effies.laboris.backend.domain.utils;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TimeEntryBuilder {

    private TimeEntryType type;
    private Instant timestamp;
    private UUID payrollId;
    private User user;
    private Job job;

    public static TimeEntryBuilder aTimeEntry(){
        return new TimeEntryBuilder();
    }

    public TimeEntryBuilder withClockIn(){
        this.type = TimeEntryType.IN;
        return this;
    }

    public TimeEntryBuilder withClockOut(){
        this.type = TimeEntryType.OUT;
        return this;
    }

    public TimeEntryBuilder withNoEntry(){
        this.type = null;
        return this;
    }

    public TimeEntryBuilder atTime(int hours){
        mountTimestamp(hours);
        return this;
    }

    public TimeEntryBuilder withJob(Job job){
        this.job = job;
        return this;
    }

    public TimeEntryBuilder forUser(User user){
        this.user = user;
        return this;
    }

    public TimeEntryBuilder withStatusPaid(){
        this.payrollId = UUID.randomUUID();
        return this;
    }

    public TimeEntryBuilder withStatusOpen(){
        this.payrollId = null;
        return this;
    }

    public TimeEntry build(){
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEntryTimestamp(this.timestamp);
        timeEntry.setEntryType(this.type);
        timeEntry.setEmployee(user);
        timeEntry.setJob(this.job);
        timeEntry.setPayrollId(this.payrollId);

        return timeEntry;
    }

    private void mountTimestamp(long duration){
        LocalDate workDate = LocalDate.now();
        Instant firstHourOfDay = workDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        this.timestamp = firstHourOfDay.plus(duration, ChronoUnit.HOURS);
    }
}
