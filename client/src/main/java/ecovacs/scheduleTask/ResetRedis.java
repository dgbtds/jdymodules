package ecovacs.scheduleTask;

import ecovacs.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author WuYe
 * @vesion 1.0 2019/11/25
 * /
 * /**
 * @program: parent
 * @description:每日刷新redis
 * @author: WuYe
 * @create: 2019-11-25 20:10
 **/
@Component
public class ResetRedis {
    @Autowired
    private CacheService cacheService;
  private final static Logger logger= LoggerFactory.getLogger(ResetRedis.class);
    final int[] i = {0};
        @Scheduled(cron="0 0 6 * * ?  ")
        public void reset() {
            cacheService.setAccepterGroupByCompanyId(25L);
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            logger.info(sdf.format(new Date())+"*********每日6点执行执行resetRedis");
        }
}
