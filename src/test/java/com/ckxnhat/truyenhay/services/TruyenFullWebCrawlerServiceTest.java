package com.ckxnhat.truyenhay.services;

import com.ckxnhat.truyenhay.services.web_crawler.TruyenFullWebCrawlerService;
import com.ckxnhat.truyenhay.type.StoryOverview;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TruyenFullWebCrawlerServiceTest {
    private final TruyenFullWebCrawlerService truyenFullWebCrawlerService = new TruyenFullWebCrawlerService();
    @Test
    public void testParseChapterNumberFroUrl(){
        String url = "https://truyenfull.vn/chang-re-ma-gioi/chuong-2/";
        int chapterNumber = truyenFullWebCrawlerService.parseChapter(url);
        assertEquals(2, chapterNumber);
    }

    @Test
    public void testParsePageNumberFromTitle(){
        String title = "Chàng Rể Ma Giới - Trang 25";
        int pageNumber = truyenFullWebCrawlerService.parsePage(title);
        assertEquals(25, pageNumber);
    }

    @Test
    public void testGetMaxPageFromUrl(){
        String url = "https://truyenfull.vn/chang-re-ma-gioi";
        int maxPage = truyenFullWebCrawlerService.getMaxPage(url);
        assertEquals(26, maxPage);
    }
    @Test
    public void testGetMaxPageFromUrlNotPaging(){
        String url = "https://truyenfull.vn/thoi-gian-tuoi-dep-cua-que-hoa";
        int maxPage = truyenFullWebCrawlerService.getMaxPage(url);
        assertEquals(1, maxPage);
    }

    @Test
    public void testGetStoryDetailUrl(){
        String url = "https://truyenfull.vn/thoi-gian-tuoi-dep-cua-que-hoa/chuong-1/";
        String storyDetailUrl = truyenFullWebCrawlerService.getStoryDetailUrl(url);
        assertEquals("https://truyenfull.vn/thoi-gian-tuoi-dep-cua-que-hoa", storyDetailUrl);
    }

    @Test
    public void testGetStoryDetailUrlWithSpecialUrl(){
        String url = "https://truyenfull.vn/thoi-gian-tuoi-dep-cua-que-hoa";
        String storyDetailUrl = truyenFullWebCrawlerService.getStoryDetailUrl(url);
        assertEquals("https://truyenfull.vn/thoi-gian-tuoi-dep-cua-que-hoa", storyDetailUrl);
    }

    @Test
    public void testGetStoryOverView(){
        String url = "https://truyenfull.vn/me-vo-khong-loi-ve";
        StoryOverview storyOverView = truyenFullWebCrawlerService.getOverview(url);
        assertEquals("Mê Vợ Không Lối Về", storyOverView.getTitle());
        assertEquals("Full", storyOverView.getStatus());
    }
}
