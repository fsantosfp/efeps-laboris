package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.TimeEntryService;
import br.com.effies.laboris.backend.presentation.dto.request.TimeEntryRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.TimeEntryResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/time-entries")
@PreAuthorize("hasRole('EMPLOYEE')")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService){
        this.timeEntryService = timeEntryService;
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponseDto> createTimeEntry(
        @Valid @RequestBody TimeEntryRequestDto request,
        @AuthenticationPrincipal User employee) {

        TimeEntry newTimeEntry = timeEntryService.create(request, employee);
        TimeEntryResponseDto response = new TimeEntryResponseDto(newTimeEntry);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TimeEntryResponseDto>> getMyTimeEntries(
        @AuthenticationPrincipal User employee,
        @RequestParam("start")
        @NotNull(message = "A data de início não pode ser nula.")
        @PastOrPresent(message = "A data de início não pode ser no futuro.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,

        @RequestParam("end")
        @NotNull(message = "A data final não pode ser nula.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
        List<TimeEntry> entries = timeEntryService.findAllByEmployeeAndPeriod(employee, start, end);

        List<TimeEntryResponseDto> response = entries.stream().map(TimeEntryResponseDto::new).toList();

        return ResponseEntity.ok(response);
    }
}
