package com.inidus.platform.fhir;

import com.inidus.platform.fhir.allergy.AllergyConnector;
import com.inidus.platform.fhir.allergy.AllergyProvider;
import com.inidus.platform.fhir.condition.ConditionConnector;
import com.inidus.platform.fhir.condition.ConditionProvider;
import com.inidus.platform.fhir.medication.MedicationStatementConnector;
import com.inidus.platform.fhir.medication.MedicationStatementProvider;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import com.inidus.platform.fhir.openehr.OpenEhrConnector;
import com.inidus.platform.fhir.procedure.ProcedureConnector;
import com.inidus.platform.fhir.procedure.ProcedureConverter;
import com.inidus.platform.fhir.procedure.ProcedureProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        FhirServlet.class,
        OpenEHRConverter.class,
        AllergyProvider.class, AllergyConnector.class,
        ConditionProvider.class, ConditionConnector.class,
        MedicationStatementProvider.class, MedicationStatementConnector.class,
        ProcedureProvider.class, ProcedureConnector.class, ProcedureConverter.class
})

public class FhirServletTest {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Autowired
    private FhirServlet testImpl;

    @Autowired
    private AllergyConnector allergyConnector;

    @Autowired
    private ConditionConnector conditionConnector;

    @Before
    public void setUp() throws Exception {
        configureCdrConnector(allergyConnector, "https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", false);
        configureCdrConnector(conditionConnector, "https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", false);

        if (testImpl.getResourceProviders().isEmpty()) {
            testImpl.init(new MockServletConfig());
            testImpl.initialize();
        }
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private void configureCdrConnector(OpenEhrConnector connector, String url, String user, String pass, boolean token) {
        connector.setUrl(url);
        connector.setUsername(user);
        connector.setPassword(pass);
        connector.setIsTokenAuth(token);
    }

    @After
    public void tearDown() {
        testImpl.destroy();
    }

    @Test
    public void allergyIntolerance_HttpOk_JSON() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance");

        testImpl.service(request, response);

        Assert.assertEquals(response.getContentAsString(), HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("application/json+fhir", response.getContentType());
    }

    @Test
    public void condition_HttpOk_JSON() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/Condition");

        testImpl.service(request, response);

        Assert.assertEquals(response.getContentAsString(), HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("application/json+fhir", response.getContentType());
    }
}
