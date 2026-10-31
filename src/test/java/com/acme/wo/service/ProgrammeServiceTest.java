package com.acme.wo.service;

import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.model.Programme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgrammeServiceTest {

    @Mock
    private ProgrammeSource sourceA;

    @Mock
    private ProgrammeSource sourceB;

    @Mock
    private YamlUploadService uploadService;

    @Mock
    private ProgrammePersistor programmePersistor;

    @Mock
    private InputStreamSource inputStreamSource;

    private ProgrammeService service;

    @BeforeEach
    void setUp() {
        service = new ProgrammeService(
                List.of(sourceA, sourceB),
                uploadService,
                programmePersistor
        );
    }

    @Test
    void returnsAllProgrammesSortedById() {
        Programme programme30 = mockProgramme(30L);
        Programme programme10 = mockProgramme(10L);
        Programme programme20 = mockProgramme(20L);

        when(sourceA.getProgrammes())
                .thenReturn(
                        List.of(programme30, programme10)
                );

        when(sourceB.getProgrammes())
                .thenReturn(
                        List.of(programme20)
                );

        List<Programme> result = service.findAll();

        assertEquals(
                List.of(10L, 20L, 30L),
                result.stream()
                        .map(Programme::id)
                        .toList()
        );

        verify(sourceA).getProgrammes();
        verify(sourceB).getProgrammes();
    }

    @Test
    void findsProgrammeById() {
        Programme programme10 = mockProgramme(10L);
        Programme programme20 = mockProgramme(20L);

        when(sourceA.getProgrammes())
                .thenReturn(List.of(programme10));

        when(sourceB.getProgrammes())
                .thenReturn(List.of(programme20));

        Programme result = service.findById(20L);

        assertSame(programme20, result);
    }

    @Test
    void returnsNullWhenProgrammeDoesNotExist() {
        Programme programme = mockProgramme(10L);

        when(sourceA.getProgrammes())
                .thenReturn(List.of(programme));

        when(sourceB.getProgrammes())
                .thenReturn(List.of());

        Programme result = service.findById(99L);

        assertNull(result);
    }

    @Test
    void persistsUploadedProgramme() {
        Programme importedProgramme =
                mock(Programme.class);

        Programme persistedProgramme =
                mock(Programme.class);

        when(uploadService.importProgramme(inputStreamSource))
                .thenReturn(importedProgramme);

        when(programmePersistor.persist(importedProgramme))
                .thenReturn(persistedProgramme);

        Programme result =
                service.persist(inputStreamSource);

        assertSame(persistedProgramme, result);

        verify(uploadService)
                .importProgramme(inputStreamSource);

        verify(programmePersistor)
                .persist(importedProgramme);
    }

    @Test
    void deletesProgrammeThroughPersistenceBoundary() {
        Long programmeId = 10L;

        service.deleteProgramme(programmeId);

        verify(programmePersistor)
                .deleteById(programmeId);
    }

    private Programme mockProgramme(Long id) {
        Programme programme = mock(Programme.class);

        when(programme.id()).thenReturn(id);

        return programme;
    }
}