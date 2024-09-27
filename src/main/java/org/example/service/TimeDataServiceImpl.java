package org.example.service;

import org.example.entity.TimeStampEntity;
import org.example.repository.TimeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;

@Service
public class TimeDataServiceImpl implements TimeDataService {

    private static int id = Integer.MIN_VALUE;

    @Autowired
    private TimeDataRepository timeDataRepository;

    @Override
    public List<TimeStampEntity> getAllTimes() {
        return timeDataRepository.findAll();
    }

    @Override
    @Scheduled(fixedDelay = 1000)
    public void saveTime() {
        List<TimeStampEntity> timeStamps = timeDataRepository.findAll();
        OptionalInt maxId = timeStamps.stream().mapToInt(TimeStampEntity::getId).max();
        if(maxId.isPresent()) {
            id = maxId.getAsInt() + 1;
        }
        Instant date = Instant.now();
        timeDataRepository.save(new TimeStampEntity(id++, date));
    }
}
