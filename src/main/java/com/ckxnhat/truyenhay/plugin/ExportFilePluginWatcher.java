package com.ckxnhat.truyenhay.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ExportFilePluginWatcher extends PluginWatcher{
    private PluginManager shapeManager;

    @Autowired
    public ExportFilePluginWatcher(PluginManager shapeManager) throws IOException {
        super("plugins/com/ckxnhat/truyenhay/services/export_file");
        this.shapeManager = shapeManager;
    }

    public void load() {
        try {
            shapeManager.loadExportFilePlugin();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
