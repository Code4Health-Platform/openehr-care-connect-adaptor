package com.inidus.platform;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ian on 15/09/2017.
 */

public class DfText {
    private static final Map<String, String> terminologyNameToFHIRSystem = new HashMap<>();
    private static final Map<String, String> FHIRSystemToTerminologyName = new HashMap<>();

    static {
        terminologyNameToFHIRSystem.put("SNOMED-CT", "http://snomed.info/sct");
        terminologyNameToFHIRSystem.put("LOINC", "http://loinc.org");
        terminologyNameToFHIRSystem.put("ICD-10", "http://hl7.org/fhir/sid/icd-10");
        terminologyNameToFHIRSystem.put("UCUM", "http://unitsofmeasure.org");
        terminologyNameToFHIRSystem.forEach((String key, String value) -> FHIRSystemToTerminologyName.put(value, key));
    }

    // Convert an openEHR DV_TEXT to a FHIR CodeableConcept
    public static CodeableConcept convertToCodeableConcept(DvText dvText) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText(dvText.getValue());

        //If this is a coded_text add the defining_code to the Codeable Concept
        // as a user_defined coding
        if (dvText instanceof DvCodedText) {
            DvCodedText dvCodedText = (DvCodedText) dvText;
            codeableConcept.addCoding(convertToCoding(dvCodedText.getDefiningCode(), true, dvCodedText.getValue()));
        }

        //Convert any openEHR DvText mappings
        if (dvText.getMappings() != null) {
            int maxMappings = dvText.getMappings().size();
            for (int cntMappings = 0; cntMappings < maxMappings; cntMappings++) {
                CodePhrase codePhrase = dvText.getMappings().get(cntMappings).getTarget();
                codeableConcept.addCoding(convertToCoding(codePhrase, false, null));
            }
        }
        return codeableConcept;
    }

    //Convert a CodeableConcept to a DV_TEXT or DV_CODED_TEXT
    public static DvText convertToDvText(CodeableConcept codeableConcept) {
        // if the CodeableConcept has a user_defined Mapping then this needs to be a DV_Coded_Text
        DvText dvText = new DvText(codeableConcept.getText());

        if (codeableConcept.hasCoding()) {
            //TO BE DONE
        }
        return dvText;
    }

    //Convert an openEHR CodePhrase object to a FHIR Coding object
    private static Coding convertToCoding(CodePhrase codePhrase, boolean isDefiningCode, String textValue) {
        Coding coding = new Coding();
        coding.setCode(codePhrase.getCodeString());
        coding.setSystem(terminologyNameToFHIRSystem.get(codePhrase.getTerminologyId().name()));
        coding.setVersion(codePhrase.getTerminologyId().versionID());

        // openEHR mapping CodePhrases do not carry a textValue;
        if (textValue != null) {
            coding.setDisplay(textValue);
        }

        // UserSelected (The code selected by the user) in FHIR is not an identical concept
        // to definingCode (The code directly associated with a dv_coded_text value but close enough.
        coding.setUserSelected(isDefiningCode);
        return coding;
    }

    //Convert a FHIR Coding object to an openEHR CodePhrase object
    private static CodePhrase convertToCodePhrase(Coding coding) {
        String code = coding.getCode();
        String termId = FHIRSystemToTerminologyName.get(coding.getSystem());
        return new CodePhrase(termId, code);

    }
}