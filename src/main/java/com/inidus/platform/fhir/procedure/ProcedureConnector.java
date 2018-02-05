package com.inidus.platform.fhir.procedure;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;


/**
 * Connects to an openEHR backend and returns selected Medication Statement data
 */
@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)@Service
public class ProcedureConnector extends OpenEhrConnector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected String getAQL() {
        String aql = "";
        try {
            aql = readFileContent(resourcesRootPath + "aql/procedures.aql").replaceAll("\n", " ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aql;
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

 //       logger.debug("AQL... =" + getAQL() + filter);
        return getEhrJson(getAQL() + filter);
    }

    private String getDatePerformed(DateRangeParam datePerformed) {
        String filter = "";
        Date fromDate = datePerformed.getLowerBoundAsInstant();

        if (null != fromDate) {
            String from = ISO_DATE.format(fromDate);
     //       logger.debug("fromDate: " + from);
            filter += String.format(" and b_a/time/value >= '%s'", from);
        }

        Date toDate = datePerformed.getUpperBoundAsInstant();
        if (null != toDate) {
            String to = ISO_DATE.format(toDate);
     //       logger.debug("fromDate: " + to);
            filter += String.format(" and b_a/time/value <= '%s'", to);
        }

        return filter;
    }

    private String getProcedureStatusFilterAql(StringParam statusParam) {

        String openEHRCode = "";


        String statusCodeParam = statusParam.getValue();

        if (statusCodeParam.equals(Procedure.ProcedureStatus.PREPARATION.toCode()))
        {
            openEHRCode = "'524','526'";
        }
        else if (statusCodeParam.equals(Procedure.ProcedureStatus.INPROGRESS.toCode()))
        {
            openEHRCode = "'245'";
        }
        else if (statusCodeParam.equals(Procedure.ProcedureStatus.SUSPENDED.toCode()))
        {
            openEHRCode = "'527', '530'";
        }
        else if (statusCodeParam.equals(Procedure.ProcedureStatus.ABORTED.toCode()))
        {
            openEHRCode = "'528' , '531', '533'";
        }
        else if (statusCodeParam.equals(Procedure.ProcedureStatus.COMPLETED.toCode()))
        {
            openEHRCode = "'532'";
        }


        if (!openEHRCode.isEmpty())
            return String.format("and b_a/ism_transition/current_state/defining_code/code_string matches {%s}", openEHRCode);
        else
            return "";
    }
}
