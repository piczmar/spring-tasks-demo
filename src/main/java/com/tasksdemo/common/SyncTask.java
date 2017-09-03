package com.tasksdemo.common;

import com.tasksdemo.common.service.ISyncService;

public class SyncTask implements Runnable {

    private final ISyncService syncService;
    private final SyncConfig config;

    public SyncTask(final SyncConfig config, final ISyncService syncService) {
        this.config = config;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        syncService.doSync(config);
    }

}
