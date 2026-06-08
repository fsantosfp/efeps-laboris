package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Displacement;
import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.JobAssignment;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.DisplacementRepository;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DisplacementService {

    private final DisplacementRepository displacementRepository;
    private final JobRepository jobRepository;
    private final JobAssignmentRepository jobAssignmentRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final GeoService geoService;

    public DisplacementService(
            DisplacementRepository displacementRepository,
            JobRepository jobRepository,
            JobAssignmentRepository jobAssignmentRepository,
            TimeEntryRepository timeEntryRepository,
            GeoService geoService
    ) {
        this.displacementRepository = displacementRepository;
        this.jobRepository = jobRepository;
        this.jobAssignmentRepository = jobAssignmentRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.geoService = geoService;
    }

    public Displacement startDisplacement(Double latitude, Double longitude, User employee) {
        // 1. O botão de deslocamento só pode ser exibido e ativado se o usuário estiver alocado em dois ou mais jobs ativos.
        List<JobAssignment> activeAssignments = jobAssignmentRepository.findByUserIdAndJobStatus(employee.getId(), JobStatus.IN_PROGRESS);
        if (activeAssignments.size() < 2) {
            throw new IllegalStateException("O deslocamento só é permitido para usuários alocados em 2 ou mais trabalhos ativos.");
        }

        // 2. Não é possível iniciar um deslocamento sem antes fazer clock out.
        Optional<TimeEntry> lastEntry = timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId());
        if (lastEntry.isEmpty() || lastEntry.get().getEntryType() != TimeEntryType.OUT) {
            throw new IllegalStateException("Não é possível iniciar um deslocamento sem antes registrar uma saída (clock out).");
        }

        // 3. Sem viagem ativa (não permitir iniciar outra viagem se já houver uma ativa)
        Optional<Displacement> activeDisplacement = displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId());
        if (activeDisplacement.isPresent()) {
            throw new IllegalStateException("Já existe um deslocamento ativo em andamento.");
        }

        // 4. Geocode do início (captura de endereço de partida)
        String address = geoService.reverseGeocode(latitude, longitude);

        // 5. Salva novo deslocamento
        Displacement displacement = new Displacement();
        displacement.setUser(employee);
        displacement.setStartTimestamp(Instant.now());
        displacement.setStartLatitude(latitude);
        displacement.setStartLongitude(longitude);
        displacement.setStartAddress(address);

        return displacementRepository.save(displacement);
    }

    public Displacement endDisplacement(Double latitude, Double longitude, UUID destinationJobId, User employee) {
        // 1. Busca deslocamento ativo
        Displacement activeDisplacement = displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum deslocamento ativo encontrado."));

        // 2. Busca trabalho de destino
        Job job = jobRepository.findById(destinationJobId)
                .orElseThrow(() -> new EntityNotFoundException("Trabalho de destino não encontrado."));

        // 3. Validação geográfica: o final do deslocamento deve ser a localização de um job.
        // Tolerância de distância < 0.1 graus de latitude/longitude
        double latDiff = Math.abs(latitude - job.getLatitude());
        double lonDiff = Math.abs(longitude - job.getLongitude());
        if (latDiff >= 0.1 || lonDiff >= 0.1) {
            throw new IllegalArgumentException("Você precisa estar no perímetro geográfico do trabalho de destino para finalizar o deslocamento.");
        }

        // 4. Validação lógica: o destino não pode ser igual à origem (último ponto batido)
        Optional<TimeEntry> lastEntry = timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId());
        if (lastEntry.isPresent() && lastEntry.get().getJob().getId().equals(destinationJobId)) {
            throw new IllegalArgumentException("O trabalho de destino não pode ser o mesmo do local de origem.");
        }

        // 5. Finaliza e salva deslocamento
        activeDisplacement.setEndTimestamp(Instant.now());
        activeDisplacement.setEndLatitude(latitude);
        activeDisplacement.setEndLongitude(longitude);
        activeDisplacement.setDestinationJob(job);

        return displacementRepository.save(activeDisplacement);
    }

    public void cancelDisplacement(User employee) {
        Displacement activeDisplacement = displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum deslocamento ativo encontrado."));
        displacementRepository.delete(activeDisplacement);
    }

    @Transactional(readOnly = true)
    public Optional<Displacement> getActiveDisplacement(User employee) {
        return displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId());
    }

    @Transactional(readOnly = true)
    public List<Displacement> getMyDisplacements(User employee, Instant start, Instant end) {
        return displacementRepository.findAllByUserIdAndPeriod(employee.getId(), start, end);
    }
}
