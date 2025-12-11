package com.ureka.techpost.domain.post.service.crawler;

import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import com.ureka.techpost.global.utils.OpenGraphParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @file BlogTossCrawler.java
 @author 유효주
 @version 1.0
 @since 2025-12-11
 @description 이 파일은 토스 테크 블로그 RSS 피드를 파싱하는 클래스입니다.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogTossCrawler implements BaseCrawler {

    private static final String TOSS_TECH_RSS_URL = "https://toss.tech/rss.xml";
    private static final String SOURCE_NAME = "토스 기술 블로그";
    private static final int MAX_CRAWL_COUNT = 5;

    private final PostRepository postRepository;

    @Override
    public List<Post> crawl() {
        List<Post> posts = new ArrayList<>();

        try {
            log.info("토스 기술 블로그 RSS 크롤링 시작: {}", TOSS_TECH_RSS_URL);

            Document rssDoc = Jsoup.connect(TOSS_TECH_RSS_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // RSS item 요소들 추출
            Elements items = rssDoc.select("item");

            int count = 0;
            for (Element item : items) {
                if (count >= MAX_CRAWL_COUNT) {
                    log.info("최대 크롤링 개수({})에 도달", MAX_CRAWL_COUNT);
                    break;
                }

                try {
                    // URL 추출
                    Element linkElement = item.selectFirst("link");
                    String url = linkElement != null ? linkElement.text() : "";
                    if (url.isEmpty()) continue;

                    // 이미 DB에 있는 URL이면 크롤링 중단
                    if (postRepository.existsByOriginalUrl(url)) {
                        log.info("이미 존재하는 URL 발견, 크롤링 중단: {}", url);
                        break;
                    }

                    // 제목 추출
                    Element titleElement = item.selectFirst("title");
                    String title = titleElement != null ? titleElement.text() : "제목 없음";

                    // 요약 추출 (RSS의 description)
                    Element descElement = item.selectFirst("description");
                    String summary = descElement != null ? descElement.text() : "요약 없음";

                    // 발행일 추출 (RSS의 pubDate)
                    Element pubDateElement = item.selectFirst("pubDate");
                    LocalDateTime publishedAt;
                    if (pubDateElement != null && !pubDateElement.text().isEmpty()) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(
                                    pubDateElement.text(),
                                    DateTimeFormatter.RFC_1123_DATE_TIME
                            );
                            publishedAt = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            log.warn("날짜 파싱 실패: {}", pubDateElement.text());
                            publishedAt = LocalDateTime.now();
                        }
                    } else {
                        publishedAt = LocalDateTime.now();
                    }

                    // 상세 페이지에서 썸네일과 작성자 추출
                    String thumbnailUrl = null;
                    String author = "Toss Tech";
                    try {
                        Document detailDoc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0")
                                .timeout(5000)
                                .get();

                        thumbnailUrl = OpenGraphParser.parseThumbnail(detailDoc);

                        // 첫 번째 <p> 태그에서 작성자 추출
                        Element firstParagraph = detailDoc.selectFirst("p");
                        if (firstParagraph != null) {
                            String firstText = firstParagraph.text();

                            // "입니다" 패턴으로 작성자 추출
                            if (firstText.contains("입니다")) {
                                // "입니다" 앞에서 한글 이름 패턴 찾기: 한글2-4자(, 한글2-4자)*
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                                        "([가-힣]{2,4}(?:,\\s*[가-힣]{2,4})*)입니다"
                                );
                                java.util.regex.Matcher matcher = pattern.matcher(firstText);
                                if (matcher.find()) {
                                    author = matcher.group(1).trim();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("상세 페이지 파싱 실패: {}", url);
                    }

                    Post post = Post.builder()
                            .title(title)
                            .summary(summary)
                            .originalUrl(url)
                            .publisher(author)
                            .publishedAt(publishedAt)
                            .sourceName(SOURCE_NAME)
                            .thumbnailUrl(thumbnailUrl)
                            .build();

                    posts.add(post);
                    count++;
                    log.debug("크롤링 성공 ({}/{}): {} ({})", count, MAX_CRAWL_COUNT, title, publishedAt);

                } catch (Exception e) {
                    log.error("게시글 파싱 실패", e);
                }
            }

            log.info("토스 기술 블로그 크롤링 완료: {} 개", posts.size());

        } catch (Exception e) {
            log.error("토스 기술 블로그 크롤링 실패", e);
        }

        return posts;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}