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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsEtnewsCrawler implements BaseCrawler {

    private static final String ETNEWS_SW_URL = "https://www.etnews.com/news/section.html?id1=04&id2=043";
    private static final String ETNEWS_BASE_URL = "https://www.etnews.com";
    private static final String SOURCE_NAME = "전자신문 SW";
    private static final int MAX_CRAWL_COUNT = 6;

    private final PostRepository postRepository;

    @Override
    public List<Post> crawl() {
        List<Post> posts = new ArrayList<>();

        try {
            log.info("전자신문 SW 크롤링 시작: {}", ETNEWS_SW_URL);

            Document doc = Jsoup.connect(ETNEWS_SW_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // news_list 클래스를 가진 ul 요소 찾기
            Element newsList = doc.selectFirst("ul.news_list");
            if (newsList == null) {
                log.warn("뉴스 리스트를 찾을 수 없습니다.");
                return posts;
            }

            // li 요소들 추출
            Elements articles = newsList.select("li");

            int count = 0;
            for (Element article : articles) {
                if (count >= MAX_CRAWL_COUNT) {
                    log.info("최대 크롤링 개수({})에 도달", MAX_CRAWL_COUNT);
                    break;
                }

                try {
                    // URL 추출
                    Element linkElement = article.selectFirst("strong > a[href]");
                    if (linkElement == null) continue;

                    String relativeUrl = linkElement.attr("href");
                    String url = relativeUrl.startsWith("http") ? relativeUrl : ETNEWS_BASE_URL + relativeUrl;

                    // 이미 DB에 있는 URL이면 건너뛰기
                    if (postRepository.existsByOriginalUrl(url)) {
                        log.debug("이미 존재하는 URL 건너뛰기: {}", url);
                        continue;
                    }

                    // 제목 추출
                    String title = linkElement.text().trim();
                    if (title.isEmpty()) {
                        title = "제목 없음";
                    }

                    // 요약 추출
                    Element summaryElement = article.selectFirst("p.summary");
                    String summary = summaryElement != null ? summaryElement.text().trim() : "";
                    if (summary.isEmpty()) {
                        summary = "요약 없음";
                    } else if (summary.length() > 500) {
                        summary = summary.substring(0, 500) + "...";
                    }

                    // 썸네일 추출
                    Element imgElement = article.selectFirst("figure img[src]");
                    String thumbnailUrl = null;
                    if (imgElement != null) {
                        thumbnailUrl = imgElement.attr("src");
                        // 상대 URL이면 절대 URL로 변환
                        if (!thumbnailUrl.startsWith("http")) {
                            thumbnailUrl = "https://img.etnews.com" + thumbnailUrl;
                        }
                    }

                    // 날짜 추출 (YYYY-MM-DD HH:MM 형식)
                    Element dateElement = article.selectFirst("span.date");
                    LocalDateTime publishedAt = LocalDateTime.now();

                    if (dateElement != null) {
                        String dateText = dateElement.text().trim();
                        try {
                            // "2025-12-11 10:46" 형식 파싱
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            publishedAt = LocalDateTime.parse(dateText, formatter);
                        } catch (Exception e) {
                            log.warn("날짜 파싱 실패: {}", dateText);
                        }
                    }

                    // 작성자는 기사 상세에서만 확인 가능하므로 기본값 사용
                    String author = "전자신문";

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

            log.info("전자신문 SW 크롤링 완료: {} 개", posts.size());

        } catch (Exception e) {
            log.error("전자신문 SW 크롤링 실패", e);
        }

        return posts;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}
