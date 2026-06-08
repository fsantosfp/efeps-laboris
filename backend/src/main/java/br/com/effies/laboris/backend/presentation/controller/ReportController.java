package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.PayrollService;
import br.com.effies.laboris.backend.domain.service.ReportService;
import br.com.effies.laboris.backend.presentation.dto.response.CompanyPayrollResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.JobCostResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.JobTimesheetResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.EmployeeJourneyResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('MANAGER')")
public class ReportController {

    private final PayrollService payrollService;
    private final ReportService reportService;

    public ReportController(PayrollService payrollService, ReportService reportService){
        this.payrollService = payrollService;
        this.reportService = reportService;
    }

    @GetMapping("/payroll")
    public ResponseEntity<CompanyPayrollResponseDto> getCompanyPayrollReport(
        @AuthenticationPrincipal User manager,
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
            CompanyPayrollResponseDto response = payrollService.calculateCompanyPayroll(manager, start, end);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobCostResponseDto> getJobCostReport(
        @AuthenticationPrincipal User manager,
        @PathVariable UUID jobId,
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){

        JobCostResponseDto response = reportService.calculateJobCostReport(manager, jobId, start, end);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}/timesheet")
    public ResponseEntity<JobTimesheetResponseDto> getJobTimesheetReport(
        @AuthenticationPrincipal User manager,
        @PathVariable UUID jobId,
        @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
        JobTimesheetResponseDto response = reportService.calculateJobTimesheetReport(manager, jobId, start, end);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee-journey")
    public ResponseEntity<List<EmployeeJourneyResponseDto>> getEmployeeJourneyReport(
        @AuthenticationPrincipal User manager,
        @RequestParam(value = "employeeIds", required = false) List<UUID> employeeIds,
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
        List<EmployeeJourneyResponseDto> response = reportService.calculateEmployeeJourneyReport(manager, employeeIds, start, end);
        return ResponseEntity.ok(response);
    }
}
