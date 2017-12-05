package com.inidus.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inidus.platform.openehr.MarandConnector;
import com.inidus.platform.openehr.OpenEhrService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {FhirServlet.class, AllergyProvider.class, MarandConnector.class})
public class FhirServletTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Autowired
    private FhirServlet testImpl;

    @MockBean
    private OpenEhrService ehrService;

    @Before
    public void setUp() throws Exception {
        given(ehrService.getAllergyById(Mockito.anyString())).willReturn(AllergyProviderTest.getDummyJson());

        if (testImpl.getResourceProviders().isEmpty()) {
            testImpl.init(new MockServletConfig());
            testImpl.initialize();
        }
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @After
    public void tearDown() {
        testImpl.destroy();
    }

    @Test
    public void allergyIntolerance_HttpOk_JSON() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance/1");

        testImpl.service(request, response);

        Assert.assertEquals(response.getContentAsString(), HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("application/json+fhir", response.getContentType());
    }

    @Test
    public void allergyIntolerance_patientRefPresent() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance/1");

        testImpl.service(request, response);

        JsonNode json = new ObjectMapper().readTree(response.getContentAsString());

        Assert.assertNotNull(json.get("patient").get("display"));
    }

    @Test
    public void allergyIntolerance_codedTextPresent() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance/1");

        testImpl.service(request, response);

        JsonNode json = new ObjectMapper().readTree(response.getContentAsString());

        Assert.assertNotNull(json.get("code").get("text"));
    }
}
