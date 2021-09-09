package org.aggregateframework.transaction.server.controller;

import org.aggregateframework.transaction.server.AbstractTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Lee on 2020/9/9 13:55.
 * aggregate-framework
 */
@AutoConfigureMockMvc
public class RegistrationControllerTest extends AbstractTestCase {
    
    @Autowired MockMvc mvc;
    
    
    @Test
    public void testGetDomain() throws Exception {
        
        mvc.perform(get("/api/domains")
                            .accept(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());
        
        
    }
}