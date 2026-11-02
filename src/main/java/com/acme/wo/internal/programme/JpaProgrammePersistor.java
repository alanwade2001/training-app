package com.acme.wo.internal.programme;

import com.acme.wo.internal.data.ProgrammeRepository;
import com.acme.wo.model.Programme;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Transactional
@Component
public class JpaProgrammePersistor implements ProgrammePersistor {

    private final ProgrammeRepository programmeRepository;

    public JpaProgrammePersistor(ProgrammeRepository programmeRepository) {
        this.programmeRepository = programmeRepository;
    }


    @Override
    public List<Programme> persist(List<Programme> programmes) {
        return programmes.stream().map(this::persist).toList();
    }

    @Override
    public Programme persist(Programme p) {
        var programmeEntity = ProgrammeMapper.mapToEntity(p);

        var savedProgrammeEntity = programmeRepository.save(programmeEntity);

        return new Programme(savedProgrammeEntity.getId()
                , programmeEntity.getName()
                , programmeEntity.getStartDate()
                , p.weeks());

    }

    @Override
    public void deleteById(Long programmeId) {
        programmeRepository.deleteById(programmeId);
    }

    @Override
    public List<Programme> findAll() {
        return getProgrammes();
    }

    @Override
    public Optional<Programme> findById(Long programmeId) {
        return programmeRepository.findById(programmeId).map(ProgrammeMapper::mapFromEntity);
    }

    public List<Programme> getProgrammes() {
        return programmeRepository.findAll().stream().map(ProgrammeMapper::mapFromEntity).toList();
    }
}
