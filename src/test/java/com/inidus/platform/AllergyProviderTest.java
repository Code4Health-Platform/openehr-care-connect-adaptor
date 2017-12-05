package com.inidus.platform;

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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AllergyProvider.class, MarandConnector.class})
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
                "    ]}\n").get("resultSet");
    }

    private static JsonNode queryOpenEhr() throws IOException {
        // create Map of data to be posted for domain creation
//        Map<String, String> data = new HashMap<>();
//        data.put(OperinoService.USERNAME, "oprn_hcbox");
//        data.put(OperinoService.PASSWORD, "XioTAJoO479");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic b3Bybl9oY2JveDpYaW9UQUpvTzQ3OQ==");
//        headers.add("Ehr-Session-disabled", "0ce5ec82-3954-4388-bfd7-e48f6db613e8");

        String url = "https://cdr.code4health.org/rest/v1/query";
        String aql = "select " +
                "e/ehr_id/value as ehrId, " +
                "e/ehr_status/subject/external_ref/id/value as subjectId, " +
                "e/ehr_status/subject/external_ref/namespace as subjectNamespace, " +
                "a/uid/value as compositionId, " +
                "b_a/uid/value as entryId, " +
                "b_a/data[at0001]/items[at0002]/value as Causative_agent, " +
                "b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code, " +
                "b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code, " +
                "b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code, " +
                "b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction, " +
                "b_a/data[at0001]/items[at0058]/valuedefining_code/code_string as Reaction_mechanism_code, " +
                "b_a/data[at0001]/items[at0006]/value/value as Comment, " +
                "b_a/protocol[at0042]/items[at0062]/value/value as Adverse_reaction_risk_Last_updated, " +
                "b_a/data[at0001]/items[at0009]/items[at0010]/value as Specific_substance, " +
                "b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code, " +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value as Manifestation, " +
                "b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description, " +
                "b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction, " +
                "b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code, " +
                "b_a/data[at0001]/items[at0009]/items[at0106]/value as Route_of_exposure, " +
                "b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "where a/name/value='Adverse reaction list'";
        String aqlRequest = "{\"aql\" : \"" + aql + "\"}";


        HttpEntity<String> postEntity = new HttpEntity<>(aqlRequest, headers);
        ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.POST, postEntity, String.class);

        return new ObjectMapper().readTree(result.getBody());
    }

    @Before
    public void setUp() throws Exception {
        given(ehrService.getAllergyById(Mockito.anyString())).willReturn(AllergyProviderTest.getDummyJson());
        given(ehrService.getAllAllergies()).willReturn(new MarandConnector().getAllAllergies());
//        given(ehrService.getAllergyById(Mockito.anyString())).willReturn(AllergyProviderTest.queryOpenEhr());

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

    @Test
    public void getAllResources() throws Exception {
        Assert.assertNotNull(testProvider.getAllResources());
    }
}
