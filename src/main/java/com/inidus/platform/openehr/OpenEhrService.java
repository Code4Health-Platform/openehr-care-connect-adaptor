package com.inidus.platform.openehr;


import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Connects to an openEHR backend and returns selected data
 */
public interface OpenEhrService {
    JsonNode getAllAllergies() throws IOException;

    JsonNode getAllergyById(String id) throws IOException;

    JsonNode getFilteredAllergy(TokenParam patientIdentifier, DateRangeParam adverseReactionRiskLastUpdated) throws IOException;
}
