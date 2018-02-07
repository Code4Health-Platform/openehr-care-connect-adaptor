package com.inidus.platform.fhir.medication;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;


/**
 * Connects to an openEHR backend and returns selected Medication Statement data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)
@Service
public class MedicationStatementConnector extends OpenEhrConnector {

    protected String getAQL() {
        return "select" +
                "  e/ehr_id/value as ehrId," +
                "  e/ehr_status/subject/external_ref/id/value as subjectId," +
                "  e/ehr_status/subject/external_ref/namespace as subjectNamespace," +
                "  a/context/start_time/value as compositionStartTime," +
                "  a/uid/value as compositionId," +
                "  a/composer/name as composerName," +
                "  a/composer/external_ref/id/value as composerId," +
                "  a/composer/external_ref/namespace as composerNamespace," +
                "  b_a/uid/value as entryId," +
                "  b_a/activities[at0001]/description[at0002]/items[at0070] as Medication_item," +
                "  b_a/activities[at0001]/description[at0002]/items[at0009]/value/value as Overall_directions_description," +
                "  b_a/activities[at0001]/description[at0002]/items[at0173, 'Dose amount description']/value/value as Dose_amount_description," +
                "  b_a/activities[at0001]/description[at0002]/items[at0173, 'Dose timing description']/value/value as Dose_timing_description," +
                "  b_a/activities[at0001]/description[at0002]/items[at0044] as Additional_instruction," +
                "  b_a/activities[at0001]/description[at0002]/items[at0105]/value/value as Patient_information_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0107]/value/value as Monitoring_instruction_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018] as Clinical_indication," +
                "  b_a/activities[at0001]/description[at0002]/items[at0091] as Route," +
                "  b_a/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_substance.v0]/items[at0071] as Form," +
                "  b_a/activities[at0001]/description[at0002]/items[at0113]/items[openEHR-EHR-CLUSTER.medication_course_summary.v0]/items[at0001]/value/defining_code/code_string as Status_code," +
                "  b_a/activities[at0001]/description[at0002]/items[at0113]/items[openEHR-EHR-CLUSTER.medication_course_summary.v0]/items[at0028]/value/value as AssertedDate," +
                "  b_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value as Order_start_date_time," +
                "  b_a/activities[at0001]/description[at0002]/items[at0113]/items[at0013]/value/value as Order_stop_date_time" +
                " from EHR e" +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.medication_list.v0]" +
                " contains" +
                "    INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.medication_order.v1]" +
                " WHERE a/name/value = 'Medication statement list'";
    }


    public JsonNode getFilteredMedicationStatements(
            StringParam patientId,
            TokenParam patientIdentifier,
            StringParam status,
            DateRangeParam dateStarted
    ) throws IOException {

        String filter = "";

        // patient identifier provided
        if (null != patientIdentifier) {
            filter += getPatientIdentifierFilterAql(patientIdentifier);
        }

        // patient id provided
        if (null != patientId) {
            filter += getPatientIdFilterAql(patientId);
        }

        // category provided
        if (null != status) {
            filter += getMedicationStatusFilterAql(status);
        }

        // date filter provided
        if (null != dateStarted) {
            filter += getdateStartedFilterAql(dateStarted);
        }

        return getEhrJson(getAQL() + filter);
    }

    private String getdateStartedFilterAql(DateRangeParam dateStarted) {
        String filter = "";
        Date fromDate = dateStarted.getLowerBoundAsInstant();

        if (null != fromDate) {
            String from = ISO_DATE.format(fromDate);
            filter += String.format(" and b_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value >= '%s'", from);
        }

        Date toDate = dateStarted.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
            filter += String.format(" and b_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value <= '%s'", to);
        }

        return filter;
    }

    private String getMedicationStatusFilterAql(StringParam statusParam) {
        String openEHRCode = "";
        String statusCodeParam = statusParam.getValue();

        if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.ACTIVE.toCode())) {
            openEHRCode = "'at0021' , 'at0027'";
        } else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.STOPPED.toCode())) {
            openEHRCode = "'at0022', 'at0025'";
        } else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.INTENDED.toCode())) {
            openEHRCode = "'at0023'";
        } else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.COMPLETED.toCode())) {
            openEHRCode = "'at0024'";
        } else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.ONHOLD.toCode())) {
            openEHRCode = "'at0026'";
        }

        if (!openEHRCode.isEmpty())
            return String.format(" and b_a/activities[at0001]/description[at0002]/items[at0113]/items[openEHR-EHR-CLUSTER.medication_course_summary.v0]/items[at0001]/value/defining_code/code_string matches {%s}", openEHRCode);
        else
            return "";
    }
}
