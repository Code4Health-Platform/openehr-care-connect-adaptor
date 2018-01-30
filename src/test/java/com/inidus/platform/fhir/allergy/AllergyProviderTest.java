package com.inidus.platform.fhir.allergy;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AllergyProvider.class, AllergyConnector.class})
public class AllergyProviderTest {
    @Autowired
    @Qualifier("AllergyProvider")
    private AllergyProvider testProvider;
    @Autowired
    private AllergyConnector ehrConnector;

    @Before
    public void setUp() {
        configureCdrConnector("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);
    }

    @Test
    public void getAllResources() throws Exception {
        Assert.assertNotNull(testProvider.getAllResources());
    }

    @Test
    public void getResourceByPatientIdentifier_ehrNamespace() throws Exception {
        TokenParam identifier = new TokenParam("uk.nhs.nhs_number", "9999999000");
        List<AllergyIntoleranceCC> result = testProvider.getFilteredResources(identifier, null, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByPatientIdentifier_FhirNamespace() throws Exception {
        TokenParam identifier = new TokenParam("https://fhir.nhs.uk/Id/nhs-number", "9999999000");
        List<AllergyIntoleranceCC> result = testProvider.getFilteredResources(identifier, null, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByDate() throws Exception {
        Date from = DatatypeConverter.parseDateTime("2016-12-07T15:47:43+01:00").getTime();
        Date to = DatatypeConverter.parseDateTime("2018-12-07T15:47:43+01:00").getTime();
        DateRangeParam dateRange = new DateRangeParam(from, to);

        List<AllergyIntoleranceCC> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByDate_withouth_from() throws Exception {
        Date to = DatatypeConverter.parseDateTime("2018-12-07T15:47:43+01:00").getTime();
        DateRangeParam dateRange = new DateRangeParam(null, to);

        List<AllergyIntoleranceCC> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByDate_withouth_to() throws Exception {
        Date from = DatatypeConverter.parseDateTime("2016-12-07T15:47:43+01:00").getTime();
        DateRangeParam dateRange = new DateRangeParam(from, null);

        List<AllergyIntoleranceCC> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByCategory_medication() throws Exception {
        StringParam food = new StringParam("medication");

        List<AllergyIntoleranceCC> result = testProvider.getFilteredResources(null, food, null);

        Assert.assertNotNull(result);
    }

    private void configureCdrConnector(String url, String user, String pass, boolean isToken) {
        ehrConnector.setIsTokenAuth(isToken);
        ehrConnector.setUrl(url);
        ehrConnector.setUsername(user);
        ehrConnector.setPassword(pass);
    }
}
