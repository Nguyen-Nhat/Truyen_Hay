package com.ckxnhat.truyenhay.services.web_crawler;

import com.ckxnhat.truyenhay.type.*;
import com.ckxnhat.truyenhay.type.ResponseApiType.DTruyenReponse;
import com.google.gson.Gson;
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
public class DTruyenWebCrawlerService implements WebCrawlerService{
    private final String BASE_URL = "https://dtruyen.com";

    public int getChapterNumberFromName(String name){
        String [] parts = name.split(" - ");
        for (String part : parts) {
            if (part.startsWith("Chương")) {
                String[] chapterParts = part.split(" ");
                return Integer.parseInt(chapterParts[1]);
            }
        }
        return 1;
    }

    @Override
    public StoryDetail getDetails(String url) {
        try{
            Document doc = Jsoup.connect(url).get();
            String title = doc.selectFirst("#chapter p.story-title a").attr("title");
            String author = doc.selectFirst("#chapter p:has(> i.fa-user)").text();
            Date date = new Date(doc.selectFirst("#chapter p:has(> i.fa-clock)").text());
            String content = doc.selectFirst("#chapter #chapter-content").html();
            String chapterTitle = doc.selectFirst(".chapter-title").text();
            //CALL API TO GET CHAPTERS
            // Khởi tạo HttpClient
            String storyID = doc.selectFirst("#storyID").val();
            String apiUrl = BASE_URL + "/ajax/chapters?storyID=" + storyID;
            HttpClient client = HttpClient.newHttpClient();

            // Tạo yêu cầu GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            // Gửi yêu cầu và nhận phản hồi
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            DTruyenReponse dTruyenReponse = gson.fromJson(response.body(), DTruyenReponse.class);

            AtomicReference<ChapterInfor> currentChapter = new AtomicReference<>();

            if (dTruyenReponse.getStatus() != 1)
                throw new IOException("Error: cannot fetch chapters from Dtruyen");
            List<ChapterInfor> chapters = dTruyenReponse.getChapters().stream()
                    .map(chapter -> {
                        String[] url_parts = url.split("/");
                        String chapterUrl = BASE_URL + "/" + url_parts[3]  + "/" + chapter.getUrl();

                        int chapterNumber = getChapterNumberFromName(chapter.getNo());
                        if (chapter.getUrl().equals(url_parts[4])) {
                            currentChapter.set(new ChapterInfor(chapterUrl, chapterTitle, chapterNumber));
                        }
                        return new ChapterInfor(chapterUrl, chapter.getNo(),chapterNumber);
                    })
                    .toList();
            if(currentChapter.get() == null) {
                currentChapter.set(new ChapterInfor(url, chapterTitle, getChapterNumberFromName(chapterTitle.split(":")[0].trim())));
            }
            return new StoryDetail(title, author, date, currentChapter.get(),chapters , content);
        } catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<ChapterInfor> getChapterInfoByPage(String url, int page) {
        try{
            Document doc = Jsoup.connect(url+ "/" + Integer.toString(page)).get();
            Elements chapterElements = doc.select("#chapters ul.chapters li a");
            return chapterElements.stream()
                    .map(element -> {
                        String chapterUrl = element.attr("href");
                        String chapterTitle = element.text();
                        int chapterNumber = getChapterNumberFromName(element.attr("title"));
                        return new ChapterInfor(chapterUrl, chapterTitle, chapterNumber);
                    })
                    .toList();
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return  null;
    }


    @Override
    public String getName() {
        return "dtruyen";
    }

    public int getMaxPage(String url) {
        try{
            Document doc = Jsoup.connect(url).get();
            Elements pagination = doc.select(".pagination li");
            return !pagination.isEmpty() ? Integer.parseInt(pagination.get(pagination.size() - 2).text()) : 1;
        }
        catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        }

        return 1;
    }

    @Override
    public StoryOverview getOverview(String url) {
        try {
            // GET OVERVIEW
            Document doc = Jsoup.connect(url).get();
            String coverImage = doc.selectFirst("#story-detail img.cover").attr("src");
            String title = doc.selectFirst("#story-detail h1.title").text();
            String description = doc.selectFirst("#story-detail div.description").text();
            String authorName = doc.selectFirst("#story-detail p.author a[itemprop=author]").attr("title");
            String genres = doc.select("#story-detail .story_categories a[itemprop=genre]")
                    .stream()
                    .map(Element::text)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            //GET RATING
            double rating = Double.parseDouble(doc.selectFirst("#story-detail .rate [data-score]").attr("data-score"));
            Elements ratingElements = doc.select("#story-detail .rate .small em strong > span");
            int totalRating = Integer.parseInt(ratingElements.get(1).text());

            //VIEW AND STATUS
            int totalViews = Integer.parseInt(doc.selectFirst(".infos p:has(> i.fa-eye)").text().replaceAll("[^\\d]", ""));

            Date updatedDate = new Date(doc.selectFirst(".infos p:has(> i.fa-refresh)").text());

            String status = doc.selectFirst(".infos p:has(> i.fa-star)").text();

            String authorUrl = doc.selectFirst("#story-detail p.author a[itemprop=author]").attr("href");
            //GET MAX CHAPTER
            Author author = new Author(authorName,authorUrl);
            int maxChapter = this.getMaxPage(url);
            return new StoryOverview(coverImage, title, description, author, genres, rating, totalRating,totalViews,updatedDate,status, maxChapter);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SearchResultStory> search(String keyword, int page) {
        try{
            keyword = keyword.split(" ").length > 1 ? keyword.replace(" ", "-") : keyword;
            String url = BASE_URL + "/searching/" + keyword +"/lastupdate/all/all/" + page;

            Document doc = Jsoup.connect(url).get();
            Elements storyElements = doc.select(".list-stories .story-list");

            int maxPage = this.getMaxPage(url);
            return storyElements.stream()
                    .map(element -> {
                        Element a = element.selectFirst("a");
                        Element temp = a.selectFirst("img");
                        String coverImage = temp.attr("data-layzr");
                        String urlStory = a.attr("href");
                        String title = a.attr("title");
                        String authorName = element.selectFirst("p[itemprop=author]").text();
                        Author author = new Author(authorName,null);
                        String lastChapter = element.selectFirst("p.last-chapter").text();

                        String lastDayUpdate = element.selectFirst(".last-chap .last-updated").text();
                        Date lastDateUpdate = new Date(Long.parseLong(lastDayUpdate) * 1000);

                        return new SearchResultStory(coverImage, title, author, lastChapter, lastDateUpdate,urlStory, maxPage);
                    })
                    .toList();
        }
        catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SearchResultStory> getStoryByGenre(String genre, int page) {
        genre = genre.split(" ").length > 1 ? genre.replace(" ", "-") : genre;
        String url = BASE_URL + "/" + genre + "/" + page;

        try {
            Document doc = Jsoup.connect(url).get();
            Elements storyElements = doc.select(".list-stories .story-list");
            int maxPage = this.getMaxPage(url);
            return storyElements.stream()
                    .map(element -> {
                        Element a = element.selectFirst("a");
                        Element temp = a.selectFirst("img");
                        String coverImage = temp.attr("data-layzr");
                        String urlStory = a.attr("href");
                        String title = a.attr("title");
                        String authorName = element.selectFirst("p[itemprop=author]").text();
                        Author author = new Author(authorName,null);
                        String lastChapter = element.selectFirst("p.last-chapter").text();

                        String lastDayUpdate = element.selectFirst(".last-chap .last-updated").text();
                        Date lastDateUpdate = new Date(Long.parseLong(lastDayUpdate) * 1000);

                        return new SearchResultStory(coverImage, title, author, lastChapter, lastDateUpdate,urlStory, maxPage);
                    })
                    .toList();

        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SearchResultStory> getRecommendation() {
        try {

            Document doc = Jsoup.connect(BASE_URL).get();
            Elements storyElements = doc.select(".grid-stories .story-grid");
            return storyElements.stream()
                    .map(element -> {
                        Element a = element.selectFirst("a");
                        Element temp = a.selectFirst("img");
                        String coverImage = temp.attr("data-layzr");
                        String urlStory = a.attr("href");
                        String title = a.attr("title");
                        String authorName = element.selectFirst("meta[itemprop=author]").attr("content");
                        Author author = new Author(authorName,null);
                        String lastChapter = element.selectFirst("p.last-chapter").text();
                        return new SearchResultStory(coverImage, title, author, lastChapter, null,urlStory, 1);
                    })
                    .toList();
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Genre> getGenres(){
        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            Elements storyElements = doc.select(".categories.clearfix > a");
            return storyElements.stream()
                    .map(element -> {
                        String name = element.text();
                        String slug = element.attr("href").split("/")[3];
                        return new Genre(name,slug);
                    })
                    .toList();
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SearchResultStory> getStoryByAuthor(String url, int page ) {
        try{
            Document doc = Jsoup.connect(url + "/" + page).get();
            Elements storyElements = doc.select(".list-stories .story-list");
            int maxPage = this.getMaxPage(url);
            return storyElements.stream()
                    .map(element -> {
                        Element a = element.selectFirst("a");
                        Element temp = a.selectFirst("img");
                        String coverImage = temp.attr("data-layzr");
                        String urlStory = a.attr("href");
                        String title = a.attr("title");
                        String authorName = element.selectFirst("p[itemprop=author]").text();
                        Author author = new Author(authorName,null);
                        String lastChapter = element.selectFirst("p.last-chapter").text();
                        String lastDayUpdate = element.selectFirst(".last-chap .last-updated").text();
                        Date lastDateUpdate = new Date(Long.parseLong(lastDayUpdate) * 1000);
                        return new SearchResultStory(coverImage, title, author, lastChapter, lastDateUpdate,urlStory, maxPage);
                    })
                    .toList();
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
}
