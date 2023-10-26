package org.aggregateframework.basic.usage.controller;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aggregateframework.basic.usage.entity.Account;
import org.aggregateframework.basic.usage.entity.SubAccount;
import org.aggregateframework.basic.usage.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 14:11
 */
@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * http://localhost:8080/aggregate-framework-basic-usage/account/create
     */
    @GetMapping("/create")
    public String create(@RequestParam(defaultValue = "1") Integer amount) {
        String accountId = UUID.randomUUID().toString().replace("-", "");
        Account account = Account.builder()
                .accountId(accountId)
                .eventStatus(0)
                .build();
        List<SubAccount> subAccounts = IntStream.range(1, 3).mapToObj(each -> SubAccount.builder()
                .accountId(accountId + "-" + each)
                .account(account)
                .eventStatus(0)
                .build()).collect(Collectors.toList());
        account.setSubAccounts(subAccounts);
        account.applyAccountCreateEvent();
        accountRepository.save(account);
        return accountId;
    }

    /**
     * http://localhost:8080/aggregate-framework-basic-usage/account/query?accountId=***
     */
    @GetMapping("/query")
    public String query(@RequestParam("accountId") String accountId){
        return JSON.toJSONString(accountRepository.findByAccountId(accountId));
    }
}
