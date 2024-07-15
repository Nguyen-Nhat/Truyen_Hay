package com.ckxnhat.truyenhay.controllers;

import com.ckxnhat.truyenhay.plugin.PluginManager;
import com.ckxnhat.truyenhay.services.export_file.ExportFileService;
import com.ckxnhat.truyenhay.services.web_crawler.WebCrawlerService;
import com.ckxnhat.truyenhay.type.response.SuccessApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("${api.prefix}")
public class ApiController {
    private PluginManager pluginManager;

    @Autowired
    public ApiController(PluginManager pluginManager) throws Exception {
        this.pluginManager = pluginManager;
    }

    @GetMapping("/servers")
    ResponseEntity<?> getServers() {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("201");
        successApiResponse.setData(pluginManager.getServerNames());
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/overview")
    ResponseEntity<?> getOverview(@PathVariable String serverName,
                                  @RequestParam String url) {

        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getOverview(url));
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/search")
    ResponseEntity<?> search(@PathVariable String serverName,
                             @RequestParam String q, @RequestParam(defaultValue = "1") int page) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setStatus("success");
        successApiResponse.setData(plugin.search(q,page));
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/genres")
    ResponseEntity<?> getStoryByGenre(@PathVariable String serverName) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getGenres());
        return ResponseEntity.ok(successApiResponse);
    }
    @GetMapping("/{serverName}/genre")
    ResponseEntity<?> getStoryByGenre(@PathVariable String serverName,
                                      @RequestParam String genre, @RequestParam(defaultValue = "1")  int page) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getStoryByGenre(genre, page));
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/recommendation")
    ResponseEntity<?> getRecommendation(@PathVariable String serverName) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getRecommendation());
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/chapter")
    ResponseEntity<?> getChapter(@PathVariable String serverName,
                                @RequestParam String url, @RequestParam(defaultValue = "1") int page) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getChapterInfoByPage(url,page));
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/details")
    ResponseEntity<?> getDetails(@PathVariable String serverName,
                                @RequestParam String url) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getDetails(url));
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/{serverName}/author")
    ResponseEntity<?> getStoryByAuthor(@PathVariable String serverName,
                                      @RequestParam String url, @RequestParam(defaultValue = "1") int page) {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        WebCrawlerService plugin = pluginManager.getServerPlugin(serverName);
        successApiResponse.setData(plugin.getStoryByAuthor(url, page));
        return ResponseEntity.ok(successApiResponse);
    }

    @GetMapping("/exportFormats")
    ResponseEntity<?> getExportFormats() {
        SuccessApiResponse successApiResponse = new SuccessApiResponse();
        successApiResponse.setStatus("success");
        successApiResponse.setData(pluginManager.getExportFileNames());
        return ResponseEntity.ok(successApiResponse);
    }

    @PostMapping("/export/{format}")
    ResponseEntity<?> exportFile(@PathVariable String format, @RequestBody String content) {
        //Send file to client
        ExportFileService exportFileService = pluginManager.getExportFilePlugin(format);
        ByteArrayInputStream inputStream = exportFileService.exportFile(content);
        var headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sample." + format);
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(inputStream));
    }

}
