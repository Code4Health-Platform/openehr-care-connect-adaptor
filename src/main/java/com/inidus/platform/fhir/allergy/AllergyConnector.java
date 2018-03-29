package com.inidus.platform.fhir.allergy;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
 * Connects to an openEHR backend and returns selected data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)
@Service()
public class AllergyConnector extends OpenEhrConnector {
    protected String getAQL() {
        return "select" +
                " e/ehr_id/value as ehrId," +
                " e/ehr_status/subject/external_ref/id/value as subjectId," +
                " e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
                " a/context/start_time/value as compositionStartTime," +
                " a/uid/value as compositionId," +
                " a/composer/name as composerName," +
                " a/composer/external_ref/id/value as composerId," +
                " a/composer/external_ref/namespace as composerNamespace," +
                " b_a/uid/value as entryId," +
                " b_a/protocol[at0042]/items[at0062]/value/value as AssertedDate," +
                " b_a/data[at0001]/items[at0002]/value/value as Causative_agent_value," +
                " b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Causative_agent_code," +
                " b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as Causative_agent_terminology," +
                " b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code," +
                " b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code," +
                " b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code," +
                " b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction," +
                " b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code," +
                " b_a/data[at0001]/items[at0006]/value/value as Comment," +
                " b_a/data[at0001]/items[at0009]/items[at0010] as Specific_substance," +
                " b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code," +
                " b_a/data[at0001]/items[at0009]/items[at0011]/value/value as Manifestation_value,    " +
                " b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/code_string as Manifestation_code, " +
                " b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/terminology_id/value as Manifestation_terminology, " +
                " b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description," +
                " b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction," +
                " b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code," +
                " b_a/data[at0001]/items[at0009]/items[at0106] as Route_of_exposure," +
                " b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment" +
                " from EHR e" +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]" +
                " contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]" +
                " where a/name/value='Adverse reaction list'";
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

        String aql = getAQL() + filter;
        return getEhrJson(aql);

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