package com.inidus.platform.openehr;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inidus.platform.OpenEhrConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;

@Service
public class MarandConnector implements OpenEhrService {
    private static final String AQL = "select" +
            " e/ehr_id/value as ehrId," +
            " e/ehr_status/subject/external_ref/id/value as subjectId," +
            " e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
            " a/uid/value as compositionId," +
            " b_a/uid/value as entryId," +
            " b_a/data[at0001]/items[at0002]/value/value as Causative_agent_value," +
            " b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Causative_agent_code," +
            " b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as Causative_agent_terminology," +
            " b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code," +
            " b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code," +
            " b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code," +
            " b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction," +
            " b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code," +
            " b_a/data[at0001]/items[at0006]/value/value as Comment," +
            " b_a/protocol[at0042]/items[at0062]/value/value as Adverse_reaction_risk_Last_updated," +
            " b_a/data[at0001]/items[at0009]/items[at0010]/value/value as Specific_substance_value," +
            " b_a/data[at0001]/items[at0009]/items[at0010]/value/defining_code/code_string as Specific_substance_code," +
            " b_a/data[at0001]/items[at0009]/items[at0010]/value/defining_code/terminology_id/value as Specific_substance_terminology," +
            " b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code," +
            " b_a/data[at0001]/items[at0009]/items[at0011]/value/value as Manifestation_value,    " +
            " b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/code_string as Manifestation_code," +
            " b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/terminology_id/value as Manifestation_terminology," +
            " b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description," +
            " b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction," +
            " b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code," +
            " b_a/data[at0001]/items[at0009]/items[at0106]/value/value as Route_of_exposure_value," +
            " b_a/data[at0001]/items[at0009]/items[at0106]/value/defining_code/code_string as Route_of_exposure_code," +
            " b_a/data[at0001]/items[at0009]/items[at0106]/value/defining_code/terminology_id/value as Route_of_exposure_terminology," +
            " b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment" +
            " from EHR e" +
            " contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]" +
            " contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]" +
            " where a/name/value='Adverse reaction list'";

    //    private static final String URL = "https://cdr.code4health.org/rest/v1/query";
    private static final String URL = "https://test.operon.systems/rest/v1/query";
    private static final String AUTH = "Basic b3Bybl9oY2JveDpYaW9UQUpvTzQ3OQ==";

    @Override
    public JsonNode getAllAllergies() throws IOException {
        return getEhrJson(AQL);
    }

    @Override
    public JsonNode getAllergyById(String id) throws IOException {
        if (null == id || id.isEmpty() || id.contains(" ")) {
            return null;
        }
        String idFilter = " and b_a/uid/value='" + id + "'";
        return getEhrJson(AQL + idFilter);

    }

    @Override
    public JsonNode getFilteredAllergy(TokenParam patientIdentifier, DateRangeParam adverseReactionRiskLastUpdated) throws IOException {
        String filter = "";

        // patient identifier provided
        if (null != patientIdentifier) {
            filter += getPatientIdentifierFilterAql(patientIdentifier);
        }

        // date filter provided
        if (null != adverseReactionRiskLastUpdated) {
            filter += getLastUpdatedFilterAql(adverseReactionRiskLastUpdated);
        }

        return getEhrJson(AQL + filter);
    }

    private JsonNode getEhrJson(String aql) throws IOException {
        String body = "{\"aql\" : \"" + aql + "\"}";
        HttpEntity<String> postEntity = new HttpEntity<>(body, createHttpHeaders());
        ResponseEntity<String> result = new RestTemplate().exchange(URL, HttpMethod.POST, postEntity, String.class);
        if (result.getStatusCode() == HttpStatus.OK) {
            JsonNode resultJson = new ObjectMapper().readTree(result.getBody());
            return resultJson.get("resultSet");
        } else {
            return null;
        }
    }

    private String getLastUpdatedFilterAql(DateRangeParam adverseReactionRiskLastUpdated) {
        String filter = "";
        Date fromDate = adverseReactionRiskLastUpdated.getLowerBoundAsInstant();
        if (null != fromDate) {
            String from = OpenEhrConverter.MARAND_DATE_FORMAT.format(fromDate);
            filter += String.format(" and b_a/protocol[at0042]/items[at0062]/value/value >= '%s'", from);
        }

        Date toDate = adverseReactionRiskLastUpdated.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = OpenEhrConverter.MARAND_DATE_FORMAT.format(toDate);
            filter += String.format(" and b_a/protocol[at0042]/items[at0062]/value/value <= '%s'", to);
        }
        return filter;
    }

    private String getPatientIdentifierFilterAql(TokenParam patientIdentifier) {
        String system = patientIdentifier.getSystem();
        if (system.isEmpty() || "https://fhir.nhs.uk/Id/nhs-number".equals(system)) {
            system = "uk.nhs.nhs_number";
        }
        String idFilter = " and e/ehr_status/subject/external_ref/id/value='" + patientIdentifier.getValue() +
                "' and e/ehr_status/subject/external_ref/namespace='" + system + "'";
        return idFilter;
    }


    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", AUTH);
        return headers;
    }
}
