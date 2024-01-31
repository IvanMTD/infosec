package net.security.infosec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class InfosecApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfosecApplication.class, args);
    }
}


/*public class InfosecApplication {
    public static void main(String[] args) {
        List<Auth> origin = Arrays.asList(Auth.ONE,Auth.THREE,Auth.FOUR);
        List<Auth> authList = Arrays.asList(Auth.ONE,Auth.TWO,Auth.FOUR);
        if(origin.stream().allMatch(a -> authList.stream().anyMatch(a2 -> a2.equals(a)))){
            System.out.println("Равны");
        }else{
            System.out.println("Неравны");
        }
    }

    static enum Auth {
        ONE,TWO, THREE, FOUR
    }
}*/
