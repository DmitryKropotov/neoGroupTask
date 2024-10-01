package org.example.testRepository;

import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@org.aspectj.lang.annotation.Aspect
@Component
public class Aspect {
    @Before("execution(* org.example.testRepository.TimeDataRepositoryTest.save(..))")
    public void beforeRepositoryMethod() throws InterruptedException {
        System.out.println("Ascpect is waiting");
        Thread.sleep(10000);
        System.out.println("Aspect 10 secs gone");
    }
}
