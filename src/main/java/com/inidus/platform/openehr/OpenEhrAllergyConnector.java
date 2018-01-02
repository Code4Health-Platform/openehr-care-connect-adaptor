package com.inidus.platform.openehr;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inidus.platform.conversion.AllergyIntoleranceCategory;
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
import java.util.Date;
import java.util.TimeZone;

/**
 * Connects to an openEHR backend and returns selected data
 */
@Service
public class OpenEhrAllergyConnector extends OpenEhrConnector {
    protected String getAQL() {
        return "select" +
                " e/ehr_id/value as ehrId," +
                " e/ehr_status/subject/external_ref/id/value as subjectId," +
                " e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
                " a/uid/value as compositionId," +
                " a/context/start_time/value as compositionStartTime," +
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
    }

    public OpenEhrAllergyConnector() throws IOException {
    }

//    public JsonNode getAllAllergies() throws IOException {
//        return getEhrJson(AQL);
//    }

    public JsonNode getAllergyById(String id) throws IOException {
        return getResourceById(id);
    }

    public JsonNode getFilteredAllergies(
            TokenParam patientIdentifier,
            StringParam category,
            DateRangeParam adverseReactionRiskLastUpdated) throws IOException {
        String filter = "";

        // patient identifier provided
        if (null != patientIdentifier) {
            filter += getPatientIdentifierFilterAql(patientIdentifier);
        }

        // category provided
        if (null != category) {
            filter += getCategoryFilterAql(category);
        }

        // date filter provided
        if (null != adverseReactionRiskLastUpdated) {
            filter += getLastUpdatedFilterAql(adverseReactionRiskLastUpdated);
        }

        return getEhrJson(getAQL() + filter);
    }

    private String getLastUpdatedFilterAql(DateRangeParam adverseReactionRiskLastUpdated) {
        String filter = "";
        Date fromDate = adverseReactionRiskLastUpdated.getLowerBoundAsInstant();
        if (null != fromDate) {
            String from = ISO_DATE.format(fromDate);
            filter += String.format(" and b_a/protocol[at0042]/items[at0062]/value/value >= '%s'", from);
        }

        Date toDate = adverseReactionRiskLastUpdated.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
            filter += String.format(" and b_a/protocol[at0042]/items[at0062]/value/value <= '%s'", to);
        }
        return filter;
    }

    private String getCategoryFilterAql(StringParam categoryParam) {
        String code = AllergyIntoleranceCategory.convertToEhr(categoryParam.getValue());
        return String.format(" and b_a/data[at0001]/items[at0120]/value/defining_code/code_string = '%s'", code);
    }
}