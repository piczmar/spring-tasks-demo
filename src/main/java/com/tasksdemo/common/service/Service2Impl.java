package com.tasksdemo.common.service;

import com.tasksdemo.common.SyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("service2")
public class Service2Impl implements ISyncService {
    private static final Logger log = LoggerFactory.getLogger(Service2Impl.class);

    @Override
    public void doSync(SyncConfig config) {
        log.info("doSync running.. for task {} ", config.getTaskNumber());
    }

}
