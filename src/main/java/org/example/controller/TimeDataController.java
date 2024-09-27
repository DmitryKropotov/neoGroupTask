package org.example.controller;

import org.example.entity.TimeStampEntity;
import org.example.service.TimeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimeDataController {

    @Autowired
    TimeDataService timeDataService;

    @RequestMapping(method = RequestMethod.GET, value = "/startRun")
    public List<TimeStampEntity> getAllData() {
        return timeDataService.getAllTimes();
    }
}
