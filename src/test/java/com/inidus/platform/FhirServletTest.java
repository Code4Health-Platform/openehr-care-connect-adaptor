package com.inidus.platform;

import com.inidus.platform.openehr.OpenEhrConnector;
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
@ContextConfiguration(classes = {FhirServlet.class, AllergyProvider.class, OpenEhrConnector.class})
public class FhirServletTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Autowired
    private FhirServlet testImpl;

    @Autowired
    private OpenEhrConnector ehrService;

    @Before
    public void setUp() throws Exception {
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
//        request.setMethod("GET");
//        request.addHeader("Content-Type", "application/json");
//        request.setRequestURI("/AllergyIntolerance");
//
//        testImpl.service(request, response);
//
//        Assert.assertEquals(response.getContentAsString(), HttpStatus.OK.value(), response.getStatus());
//        Assert.assertEquals("application/json+fhir", response.getContentType());
    }
}
