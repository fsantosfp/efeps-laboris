package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.TimeEntryService;
import br.com.effies.laboris.backend.presentation.dto.request.TimeEntryRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.TimeEntryResponseDto;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
