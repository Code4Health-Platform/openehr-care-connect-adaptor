package com.inidus.platform.openehr;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Connects to an openEHR backend and returns selected data
 */
public interface OpenEhrService {
    JsonNode getAllAllergies() throws IOException;

    JsonNode getAllergyById(String id) throws IOException;
}
