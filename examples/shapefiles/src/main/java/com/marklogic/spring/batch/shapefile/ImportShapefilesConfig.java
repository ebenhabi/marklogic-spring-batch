package com.marklogic.spring.batch.shapefile;

import com.marklogic.client.helper.DatabaseClientProvider;
import com.marklogic.spring.batch.item.reader.DirectoryReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.File;

/**
 * This is fairly minimal so far. Possible improvements - allow for separate collections and permissions for the
 * shapefile ZIP and JSON files; provide support for command-line ogr2ogr; allow for customization of the URIs.
 */
@EnableBatchProcessing
public class ImportShapefilesConfig {

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, @Qualifier("step1") Step step1) {
        return jobBuilderFactory.get("importShapefilesJob").start(step1).build();
    }

    @Bean
    @JobScope
    protected Step step1(
            StepBuilderFactory stepBuilderFactory,
            DatabaseClientProvider databaseClientProvider,
            @Value("#{jobParameters['input_file_path']}") String inputFilePath,
            @Value("#{jobParameters['ogre_url']}") String ogreUrl,
            @Value("#{jobParameters['output_collections']}") String[] collections,
            @Value("#{jobParameters['output_permissions']}") String permissions) {

        DirectoryReader reader = new DirectoryReader(new File(inputFilePath));

        ShapefileAndJsonWriter writer = new ShapefileAndJsonWriter(databaseClientProvider.getDatabaseClient());
        writer.setPermissions(permissions);
        writer.setCollections(collections);

        return stepBuilderFactory.get("step1")
                .<File, ShapefileAndJson>chunk(10)
                .reader(reader)
                .processor(shapefileProcessor(ogreUrl))
                .writer(writer)
                .build();
    }

    /**
     * Protected so that it can be overridden by a subclass, specifically for testing purposes.
     *
     * @param ogreUrl
     * @return
     */
    protected ShapefileProcessor shapefileProcessor(String ogreUrl) {
        OgreProxy proxy = ogreUrl != null ? new HttpClientOgreProxy(ogreUrl) : new HttpClientOgreProxy();
        return new ShapefileProcessor(proxy);
    }
}