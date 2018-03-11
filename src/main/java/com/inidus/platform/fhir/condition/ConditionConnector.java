package com.inidus.platform.fhir.condition;

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
 * Connects to an openEHR backend and returns selected ProblemDiagnosis data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)
@Service
public class ConditionConnector extends OpenEhrConnector {
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
                " b_a/data[at0001]/items[at0002]/value/value as Problem_Diagnosis_value," +
                " b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Problem_Diagnosis_code," +
                " b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as Problem_Diagnosis_terminology," +
                " b_a/data[at0001]/items[at0012]/value/value as Body_site_value," +
                " b_a/data[at0001]/items[at0012]/value/defining_code/code_string as Body_site_code," +
                " b_a/data[at0001]/items[at0012]/value/defining_code/terminology_id/value as Body_site_terminology," +
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
                " EVALUATION b_a[openEHR-EHR-EVALUATION.problem_diagnosis.v1] or" +
                " CLUSTER b_b[openEHR-EHR-CLUSTER.problem_status.v0])" +
                " where a/name/value='Problem list'";
    }

    public JsonNode getFilteredConditions(
            StringParam patientId,
            TokenParam patientIdentifier,
            StringParam category,
            StringParam clinical_status,
            DateRangeParam conditionLastAsserted
    ) throws IOException {

        String filter = "";

        // patient identifier provided
        if (null != patientIdentifier) {
            filter += getPatientIdentifierFilterAql(patientIdentifier);
        }

        // patient identifier provided
        if (null != patientId) {
            filter += getPatientIdFilterAql(patientId);
        }


        // category provided
        if (null != category) {
            filter += getConditionCategoryFilterAql(category);
        }

        // category provided
        if (null != clinical_status) {
            filter += getClinicalStatusFilterAql(clinical_status);
        }

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

    private String getConditionCategoryFilterAql(StringParam categoryParam) {
        if (categoryParam.getValue().equals("problem-list-item")) {
            //Dummy AQL line to prevent non problem list items from being returned
            return "";
        } else
            return " and a/name/value = ''";
    }

    private String getClinicalStatusFilterAql(StringParam statusParam) {
        String code = "";
        String element = "";
        switch (statusParam.getValue()) {
            case "active":
                code = "'at0026','at0085','at0086'";
                element = "at0003";
                break;
            case "inactive":
                code = "'at0027'";
                element = "at0003";
                break;
            case "resolved":
                code = "'at0084'";
                element = "at0083";
                break;
        }

        if (!code.isEmpty() && !element.isEmpty()) {
            return String.format(" and b_a/data[at0001]/items[openEHR-EHR-CLUSTER.problem_status.v0]/items[%s]/value/defining_code/code_string matches {%s}", element, code);
        } else {
            return "";
        }
    }
}
