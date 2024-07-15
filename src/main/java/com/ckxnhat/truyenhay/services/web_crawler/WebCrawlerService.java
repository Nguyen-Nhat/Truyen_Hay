package com.ckxnhat.truyenhay.services.web_crawler;


import com.ckxnhat.truyenhay.type.*;

import java.util.List;

public interface WebCrawlerService {
    public StoryOverview getOverview(String url);
    public List<SearchResultStory> search(String keyword, int page);
    public List<SearchResultStory> getStoryByGenre(String genre, int page);
    public List<SearchResultStory> getRecommendation();
    public String getName();
    public StoryDetail getDetails(String url);
    public List<ChapterInfor> getChapterInfoByPage(String url, int page);
    public List<Genre> getGenres();
    public List<SearchResultStory> getStoryByAuthor(String url, int page);
}
