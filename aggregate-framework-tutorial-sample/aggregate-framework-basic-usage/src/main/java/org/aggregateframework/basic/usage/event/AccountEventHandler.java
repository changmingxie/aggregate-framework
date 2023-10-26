package org.aggregateframework.basic.usage.event;


import lombok.extern.slf4j.Slf4j;
import org.aggregateframework.basic.usage.entity.Account;
import org.aggregateframework.basic.usage.repository.AccountRepository;
import org.aggregateframework.eventhandling.annotation.AsyncConfig;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.annotation.QueueFullPolicy;
import org.aggregateframework.eventhandling.annotation.TransactionCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 16:43
 */
@Component
@Slf4j
public class AccountEventHandler {

    @Autowired
    private AccountRepository accountRepository;

    private static final Integer AGG_EVENT_STATUS_POS_1 = 1 << 0;

    private static final Integer AGG_EVENT_STATUS_POS_2 = 1 << 1;

    @EventHandler(asynchronous = true, postAfterTransaction = true, isTransactionMessage = true,
            asyncConfig = @AsyncConfig(queueFullPolicy = QueueFullPolicy.DISCARD),
            transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkAccountCreateEvent"))
    @Transactional(rollbackFor = Throwable.class)
    public void handleAccountCreateEvent(AccountCreateEvent accountCreateEvent) {
        log.info("exec handleAccountCreateEvent，{}", accountCreateEvent);
        Account account = accountRepository.findByAccountId(accountCreateEvent.getAccountId());
        if (account != null) {
            account.setEventStatus(account.getEventStatus() | AGG_EVENT_STATUS_POS_1);
            account.getSubAccounts().forEach(each -> each.setEventStatus(each.getEventStatus() | AGG_EVENT_STATUS_POS_1));
        }
        accountRepository.save(account);
    }

    @EventHandler(asynchronous = true, postAfterTransaction = true, isTransactionMessage = true,
            asyncConfig = @AsyncConfig(queueFullPolicy = QueueFullPolicy.DISCARD),
            transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "batchCheckAccountCreateEvent"))
    @Transactional(rollbackFor = Throwable.class)
    public void batchHandleAccountCreateEvent(List<AccountCreateEvent> accountCreateEvents) {
        log.info("exec batchHandleAccountCreateEvent，{}", accountCreateEvents);
        List<Account> accounts = accountRepository.findByAccountIds(accountCreateEvents.stream().map(AccountCreateEvent::getAccountId).collect(Collectors.toList()));
        accounts.forEach(account -> {
            account.setEventStatus(account.getEventStatus() | AGG_EVENT_STATUS_POS_2);
            account.getSubAccounts().forEach(each -> each.setEventStatus(each.getEventStatus() | AGG_EVENT_STATUS_POS_2));
        });
        accountRepository.save(accounts);
    }

    public boolean checkAccountCreateEvent(AccountCreateEvent accountCreateEvent) {
        return accountRepository.findByAccountId(accountCreateEvent.getAccountId()) != null;
    }

    public boolean batchCheckAccountCreateEvent(List<AccountCreateEvent> accountCreateEvents) {
        return !accountRepository.findByAccountIds(accountCreateEvents.stream().map(AccountCreateEvent::getAccountId).collect(Collectors.toList())).isEmpty();
    }
}
