package com.ckxnhat.truyenhay.plugin;

import com.ckxnhat.truyenhay.exception.AppException;
import com.ckxnhat.truyenhay.services.export_file.ExportFileService;
import com.ckxnhat.truyenhay.services.web_crawler.WebCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PluginManager {
    private final Map<String, WebCrawlerService> serverPlugins = new ConcurrentHashMap<>();
    private final Map<String, ExportFileService> exportFilePlugins = new ConcurrentHashMap<>();
    private static final String DEFAULT_CLASS_DIR = "plugins";

    public PluginManager() {
        try {
            loadServerPlugin();
            loadExportFilePlugin();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadServerPlugin() throws Exception {
        serverPlugins.clear();
        File directory = new File(DEFAULT_CLASS_DIR);
        if(!directory.exists()){
            directory.mkdirs();
        }
        URL classUrl = directory.toURI().toURL();
        System.out.println(classUrl);
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classUrl}, this.getClass().getClassLoader())) {
            Files.walk(directory.toPath())
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        String className = getClassName(directory, path.toFile());
                        try {
                            Class<?> clazz = Class.forName(className, true, classLoader);
                            if (WebCrawlerService.class.isAssignableFrom(clazz) &&
                                    !clazz.isInterface() &&
                                    !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                                WebCrawlerService webCrawlerService
                                        = (WebCrawlerService) clazz.getDeclaredConstructor().newInstance();
                                String clazzName = webCrawlerService.getName().toLowerCase();
                                serverPlugins.put(clazzName, webCrawlerService);
                                System.out.println("Loaded plugin: " + className);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void loadExportFilePlugin() throws Exception {
        exportFilePlugins.clear();
        File directory = new File(DEFAULT_CLASS_DIR);
        URL classUrl = directory.toURI().toURL();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classUrl}, this.getClass().getClassLoader())) {
            Files.walk(directory.toPath())
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        String className = getClassName(directory, path.toFile());
                        try {
                            Class<?> clazz = Class.forName(className, true, classLoader);
                            if (ExportFileService.class.isAssignableFrom(clazz) &&
                                    !clazz.isInterface() &&
                                    !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                                ExportFileService exportFileService
                                        = (ExportFileService) clazz.getDeclaredConstructor().newInstance();
                                String clazzName = exportFileService.getName().toLowerCase();
                                exportFilePlugins.put(clazzName, exportFileService);
                                System.out.println("Loaded plugin: " + className);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private String getClassName(File root, File file) {
        String rootPath = root.getPath();
        String filePath = file.getPath();
        String className = filePath.substring(rootPath.length() + 1, filePath.length() - 6);
        return className.replace(File.separatorChar, '.');
    }

    public WebCrawlerService getServerPlugin(String className) {
        WebCrawlerService plugin = serverPlugins.get(className);

        if (plugin == null) {
            throw new AppException("Plugin not found: " + className,400);
        }
        return plugin;
    }

    public String[] getServerNames() {
        if (serverPlugins.isEmpty()) {
            try {
                loadServerPlugin();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return serverPlugins.keySet().toArray(new String[0]);
    }

    public String[] getExportFileNames() {
        if (exportFilePlugins.isEmpty()) {
            try {
                loadExportFilePlugin();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return exportFilePlugins.keySet().toArray(new String[0]);
    }

    public ExportFileService getExportFilePlugin(String className) {
        ExportFileService plugin = exportFilePlugins.get(className);

        if (plugin == null) {
            throw new AppException("Plugin not found: " + className, 400);
        }
        return plugin;
    }

}
