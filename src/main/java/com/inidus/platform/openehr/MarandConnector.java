package com.inidus.platform.openehr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class MarandConnector implements OpenEhrService {
    private static final String AQL = "select " +
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
            "b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code, " +
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

    private static final String URL = "https://cdr.code4health.org/rest/v1/query";
    private static final String AUTH = "Basic b3Bybl9oY2JveDpYaW9UQUpvTzQ3OQ==";

    @Override
    public JsonNode getAllergyById(String id) throws IOException {
        if (null == id || id.isEmpty() || id.contains(" ")) {
            return null;
        }
        String idFilter = " and b_a/uid/value='" + id + "'";
        String body = "{\"aql\" : \"" + AQL + idFilter + "\"}";

        HttpEntity<String> postEntity = new HttpEntity<>(body, createHttpHeaders());
        ResponseEntity<String> result = new RestTemplate().exchange(URL, HttpMethod.POST, postEntity, String.class);
        JsonNode resultJson = new ObjectMapper().readTree(result.getBody());
        return resultJson.get("resultSet");
    }

    @Override
    public JsonNode getAllAllergies() throws IOException {
        String body = "{\"aql\" : \"" + AQL + "\"}";

        HttpEntity<String> postEntity = new HttpEntity<>(body, createHttpHeaders());
        ResponseEntity<String> result = new RestTemplate().exchange(URL, HttpMethod.POST, postEntity, String.class);
        JsonNode resultJson = new ObjectMapper().readTree(result.getBody());
        return resultJson.get("resultSet");
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", AUTH);
        return headers;
    }
}
