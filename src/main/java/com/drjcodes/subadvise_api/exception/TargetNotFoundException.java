package com.drjcodes.subadvise_api.exception;

import java.util.UUID;

public class TargetNotFoundException extends RuntimeException {

    public TargetNotFoundException(UUID targetId) {
        super("Target not found with id " + targetId);
    }
}