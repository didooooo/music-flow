package app;

//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MusicFlowApplication {

    public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.load();
//        System.out.println("SPRING_DATASOURCE_USERNAME: " + dotenv.get("DATABASE_USERNAME"));
        SpringApplication.run(MusicFlowApplication.class, args);
    }

}
