package com.inidus.platform.fhir.openehr;

import com.inidus.platform.fhir.condition.ConditionConnector;
import com.inidus.platform.fhir.condition.ConditionProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.*;

public class OpenEHRConverterTest {
    @Autowired

    private ConditionConnector ehrConnector;
    @Before
    public void setUp() throws Exception {
        //     configureCdrConnector("http://178.62.71.220:8080", "guest", "guest", true);
        configureCdrConnector("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);
    }
    private void configureCdrConnector(String url, String user, String pass, boolean isToken) {
        ehrConnector.setIsTokenAuth(isToken);
        ehrConnector.setUrl(url);
        ehrConnector.setUsername(user);
        ehrConnector.setPassword(pass);
    }

    @Test
    public void convertCodableConcept() {


    }
}