package com.inidus.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AllergyProvider.class, OpenEhrService.class})
public class AllergyProviderTest {
    @Autowired
    @Qualifier("AllergyProvider")
    private AllergyProvider testProvider;

    @MockBean
    private OpenEhrService ehrService;

    static JsonNode getDummyJson() throws IOException {
        return new ObjectMapper().readTree("{\"resultSet\": [\n" +
                "        {\n" +
                "            \"compositionId\": \"acee15e2-484a-4f12-823c-e1b3757cfe70::debra.oprn1.ehrscape.com::1\",\n" +
                "            \"Causative_agent\": {\n" +
                "                \"@class\": \"DV_CODED_TEXT\",\n" +
                "                \"value\": \"allergy to penicillin\",\n" +
                "                \"defining_code\": {\n" +
                "                    \"@class\": \"CODE_PHRASE\",\n" +
                "                    \"terminology_id\": {\n" +
                "                        \"@class\": \"TERMINOLOGY_ID\",\n" +
                "                        \"value\": \"SNOMED-CT\"\n" +
                "                    },\n" +
                "                    \"code_string\": \"91936005\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"Onset_of_reaction\": \"2017-11-13T22:41:52.644+01:00\",\n" +
                "            \"ehrId\": \"705acfd1-cd47-44c3-acbb-f2e1b435f48e\",\n" +
                "            \"Route_of_exposure\": {\n" +
                "                \"@class\": \"DV_TEXT\",\n" +
                "                \"value\": \"intramuscular\"\n" +
                "            },\n" +
                "            \"Reaction_mechanism_code\": null,\n" +
                "            \"subjectId\": \"9999999000\",\n" +
                "            \"entryId\": \"cc99f287-ea3a-4a20-a17b-c6c8c799981f\",\n" +
                "            \"Severity_code\": \"at0093\",\n" +
                "            \"Reaction_description\": \"Diffuse papular rash and pharngeal oedema\",\n" +
                "            \"Certainty_code\": \"at0023\",\n" +
                "            \"Specific_substance\": {\n" +
                "                \"@class\": \"DV_TEXT\",\n" +
                "                \"value\": \"Penicillin-V\"\n" +
                "            },\n" +
                "            \"subjectNamespace\": \"uk.nhs.nhs_number\",\n" +
                "            \"Adverse_reaction_risk_Last_updated\": \"2017-11-13T22:41:52.645+01:00\",\n" +
                "            \"Criticality_code\": \"at0102\",\n" +
                "            \"Adverse_reaction_risk_Comment\": \"Reaction observed by nurse\",\n" +
                "            \"Onset_of_last_reaction\": \"2017-11-13T22:41:52.643+01:00\",\n" +
                "            \"Status_code\": \"at0064\"\n" +
                "        }\n" +
                "    ]}\n").get("resultSet").get(0);
    }

    @Before
    public void setUp() throws Exception {
        given(ehrService.getAllergyById(Mockito.anyString())).willReturn(AllergyProviderTest.getDummyJson());
    }

    @Test
    public void getResourceById_dummyDataMandatoryFieldsPresent() throws Exception {
        AllergyIntolerance resource = testProvider.getResourceById(new IdType("1"));

        Assert.assertNotNull("ID missing", resource.getId());
        Assert.assertNotNull("patient reference missing", resource.getPatient().getReference());
        Assert.assertNotNull("verification status missing", resource.getVerificationStatus());
        Assert.assertNotNull("clinical status missing", resource.getClinicalStatus());
        Assert.assertNotNull("note text missing", resource.getNote().get(0).getText());

        Assert.assertTrue("either asserted date or onset date has to be present",
                resource.getAssertedDate() != null || resource.getOnset() != null);

        Assert.assertNotNull("code missing", resource.getCode());
    }

    @Test
    public void getResourceById_dummyDataFieldsPresent() throws Exception {
        AllergyIntolerance resource = testProvider.getResourceById(new IdType("1"));

        Assert.assertNotNull("ID missing", resource.getId());
        Assert.assertNotNull("clinical status missing", resource.getClinicalStatus());
        Assert.assertNotNull("verification status missing", resource.getVerificationStatus());
        Assert.assertNotNull("category missing", resource.getCategory());
        Assert.assertNotNull("criticality missing", resource.getCriticality());
        Assert.assertNotNull("code missing", resource.getCode());
        Assert.assertNotNull("patient missing", resource.getPatient());
        Assert.assertNotNull("patient reference missing", resource.getPatient().getReference());
        Assert.assertNotNull("patient ID missing", resource.getPatient().getIdentifier().getValue());
        Assert.assertNotNull("patient ID system info missing", resource.getPatient().getIdentifier().getSystem());
        Assert.assertNotNull("last occurrence missing", resource.getLastOccurrence());
        Assert.assertNotNull("asserted date missing", resource.getAssertedDate());
        Assert.assertNotNull("note missing", resource.getNote().get(0).getText());
        Assert.assertNotNull("reaction substance missing", resource.getReaction().get(0).getSubstance().getText());
        Assert.assertNotNull("reaction substance coding missing", resource.getReaction().get(0).getSubstance().getCoding());
        Assert.assertNotNull("reaction manifestation missing", resource.getReaction().get(0).getManifestation());
        Assert.assertNotNull("reaction description missing", resource.getReaction().get(0).getDescription());
        Assert.assertNotNull("reaction onset missing", resource.getReaction().get(0).getOnset());
        Assert.assertNotNull("reaction severity missing", resource.getReaction().get(0).getSeverity());
        Assert.assertNotNull("reaction exposure route missing", resource.getReaction().get(0).getExposureRoute());
        Assert.assertNotNull("reaction note missing", resource.getReaction().get(0).getNote());
    }
}