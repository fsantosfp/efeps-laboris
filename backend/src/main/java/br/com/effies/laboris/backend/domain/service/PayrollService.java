package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Payroll;
import br.com.effies.laboris.backend.domain.entity.PayrollDetail;
import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.entity.enums.UserStatus;
import br.com.effies.laboris.backend.domain.helper.TimeEntryCalculationHelper;
import br.com.effies.laboris.backend.domain.repository.PayrollDetailRepository;
import br.com.effies.laboris.backend.domain.repository.PayrollRepository;
import br.com.effies.laboris.backend.domain.repository.SalaryHistoryRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.SettlePayrollRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.CompanyPayrollResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.MyPayrollResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.MyPayrollResponseDto.PeriodSummary;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final TimeEntryRepository timeEntryRepository;
    private final SalaryHistoryRepository salaryRepository;
    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollDetailRepository payrollDetailRepository;

    public PayrollService(
        TimeEntryRepository timeEntryRepository,
        SalaryHistoryRepository salaryRepository,
        UserRepository userRepository,
        PayrollRepository payrollRepository,
        PayrollDetailRepository payrollDetailRepository
    ){
        this.salaryRepository = salaryRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.payrollDetailRepository = payrollDetailRepository;
    }

    public MyPayrollResponseDto calculateEmployeePayroll(User employee, Instant start, Instant end){
        return calculatePayroll(employee, start, end);
    }

    public CompanyPayrollResponseDto calculateCompanyPayroll(User manager, Instant start, Instant end){
        List<User> employees = userRepository.findByCompanyIdAndRoleAndStatus(
            manager.getCompany().getId(), UserRole.EMPLOYEE, UserStatus.ACTIVE);

        BigDecimal grandTotalAmount = BigDecimal.ZERO;
        BigDecimal grandTotalHours = BigDecimal.ZERO;
        List<CompanyPayrollResponseDto.EmployeePayrollDto> employeePayrolls = new ArrayList<>();

        for (User employee : employees) {

            MyPayrollResponseDto individualPayroll = calculatePayroll(employee, start, end);

            BigDecimal employeeTotalAmount = individualPayroll.getOpenToReceive().getTotalAmount();
            BigDecimal employeeTotalHours = individualPayroll.getOpenToReceive().getTotalHours();

            grandTotalAmount = grandTotalAmount.add(employeeTotalAmount);
            grandTotalHours = grandTotalHours.add(employeeTotalHours);

            employeePayrolls.add(CompanyPayrollResponseDto.EmployeePayrollDto.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .totalHours(employeeTotalHours)
                .totalAmount(employeeTotalAmount)
                .build());
        }

        return CompanyPayrollResponseDto.builder()
            .periodTotals(CompanyPayrollResponseDto.PeriodSummary.builder()
                .totalHours(grandTotalHours)
                .totalAmount(grandTotalAmount)
                .build())
            .employeePayrolls(employeePayrolls)
            .build();
    }

    @Transactional
    public void settlePayroll(SettlePayrollRequestDto request, User manager){
        List<TimeEntry> openEntries = timeEntryRepository.findOpenEntriesForEmployeesInPeriod(
            request.getEmployeeIds(),
            request.getStartDate(),
            request.getEndDate());

        if(openEntries.isEmpty()){ throw new IllegalStateException("Não há horas em aberto para os funcionários selecionados neste período.");}

        Payroll payrollRun = new Payroll();
        payrollRun.setCompany(manager.getCompany());
        payrollRun.setPeriodStart(request.getStartDate());
        payrollRun.setPeriodEnd(request.getEndDate());
        payrollRun.setSettledByUser(manager);

        Payroll savedPayroll = payrollRepository.saveAndFlush(payrollRun);

        Map<User, List<TimeEntry>> entriesByEmployee = openEntries.stream()
            .collect(Collectors.groupingBy(TimeEntry::getEmployee));

        for (Map.Entry<User, List<TimeEntry>> employeeEntry :  entriesByEmployee.entrySet()){
            User employee = employeeEntry.getKey();

            MyPayrollResponseDto individualPayroll = calculatePayroll(employee, request.getStartDate(), request.getEndDate());

            PayrollDetail payrollDetail = new PayrollDetail();
            payrollDetail.setEmployee(employee);
            payrollDetail.setTotalAmount(individualPayroll.getOpenToReceive().getTotalAmount());
            payrollDetail.setTotalHours(individualPayroll.getOpenToReceive().getTotalHours());
            payrollDetail.setPayroll(savedPayroll);

            payrollDetailRepository.save(payrollDetail);

            List<UUID> entryIdsToUpdate = openEntries.stream().map(TimeEntry::getId).toList();
            timeEntryRepository.updatePayrollIdForEntries(entryIdsToUpdate, savedPayroll.getId());
        }
    }

    private MyPayrollResponseDto calculatePayroll(User employee, Instant start, Instant end){

        List<TimeEntry> entries = timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), start, end);

        Map<LocalDate, List<TimeEntry>> entriesByDay = entries.stream().collect(Collectors.groupingBy(entry ->
            entry.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalDate()));

        List<MyPayrollResponseDto.DailyEntryDto> dailyBreakdown = new ArrayList<>();

        BigDecimal totalOpenAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        BigDecimal totalOpensHours = BigDecimal.ZERO;
        BigDecimal totalPaidHours = BigDecimal.ZERO;

        for(Map.Entry<LocalDate, List<TimeEntry>> dayEntry : entriesByDay.entrySet()){
            LocalDate workDate = dayEntry.getKey();
            List<TimeEntry> dayEntries = dayEntry.getValue();

            BigDecimal hoursWorkedOnDay = TimeEntryCalculationHelper.calculateHoursWorked(dayEntries);

            Optional<SalaryHistory> salary = salaryRepository
                .findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), workDate);

            if(salary.isPresent()){
                BigDecimal hourlyRate = salary.get().getHourlyRate();
                BigDecimal amountEarnedOnDay =  hourlyRate.multiply(hoursWorkedOnDay).setScale(2, RoundingMode.HALF_UP);

                TimeEntry firstEntry = dayEntries.getFirst();
                boolean isPaid = firstEntry.getPayrollId() != null;
                String status =  isPaid ? "PAID" : "OPEN";

                dailyBreakdown.add(
                    MyPayrollResponseDto.DailyEntryDto.builder()
                        .date(workDate)
                        .jobAddress(firstEntry.getJob().getAddress())
                        .hoursWorked(hoursWorkedOnDay)
                        .amountEarned(amountEarnedOnDay)
                        .status(status)
                        .build());

                if(isPaid){
                    totalPaidHours = totalPaidHours.add(hoursWorkedOnDay);
                    totalPaidAmount = totalPaidAmount.add(amountEarnedOnDay);
                }else{
                    totalOpensHours = totalOpensHours.add(hoursWorkedOnDay);
                    totalOpenAmount = totalOpenAmount.add(amountEarnedOnDay);
                }
            }
        }

        return MyPayrollResponseDto.builder()
            .openToReceive(periodSummaryBuilder(totalOpenAmount, totalOpensHours))
            .alreadyPaid(periodSummaryBuilder(totalPaidAmount, totalPaidHours))
            .dailyBreakdown(dailyBreakdown)
            .build();

    }

    private PeriodSummary periodSummaryBuilder(BigDecimal amount, BigDecimal hours){
        return MyPayrollResponseDto.PeriodSummary.builder()
            .totalAmount(amount)
            .totalHours(hours)
            .build();
    }
}
