package org.example.service;

import org.example.entity.TimeStampEntity;
import org.example.model.TimeStampModel;
import org.example.repository.TimeDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Instant;
import java.util.List;

@SpringBootTest
@EnableJpaRepositories(basePackages = "org.example.repository")
@EnableScheduling
@Import(ServiceTest.Config.class)
public class ServiceTest {

    @Autowired
    private AbstractDriverBasedDataSource dataSource;

    @Autowired
    private TimeDataService timeDataService;

    @Autowired
    private TimeDataRepository timeDataRepository;

    @Test
    public void testGettingAllTimes() throws InterruptedException {
        timeDataRepository.deleteAll();
        Thread.sleep(5700);
        List<TimeStampModel> timeDataServices = timeDataService.getAllTimes();
        assert GeneralMethods.isListInAscendingOrder(timeDataServices);
        assert timeDataServices.size() == 5;
    }

    @Test
    public void testNormalWork() throws InterruptedException {
        Instant dateBeginningOfTest = Instant.now();
        Thread.sleep(5700);
        Instant dateEndOfTest = Instant.now();
        List<TimeStampEntity> times = timeDataRepository.findAll().stream().filter(timeStampEntity ->
                timeStampEntity.getTime().isAfter(dateBeginningOfTest) && timeStampEntity.getTime().isBefore(dateEndOfTest)).toList();
        assert times.size() == 5;
    }

    @Test
    public void testConnectionBreak() throws InterruptedException {
        Instant dateBeginningOfTest = Instant.now();
        Thread.sleep(2000);
        String url = dataSource.getUrl();
        dataSource.setUrl("fakedUrl");
        Thread.sleep(5000);
        dataSource.setUrl(url);
        Thread.sleep(2700);
        Instant dateEndOfTest = Instant.now();
        List<TimeStampEntity> times = timeDataRepository.findAll().stream().filter(timeStampEntity ->
                timeStampEntity.getTime().isAfter(dateBeginningOfTest) && timeStampEntity.getTime().isBefore(dateEndOfTest)).toList();

        List<TimeStampModel> timeDataServices = timeDataService.getAllTimes().stream().filter(timeStampEntity ->
                timeStampEntity.getTime().isAfter(dateBeginningOfTest) && timeStampEntity.getTime().isBefore(dateEndOfTest)).toList();
        assert GeneralMethods.isListInAscendingOrder(timeDataServices);
        assert timeDataServices.size() == 9;

    }

    @Configuration
    @ComponentScan(value = {"org.example.configuration", "org.example.service"})
    static class Config {}
}
