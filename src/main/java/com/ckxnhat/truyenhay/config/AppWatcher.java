package com.ckxnhat.truyenhay.config;

import com.ckxnhat.truyenhay.plugin.ExportFilePluginWatcher;
import com.ckxnhat.truyenhay.plugin.ServerPluginWatcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppWatcher implements CommandLineRunner {
    private final ServerPluginWatcher serverPluginWatcher;
    private final ExportFilePluginWatcher exportFilePluginWatcher;

    public AppWatcher(ServerPluginWatcher shapeWatcherService,
                      ExportFilePluginWatcher exportFilePluginWatcher) {
        this.serverPluginWatcher = shapeWatcherService;
        this.exportFilePluginWatcher = exportFilePluginWatcher;
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(serverPluginWatcher::startWatching).start();
        new Thread(exportFilePluginWatcher::startWatching).start();
    }
}
