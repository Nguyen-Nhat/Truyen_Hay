package com.ckxnhat.truyenhay.services;


import com.ckxnhat.truyenhay.services.web_crawler.DTruyenWebCrawlerService;
import com.ckxnhat.truyenhay.type.ChapterInfor;
import com.ckxnhat.truyenhay.type.StoryOverview;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DtruyenWebCrawlerServiceTest {
    private final DTruyenWebCrawlerService dTruyenWebCrawlerService = new DTruyenWebCrawlerService();

    @Test
    public void testParseChapterNumberFromTitle(){
        String title = "Chương 7 - Trùng Sinh Nghịch Chuyển Tiên Đồ";
        int chapterNumber = dTruyenWebCrawlerService.getChapterNumberFromName(title);
        assertEquals(7, chapterNumber);
    }

    @Test
    void testGetChapterInfoByPage(){
        String url = "https://dtruyen.com/trung-sinh-nghich-chuyen-tien-do";
        int page = 1;
        List<ChapterInfor> chapterInfors = dTruyenWebCrawlerService.getChapterInfoByPage(url,page);
        assertFalse(chapterInfors.isEmpty());
        assertEquals(30, chapterInfors.size());
    }

    @Test
    public void testGetMaxPage(){
        String url = "https://dtruyen.com/trung-sinh-nghich-chuyen-tien-do";
        int maxPage = dTruyenWebCrawlerService.getMaxPage(url);
        assertEquals(17, maxPage);
    }

    @Test
    public void testGetMaxPageWithNoPagingUrl(){
        String url = "https://dtruyen.com/tao-hoa-tu-chi-vuong";
        int maxPage = dTruyenWebCrawlerService.getMaxPage(url);
        assertEquals(1, maxPage);
    }

    @Test
    public void testGetStoryOverview(){
        String url = "https://dtruyen.com/tu-cam";
        StoryOverview storyOverview = dTruyenWebCrawlerService.getOverview(url);
        assertNotNull(storyOverview);
        assertEquals("Tự Cẩm", storyOverview.getTitle());
        assertEquals("https://img.dtruyen.com/public/images/large/tucameSlJFCkbiN.jpg", storyOverview.getCoverImage());
        assertEquals("Hoàn Thành", storyOverview.getStatus());
    }

}
