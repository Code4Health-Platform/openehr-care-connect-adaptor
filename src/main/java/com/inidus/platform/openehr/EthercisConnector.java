package com.inidus.platform.openehr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Service
public class EthercisConnector implements OpenEhrService {
    // Ethercis
    private String cdrURL = "http://178.62.71.220:8080";
    private String cdrUsername = "guest";
    private String cdrPassword = "guest";


    @Override
    public JsonNode getAllAllergies() throws IOException {
        String sessionToken = getSessionToken(cdrUsername, cdrPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Ehr-Session", sessionToken);
        // headers.add("Authorization", cdrBasicAuth);

        String aql = "select " +
                "e/ehr_id/value as ehrId, " +
                "e/ehr_status/subject/external_ref/id/value as subjectId, " +
                "e/ehr_status/subject/external_ref/namespace as subjectNamespace, " +
                "a/uid/value as compositionId, " +
                "b_a/uid/value as entryId, " +
                "b_a/data[at0001]/items[at0002]/value/value as Causative_agent, " +
                "b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Causative_agent_code, " +
                "b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code, " +
                "b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code, " +
                "b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code, " +
                "b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction, " +
                "b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code, " +
                "b_a/data[at0001]/items[at0006]/value/value as Comment, " +
                "b_a/protocol[at0042]/items[at0062]/value/value as Adverse_reaction_risk_Last_updated, " +
                "b_a/data[at0001]/items[at0009]/items[at0010]/value/value as Specific_substance, " +
                "b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code, " +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value/value as Manifestation, " +
                "b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description, " +
                "b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction, " +
                "b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code, " +
                "b_a/data[at0001]/items[at0009]/items[at0106]/value/value as Route_of_exposure, " +
                "b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "where a/name/value='Adverse reaction list'";

        String body = "{\"aql\" : \"" + aql + "\"}";

        HttpEntity<String> postEntity = new HttpEntity<>(body, headers);

        String url = cdrURL + "/rest/v1/query";

        ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.POST, postEntity, String.class);

        JsonNode resultJson = new ObjectMapper().readTree(result.getBody());

        deleteSessionToken(sessionToken);

        return resultJson.get("resultSet");

    }

    @Override
    public JsonNode getAllergyById(String id) throws IOException {
        return null;
    }

    @Override
    public JsonNode getAllergyByPatientIdentifier(String patientId, String idNamespace) throws IOException {
        return null;
    }

    @Override
    public JsonNode getAllergyByPatientId(String id) throws IOException {
        return null;
    }

    public String getSessionToken(String userName, String userPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> postEntity = new HttpEntity<>("", headers);

        String url = cdrURL + "/rest/v1/session";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("username", userName)
                .queryParam("password", userPassword);

        ResponseEntity<String> result = new RestTemplate().exchange(
                builder.build().encode().toUri(),
                HttpMethod.POST,
                postEntity,
                String.class);

        JsonNode resultJson = null;
        try {
            resultJson = new ObjectMapper().readTree(result.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultJson.get("sessionId").asText();
    }

    public void deleteSessionToken(String sessionToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Ehr-Session", sessionToken);

        String body = "";
        HttpEntity<String> postEntity = new HttpEntity<>(body, headers);

        String url = cdrURL + "/rest/v1/session";
        ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.DELETE, postEntity, String.class);

        JsonNode resultJson = null;
        try {
            resultJson = new ObjectMapper().readTree(result.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Should return "DELETE"
        String action = resultJson.asText("action");
    }
}
