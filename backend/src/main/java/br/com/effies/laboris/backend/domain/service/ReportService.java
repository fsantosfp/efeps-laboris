package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.helper.TimeEntryCalculationHelper;
import br.com.effies.laboris.backend.domain.model.DailyShift;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.presentation.dto.response.JobCostResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final JobRepository jobRepository;
    private final TimeEntryRepository timeEntryRepository;

    public ReportService(JobRepository jobRepository, TimeEntryRepository timeEntryRepository){
        this.jobRepository = jobRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    public JobCostResponseDto calculateJobCostReport(User manager, UUID jobId, Instant start, Instant end){
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + jobId));

        job.ensureBelongsTo(manager);

        List<TimeEntry> entries = timeEntryRepository.findAllByJobIdAndPeriod(jobId, start, end);

        List<DailyShift> allShifts = processEntriesIntoShifts(entries);

        Map<String, List<DailyShift>> shiftsByGroup = allShifts.stream()
            .collect(Collectors.groupingBy(shift ->
                shift.date() + "|" + shift.startTime() + "|" + shift.endTime()
            ));

        List<JobCostResponseDto.DailyBreakdownDto> dailyBreakdown = new ArrayList<>();
        BigDecimal grandTotalAmount = BigDecimal.ZERO;
        BigDecimal grandTotalHours = BigDecimal.ZERO;

        for(List<DailyShift> shiftGroup : shiftsByGroup.values()){
            if(shiftGroup.isEmpty()) continue;

            DailyShift representativeShift = shiftGroup.getFirst();
            int employeeCount = shiftGroup.size();

            BigDecimal hoursWorkedForShift = representativeShift.hoursWorked();
            BigDecimal amountToBillingForGroup = job.getBillingRate()
                .multiply(hoursWorkedForShift)
                .multiply(BigDecimal.valueOf(employeeCount))
                .setScale(2, RoundingMode.HALF_UP);

            dailyBreakdown.add(JobCostResponseDto.DailyBreakdownDto.builder()
                .date(representativeShift.date())
                .hoursWorked(representativeShift.hoursWorked())
                .start(representativeShift.startTime())
                .end(representativeShift.endTime())
                .employeesCount(employeeCount)
                .amountToBill(amountToBillingForGroup)
                .build());

            BigDecimal totalHoursForGroup = hoursWorkedForShift.multiply(BigDecimal.valueOf(employeeCount));
            grandTotalHours = grandTotalHours.add(totalHoursForGroup);
            grandTotalAmount = grandTotalAmount.add(amountToBillingForGroup);
        }

        dailyBreakdown.sort(Comparator.comparing(JobCostResponseDto.DailyBreakdownDto::getDate)
            .thenComparing(JobCostResponseDto.DailyBreakdownDto::getStart));

        return JobCostResponseDto.builder()
            .jobInfo(JobCostResponseDto.JobInfo.builder()
                .jobId(jobId)
                .address(job.getAddress())
                .clientName(job.getClientName())
                .billingRate(job.getBillingRate())
                .build())
            .periodTotals(JobCostResponseDto.PeriodSummary.builder()
                .totalAmount(grandTotalAmount)
                .totalHours(grandTotalHours.setScale(2, RoundingMode.HALF_UP))
                .build())
            .dailyBreakdown(dailyBreakdown)
            .build();
    }

    private List<DailyShift> processEntriesIntoShifts(List<TimeEntry> allEntries){
        Map<User, List<TimeEntry>> entriesByEmployee = allEntries.stream().collect(Collectors.groupingBy(TimeEntry::getEmployee));
        List<DailyShift> shifts = new ArrayList<>();

        for(List<TimeEntry> employeeEntries : entriesByEmployee.values()){
            employeeEntries.sort(Comparator.comparing(TimeEntry::getEntryTimestamp));

            Instant clockInTime = null;
            List<TimeEntry> currentShiftEntries = new ArrayList<>();

            for (TimeEntry entry : employeeEntries){

                if(entry.getEntryType() == TimeEntryType.CLOCK_IN){
                    clockInTime = entry.getEntryTimestamp();
                    currentShiftEntries.clear();
                }

                if(clockInTime != null){
                    currentShiftEntries.add(entry);
                }

                if(entry.getEntryType() == TimeEntryType.CLOCK_OUT && clockInTime != null) {
                    BigDecimal hoursWorked = TimeEntryCalculationHelper.calculateHoursWorked(currentShiftEntries);

                    LocalDate date = clockInTime.atZone(ZoneOffset.UTC).toLocalDate();
                    LocalTime startTime = clockInTime.atZone(ZoneOffset.UTC).toLocalTime().truncatedTo(ChronoUnit.MINUTES);
                    LocalTime endTime = entry.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalTime().truncatedTo(ChronoUnit.MINUTES);

                    shifts.add(new DailyShift(date, startTime, endTime, hoursWorked, entry.getEmployee()));
                    clockInTime = null;
                }
            }
        }

        return shifts;
    }
}
