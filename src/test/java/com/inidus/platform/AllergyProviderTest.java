package com.inidus.platform;

import ca.uhn.fhir.rest.param.StringParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inidus.platform.openehr.MarandConnector;
import com.inidus.platform.openehr.OpenEhrService;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AllergyProvider.class, MarandConnector.class})
public class AllergyProviderTest {
    @Autowired
    @Qualifier("AllergyProvider")
    private AllergyProvider testProvider;
    @Autowired
    @Qualifier("marandConnector")
    private OpenEhrService ehrService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getAllResources() throws Exception {
        Assert.assertNotNull(testProvider.getAllResources());
    }

    @Test
    public void getResourceByPatientIdentifier() throws Exception {
        StringParam id = new StringParam("9999999000");
        StringParam ns = new StringParam("uk.nhs.nhs_number");
        Assert.assertNotNull(testProvider.getResourceByPatientIdentifier(id, ns));
    }
}
