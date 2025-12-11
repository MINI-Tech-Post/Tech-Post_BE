package com.ureka.techpost.global.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class OpenGraphParser {

    /**
     * og:title 또는 title 태그에서 제목 추출
     */
    public static String parseTitle(Document doc) {
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null && ogTitle.hasAttr("content")) {
            return ogTitle.attr("content");
        }

        Element titleTag = doc.selectFirst("title");
        return titleTag != null ? titleTag.text() : "제목 없음";
    }

    /**
     * og:description 또는 meta description에서 요약 추출
     */
    public static String parseDescription(Document doc) {
        Element ogDescription = doc.selectFirst("meta[property=og:description]");
        if (ogDescription != null && ogDescription.hasAttr("content")) {
            return ogDescription.attr("content");
        }

        Element metaDescription = doc.selectFirst("meta[name=description]");
        if (metaDescription != null && metaDescription.hasAttr("content")) {
            return metaDescription.attr("content");
        }

        return "요약 없음";
    }

    /**
     * og:image에서 썸네일 URL 추출
     */
    public static String parseThumbnail(Document doc) {
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        return ogImage != null && ogImage.hasAttr("content")
                ? ogImage.attr("content")
                : null;
    }

    /**
     * article:author 또는 author 메타태그에서 작성자 추출
     */
    public static String parseAuthor(Document doc, String defaultAuthor) {
        Element articleAuthor = doc.selectFirst("meta[property=article:author]");
        if (articleAuthor != null && articleAuthor.hasAttr("content")) {
            return articleAuthor.attr("content");
        }

        Element metaAuthor = doc.selectFirst("meta[name=author]");
        if (metaAuthor != null && metaAuthor.hasAttr("content")) {
            return metaAuthor.attr("content");
        }

        return defaultAuthor;
    }

    /**
     * article:published_time에서 발행일 추출
     */
    public static Optional<LocalDateTime> parsePublishedTime(Document doc) {
        Element publishedTime = doc.selectFirst("meta[property=article:published_time]");
        if (publishedTime != null && publishedTime.hasAttr("content")) {
            try {
                String timeStr = publishedTime.attr("content");
                return Optional.of(LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME));
            } catch (DateTimeParseException e) {
                // 파싱 실패 시 현재 시간 반환
            }
        }
        return Optional.empty();
    }
}