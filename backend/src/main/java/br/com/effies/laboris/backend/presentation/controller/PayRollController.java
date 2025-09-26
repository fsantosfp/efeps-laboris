package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.PayrollService;
import br.com.effies.laboris.backend.presentation.dto.response.MyPayrollResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/my-payroll")
@PreAuthorize("hasRole('EMPLOYEE')")
public class PayRollController {

    private final PayrollService payrollService;

    PayRollController(PayrollService payrollService){
        this.payrollService = payrollService;
    }

    @GetMapping
    public ResponseEntity<MyPayrollResponseDto> getMyPayRoll(
        @AuthenticationPrincipal User employee,
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
     MyPayrollResponseDto response = payrollService.calculateMyPayroll(employee, start, end);
     return ResponseEntity.ok(response);
    }
}
