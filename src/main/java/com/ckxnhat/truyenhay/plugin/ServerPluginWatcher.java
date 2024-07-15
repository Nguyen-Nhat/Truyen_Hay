package com.ckxnhat.truyenhay.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ServerPluginWatcher extends PluginWatcher {
    private PluginManager shapeManager;

    @Autowired
    public ServerPluginWatcher(PluginManager shapeManager) throws IOException {
        super("plugins/com/ckxnhat/truyenhay/services/web_crawler");
        this.shapeManager = shapeManager;
    }

    public void load() {
        try {
            shapeManager.loadServerPlugin();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
