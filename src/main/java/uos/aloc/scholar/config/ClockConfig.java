package uos.aloc.scholar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock appClock() {
        return Clock.system(DEFAULT_ZONE);
    }
}
