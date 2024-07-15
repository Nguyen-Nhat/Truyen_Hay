package com.ckxnhat.truyenhay.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchResultStory {
    private String coverImage;
    private String title;
    private Author author;
    private String lastChapter;
    private Date lastDayUpdate;
    private String url;
    private int maxPage;

}
