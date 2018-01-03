package com.inidus.platform.fhir.openehr;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Connects to an openEHR backend and returns selected data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)
@Service
public class OpenEhrConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected String getAQL(){
        return "";
    };

//    private static String AQL = "select" +
//            " e/ehr_id/value as ehrId," +
//            " e/ehr_status/subject/external_ref/id/value as subjectId," +
//            " e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
//            " a/uid/value as compositionId," +
//            " a/composer/external_ref/id/value as composerId," +
//            " a/composer/external_ref/namespace as composerNamespace,"+
//            " a/uid/value as compositionId,"+
//            " a/context/start_time/value as compositionStartTime," +
//            " b_a/uid/value as entryId," +
//            " b_a/data[at0001]/items[at0002]/value/value as Causative_agent_value," +
//            " b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Causative_agent_code," +
//            " b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as Causative_agent_terminology," +
//            " b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code," +
//            " b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code," +
//            " b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code," +
//            " b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction," +
//            " b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code," +
//            " b_a/data[at0001]/items[at0006]/value/value as Comment," +
//            " b_a/protocol[at0042]/items[at0062]/value/value as Adverse_reaction_risk_Last_updated," +
//            " b_a/data[at0001]/items[at0009]/items[at0010]/value/value as Specific_substance_value," +
//            " b_a/data[at0001]/items[at0009]/items[at0010]/value/defining_code/code_string as Specific_substance_code," +
//            " b_a/data[at0001]/items[at0009]/items[at0010]/value/defining_code/terminology_id/value as Specific_substance_terminology," +
//            " b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code," +
//            " b_a/data[at0001]/items[at0009]/items[at0011]/value/value as Manifestation_value,    " +
//            " b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/code_string as Manifestation_code," +
//            " b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/terminology_id/value as Manifestation_terminology," +
//            " b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description," +
//            " b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction," +
//            " b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code," +
//            " b_a/data[at0001]/items[at0009]/items[at0106]/value/value as Route_of_exposure_value," +
//            " b_a/data[at0001]/items[at0009]/items[at0106]/value/defining_code/code_string as Route_of_exposure_code," +
//            " b_a/data[at0001]/items[at0009]/items[at0106]/value/defining_code/terminology_id/value as Route_of_exposure_terminology," +
//            " b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment" +
//            " from EHR e" +
//            " contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]" +
//            " contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]" +
//            " where a/name/value='Adverse reaction list'";

    protected static final DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

    // private static final String URL = "https://cdr.code4health.org/rest/v1/query";
    // private static final String URL = "https://test.operon.systems/rest/v1/query";
    // private static final String AUTH = "Basic b3Bybl9oY2JveDpYaW9UQUpvTzQ3OQ==";

    private String url;
    private String username;
    private String password;
    private boolean isTokenAuth;

    {
        ISO_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public OpenEhrConnector() throws IOException {
    }

    public JsonNode getAllResources() throws IOException {
        return getEhrJson(getAQL());
    }

    public JsonNode getResourceById(String id) throws IOException {
        if (null == id || id.isEmpty() || id.contains(" ")) {
            return null;
        }

        // Test for presence of entryId as well as compositionId
        // delineated by '_' character
        // If entryID exists query on compositionId and entryId.

        String[] openEHRIds;

        openEHRIds = id.split("\\|");
        String compositionId = openEHRIds[0];

        String idFilter = " and a/uid/value='" + compositionId + "'";

        if (openEHRIds.length > 1)
        {
            String entryId = openEHRIds[1];
            idFilter.concat(" and b_a/uid/value='" + entryId + "'");
        }
        return getEhrJson(getAQL() + idFilter);
    }

    protected JsonNode getEhrJson(String aql) throws IOException {
        MultiValueMap<String, String> headers;
        if (isTokenAuth) {
            headers = createTokenHeaders();
        } else {
            headers = createAuthHeaders();
        }

        logger.info("AQL:  " + aql);

        String body = "{\"aql\" : \"" + aql + "\"}";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String url = this.url + "/rest/v1/query";

        ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);

        if (isTokenAuth) {
            deleteSessionToken(headers);
        }

        if (result.getStatusCode() == HttpStatus.OK) {
            JsonNode resultJson = new ObjectMapper().readTree(result.getBody());
            return resultJson.get("resultSet");
        } else {
            return null;
        }
    }

    protected String getPatientIdentifierFilterAql(TokenParam patientIdentifier) {
        String system = patientIdentifier.getSystem();
        if (system.isEmpty() || "https://fhir.nhs.uk/Id/nhs-number".equals(system)) {
            system = "uk.nhs.nhs_number";
        }
        String idFilter = " and e/ehr_status/subject/external_ref/id/value='" + patientIdentifier.getValue() +
                "' and e/ehr_status/subject/external_ref/namespace='" + system + "'";
        return idFilter;
    }

    protected String getPatientIdFilterAql(StringParam patientId) {

        String idFilter = " and e/ehr_id/value='" + patientId.getValue() + "'";

        return idFilter;
    }
    private HttpHeaders createAuthHeaders() {
        String plainCredits = username + ":" + password;
        String auth = "Basic " + new String(Base64.encodeBase64(plainCredits.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", auth);
        return headers;
    }

    private HttpHeaders createTokenHeaders() throws IOException {
        String sessionToken = getSessionToken(username, password, url + "/rest/v1/session");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Ehr-Session", sessionToken);
        return headers;
    }

    private String getSessionToken(String userName, String userPassword, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("", headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("username", userName)
                .queryParam("password", userPassword);

        ResponseEntity<String> result = new RestTemplate().exchange(
                builder.build().encode().toUri(),
                HttpMethod.POST,
                request,
                String.class);

        JsonNode resultJson = new ObjectMapper().readTree(result.getBody());
        return resultJson.get("sessionId").asText();
    }

    private void deleteSessionToken(MultiValueMap<String, String> headers) {
        HttpEntity<String> request = new HttpEntity<>("", headers);

        String url = this.url + "/rest/v1/session";
        ResponseEntity<String> result = new RestTemplate().exchange(url, HttpMethod.DELETE, request, String.class);

        JsonNode resultJson = null;
        try {
            resultJson = new ObjectMapper().readTree(result.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Should return "DELETE"
        String action = resultJson.asText("action");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIsTokenAuth(boolean tokenAuth) {
        isTokenAuth = tokenAuth;
    }
}
