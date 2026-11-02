package com.acme.wo.service;

import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.model.Programme;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgrammeServiceTest {

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
                uploadService,
                programmePersistor
        );
    }

    @Test
    void returnsAllProgrammesSortedById() {
        Programme programme30 = new Programme(30L, "", LocalDate.now(), List.of());
        Programme programme10 = new Programme(10L, "", LocalDate.now(), List.of());
        Programme programme20 = new Programme(20L, "", LocalDate.now(), List.of());

        doReturn( List.of(programme10, programme20, programme30) ).when(programmePersistor).findAll();

        List<Programme> result = service.findAll();

        assertEquals(
                List.of(10L, 20L, 30L),
                result.stream()
                        .map(Programme::id)
                        .toList()
        );

        verify(programmePersistor).findAll();
    }

    @Test
    void findsProgrammeById() {
        Programme programme10 = mockProgramme(10L);
        Programme programme20 = mockProgramme(20L);

        doReturn(Optional.of(programme20)).when(programmePersistor).findById(anyLong());

        Programme result = service.findById(20L).orElseThrow();

        assertSame(programme20, result);
    }

    @Test
    void returnsEmptyWhenProgrammeDoesNotExist() {
        Programme programme = mockProgramme(10L);

        var result = service.findById(99L);
        Assertions.assertThat(result).isEmpty();
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

        //when(programme.id()).thenReturn(id);

        return programme;
    }
}