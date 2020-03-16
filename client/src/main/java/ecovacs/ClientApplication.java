package ecovacs;

import ecovacs.cache.CacheService;
import ecovacs.cache.CacheServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class ClientApplication {
    @Autowired
    private static CacheServiceImpl cacheService;
    private final static Logger log= LoggerFactory.getLogger(ClientApplication.class);
    public static void main(String[] args) {

        log.debug("debug...");
        log.info("info...");
        log.error("error...");
        SpringApplication.run(ClientApplication.class, args);
    }

}
