package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.payment.PaymentMethodRequest;
import com.revpay.dto.payment.PaymentMethodResponse;
import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import com.revpay.repository.PaymentMethodRepository;
import com.revpay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
                                UserRepository userRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getMyPaymentMethods(String email) {
        User user = getUser(email);
        return paymentMethodRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentMethodResponse addPaymentMethod(String email, PaymentMethodRequest req) {
        User user = getUser(email);

        PaymentMethod pm = new PaymentMethod();
        pm.setUser(user);
        pm.setType(req.getType());
        pm.setMaskedIdentifier(req.getMaskedIdentifier());
        pm.setProvider(req.getProvider());
        pm.setExpiryMonth(req.getExpiryMonth());
        pm.setExpiryYear(req.getExpiryYear());
        pm.setDefault(req.isDefault());
        pm.setVerified(true);

        paymentMethodRepository.save(pm);
        return toResponse(pm);
    }

    @Transactional
    public void deletePaymentMethod(String email, Long id) {
        User user = getUser(email);
        PaymentMethod pm = paymentMethodRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> RevPayException.notFound("Payment method not found"));
        paymentMethodRepository.delete(pm);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> RevPayException.notFound("User not found"));
    }

    public PaymentMethodResponse toResponse(PaymentMethod pm) {
        PaymentMethodResponse r = new PaymentMethodResponse();
        r.setId(pm.getId());
        r.setType(pm.getType());
        r.setMaskedIdentifier(pm.getMaskedIdentifier());
        r.setProvider(pm.getProvider());
        r.setExpiryMonth(pm.getExpiryMonth());
        r.setExpiryYear(pm.getExpiryYear());
        r.setDefault(pm.isDefault());
        r.setVerified(pm.isVerified());
        r.setCreatedAt(pm.getCreatedAt());
        return r;
    }
}