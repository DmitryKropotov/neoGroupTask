package org.example.service;

import org.example.entity.TimeStampEntity;
import org.example.model.TimeStampModel;
import org.example.repository.TimeDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;
import java.util.List;

@SpringBootTest
@EnableScheduling
@EnableAspectJAutoProxy
@Import(ServiceTest.Config.class)
public class ServiceSlowConnectionTest {

    @Autowired
    private TimeDataService timeDataService;

    @Autowired
    private TimeDataRepository timeDataRepository;

    @Test
    public void testSlowConnection() throws InterruptedException {
        Instant dateBeginningOfTest = Instant.now();
        Thread.sleep(10700);
        Instant dateEndOfTest = Instant.now();
        List<TimeStampEntity> times = timeDataRepository.findAll().stream().filter(timeStampEntity ->
                timeStampEntity.getTime().isAfter(dateBeginningOfTest) && timeStampEntity.getTime().isBefore(dateEndOfTest)).toList();
        List<TimeStampModel> timeDataServices = timeDataService.getAllTimes().stream().filter(timeStampEntity ->
                timeStampEntity.getTime().isAfter(dateBeginningOfTest) && timeStampEntity.getTime().isBefore(dateEndOfTest)).toList();
        assert GeneralMethods.isListInAscendingOrder(timeDataServices);
        assert times.size() == 10;
    }
}
