package com.ureka.techpost.domain.post.service.crawler;

import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogTvingCrawler implements BaseCrawler {

    private static final String TVING_TECH_RSS_URL = "https://medium.com/feed/tving-team";
    private static final String SOURCE_NAME = "티빙 기술 블로그";
    private static final int MAX_CRAWL_COUNT = 5;

    private final PostRepository postRepository;

    @Override
    public List<Post> crawl() {
        List<Post> posts = new ArrayList<>();

        try {
            log.info("티빙 기술 블로그 RSS 크롤링 시작: {}", TVING_TECH_RSS_URL);

            Document rssDoc = Jsoup.connect(TVING_TECH_RSS_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // RSS의 item 요소들 추출 (이미 최신순으로 정렬되어 있음)
            Elements items = rssDoc.select("item");

            int count = 0;
            for (Element item : items) {
                if (count >= MAX_CRAWL_COUNT) {
                    log.info("최대 크롤링 개수({})에 도달", MAX_CRAWL_COUNT);
                    break;
                }

                try {
                    // URL 추출 및 쿼리 파라미터 제거
                    Element linkElement = item.selectFirst("link");
                    String url = linkElement != null ? linkElement.text() : "";
                    if (url.isEmpty()) continue;

                    // Medium URL의 쿼리 파라미터 제거 (예: ?source=rss----52cdc55547df---4)
                    if (url.contains("?")) {
                        url = url.substring(0, url.indexOf("?"));
                    }

                    // 이미 DB에 있는 URL이면 크롤링 중단
                    if (postRepository.existsByOriginalUrl(url)) {
                        log.info("이미 존재하는 URL 발견, 크롤링 중단: {}", url);
                        break;
                    }

                    // 제목 추출
                    Element titleElement = item.selectFirst("title");
                    String title = titleElement != null ? titleElement.text() : "제목 없음";

                    // 요약 추출 (RSS의 description, HTML 태그 제거)
                    Element descElement = item.selectFirst("description");
                    String summary = "요약 없음";
                    if (descElement != null) {
                        // HTML 태그 제거하고 텍스트만 추출
                        Document tempDoc = Jsoup.parse(descElement.text());
                        summary = tempDoc.text();
                        if (summary.length() > 500) {
                            summary = summary.substring(0, 500) + "...";
                        }
                    }

                    // 작성자 추출 (dc:creator)
                    Element authorElement = item.selectFirst("dc|creator, creator");
                    String author = authorElement != null ? authorElement.text() : "Tving Team";

                    // 발행일 추출 (RSS의 pubDate)
                    Element pubDateElement = item.selectFirst("pubDate");
                    LocalDateTime publishedAt;
                    if (pubDateElement != null && !pubDateElement.text().isEmpty()) {
                        try {
                            // RFC 1123 형식 파싱
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

                    // 썸네일 추출 (content:encoded에서 첫 번째 이미지 찾기)
                    String thumbnailUrl = null;
                    Element contentElement = item.selectFirst("content|encoded, encoded");
                    if (contentElement != null) {
                        Document contentDoc = Jsoup.parse(contentElement.text());
                        Element imgElement = contentDoc.selectFirst("img");
                        if (imgElement != null) {
                            thumbnailUrl = imgElement.attr("src");
                        }
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

            log.info("티빙 기술 블로그 크롤링 완료: {} 개", posts.size());

        } catch (Exception e) {
            log.error("티빙 기술 블로그 크롤링 실패", e);
        }

        return posts;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}