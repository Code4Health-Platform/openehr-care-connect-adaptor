package com.inidus.platform.fhir.openehr;

import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;

public class OpenEHRConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Date convertAssertedDate(JsonNode ehrJson) {
        String dateString = ehrJson.get("AssertedDate").textValue();
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

        String value = ehrJson.get(scalarElementName + "_value").textValue();
        String terminology = ehrJson.get(scalarElementName + "_terminology").textValue();
        String code = ehrJson.get(scalarElementName + "_code").textValue();

        if (null != terminology && null != code) {
            DvCodedText openEHRCodeable = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(openEHRCodeable);
        } else {
            return new CodeableConcept().setText(value);
        }
    }
}
