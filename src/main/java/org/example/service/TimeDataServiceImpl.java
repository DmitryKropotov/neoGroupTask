package org.example.service;

import org.example.entity.TimeStampEntity;
import org.example.repository.TimeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;;

@Service
public class TimeDataServiceImpl implements TimeDataService {

    @Autowired
    private TimeDataRepository timeDataRepository;

    @Autowired
    private DataSource dataSource;

    private static int id = Integer.MIN_VALUE;

    private final List<Instant> UNSAVED_TIME_INSTANTS = Collections.synchronizedList(new ArrayList<>());

    private DatabaseConnectionFixer databaseConnectionFixer;

    @Override
    public List<TimeStampEntity> getAllTimes() {
        return timeDataRepository.findAll();
    }

    @Scheduled(fixedDelay = 1000)
    public void saveTime() {
        if (!UNSAVED_TIME_INSTANTS.isEmpty()) {
            Instant date = Instant.now();
            UNSAVED_TIME_INSTANTS.add(date);
        } else {
            boolean canChangeId = true;
            List<TimeStampEntity> timeStamps = null;
            try {
                timeStamps = timeDataRepository.findAll();
            } catch (Exception e) {
                System.out.println("Connection lost");
                canChangeId = false;
                databaseConnectionFixer = new DatabaseConnectionFixer();
                databaseConnectionFixer.start();
            }
            if (canChangeId) {
                OptionalInt maxId = timeStamps.stream().mapToInt(TimeStampEntity::getId).max();
                if (maxId.isPresent()) {
                    id = maxId.getAsInt() + 1;
                }
                Instant date = Instant.now();
                try {
                    timeDataRepository.save(new TimeStampEntity(id++, date));
                } catch (Exception e) {
                    System.out.println("Connection lost");
                    id--;
                    UNSAVED_TIME_INSTANTS.add(date);
                    DatabaseConnectionFixer databaseConnectionFixer = new DatabaseConnectionFixer();
                    databaseConnectionFixer.start();
                }
            } else {
                Instant date = Instant.now();
                UNSAVED_TIME_INSTANTS.add(date);
            }
        }
    }

    private class DatabaseConnectionFixer extends Thread {

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                try (Connection connection = dataSource.getConnection()) {
                    if (connection != null) {
                        synchronized (UNSAVED_TIME_INSTANTS) {
                            saveAllInstants();
                            UNSAVED_TIME_INSTANTS.clear();
                        }
                    } else {
                        System.out.println("Connection is still unavailable");
                    }
                } catch (SQLException e) {
                    System.out.println("Connection is still unavailable" + e.getMessage());
                }
            } while (!UNSAVED_TIME_INSTANTS.isEmpty());
        }

        private void saveAllInstants() {
            UNSAVED_TIME_INSTANTS.forEach((Instant timeInstant) -> {
                try {
                    timeDataRepository.save(new TimeStampEntity(id++, timeInstant));
                } catch (Exception e) {
                    System.out.println(e);
                }
            });
        }
    }
}
