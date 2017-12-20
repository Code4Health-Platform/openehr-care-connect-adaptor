package com.inidus.platform;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.inidus.platform.openehr.OpenEhrConnector;
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
@ContextConfiguration(classes = {AllergyProvider.class, OpenEhrConnector.class})
public class AllergyProviderTest {
    @Autowired
    @Qualifier("AllergyProvider")
    private AllergyProvider testProvider;
    @Autowired
    private OpenEhrConnector ehrService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getAllResources() throws Exception {
        configureEthercisCdr("http://178.62.71.220:8080", "guest", "guest", true);
        Assert.assertNotNull(testProvider.getAllResources());
    }

    private void configureEthercisCdr(String url, String user, String pass, boolean isToken) {
        ehrService.setIsTokenAuth(isToken);
        ehrService.setUrl(url);
        ehrService.setUsername(user);
        ehrService.setPassword(pass);
    }

    @Test
    public void getResourceByPatientIdentifier_ehrNamespace() throws Exception {
        configureEthercisCdr("http://178.62.71.220:8080", "guest", "guest", true);
        TokenParam identifier = new TokenParam("uk.nhs.nhs_number", "9999999000");
        List<CCAllergyIntolerance> result = testProvider.getFilteredResources(identifier, null, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByPatientIdentifier_FhirNamespace() throws Exception {
        configureEthercisCdr("http://178.62.71.220:8080", "guest", "guest", true);
        TokenParam identifier = new TokenParam("https://fhir.nhs.uk/Id/nhs-number", "9999999000");
        List<CCAllergyIntolerance> result = testProvider.getFilteredResources(identifier, null, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByDate() throws Exception {
        configureEthercisCdr("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);

        Date from = DatatypeConverter.parseDateTime("2016-12-07T15:47:43+01:00").getTime();
        Date to = DatatypeConverter.parseDateTime("2018-12-07T15:47:43+01:00").getTime();
        DateRangeParam dateRange = new DateRangeParam(from, to);

        List<CCAllergyIntolerance> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByDate_withouth_from() throws Exception {
        configureEthercisCdr("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);

        Date to = DatatypeConverter.parseDateTime("2018-12-07T15:47:43+01:00").getTime();
        DateRangeParam dateRange = new DateRangeParam(null, to);

        List<CCAllergyIntolerance> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByDate_withouth_to() throws Exception {
        configureEthercisCdr("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);

        Date from = DatatypeConverter.parseDateTime("2016-12-07T15:47:43+01:00").getTime();
        DateRangeParam dateRange = new DateRangeParam(from, null);

        List<CCAllergyIntolerance> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByCategory_medication() throws Exception {
        configureEthercisCdr("http://178.62.71.220:8080", "guest", "guest", true);
        StringParam food = new StringParam("medication");

        List<CCAllergyIntolerance> result = testProvider.getFilteredResources(null, food, null);

        Assert.assertNotNull(result);
    }
}
