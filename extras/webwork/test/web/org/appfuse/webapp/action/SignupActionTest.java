package org.appfuse.webapp.action;

import org.appfuse.Constants;
import org.appfuse.model.Address;
import org.appfuse.model.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.opensymphony.webwork.ServletActionContext;


public class SignupActionTest extends BaseActionTestCase {
    private SignupAction action;

    protected void setUp() throws Exception {    
        super.setUp();
        action = (SignupAction) ctx.getBean("signupAction");
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        action = null;
    }
    
    public void testDisplayForm() throws Exception {
        MockHttpServletRequest request =
            new MockHttpServletRequest(null, "GET", "/signup.html");
        ServletActionContext.setRequest(request);
        assertEquals(action.execute(), "input");
    }  
    
    public void testExecute() throws Exception {
        User user = new User();
        user.setUsername("self-registered");
        user.setPassword("Password1");
        user.setConfirmPassword("Password1");
        user.setFirstName("First");
        user.setLastName("Last");
        
        Address address = new Address();
        address.setCity("Denver");
        address.setProvince("CO");
        address.setCountry("USA");
        address.setPostalCode("80210");        
        user.setAddress(address);
        
        user.setEmail("self-registered@raibledesigns.com");
        user.setWebsite("http://raibledesigns.com");
        user.setPasswordHint("Password is one with you.");
        action.setUser(user);

        // set mock requests and responses so setting cookies doesn't fail
        ServletActionContext.setRequest(new MockHttpServletRequest());
        ServletActionContext.setResponse(new MockHttpServletResponse());
        
        // start SMTP Server
        //SimpleSmtpServer server = SimpleSmtpServer.start(2525);
        // change the port on the mailSender
        //JavaMailSenderImpl mailSender = (JavaMailSenderImpl) ctx.getBean("mailSender");
        //mailSender.setPort(2525);
        
        assertEquals(action.save(), "success");
        assertFalse(action.hasActionErrors());
        
        // verify an account information e-mail was sent
        //server.stop();
       // assertTrue(server.getReceievedEmailSize() == 1);
        //Iterator emailIter = server.getReceivedEmail();
        //SmtpMessage email = (SmtpMessage) emailIter.next();
        //assertEquals(email.getHeaderValue("Subject"), "AppFuse Account Information");
        //log.debug("received e-mail: " + email.getBody()); 

        // verify that success messages are in the session
        assertNotNull(action.getSession().getAttribute(Constants.REGISTERED));
    }
}