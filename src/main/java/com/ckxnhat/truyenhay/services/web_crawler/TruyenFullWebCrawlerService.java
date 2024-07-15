package com.ckxnhat.truyenhay.services.web_crawler;


import com.ckxnhat.truyenhay.type.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TruyenFullWebCrawlerService implements WebCrawlerService {
    private final String BASE_URL = "https://truyenfull.vn/";

    public int parsePage(String page){
        String[] parts = page.split(" - ");
        for (String part: parts){
            if(part.startsWith("Trang")){
                String[] pageParts = part.split(" ");
                return Integer.parseInt(pageParts[1]);
            }
        }
        return 1;
    }
    public int parseChapter(String url){
        String[] parts = url.split("/");
        String chapter = parts[parts.length - 1];
        String[] chapterParts = chapter.split("-");
        return Integer.parseInt(chapterParts[1]);
    }

    public int getMaxPage(String url) {
        try{
            Document doc = Jsoup.connect(url).get();
            Elements pagination = doc.select(".pagination li");
            if (pagination.isEmpty()){
                return 1;
            }
           String pageString = pagination.get(pagination.size() - 2).selectFirst("a").attr("title");
            return parsePage(pageString);
        }
        catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        }

        return 1;
    }
    @Override
    public StoryOverview getOverview(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element image = doc.selectFirst(".book img");
            String coverImage = image.attr("src");

            String title = doc.selectFirst(".book img").attr("alt");

            String description = doc.selectFirst(".desc-text").text();

            String authorName = doc.selectFirst(".info a[itemprop=author]").text();
            String authorUrl = doc.selectFirst(".info a[itemprop=author]").attr("href");
            Author author = new Author(authorName, authorUrl);
            Elements genreElements = doc.select(".info a[itemprop=genre]");
            String genre = genreElements.stream().map(Element::text).reduce((s1, s2) -> s1 + ", " + s2).orElse("");

            double rating = Double.parseDouble(doc.selectFirst(".desc .rate .rate-holder").attr("data-score"));
            int totalRating = Integer.parseInt(doc.selectFirst(".desc .rate .small span[itemprop=ratingCount]").text());
            int totalViews = 0;
            Date updatedDate = null;
            String status = doc.selectFirst(".info").lastElementChild().selectFirst("span").text();
            int maxPageOfChapter = getMaxPage(url);
            return new StoryOverview(coverImage, title, description, author, genre, rating, totalRating, totalViews, updatedDate, status, maxPageOfChapter);
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SearchResultStory> search(String keyword, int page) {
        keyword = keyword.replace(" ", "+");
        String getUrl = BASE_URL + "/tim-kiem/?tukhoa=" + keyword;
        try {
            Document doc = Jsoup.connect(getUrl).get();

            Elements storyElements = doc.select("#list-page .list-truyen .row[itemscope]");
            return storyElements.stream().map(element -> {

                String coverImage = element.selectFirst("div[data-image]").attr("data-image");

                String title = element.selectFirst("div[data-image]").attr("data-alt");

                String authorName = element.selectFirst(".author").text();
                Author author = new Author(authorName, null);
                String lastChapter = element.selectFirst(".text-info a").attr("title").split(" - ")[1];
                Date lastDayUpdate = null;
                String url = element.selectFirst("a").attr("href");
                int maxPage = getMaxPage(getUrl);
                return new SearchResultStory(coverImage, title, author, lastChapter, lastDayUpdate, url, maxPage);
            }).toList();
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return  null;
    }

    public String getStoryDetailUrl(String url){
        String[] parts = url.split("/");
        if (parts.length >= 4){
            return BASE_URL + parts[3];
        }
        return url;
    }
    @Override
    public List<SearchResultStory> getStoryByGenre(String genre, int page) {
        genre = genre.replace(" ", "-");
        String url = BASE_URL + "/the-loai/" + genre + "/trang-" + page + "/";
        try{
            Document doc = Jsoup.connect(url).get();
            Elements storyElements = doc.select("#list-page .list-truyen .row[itemscope]");
            return storyElements.stream().filter(element -> !element.html().equals("")).map(element -> {
                Element coverImageElm = element.selectFirst("div[data-image]");
                String coverImage = coverImageElm.attr("data-image");
                String title = element.selectFirst("div[data-image]").attr("data-alt");
                String authorName = element.selectFirst(".author").text();
                Author author = new Author(authorName, null);
                String lastChapter = element.selectFirst(".text-info a").attr("title").split(" - ")[1];
                Date lastDayUpdate = null;
                String storyUrl = element.selectFirst(".text-info a").attr("href");
                storyUrl = getStoryDetailUrl(storyUrl);
                int maxPage = getMaxPage(url);
                return new SearchResultStory(coverImage, title, author, lastChapter, lastDayUpdate, storyUrl, maxPage);
            }).toList();
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return  null;
    }

    @Override
    public List<SearchResultStory> getRecommendation() {
        try{
            Document doc = Jsoup.connect(BASE_URL).get();
            Elements storyElements = doc.select("#intro-index .index-intro .item");
            return storyElements.stream().map(element -> {
                Element a = element.selectFirst("a");
                String coverImage = a.selectFirst("img").attr("src");
                String title = a.selectFirst(".title h3").text();
                Author author = null;
                String lastChapter = null;
                Date lastDayUpdate = null;
                String url = a.attr("href");
                String[] parts = url.split("/");
                if (parts.length >= 4){
                   url = BASE_URL + parts[3];
                }
                int maxPage = 1;
                return new SearchResultStory(coverImage, title, author, lastChapter, lastDayUpdate, url, maxPage);
            }).toList();
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getName() {
        return "truyenfull";
    }

    @Override
    public StoryDetail getDetails(String url) {
        try{
            Document doc = Jsoup.connect(url).get();
             String title = doc.selectFirst(".truyen-title").attr("title");
             Element chapterTitleElm = doc.selectFirst(".chapter-title");
             String author = null;
             Date date = null;
             AtomicReference<ChapterInfor> currentChapter = new AtomicReference<>();
             List<ChapterInfor> chapters;
             String content = doc.selectFirst(".chapter-c").html();

            String storyID = doc.selectFirst("#truyen-id").val();
            String apiUrl = BASE_URL + "/ajax.php?type=chapter_option&data=" + storyID;


            HttpClient client = HttpClient.newHttpClient();

            // Tạo yêu cầu GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            // Gửi yêu cầu và nhận phản hồi
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Document apiDoc = Jsoup.parse(response.body());
            Elements chapterElements = apiDoc.select("option");
            chapters = chapterElements.stream().map(element -> {
                String[] parts = url.split("/");
                String chapterUrl = BASE_URL + parts[3] + "/" + element.attr("value");

                String chapterTitle = element.text();
                int chapterNumber = parseChapter(chapterUrl);
                if (chapterUrl.equals(url)){
                    currentChapter.set(new ChapterInfor(chapterTitleElm.attr("href"), chapterTitleElm.attr("title").split(" - ")[1], chapterNumber));
                }
                return new ChapterInfor(chapterUrl, chapterTitle, chapterNumber);
            }).toList();



            return new StoryDetail(title, author, date, currentChapter.get(), chapters, content);
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        catch (InterruptedException e){
            System.out.println("Error: " + e.getMessage());
        }
        return  null;
    }

    @Override
    public List<ChapterInfor> getChapterInfoByPage(String url, int page) {
       String getURL = url + "/trang-" + page + "/";
        try{
            Document doc = Jsoup.connect(getURL).get();
            Elements chapterElements = doc.select("ul.list-chapter li");
            return chapterElements.stream().map(element -> {
                Element a = element.selectFirst("a");
                String title = a.attr("title").split(" - ")[1];
                String chapterUrl = a.attr("href");
                int chapterNumber = parseChapter(chapterUrl);
                return new ChapterInfor(chapterUrl, title,chapterNumber);
            }).toList();
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Genre> getGenres() {
        try{
            Document doc = Jsoup.connect(BASE_URL).get();
            Elements genreElements = doc.select(".list-cat .row div");
            return genreElements.stream().map(element -> {
                String name = element.selectFirst("a").text();
                String url = element.selectFirst("a").attr("href").split("/")[4];
                return new Genre(name, url);
            }).toList();
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SearchResultStory> getStoryByAuthor(String url, int page) {
        try{
            Document doc = Jsoup.connect(url + "/trang-" + page).get();
            int maxPage = getMaxPage(url);
            Elements storyElements = doc.select("#list-page .list-truyen .row[itemscope]");
            return storyElements.stream().filter(element -> !element.html().equals("")).map(element -> {
                Element coverImageElm = element.selectFirst("div[data-image]");
                String coverImage = coverImageElm.attr("data-image");
                String title = element.selectFirst("div[data-image]").attr("data-alt");
                String authorName = element.selectFirst(".author").text();
                Author author = new Author(authorName, null);
                String lastChapter = element.selectFirst(".text-info a").attr("title").split(" - ")[1];
                Date lastDayUpdate = null;
                String storyUrl = element.selectFirst(".text-info a").attr("href");
                storyUrl = getStoryDetailUrl(storyUrl);
                return new SearchResultStory(coverImage, title, author, lastChapter, lastDayUpdate, storyUrl, maxPage);
            }).toList();
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return  null;
    }
}
