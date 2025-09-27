package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.JobService;
import br.com.effies.laboris.backend.presentation.dto.request.CreateJobRequestDto;
import br.com.effies.laboris.backend.presentation.dto.request.JobAssignmentRequestDto;
import br.com.effies.laboris.backend.presentation.dto.request.RemoveAssignmentRequestDto;
import br.com.effies.laboris.backend.presentation.dto.request.UpdateJobRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.JobResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService){
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponseDto> createJob(
        @Valid @RequestBody CreateJobRequestDto request,
        @AuthenticationPrincipal User manager
    ){

        Job newJob = jobService.create(request, manager);
        JobResponseDto response = new JobResponseDto(newJob);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    public ResponseEntity<List<JobResponseDto>> listJobs(@AuthenticationPrincipal User manager){
         List<Job> jobs = jobService.findAllByManager(manager);
         List<JobResponseDto> response = jobs.stream().map(JobResponseDto::new).toList();

         return ResponseEntity.ok(response);
    }

    @PatchMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> updateJobStatus(
        @PathVariable UUID jobId,
        @Valid @RequestBody UpdateJobRequestDto request,
        @AuthenticationPrincipal User manager
        ){
         Job updatedJob = jobService.updateStatus(jobId, request, manager);
         JobResponseDto response = new JobResponseDto(updatedJob);

         return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> getJobById(
        @PathVariable UUID jobId,
        @AuthenticationPrincipal User manager
    ){

        Job job = jobService.getJobDetails(jobId, manager);
        List<User> assignedEmployees = jobService.findAssignedEmployees(jobId);

        JobResponseDto response = new JobResponseDto(job, assignedEmployees);

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJobById(
        @PathVariable UUID jobId,
        @AuthenticationPrincipal User manager
    ){

        jobService.delete(jobId, manager);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{jobId}/assignments")
    public ResponseEntity<Void> assignEmployeesToJob(
        @PathVariable UUID jobId,
        @Valid @RequestBody JobAssignmentRequestDto request,
        @AuthenticationPrincipal User manager){

        jobService.assignEmployees(jobId, request.getEmployeeIds(), manager);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jobId}/assignments")
    public ResponseEntity<Void> removeAssignmentsFromJob(
        @PathVariable UUID jobId,
        @Valid @RequestBody RemoveAssignmentRequestDto request,
        @AuthenticationPrincipal User manager){

        jobService.removeAssignments(jobId, request.getEmployeeIds(), manager);

        return ResponseEntity.noContent().build();
    }
}
