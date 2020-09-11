package site.fifa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FIFApp {

    public static void main(String[] args) {
        SpringApplication.run(FIFApp.class, args);
    }

}
