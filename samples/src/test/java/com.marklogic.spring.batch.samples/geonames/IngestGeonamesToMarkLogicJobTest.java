package com.marklogic.spring.batch.samples.geonames;

import com.marklogic.client.helper.DatabaseClientConfig;
import com.marklogic.client.spring.SimpleDatabaseClientProvider;
import com.marklogic.junit.Fragment;
import com.marklogic.spring.batch.test.AbstractJobRunnerTest;
import com.marklogic.spring.batch.test.SpringBatchNamespaceProvider;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {com.marklogic.spring.batch.samples.geonames.IngestGeonamesToMarkLogicJob.class})
public class IngestGeonamesToMarkLogicJobTest extends AbstractJobRunnerTest {

    @Autowired
    private DatabaseClientConfig databaseClientConfig;

    public IngestGeonamesToMarkLogicJobTest() {
        setDatabaseClientProvider(new SimpleDatabaseClientProvider(databaseClientConfig));
        setNamespaceProvider(new SpringBatchNamespaceProvider());
    }

    @Test
    public void ingestCitiesTest() throws Exception {
        JobParametersBuilder jpb = new JobParametersBuilder();
        jpb.addLong("chunk", 10L);
        jpb.addString("input_file_path", "src/test/resources/geonames/cities10.txt");
        JobExecution jobExecution = getJobLauncherTestUtils().launchJob(jpb.toJobParameters());

        String xquery = "xquery version \"1.0-ml\";\n" +
                "declare namespace g = \"http://geonames.org\";\n" +
                "xdmp:estimate(cts:search(fn:doc(), cts:element-query(xs:QName(\"g:geoname\"), cts:and-query(()))))";

        String result = getClient().newServerEval().xquery(xquery).evalAs(String.class);
        assertEquals(10, Integer.parseInt(result));
        Fragment frag = getClientTestHelper().parseUri("http://geonames.org/geoname/4140963", "geonames");
        frag.assertElementValue("//geo:population", "601723");
        
            
    }
}
