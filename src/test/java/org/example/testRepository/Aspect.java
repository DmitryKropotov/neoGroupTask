package org.example.testRepository;

import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@org.aspectj.lang.annotation.Aspect
@Component
public class Aspect {

    private static int saveCounter = 0;
    private static int saveAllCounter = 0;

    @Before("execution(* org.example.testRepository.TimeDataRepositoryTest.save(..))")
    public void beforeSaveMethod() throws InterruptedException {
        saveCounter++;
        if(saveCounter<3) {
            Thread.sleep(200);
        }
    }

    @Before("execution(* org.example.testRepository.TimeDataRepositoryTest.saveAll(..))")
    public void beforeSaveAllMethod() throws InterruptedException {
        saveAllCounter++;
        if(saveAllCounter==1) {
            Thread.sleep(3000);
        }
    }
}
