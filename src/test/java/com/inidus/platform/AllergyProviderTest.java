package com.inidus.platform;

import ca.uhn.fhir.rest.param.TokenParam;
import com.inidus.platform.openehr.MarandConnector;
import com.inidus.platform.openehr.OpenEhrService;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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
    public void getResourceByPatientIdentifier_ehrNamespace() throws Exception {
        TokenParam identifier = new TokenParam("uk.nhs.nhs_number", "9999999000");
        List<AllergyIntolerance> result = testProvider.getResourceByPatientIdentifier(identifier);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByPatientIdentifier_FhirNamespace() throws Exception {
        TokenParam identifier = new TokenParam("https://fhir.nhs.uk/Id/nhs-number", "9999999000");
        List<AllergyIntolerance> result = testProvider.getResourceByPatientIdentifier(identifier);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

//    @Test
//    public void getResourceByPatientId() throws Exception {
//        StringParam id = new StringParam("1");
//        Assert.assertNotNull(testProvider.getResourceByPatientId(id));
//    }
}
