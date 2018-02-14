package com.inidus.platform.fhir.openehr;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
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
public abstract class OpenEhrConnector {
    protected static final DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    private String url;
    private String username;
    private String password;
    private boolean isTokenAuth;

    {
        ISO_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Retreive all resources without filtering
     *
     * @return - AQL resultset as a JsonNode tree
     * @throws IOException
     */
    public JsonNode getAllResources() throws IOException {
        return getEhrJson(getAQL());
    }

    protected abstract String getAQL();

    /**
     * Adds the requisite AQL clause to filter the resultset to include only
     * matching logical identifiers.
     * Depennding on local policy, the CDR may label each ENTRY with a uid,
     * or rely on the encompassing compositionUid where only a single entry exists per
     * composition
     *
     * @param id - the logical FHIR resource identifier
     * @return - the AQL clause
     * @throws IOException
     */
    public JsonNode getResourceById(String id) throws IOException {
        if (null == id || id.isEmpty() || id.contains(" ")) {
            return null;
        }

        String[] openEHRIds = id.split("\\|");
        String compositionId = openEHRIds[0];

        String idFilter = " and a/uid/value='" + compositionId + "'";

        if (openEHRIds.length > 1) {
            String entryId = openEHRIds[1];
            idFilter = idFilter.concat(" and b_a/uid/value='" + entryId + "'");
        }
        return getEhrJson(getAQL() + idFilter);
    }

    /**
     * Retrieves an AQL resultset from an openEHR Ehrsscape-compliant CDR
     *
     * @param aql - the AQL string ot be sent to the CDR
     * @return the CDR /query resultset as a JsonNode tree
     * @throws IOException
     */
    protected JsonNode getEhrJson(String aql) throws IOException {
        MultiValueMap<String, String> headers;
        if (isTokenAuth) {
            headers = createTokenHeaders();
        } else {
            headers = createAuthHeaders();
        }

        // Strip any new lines from AQL
        String body = "{\"aql\" : \"" + aql.replaceAll("\n", " ") + "\"}";

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

    /**
     * Returns the AQL caluse required to filtee the /query resultSet by external
     * patient identifier e.g an NHS number
     *
     * @param patientIdentifier
     * @return - AQL clause as a string
     */
    protected String getPatientIdentifierFilterAql(TokenParam patientIdentifier) {
        String system = patientIdentifier.getSystem();
        if (system.isEmpty() || "https://fhir.nhs.uk/Id/nhs-number".equals(system)) {
            system = "uk.nhs.nhs_number";
        }
        String idFilter = " and e/ehr_status/subject/external_ref/id/value='" + patientIdentifier.getValue() +
                "' and e/ehr_status/subject/external_ref/namespace='" + system + "'";
        return idFilter;
    }

    /**
     * Retreives the AQL clause required to filter a resulSet by patient/subject logical id
     * which is the openEHR ehr.ehr_id value
     *
     * @param patientId
     * @return
     */
    protected String getPatientIdFilterAql(StringParam patientId) {
        return " and e/ehr_id/value='" + patientId.getValue() + "'";
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
