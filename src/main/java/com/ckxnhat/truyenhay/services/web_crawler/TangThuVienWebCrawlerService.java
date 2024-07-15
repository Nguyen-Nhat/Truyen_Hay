package com.ckxnhat.truyenhay.services.web_crawler;

import com.ckxnhat.truyenhay.exception.AppException;
import com.ckxnhat.truyenhay.type.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TangThuVienWebCrawlerService implements WebCrawlerService{
    private final String BASE_URL = "https://truyen.tangthuvien.vn/";
    private List<Genre> genresList = new ArrayList<>();

    @Override
    public StoryOverview getOverview(String url) {
        try {
            Document document = getDocument(url);
            String coverImage = document.selectFirst(".book-img img").attr("src");
            String title = document.selectFirst(".book-info h1").text();
            String description = document.selectFirst(".book-info-detail .book-intro p").text();

            //Get all children of p class tag
            Element info = document.selectFirst(".book-info .tag");

            Element authorElement = info.child(0);
            String authorName = authorElement.text();
            String authorUrl = authorElement.attr("href");
            Author author = new Author(authorName, authorUrl);

            String status = info.child(1).text();
            String genre = info.child(2).text();

            double rating = Double.parseDouble(document.selectFirst("#j_bookScore cite").text());
            int totalRating = Integer.parseInt(document.selectFirst("#j_userCount span").text());

            String updateDateString = document.selectFirst(".volume-wrap em.count").text(); //(Cập nhật: 30/05/2024 12:47)
            updateDateString = updateDateString.substring(11, updateDateString.length() - 1);
            Date updateDate = convertStringToDate(updateDateString, "dd/MM/yyyy HH:mm");

            String maxChaptersString = document.selectFirst("#j-bookCatalogPage").text();

            //Conver Danh sách chương (250 chương) to 250
            int maxChapters = Integer.parseInt
                    (maxChaptersString.substring(18, maxChaptersString.length() - 8));
            int pagePerChapter = 75;

            int maxPage = maxChapters / pagePerChapter + (maxChapters % pagePerChapter == 0 ? 0 : 1);
            int totalViews = Integer.parseInt(document.selectFirst(".ULtwOOTH-view").text());

            return new StoryOverview(coverImage, title, description, author, genre, rating, totalRating, totalViews, updateDate, status, maxPage);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<SearchResultStory> search(String keyword, int page) {
        try {
            Document document = Jsoup.connect(BASE_URL + "ket-qua-tim-kiem?term="
                    + keyword + "&page=" + page).get();

            return getSearchResultStories(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkNoResult(Elements stories) {
        if (stories.isEmpty()) {
            return true;
        }

        Element firstElement = stories.first();
        return firstElement.selectFirst("li p").text().contains("Không tìm thấy");
    }

    @Override
    public List<SearchResultStory> getStoryByGenre(String genre, int page) {
        if (genresList.isEmpty()) {
            getGenres();
        }
        Genre _genre = null;

        for (Genre g : genresList) {
            if (g.getSlug().equals(genre)) {
                _genre = g;
                break;
            }
        }
        if (_genre == null) {
            throw new RuntimeException("Genre not found");
        }
        int index = genresList.indexOf(_genre) + 1;
        try {
            Document document = Jsoup.connect(BASE_URL + "/tong-hop?ctg="
                    + index + "&page=" + page).get();
            Elements paginationElements = document.select(".pagination a");
            int maxPage = Integer.parseInt(paginationElements.get(paginationElements.size() - 2).text());

            Elements stories = document.select(".book-img-text ul li");
            return getSearchResultStories(maxPage, stories);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SearchResultStory> getRecommendation() {
        try {
            Document document = Jsoup.connect(BASE_URL).timeout(10000).get();
            Elements stories = document.select(".center-book-list li");
            return stories.stream().map(story -> {
                String coverImage = story.selectFirst(".book-img img").attr("src");
                String title = story.selectFirst(".book-info a").text();

                Element authorElement = story.selectFirst(".state-box .author");
                String authorName = authorElement.text();
                String authorUrl = authorElement.attr("href");

                Author author = new Author(authorName, authorUrl);
                String url = story.selectFirst(".book-img a").attr("href");

                return new SearchResultStory(coverImage, title, author, null, null, url, 0);
            }).toList();
        } catch (SocketTimeoutException e) {
            throw new AppException("Request timeout", 408);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "tangthuvien";
    }

    @Override
    public StoryDetail getDetails(String url) {
        try {
            Document document = getDocument(url);
            String title = document.selectFirst(".truyen-title a").text();
            String author = document.selectFirst(".chapter strong a").text();


            Elements elementContainDate = document.select(".chapter p");
            Date date = null;

            if (elementContainDate.size() > 1) {
                Element dateElement = elementContainDate.get(1);
                String dateString =  dateElement.text().substring(11);
                date = convertStringToDate(dateString, "HH:mm dd-MM-yyyy");
            }

            String content = document.selectFirst(".box-chap").text();
            String currChapterTitle = document.selectFirst(".chapter h2").text();

            int storyId = Integer.parseInt(
                    document.selectFirst("input[name=story_id]").attr("value"));

            document = Jsoup.connect
                    ("https://truyen.tangthuvien.vn/story/chapters/?story_id="
                            + storyId).get();
            Elements chapterInfors = document.select("li a");
            chapterInfors.removeIf(chapterInfor -> chapterInfor.attr("href").contains("javascript"));

            //Create a list of ChapterInfor
            AtomicInteger index = new AtomicInteger();
            List<ChapterInfor> chapters =  chapterInfors.stream().map(chapterInfor -> {
                index.addAndGet(1);
                String chapterTitle = chapterInfor.text();
                String chapterUrl = chapterInfor.attr("href");
                return new ChapterInfor(chapterUrl, chapterTitle, index.get());
            }).toList();

            ChapterInfor currentChapter = chapters.stream().
                    filter(chapterInfor -> chapterInfor.getTitle().equals(currChapterTitle)).findFirst().get();

            return new StoryDetail(title, author, date, currentChapter, chapters, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ChapterInfor> getChapterInfoByPage(String url, int page) {
        try {
            int limit = 75;
            int pageRequest = page - 1;
            Document document = getDocument(url);
            int storyId = Integer.parseInt(document.selectFirst("#story_id_hidden").attr("value"));
            document = Jsoup.connect
                    ("https://truyen.tangthuvien.vn/doc-truyen/page/"
                            + storyId + "?page=" + pageRequest + "&limit=" + limit).get();
            //Get all chapter infor
            Elements chapterInfors = document.select("li a");
            chapterInfors.removeIf(chapterInfor -> chapterInfor.attr("href").contains("javascript"));

            //Create a list of ChapterInfor
            AtomicInteger index = new AtomicInteger();
            return chapterInfors.stream().map(chapterInfor -> {
                index.addAndGet(1);
                String chapterTitle = chapterInfor.text();
                String chapterUrl = chapterInfor.attr("href");
                return new ChapterInfor(chapterUrl, chapterTitle,
                        pageRequest * limit + index.get());
            }).toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Genre> getGenres() {
        if (!genresList.isEmpty()) {
            return genresList;
        }

        try {
            Document document = Jsoup.connect(BASE_URL).get();
            Elements genres = document.select(".classify-list a");
            //Remove 2 last elements
            genres.removeLast();
            genres.removeLast();
            genresList = genres.stream().map(genre -> {
                String genreName = genre.selectFirst("span i").text();
                String url = genre.attr("href");
                String slug = url.substring(url.lastIndexOf("/") + 1);
                return new Genre(genreName, slug);
            }).toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return genresList;
    }

    @Override
    public List<SearchResultStory> getStoryByAuthor(String url, int page) {
        try {
            Document document = Jsoup.connect(url + "&page=" + page).get();
            return getSearchResultStories(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SearchResultStory> getSearchResultStories(Document document) {
        Elements stories = document.select(".book-img-text ul li");
        if (checkNoResult(stories)) {
            return new ArrayList<>();
        }

        Elements paginationElements = document.select(".pagination a");
        int maxPage;
        if (!paginationElements.isEmpty()) {
            maxPage = Integer.parseInt(paginationElements.get(paginationElements.size() - 2).text());
        } else {
            maxPage = 1;
        }

        return getSearchResultStories(maxPage, stories);
    }

    private List<SearchResultStory> getSearchResultStories(int maxPage, Elements stories) {
        return stories.stream().map(story -> {
            String coverImage = story.selectFirst("img").attr("src");
            String title = story.selectFirst(".book-mid-info a").text();

            Element authorElement = story.selectFirst(".book-mid-info .author a");
            String authorName = authorElement.text();
            String authorUrl = authorElement.attr("href");

            Author author = new Author(authorName, authorUrl);

            String lastChapter = story.selectFirst(".author span").text();
            String lastDayUpdateStr = story.selectFirst(".update span").text();
            Date lastDayUpdate = convertStringToDate(lastDayUpdateStr, "yyyy-MM-dd HH:mm:ss");
            String url = story.selectFirst(".book-img-box a").attr("href");

            return new SearchResultStory(coverImage, title, author, lastChapter, lastDayUpdate, url, maxPage);
        }).toList();
    }

    Date convertStringToDate(String date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(date);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Document getDocument(String url) throws IOException {
        if (url.contains("\"")) {
            url = url.replaceAll("\"", "%22");
        }
        Connection connection = Jsoup.connect(url).followRedirects(false);
        Connection.Response response = connection.execute();

        // Check if the response has a redirect status code
        if (response.hasHeader("location")) {
            String redirectUrl = response.header("location");
            assert redirectUrl != null;
            redirectUrl = redirectUrl.replaceAll("\"", "%22");
            return Jsoup.connect(redirectUrl).get();
        }

        return response.parse();
    }
}
