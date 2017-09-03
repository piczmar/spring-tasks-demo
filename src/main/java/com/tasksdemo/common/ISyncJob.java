package com.tasksdemo.common;

import com.tasksdemo.common.service.ISyncService;

public interface ISyncJob {
    void doSync(ISyncService syncService);
}
