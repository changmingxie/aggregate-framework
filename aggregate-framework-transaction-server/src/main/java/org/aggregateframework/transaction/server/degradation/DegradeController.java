package org.aggregateframework.transaction.server.degradation;

import org.aggregateframework.transaction.server.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Lee on 2020/9/24 15:32.
 * tcc-transaction
 */

@RestController
@RequestMapping("/api/degrade")
public class DegradeController {
    
    @Autowired private DegradationContainer container;
    
    
    @PutMapping
    public Object degrade(@RequestParam String domain, @RequestParam boolean degrade) {
        try {
            container.change(domain, degrade);
            return Result.ok();
        } catch (Exception e) {
            return Result.err(e);
        }
    }
    
    @GetMapping
    public Object list() {
        return Result.ok(container.getDomains());
    }
}
