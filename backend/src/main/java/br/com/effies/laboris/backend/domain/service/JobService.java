package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.JobAssignment;
import br.com.effies.laboris.backend.domain.entity.JobAssignmentId;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateJobRequestDto;
import br.com.effies.laboris.backend.presentation.dto.request.UpdateJobRequestDto;
import com.google.maps.model.LatLng;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobAssignmentRepository jobAssignmentRepository;
    private final GeoService geoService;

    public JobService(
        JobRepository jobRepository,
        UserRepository userRepository,
        JobAssignmentRepository jobAssignmentRepository,
        GeoService geoService
    ){
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.jobAssignmentRepository = jobAssignmentRepository;
        this.geoService = geoService;
    }

    public Job create(CreateJobRequestDto request, User manager){

        // TODO: Adicionar a regra de negócio para não permitir criar jobs no mesmo endereço se um já estiver ativo.

        LatLng coordinates = geoService.geocodeAddress(request.getAddress());

        Job newJob = new Job();
        newJob.setAddress(request.getAddress());
        newJob.setLatitude(coordinates.lat);
        newJob.setLongitude(coordinates.lng);
        newJob.setClientName(request.getClientName());
        newJob.setBudget(request.getBudget());
        newJob.setBillingRate(request.getBillingRate());
        newJob.setStartDate(request.getStartDate());
        newJob.setEndDate(request.getEndDate());

        newJob.setCompany(manager.getCompany());

        return jobRepository.save(newJob);

    }

    public List<Job> findAllByManager(User manager){
        UUID companyId = manager.getCompany().getId();
        return jobRepository.findAllByCompanyId(companyId);
    }

    @Transactional
    public void assignEmployees(UUID jobId, List<UUID> employeeIds, User manager){

        Job job = findJobById(jobId);

        job.ensureNotExcluded();
        job.ensureBelongsTo(manager);

        for (UUID employeeId : employeeIds){

            User employee = userRepository.findById(employeeId)
                .orElseThrow( () -> new EntityNotFoundException("Funcionario não encontrado com o ID:" + employeeId ));

            JobAssignmentId assignmentId = new JobAssignmentId();
            assignmentId.setJobId(job.getId());
            assignmentId.setUserId(employee.getId());

            JobAssignment newAssignment = new JobAssignment();
            newAssignment.setId(assignmentId);
            newAssignment.setJob(job);
            newAssignment.setUser(employee);

            jobAssignmentRepository.save(newAssignment);
        }

    }

    public List<User> findAssignedEmployees(UUID jobId){
        return jobAssignmentRepository.findAllByJob(jobId).stream()
            .map(JobAssignment::getUser)
            .toList();
    }

    @Transactional
    public Job updateStatus(UUID jobId, UpdateJobRequestDto request, User manager){

        Job job = findJobById(jobId);

        job.ensureNotExcluded();
        job.ensureBelongsTo(manager);

        // TODO: Adicionar regras de transição de status. Ex: não pode ir de COMPLETED para PENDING.
        // Por agora, permitimos qualquer mudança.

        job.setStatus(request.getStatus());
        job.setBillingRate(request.getBillingRate());
        return jobRepository.save(job);

    }

    public Job getJobDetails(UUID jobId, User manager){
        Job job = findJobById(jobId);

        job.ensureNotExcluded();
        job.ensureBelongsTo(manager);

        return job;
    }

    @Transactional
    public void delete(UUID jobId, User manager){

        Job job = findJobById(jobId);

        job.ensureNotExcluded();
        job.ensureBelongsTo(manager);

        if( job.getStatus() != JobStatus.PENDING) {
            throw new IllegalStateException("Apenas trabalhos com status 'PENDING' podem ser deletados.");
        }

        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);

    }

    public void removeAssignments(UUID jobId, List<UUID> employeeIds, User manager){

        Job job = findJobById(jobId);

        job.ensureNotExcluded();
        job.ensureBelongsTo(manager);

        for (UUID employeeId : employeeIds){
            JobAssignmentId assignmentId = new JobAssignmentId();
            assignmentId.setJobId(jobId);
            assignmentId.setUserId(employeeId);

            if(jobAssignmentRepository.existsById(assignmentId)){
                jobAssignmentRepository.deleteById(assignmentId);
            }
        }
    }

    private Job findJobById(UUID id){
        return jobRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Trabalho não encontrado."));
    }
}
