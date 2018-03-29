package com.inidus.platform.fhir.procedure;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import org.hl7.fhir.dstu3.model.Procedure;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;


/**
 * Connects to an openEHR backend and returns selected Medication Statement data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)
@Service
public class ProcedureConnector extends OpenEhrConnector {
    protected String getAQL() {
        return "select\n" +
                "   e/ehr_id/value as ehrId,\n" +
                "   e/ehr_status/subject/external_ref/id/value as subjectId,\n" +
                "   e/ehr_status/subject/external_ref/namespace as subjectNamespace,\n" +
                "   a/context/start_time/value as compositionStartTime,\n" +
                "   a/uid/value as compositionId,\n" +
                "   a/composer/name as composerName,\n" +
                "   a/composer/external_ref/id/value as composerId,\n" +
                "   a/composer/external_ref/namespace as composerNamespace,\n" +
                "   b_a/uid/value as entryId,\n" +
                "   b_a/description[at0001]/items[at0002]/value/value as Procedure_name_value,\n" +
                "   b_a/description[at0001]/items[at0002]/value/defining_code/code_string as Procedure_name_code,\n" +
                "   b_a/description[at0001]/items[at0002]/value/defining_code/terminology_id/value as Procedure_name_terminology,\n" +
                "   b_a/description[at0001]/items[at0048]/value/value as Outcome_value,\n" +
                "   b_a/description[at0001]/items[at0048]/value/defining_code/code_string as Outcome_code,\n" +
                "   b_a/description[at0001]/items[at0048]/value/defining_code/terminology_id/value as Outcome_terminology,\n" +
                "   b_a/description[at0001]/items[at0006]/value/value as Complication_value,\n" +
                "   b_a/description[at0001]/items[at0006]/value/defining_code/code_string as Complication_code,\n" +
                "   b_a/description[at0001]/items[at0006]/value/defining_code/terminology_id/value as Complication_terminology,\n" +
                "   b_a/description[at0001]/items[at0067] as Procedure_type,\n" +
                "   b_a/description[at0001]/items[at0063]/value/value as Body_site_value,\n" +
                "   b_a/description[at0001]/items[at0063]/value/defining_code/code_string as Body_site_code,\n" +
                "   b_a/description[at0001]/items[at0063]/value/defining_code/terminology_id/value as Body_site_terminology,\n" +
                "   b_a/description[at0001]/items[at0014]/value/value as Reason_value,\n" +
                "   b_a/description[at0001]/items[at0014]/value/defining_code/code_string as Reason_code,\n" +
                "   b_a/description[at0001]/items[at0014]/value/defining_code/terminology_id/value as Reason_terminology,\n" +
                "   b_a/description[at0001]/items[at0005]/value/value as Comment,\n" +
                "   b_a/other_participations as OtherParticipations,\n" +
                "   b_a/time/value as Procedure_time,\n" +
                "   b_a/ism_transition/current_state/defining_code/code_string as Status_code,\n" +
                "   b_a/ism_transition/careflow_step/defining_code/code_string as Careflow_step_code,\n" +
                "   b_a/description[at0001]/items[at0049]/value/value as Description\n" +
                "from EHR e\n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.health_summary.v1]\n" +
                "contains ACTION b_a[openEHR-EHR-ACTION.procedure.v1]\n" +
                "where a/name/value='Procedures list'";
    }


    public JsonNode getFilteredProcedures(
            StringParam patientId,
            TokenParam patientIdentifier,
            StringParam status,
            DateRangeParam datePerformed
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
            filter += getProcedureStatusFilterAql(status);
        }

        // date filter provided
        if (null != datePerformed) {
            filter += getDatePerformed(datePerformed);
        }

        return getEhrJson(getAQL() + filter);
    }

    private String getDatePerformed(DateRangeParam datePerformed) {
        String filter = "";
        Date fromDate = datePerformed.getLowerBoundAsInstant();

        if (null != fromDate) {
            String from = ISO_DATE.format(fromDate);
            filter += String.format(" and b_a/time/value >= '%s'", from);
        }

        Date toDate = datePerformed.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
            filter += String.format(" and b_a/time/value <= '%s'", to);
        }

        return filter;
    }

    private String getProcedureStatusFilterAql(StringParam statusParam) {
        String openEHRCode = "";

        String statusCodeParam = statusParam.getValue();

        if (statusCodeParam.equals(Procedure.ProcedureStatus.PREPARATION.toCode())) {
            openEHRCode = "'524','526'";
        } else if (statusCodeParam.equals(Procedure.ProcedureStatus.INPROGRESS.toCode())) {
            openEHRCode = "'245'";
        } else if (statusCodeParam.equals(Procedure.ProcedureStatus.SUSPENDED.toCode())) {
            openEHRCode = "'527', '530'";
        } else if (statusCodeParam.equals(Procedure.ProcedureStatus.ABORTED.toCode())) {
            openEHRCode = "'528' , '531', '533'";
        } else if (statusCodeParam.equals(Procedure.ProcedureStatus.COMPLETED.toCode())) {
            openEHRCode = "'532'";
        }

        if (!openEHRCode.isEmpty())
            return String.format("and b_a/ism_transition/current_state/defining_code/code_string matches {%s}", openEHRCode);
        else
            return "";
    }
}
