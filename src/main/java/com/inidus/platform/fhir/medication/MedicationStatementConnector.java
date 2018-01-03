package com.inidus.platform.fhir.medication;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;


/**
 * Connects to an openEHR backend and returns selected ProblemDiagnosis data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)@Service
public class MedicationStatementConnector extends OpenEhrConnector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected String getAQL() {

        return "select" +
                "  e/ehr_id/value as ehrId," +
                "  e/ehr_status/subject/external_ref/id/value as subjectId," +
                "  e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
                "  a/context/start_time/value as compositionStartTime," +
                "  a/uid/value as compositionId," +
                "  a/composer/name as composerName," +
                "  b_a/uid/value as entryId," +
                "  b_a/activities[at0001]/description[at0002]/items[at0070]/value/value as Medication_item_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0070]/value/defining_code/code_string as Medication_item_code," +
                "  b_a/activities[at0001]/description[at0002]/items[at0070]/value/defining_code/terminology_id/value as Medication_item_terminology," +
                "  b_a/activities[at0001]/description[at0002]/items[at0009]/value/value as Overall_directions_description," +
                "  b_a/activities[at0001]/description[at0002]/items[at0173, 'Dose amount description']/value/value as Dose_amount_description," +
                "  b_a/activities[at0001]/description[at0002]/items[at0173, 'Dose timing description']/value/value as Dose_timing_description," +
                "  b_a/activities[at0001]/description[at0002]/items[at0044]/value/value as Additional_instruction," +
                "  b_a/activities[at0001]/description[at0002]/items[at0105]/value/value as Patient_information," +
                "  b_a/activities[at0001]/description[at0002]/items[at0107]/value/value as Monitoring_instruction," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018]/value/value as Clinical_indication_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018]/value/defining_code/code_string as Clinical_indication_code," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018]/value/terminology_id/value as Clinical_indication_terminology" +
                " from EHR e" +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.medication_list.v0]" +
                " contains INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.medication_order.v1]";
    }

    public MedicationStatementConnector() throws IOException {

    }


    public JsonNode getFilteredMedicationStatments(
         //   StringParam listParam,
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
            filter += String.format(" and a/context/start_time/value >= '%s'", from);
        }

        Date toDate = conditionAsserted.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
            filter += String.format(" and a/context/start_time/value <= '%s'", to);
        }
        return filter;
    }

    private String getConditionCategoryFilterAql(StringParam categoryParam) {
        if (categoryParam.getValue().equals("problem-list-item")) {
            //Dummy AQL line to prevent non problem list items from being returned
            return "";
        }
        else
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

        if (!code.isEmpty() && !element.isEmpty()
)           return String.format(" and b_a/data[at0001]/items[openEHR-EHR-CLUSTER.problem_status.v0]/items[%s]/value/defining_code/code_string matches {%s}", element,code);
        else
            return "";
    }
}
