package com.ckxnhat.truyenhay.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StoryDetail {
    private String title;
    private String author;
    private Date date;
    private ChapterInfor currentChapter;
    private List<ChapterInfor> chapters;
    private String content;
}
