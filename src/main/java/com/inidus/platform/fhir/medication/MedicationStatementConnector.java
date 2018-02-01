package com.inidus.platform.fhir.medication;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import org.hl7.fhir.dstu3.model.MedicationStatement;
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
                "  b_a/activities[at0001]/description[at0002]/items[at0044]/value/value as Additional_instruction_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0044]/value/defining_code/code_string as Additional_instruction_code," +
                "  b_a/activities[at0001]/description[at0002]/items[at0044]/value/defining_code/terminology_id/value as Additional_instruction_terminology," +
                "  b_a/activities[at0001]/description[at0002]/items[at0105]/value/value as Patient_information_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0107]/value/value as Monitoring_instruction_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018]/value/value as Clinical_indication_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018]/value/defining_code/code_string as Clinical_indication_code," +
                "  b_a/activities[at0001]/description[at0002]/items[at0018]/value/terminology_id/value as Clinical_indication_terminology," +
                "  b_a/activities[at0001]/description[at0002]/items[at0091]/value/value as Route_value," +
                "  b_a/activities[at0001]/description[at0002]/items[at0091]/value/defining_code/code_string as Route_code," +
                "  b_a/activities[at0001]/description[at0002]/items[at0091]/value/defining_code/terminology_id/value as Route_terminology," +
                "  b_a/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_substance.v0]/items[at0071]/value/value as Form_value," +
                "  b_a/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_substance.v0]/items[at0071]/value/defining_code/code_string as Form_code," +
                "  b_a/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_substance.v0]/items[at0071]/value/defining_code/terminology_id/value as Form_terminology," +
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

 //       logger.debug("AQL... =" + getAQL() + filter);
        return getEhrJson(getAQL() + filter);
    }

    private String getdateStartedFilterAql(DateRangeParam dateStarted) {
        String filter = "";
        Date fromDate = dateStarted.getLowerBoundAsInstant();

        if (null != fromDate) {
            String from = ISO_DATE.format(fromDate);
     //       logger.debug("fromDate: " + from);
            filter += String.format(" and b_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value >= '%s'", from);
        }

        Date toDate = dateStarted.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
     //       logger.debug("fromDate: " + to);
            filter += String.format(" and b_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value <= '%s'", to);
        }

        logger.debug("Filter: " + filter);
        return filter;
    }

    private String getMedicationStatusFilterAql(StringParam statusParam) {
        String openEHRCode = "";

/*
        at0021::Active [This is an active medication.]
        at0022::Stopped [This is a medication that has previously been issued, dispensed or administered but has now been discontinued.]
        at0023::Never active [A medication which was ordered or authorised but has been cancelled prior to being issued, dispensed or adiminstered.]
        at0024::Completed [The medication course has been completed.]
        at0025::Obsolete [This medication order has been superseded by another.]
        at0026::Suspended [Actions resulting from the order are to be temporarily halted, but are expected to continue later. May also be called 'on-hold'.]
        at0027::Draft [The medication order has been made but further processes e.g. sign-off or verification are required before it becomes actionable.]
    */
        String statusCodeParam = statusParam.getValue();

        if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.ACTIVE.toCode()))
        {
            openEHRCode = "'at0021' , 'at0027'";
        }
        else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.STOPPED.toCode()))
        {
            openEHRCode = "'at0022', 'at0025'";
        }
        else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.INTENDED.toCode()))
        {
            openEHRCode = "'at0023'";
        }
        else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.COMPLETED.toCode()))
        {
            openEHRCode = "'at0024'";
        }
        else if (statusCodeParam.equals(MedicationStatement.MedicationStatementStatus.ONHOLD.toCode()))
        {
            openEHRCode = "'at0026'";
        }

        if (!openEHRCode.isEmpty())
            return String.format(" and b_a/activities[at0001]/description[at0002]/items[at0113]/items[openEHR-EHR-CLUSTER.medication_course_summary.v0]/items[at0001]/value/defining_code/code_string matches {%s}", openEHRCode);
        else
            return "";
    }
}
