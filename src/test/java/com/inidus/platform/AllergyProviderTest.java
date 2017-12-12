package com.inidus.platform;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.inidus.platform.conversion.OpenEhrConverter;
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

import java.util.Date;
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
        List<AllergyIntolerance> result = testProvider.getFilteredResources(identifier, null, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByPatientIdentifier_FhirNamespace() throws Exception {
        TokenParam identifier = new TokenParam("https://fhir.nhs.uk/Id/nhs-number", "9999999000");
        List<AllergyIntolerance> result = testProvider.getFilteredResources(identifier, null, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("https://fhir.nhs.uk/Id/nhs-number", result.get(0).getPatient().getIdentifier().getSystem());
    }

    @Test
    public void getResourceByDate() throws Exception {
        Date from = OpenEhrConverter.MARAND_DATE_FORMAT.parse("2016-12-07T15:47:43+01:00");
        Date to = OpenEhrConverter.MARAND_DATE_FORMAT.parse("2018-12-07T15:47:43+01:00");
        DateRangeParam dateRange = new DateRangeParam(from, to);

        List<AllergyIntolerance> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByDate_withouth_from() throws Exception {
        Date to = OpenEhrConverter.MARAND_DATE_FORMAT.parse("2018-12-07T15:47:43+01:00");
        DateRangeParam dateRange = new DateRangeParam(null, to);

        List<AllergyIntolerance> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByDate_withouth_to() throws Exception {
        Date from = OpenEhrConverter.MARAND_DATE_FORMAT.parse("2016-12-07T15:47:43+01:00");
        DateRangeParam dateRange = new DateRangeParam(from, null);

        List<AllergyIntolerance> result = testProvider.getFilteredResources(null, null, dateRange);

        Assert.assertNotNull(result);
    }

    @Test
    public void getResourceByCategory_medication() throws Exception {
        StringParam food = new StringParam("medication");

        List<AllergyIntolerance> result = testProvider.getFilteredResources(null, food, null);

        Assert.assertNotNull(result);
    }
}
