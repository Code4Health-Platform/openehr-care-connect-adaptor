package com.inidus.platform.fhir.openehr;

import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.*;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenEHRConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected Date convertAssertedDate(JsonNode ehrJson) {

        //Test explicitly for 'AssertedDAte as it may not always exist in the resultset
        JsonNode dateElement = ehrJson.get("AssertedDate");
        String dateString = null;
        if (dateElement != null)
            dateString = dateElement.textValue();

        if (null == dateString) {
            dateString = ehrJson.get("compositionStartTime").textValue();
        }
        return (DatatypeConverter.parseDateTime(dateString).getTime());
    }

    protected Practitioner convertAsserter(JsonNode ehrJson) {

        // Convert Composer name and ID.
        Practitioner asserter = new Practitioner();
        asserter.setId("Practitioner/#composer");

        String asserterName = ehrJson.get("composerName").textValue();
        if (null != asserterName) {
            asserter.addName().setText(asserterName);
        }

        // Not supported by EtherCis - causes exception
        //        String asserterID = ehrJson.get("composerId").textValue();
        //        if (null != asserterID) {
        //
        //            Identifier id = asserter.addIdentifier();
        //            id.setValue(asserterID);
        //
        //            String asserterNamespace = ehrJson.get("composerNamespace").textValue();
        //            if (null != asserterNamespace) {
        //                id.setSystem(asserterNamespace);
        //
        //            }
        //        }

        return asserter;
    }

    protected Reference convertPatientReference(JsonNode ehrJson) {
        Reference reference = new Reference();
        reference.setReference("Patient/" + ehrJson.get("ehrId").textValue());
        reference.setIdentifier(convertPatientIdentifier(ehrJson));
        return reference;
    }

    protected String convertResourceId(JsonNode ehrJson) {
        String entryId = ehrJson.get("entryId").textValue();
        String compositionId = ehrJson.get("compositionId").textValue();
        if (entryId == null)
            return compositionId;
        else {
            return compositionId + "|" + entryId;
        }
    }

    protected Identifier convertPatientIdentifier(JsonNode ehrJson) {
        Identifier identifier = new Identifier();
        identifier.setValue(ehrJson.get("subjectId").textValue());
        identifier.setSystem(convertPatientIdentifierSystem(ehrJson));
        return identifier;
    }

    protected String convertPatientIdentifierSystem(JsonNode ehrJson) {
        String subjectIdNamespace = ehrJson.get("subjectNamespace").textValue();
        if ("uk.nhs.nhs_number".equals(subjectIdNamespace)) {
            return "https://fhir.nhs.uk/Id/nhs-number";
        } else {
            return subjectIdNamespace;
        }
    }

    protected CodeableConcept convertScalarCodableConcept(JsonNode ehrJson, String scalarElementName) {

        String value = getResultsetString(ehrJson, scalarElementName + "_value");
        String terminology = getResultsetString(ehrJson, scalarElementName + "_terminology");
        String code = getResultsetString(ehrJson, scalarElementName + "_code");


        if (null != terminology && null != code) {
            DvCodedText openEHRCodeable = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(openEHRCodeable);
        } else if (null != value) {
            return new CodeableConcept().setText(value);
        } else
            return null;
    }


    protected List<CodeableConcept> convertCodeableConceptList(JsonNode ehrJson, String nodeName) {

        List<CodeableConcept> resultList = new ArrayList<>();
        JsonNode codeables;

        codeables = ehrJson.get(nodeName);

        if (codeables != null && codeables.isContainerNode()) {
            logger.info("JSONNode : "+ codeables.toString());
            for (JsonNode codeableItem: codeables) {
                resultList.add(getCodeableConceptObject(codeableItem));
            }
        }
        else
            resultList.add(convertScalarCodableConcept(ehrJson, nodeName));

        return resultList;
    }

    protected CodeableConcept convertCodeableConcept(JsonNode ehrJson, String nodeName) {

        JsonNode codeables = ehrJson.get(nodeName);
        logger.info("JSON Value Node : "+ codeables.toString());
        if (codeables != null && codeables.isContainerNode()) {
            return getCodeableConceptObject(codeables.get("value"));
        }

        else
        {
            return (convertScalarCodableConcept(ehrJson,nodeName));
        }

    }

    private CodeableConcept getCodeableConceptObject(JsonNode codeableItem) {
        String terminology = null; String code = null;
        String value;
        logger.info("JSON Item Node : "+ codeableItem.toString());
        value = codeableItem.get("value").textValue();
        String datatype = codeableItem.get("@class").textValue();

        if (datatype.equals("DV_CODED_TEXT")) {
            terminology = codeableItem.get("defining_code").get("terminology_id").get("value").textValue();
            code = codeableItem.get("defining_code").get("code_string").textValue();
        }

        if (null != terminology && null != code)
            return DfText.convertToCodeableConcept(new DvCodedText(value, terminology,code));
        else
            return DfText.convertToCodeableConcept(new DvText(value));
    }

    /**
     Converts the value of an openEHR Resultset node into a string,
     ensuring that a null is returned if the value or the node itself is null.
     This ensures that HAPI-FHIR will ignore attempts to set a value.
     * @param ehrJson Resultset Tree
     * @param nodeName
     * @return
     */
    protected String getResultsetString(JsonNode ehrJson, String nodeName) {
        String nodeString = null;
        JsonNode node = ehrJson.get(nodeName);

        if (node != null) {
            nodeString = node.textValue();
            //Ensure that emptyString are marked as null which prevetns them being stored in FHIR
            if (nodeString != null && nodeString.isEmpty())
                nodeString = null;
        }
        return nodeString;
    }


    /**
     Converts an openEHR ISO Date String into a FHIR DateTimeType, used where the DateTime is a choice
     * @param ehrJson
     * @param nodeName
     * @return
     */
    protected DateTimeType convertChoiceDate(JsonNode ehrJson, String nodeName){
        String onsetDate = getResultsetString(ehrJson, nodeName);
        if (null != onsetDate) {
            return new DateTimeType(onsetDate);
        }
        else return null;
    }

}
