package com.tasksdemo.common.service;

import com.tasksdemo.common.SyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("service1")
public class Service1Impl implements ISyncService {
    private static final Logger log = LoggerFactory.getLogger(Service1Impl.class);

    @Override
    public void doSync(SyncConfig config) {
        log.info("doSync running.. for task {} ", config.getTaskNumber());

        try {
            Thread.sleep(30*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
