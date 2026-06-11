package net.security.infosec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InfosecApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfosecApplication.class, args);
    }
}
