package com.inidus.platform.fhir.allergy;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.exceptions.FHIRException;

public class AllergyIntoleranceCategory {

    public static AllergyIntolerance.AllergyIntoleranceCategory convertToFhir(String category_code) {
        if ("at0121".equals(category_code)) {
            return AllergyIntolerance.AllergyIntoleranceCategory.FOOD;
        } else if ("at0122".equals(category_code)) {
            return AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
        } else if ("at0123".equals(category_code)) {
            return AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
        } else {
            return null;
        }
    }

    public static String convertToEhr(String fhirString) {
        try {
            AllergyIntolerance.AllergyIntoleranceCategory category
                    = AllergyIntolerance.AllergyIntoleranceCategory.fromCode(fhirString);
            switch (category) {
                case FOOD:
                    return "at0121";
                case MEDICATION:
                    return "at0122";
                case ENVIRONMENT:
                    return "at0123";
                default:
                    throw new IllegalArgumentException("Unhandled case");
            }
        } catch (FHIRException e) {
        }
        return null;
    }
}
