package com.example.uniqueAproovaResidency.module.payment.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProviderFactory {

    private final Map<String, PaymentProvider> providers;

    @Value("${app.payment.provider:direct_upi}")
    private String configuredProvider;

    public PaymentProvider getProvider() {
        PaymentProvider provider = providers.get(configuredProvider.toLowerCase());
        if (provider == null) {
            return providers.get("direct_upi");
        }
        return provider;
    }
}
