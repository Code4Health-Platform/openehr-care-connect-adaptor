package com.inidus.platform.openehr;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Date;


/**
 * Connects to an openEHR backend and returns selected ProblemDiagnosis data
 */
@Service
public class OpenEhrConditionConnector extends OpenEhrConnector {
    protected String getAQL() {
        return "select" +
                " e/ehr_id/value as ehrId," +
                " e/ehr_status/subject/external_ref/id/value as subjectId," +
                " e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
                " a/context/start_time/value as compositionStartTime," +
                " a/uid/value as compositionId," +
                " a/composer/name as composerName,"+
                " a/composer/external_ref/id/value as composerId," +
                " a/composer/external_ref/namespace as composerNamespace," +
                " b_a/uid/value as entryId," +
                " b_a/data[at0001]/items[at0002]/value/value as Problem_Diagnosis_value," +
                " b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Problem_Diagnosis_code," +
                " b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as Problem_Diagnosis_terminology," +
                " b_a/data[at0001]/items[at0012]/value/value as Body_site_value," +
                " b_a/data[at0001]/items[at0012]/value/defining_code/code_string as Body_site_code," +
                " b_a/data[at0001]/items[at0012]/value/defining_code/terminology_id as Body_site_terminology," +
                " b_a/data[at0001]/items[at0077]/value/value as Date_time_of_onset," +
                " b_a/data[at0001]/items[at0030]/value/value as Date_time_of_resolution," +
                " b_a/data[at0001]/items[at0005]/value/defining_code/code_string as Severity_code," +
                " b_a/data[at0001]/items[at0073]/value/defining_code/code_string as Diagnostic_certainty_code," +
                " b_a/data[at0001]/items[at0069]/value/value as Comment," +
                " b_a/protocol[at0032]/items[at0070]/value/value as AssertedDate," +
                " b_b/items[at0003]/value/defining_code/code_string as Active_inactive_code," +
                " b_b/items[at0083]/value/defining_code/code_string as Resolution_status_code," +
                " b_b/items[at0001]/value/defining_code/code_string as Episodicity_code," +
                " b_b/items[at0071]/value/value as First_occurrence" +
                " from EHR e" +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.problem_list.v1]" +
                " contains (" +
                "  EVALUATION b_a[openEHR-EHR-EVALUATION.problem_diagnosis.v1] or" +
                "  CLUSTER b_b[openEHR-EHR-CLUSTER.problem_status.v0])" +
                " where a/name/value='Problem list'";
    }

    public OpenEhrConditionConnector() throws IOException {

    }

    public JsonNode getAllConditions() throws IOException {
        return getAllResources();
    }

    public JsonNode getConditionById(String id) throws IOException {
        return getResourceById(id);
    }

    public JsonNode getFilteredConditions(
            TokenParam patientIdentifier,
            StringParam category,
            DateRangeParam conditionLastAsserted) throws IOException {

        String filter = "";

        // patient identifier provided
        if (null != patientIdentifier) {
            filter += getPatientIdentifierFilterAql(patientIdentifier);
        }

        // category provided
    //    if (null != category) {
    //        filter += getCategoryFilterAql(category);
    //    }

        // date filter provided
        if (null != conditionLastAsserted) {
            filter += getLastAssertedFilterAql(conditionLastAsserted);
        }

        return getEhrJson(getAQL() + filter);
    }

    private String getLastAssertedFilterAql(DateRangeParam conditionAsserted) {
        String filter = "";
        Date fromDate = conditionAsserted.getLowerBoundAsInstant();
        if (null != fromDate) {
            String from = ISO_DATE.format(fromDate);
            filter += String.format(" and b_a/protocol[at0032]/items[at0070]/value/value >= '%s'", from);
        }

        Date toDate = conditionAsserted.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
            filter += String.format(" and b_a/protocol[at0032]/items[at0070]/value/value <= '%s'", to);
        }
        return filter;
    }
}
