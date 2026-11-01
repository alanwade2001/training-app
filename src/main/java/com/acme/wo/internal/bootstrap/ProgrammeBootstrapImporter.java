package com.acme.wo.internal.bootstrap;

import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.model.Programme;
import com.acme.wo.service.YamlUploadService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ProgrammeBootstrapImporter {

    private final BootstrapResourceLoader resourceLoader;
    private final YamlUploadService yamlUploadService;
    private final ProgrammePersistor programmePersistor;

    public ProgrammeBootstrapImporter(BootstrapResourceLoader resourceLoader, YamlUploadService yamlUploadService, ProgrammePersistor programmePersistor) {
        this.resourceLoader = resourceLoader;
        this.yamlUploadService = yamlUploadService;
        this.programmePersistor = programmePersistor;
    }

    public void importDirectory(String location) {

        resourceLoader.resources(location)
                .forEach(this::importProgramme);
    }

    private void importProgramme(Resource resource) {

        Programme programme =
                yamlUploadService.importProgramme(resource);

        programmePersistor.persist(programme);
    }
}