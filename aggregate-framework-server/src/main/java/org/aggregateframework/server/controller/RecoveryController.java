package org.aggregateframework.server.controller;

import org.aggregateframework.AggServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("recover")
public class RecoveryController {

    @Autowired
    private AggServer aggServer;

    @RequestMapping("/start/{domain}")
    @ResponseBody
    public String startRecover(@PathVariable("domain") String domain) {
        aggServer.getTransactionStoreRecovery().startRecover(domain);
        return "triggered";
    }
}
