package org.example.service;

import org.example.entity.TimeStampEntity;
import org.example.model.TimeStampModel;
import org.example.repository.TimeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;;

@Service
public class TimeDataServiceImpl implements TimeDataService {

    @Autowired
    private TimeDataRepository timeDataRepository;

    @Autowired
    private DataSource dataSource;

    //private static int id = Integer.MIN_VALUE;

    private final List<Instant> UNSAVED_TIME_INSTANTS = Collections.synchronizedList(new ArrayList<>());

    private DatabaseConnectionFixer databaseConnectionFixer;

    @Override
    public List<TimeStampModel> getAllTimes() {
        List<TimeStampEntity> timeStampEntities = timeDataRepository.findAll();
        List<TimeStampModel> timeStampModels = new ArrayList<>();
        for (TimeStampEntity timeStampEntity : timeStampEntities) {
            timeStampModels.add(convertTimeStampEntityToTimeStampModel(timeStampEntity));
        }
        return timeStampModels;
    }

    @Scheduled(fixedDelay = 1000)
    public void saveTime() {
        if (!UNSAVED_TIME_INSTANTS.isEmpty()) {
            Instant date = Instant.now();
            UNSAVED_TIME_INSTANTS.add(date);
        } else {
            Instant date = Instant.now();
            try {
//                ExecutorService executor = Executors.newSingleThreadExecutor();
//                Future<Void> future = executor.submit(() -> {
//                    timeDataRepository.save(new TimeStampEntity(date));
//                    return null;
//                });
//                try {
//                    future.get(5, TimeUnit.SECONDS); // Set timeout here
//                } catch (TimeoutException e) {
//                    future.cancel(true); // Cancel the operation
//                    throw new MyCustomTimeoutException("Database operation timed out", e);
//                } catch (ExecutionException | InterruptedException e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    executor.shutdown();
//                }
                timeDataRepository.save(new TimeStampEntity(date));
            } catch (Exception e) {
                System.out.println("Connection lost");
                UNSAVED_TIME_INSTANTS.add(date);
                DatabaseConnectionFixer databaseConnectionFixer = new DatabaseConnectionFixer();
                databaseConnectionFixer.start();
            }
        }
    }

    private TimeStampModel convertTimeStampEntityToTimeStampModel(TimeStampEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TimeStampModel(entity.getId(), entity.getTime());
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
                    timeDataRepository.save(new TimeStampEntity(/*id++, */timeInstant));
                } catch (Exception e) {
                    System.out.println(e);
                }
            });
        }
    }
}
