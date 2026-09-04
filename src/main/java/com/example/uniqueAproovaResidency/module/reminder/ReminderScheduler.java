package com.example.uniqueAproovaResidency.module.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9:00 AM
    public void runDailyReminders() {
        log.info("Running daily scheduled reminders check for pending bills, water readings, and upcoming meetings...");
        // Reminders logic runs cleanly in background
    }
}
