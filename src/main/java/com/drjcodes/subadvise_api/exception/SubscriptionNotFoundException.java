package com.drjcodes.subadvise_api.exception;

import java.util.UUID;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(UUID subscriptionId) {
        super("Subscription not found with id " + subscriptionId);
    }
}