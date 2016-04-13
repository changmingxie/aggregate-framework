package org.aggregateframework.test.quickstart.command.service;

import org.aggregateframework.test.quickstart.command.domain.entity.Payment;
import org.aggregateframework.test.quickstart.command.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class NotifyListener {

    @Autowired
    PaymentRepository paymentRepository;

    public void handleConfirmMessage(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo);

        payment.confirm();

        paymentRepository.save(payment);
    }
}
