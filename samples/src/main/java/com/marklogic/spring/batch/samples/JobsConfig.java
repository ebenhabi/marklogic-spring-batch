package com.marklogic.spring.batch.samples;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableBatchProcessing
@Import({ com.marklogic.spring.batch.config.MarkLogicApplicationContext.class,
        DeleteDocumentsJob.class,
        ImportDocumentsFromDirectoryJob.class,
        CorbJob.class,
        ExportContentFromMarkLogicJob.class,
        YourJob.class})
public class JobsConfig {
}
