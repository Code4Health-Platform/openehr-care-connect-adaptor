package com.inidus.platform.openehr;


import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Connects to an openEHR backend and returns selected data
 */
public interface OpenEhrService {
    JsonNode getAllAllergies() throws IOException;

    JsonNode getAllergyById(String id) throws IOException;

    JsonNode getAllergyByPatientIdentifier(String patientId, String idNamespace) throws IOException;

    JsonNode getAllergyByPatientId(String id) throws IOException;

}
