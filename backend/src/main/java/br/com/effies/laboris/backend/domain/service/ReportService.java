package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.entity.Displacement;
import br.com.effies.laboris.backend.domain.helper.TimeEntryCalculationHelper;
import br.com.effies.laboris.backend.domain.model.DailyShift;
import br.com.effies.laboris.backend.domain.repository.DisplacementRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import java.time.Duration;
import br.com.effies.laboris.backend.presentation.dto.response.JobCostResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.JobTimesheetResponseDto;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportService {

    private final JobRepository jobRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final DisplacementRepository displacementRepository;
    private final UserRepository userRepository;

    public ReportService(
            JobRepository jobRepository,
            TimeEntryRepository timeEntryRepository,
            DisplacementRepository displacementRepository,
            UserRepository userRepository
    ){
        this.jobRepository = jobRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.displacementRepository = displacementRepository;
        this.userRepository = userRepository;
    }

    public JobCostResponseDto calculateJobCostReport(User manager, UUID jobId, Instant start, Instant end){
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + jobId));

        job.ensureBelongsTo(manager);

        List<TimeEntry> entries = timeEntryRepository.findAllByJobIdAndPeriod(jobId, start, end);
        List<Displacement> displacements = displacementRepository.findAllByDestinationJobIdAndPeriod(jobId, start, end);

        List<DailyShift> allShifts = processEntriesIntoShifts(entries, displacements);

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
            BigDecimal billingRate = job.getBillingRate() != null ? job.getBillingRate() : BigDecimal.ZERO;
            BigDecimal amountToBillingForGroup = billingRate
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

    private List<DailyShift> processEntriesIntoShifts(List<TimeEntry> allEntries, List<Displacement> displacements){
        Map<User, Map<LocalDate, List<TimeEntry>>> entriesByUserAndDay = groupEntriesByUserAndDay(allEntries);

        Map<User, Map<LocalDate, List<Displacement>>> displacementsByUserAndDay = displacements.stream()
            .collect(Collectors.groupingBy(
                Displacement::getUser,
                Collectors.groupingBy(d -> d.getStartTimestamp()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                )
            ));

        return entriesByUserAndDay.entrySet().stream()
            .flatMap(userEntry -> {
                User employee = userEntry.getKey();
                Map<LocalDate, List<TimeEntry>> dailyEntries = userEntry.getValue();
                Map<LocalDate, List<Displacement>> userDailyDisplacements = displacementsByUserAndDay.getOrDefault(employee, Map.of());

                return dailyEntries.entrySet().stream()
                    .flatMap(dailyEntry -> {
                        LocalDate date = dailyEntry.getKey();
                        List<TimeEntry> dayEntries = dailyEntry.getValue();
                        List<Displacement> dayDisplacements = userDailyDisplacements.getOrDefault(date, List.of());
                        return createShiftFromDailyEntries(employee, date, dayEntries, dayDisplacements);
                    });
            })
            .collect(Collectors.toList());
    }

    private Map<User, Map<LocalDate, List<TimeEntry>>> groupEntriesByUserAndDay(List<TimeEntry> allEntries){
        return allEntries.stream()
            .collect(Collectors.groupingBy(
                TimeEntry::getEmployee,
                Collectors.groupingBy( entry -> entry.getEntryTimestamp()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                )
            ));
    }

    private Stream<DailyShift> createShiftFromDailyEntries(User employee, LocalDate date, List<TimeEntry> dailyEntries, List<Displacement> dayDisplacements){
        Optional<TimeEntry> firstIn = dailyEntries.stream()
            .filter(entry -> entry.getEntryType() == TimeEntryType.IN)
            .min(Comparator.comparing(TimeEntry::getEntryTimestamp));

        Optional<TimeEntry> lastOut = dailyEntries.stream()
            .filter(entry -> entry.getEntryType() == TimeEntryType.OUT)
            .max(Comparator.comparing(TimeEntry::getEntryTimestamp));

        if( firstIn.isPresent() && lastOut.isPresent()){
            TimeEntry in = firstIn.get();
            TimeEntry out = lastOut.get();

            if(out.getEntryTimestamp().isAfter(in.getEntryTimestamp())){
                LocalTime startTime = in.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalTime()
                    .truncatedTo(ChronoUnit.MINUTES);

                LocalTime endTime = out.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalTime()
                    .truncatedTo(ChronoUnit.MINUTES);

                BigDecimal hoursWorked = TimeEntryCalculationHelper.calculateHoursWorked(dailyEntries);

                BigDecimal displacementHours = BigDecimal.ZERO;
                for (Displacement d : dayDisplacements) {
                    if (d.getEndTimestamp() != null) {
                        long seconds = Duration.between(d.getStartTimestamp(), d.getEndTimestamp()).getSeconds();
                        displacementHours = displacementHours.add(BigDecimal.valueOf(seconds / 3600.0));
                    }
                }
                displacementHours = displacementHours.setScale(2, RoundingMode.HALF_UP);
                hoursWorked = hoursWorked.add(displacementHours);

                DailyShift shift = new DailyShift(date, startTime, endTime, hoursWorked, employee);

                return Stream.of(shift);
            }
        }

        return Stream.empty();
    }

    public JobTimesheetResponseDto calculateJobTimesheetReport(User manager, UUID jobId, Instant start, Instant end) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + jobId));
        job.ensureBelongsTo(manager);

        List<TimeEntry> entries;
        if (start != null && end != null) {
            entries = timeEntryRepository.findAllByJobIdAndPeriod(jobId, start, end);
        } else {
            entries = timeEntryRepository.findAllByJobIdOrderByEntryTimestampAsc(jobId);
        }

        Instant periodStart = start;
        Instant periodEnd = end;
        if (periodStart == null || periodEnd == null) {
            Optional<Instant> minTime = entries.stream().map(TimeEntry::getEntryTimestamp).min(Comparator.naturalOrder());
            Optional<Instant> maxTime = entries.stream().map(TimeEntry::getEntryTimestamp).max(Comparator.naturalOrder());
            if (minTime.isPresent() && maxTime.isPresent()) {
                periodStart = minTime.get().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
                periodEnd = maxTime.get().atZone(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            } else {
                periodStart = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
                periodEnd = Instant.now();
            }
        }

        Map<User, List<TimeEntry>> entriesByUser = entries.stream()
            .collect(Collectors.groupingBy(TimeEntry::getEmployee));

        List<JobTimesheetResponseDto.EmployeeTimesheetDto> employeeTimesheets = new ArrayList<>();

        for (Map.Entry<User, List<TimeEntry>> userEntry : entriesByUser.entrySet()) {
            User employee = userEntry.getKey();
            List<TimeEntry> userEntries = userEntry.getValue();

            List<TimeEntry> allEmployeeEntries = timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), periodStart, periodEnd);
            List<Displacement> userDisplacements = displacementRepository.findAllByUserIdAndDestinationJobIdAndPeriod(employee.getId(), jobId, periodStart, periodEnd);

            Map<LocalDate, List<TimeEntry>> entriesByDay = userEntries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getEntryTimestamp()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                ));

            List<JobTimesheetResponseDto.DailyHoursDto> dailyHours = new ArrayList<>();
            for (Map.Entry<LocalDate, List<TimeEntry>> dayEntry : entriesByDay.entrySet()) {
                LocalDate date = dayEntry.getKey();
                List<TimeEntry> dayEntries = dayEntry.getValue();

                Optional<TimeEntry> firstIn = dayEntries.stream()
                    .filter(entry -> entry.getEntryType() == TimeEntryType.IN)
                    .min(Comparator.comparing(TimeEntry::getEntryTimestamp));

                Optional<TimeEntry> lastOut = dayEntries.stream()
                    .filter(entry -> entry.getEntryType() == TimeEntryType.OUT)
                    .max(Comparator.comparing(TimeEntry::getEntryTimestamp));

                LocalTime startLocalTime = null;
                LocalTime endLocalTime = null;

                if (firstIn.isPresent() && lastOut.isPresent()) {
                    TimeEntry in = firstIn.get();
                    TimeEntry out = lastOut.get();
                    if (out.getEntryTimestamp().isAfter(in.getEntryTimestamp())) {
                        startLocalTime = in.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalTime()
                            .truncatedTo(ChronoUnit.MINUTES);
                        endLocalTime = out.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalTime()
                            .truncatedTo(ChronoUnit.MINUTES);
                    }
                }

                List<TimeEntry> dayAllEntries = allEmployeeEntries.stream()
                    .filter(entry -> entry.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalDate().equals(date))
                    .toList();

                List<Displacement> dayDisplacements = userDisplacements.stream()
                    .filter(d -> d.getStartTimestamp().atZone(ZoneOffset.UTC).toLocalDate().equals(date))
                    .toList();

                BigDecimal hoursWorked = TimeEntryCalculationHelper.calculateHoursWorked(dayEntries);

                BigDecimal displacementHours = BigDecimal.ZERO;
                for (Displacement d : dayDisplacements) {
                    if (d.getEndTimestamp() != null) {
                        long seconds = Duration.between(d.getStartTimestamp(), d.getEndTimestamp()).getSeconds();
                        displacementHours = displacementHours.add(BigDecimal.valueOf(seconds / 3600.0));
                    }
                }
                displacementHours = displacementHours.setScale(2, RoundingMode.HALF_UP);
                hoursWorked = hoursWorked.add(displacementHours);

                BigDecimal interval = TimeEntryCalculationHelper.calculateIntervalHoursForJob(dayAllEntries, jobId);
                String displacementAddress = dayDisplacements.isEmpty() ? null : dayDisplacements.get(0).getStartAddress();

                dailyHours.add(JobTimesheetResponseDto.DailyHoursDto.builder()
                    .date(date)
                    .start(startLocalTime)
                    .end(endLocalTime)
                    .hoursWorked(hoursWorked)
                    .displacement(displacementAddress)
                    .interval(interval)
                    .displacementHours(displacementHours)
                    .build());
            }

            dailyHours.sort(Comparator.comparing(JobTimesheetResponseDto.DailyHoursDto::getDate));

            employeeTimesheets.add(JobTimesheetResponseDto.EmployeeTimesheetDto.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .dailyHours(dailyHours)
                .build());
        }

        employeeTimesheets.sort(Comparator.comparing(JobTimesheetResponseDto.EmployeeTimesheetDto::getEmployeeName));

        return JobTimesheetResponseDto.builder()
            .jobId(jobId)
            .address(job.getAddress())
            .employeeTimesheets(employeeTimesheets)
            .build();
    }
}
