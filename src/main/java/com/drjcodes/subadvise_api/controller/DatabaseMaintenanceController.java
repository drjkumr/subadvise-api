package com.drjcodes.subadvise_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drjcodes.subadvise_api.service.DatabasePurgeService;

@RestController
@RequestMapping("/api/data")
public class DatabaseMaintenanceController {

    private final DatabasePurgeService databasePurgeService;

    public DatabaseMaintenanceController(DatabasePurgeService databasePurgeService) {
        this.databasePurgeService = databasePurgeService;
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> purgeSubscriptions() {
        databasePurgeService.purgeSubscriptions();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/targets")
    public ResponseEntity<Void> purgeTargets() {
        databasePurgeService.purgeTargets();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscriptions-and-targets")
    public ResponseEntity<Void> purgeSubscriptionsAndTargets() {
        databasePurgeService.purgeSubscriptionsAndTargets();
        return ResponseEntity.noContent().build();
    }
}
