package org.example.service;

import org.example.entity.TimeStampEntity;
import org.example.model.TimeStampModel;
import org.example.repository.TimeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
;

@Service
public class TimeDataServiceImpl implements TimeDataService {

    @Autowired
    private TimeDataRepository timeDataRepository;

    @Autowired
    private DataSource dataSource;

    private static boolean threadsStarted = false;
    private volatile boolean exceptionInSaveAll = false;
    private volatile List<Instant> UNSAVED_TIME_INSTANTS = new ArrayList<>();//It's ok to do the list not Collections.synchronizedList, because I use ReentrantLock for synchronization
    private final List<Instant> UNSAVED_TIME_INSTANTS_TEMPORAL_SAVER = new ArrayList<>();//This list is used only in one thread
    private ReentrantLock lock = new ReentrantLock();

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
        if(!threadsStarted) {
            new DatabaseConnectionFixer().start();
            threadsStarted = true;
        }
        if (!UNSAVED_TIME_INSTANTS.isEmpty() || !UNSAVED_TIME_INSTANTS_TEMPORAL_SAVER.isEmpty()) {
            Instant date = Instant.now();
            if(!exceptionInSaveAll && lock.tryLock()) {
                try {
                    if(!UNSAVED_TIME_INSTANTS_TEMPORAL_SAVER.isEmpty()) {
                        UNSAVED_TIME_INSTANTS.addAll(UNSAVED_TIME_INSTANTS_TEMPORAL_SAVER);
                        UNSAVED_TIME_INSTANTS_TEMPORAL_SAVER.clear();
                    }
                    UNSAVED_TIME_INSTANTS.add(date);
                } finally {
                    lock.unlock();
                }
            } else {
                UNSAVED_TIME_INSTANTS_TEMPORAL_SAVER.add(date);
            }
        } else {
            Instant date = Instant.now();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Void> future = executor.submit(() -> {
                timeDataRepository.save(new TimeStampEntity(date));
                return null;
            });
            try {
                future.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                UNSAVED_TIME_INSTANTS.add(date);
            } catch (ExecutionException | InterruptedException e) {
                UNSAVED_TIME_INSTANTS.add(date);
            } finally {
                executor.shutdown();
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
            while (true) {
                while (UNSAVED_TIME_INSTANTS.isEmpty()) {}
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                try (Connection connection = dataSource.getConnection()) {
                    if (connection != null) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future<Void> future = executor.submit(() -> {
                            lock.lock();
                            List<Instant> copyOfUnsavedTimeInstants = new ArrayList<>(UNSAVED_TIME_INSTANTS);
                            try {
                                timeDataRepository.saveAll(convertListTimeInstantToTimeStampEntityList(copyOfUnsavedTimeInstants));
                                UNSAVED_TIME_INSTANTS.clear();
                            } finally {
                                lock.unlock();
                            }
                            return null;
                        });
                        try {
                            future.get(2, TimeUnit.SECONDS);
                            exceptionInSaveAll = false;
                        } catch (TimeoutException e) {
                            exceptionInSaveAll = true;
                            future.cancel(true);
                        } catch (ExecutionException | InterruptedException e) {
                            exceptionInSaveAll = true;
                       } finally {
                            executor.shutdown();
                       }
                    } else {
                        System.out.println("Connection is still unavailable");
                    }
                } catch (SQLException e) {
                    System.out.println("Connection is still unavailable" + e.getMessage());
                }
            }
        }
    }

    private List<TimeStampEntity> convertListTimeInstantToTimeStampEntityList(List<Instant> instants) {
        List<TimeStampEntity> timeStampEntityList = new ArrayList<>();
        for (Instant instant : instants) {
            timeStampEntityList.add(new TimeStampEntity(instant));
        }
        return timeStampEntityList;
    }
}
