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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsZdnetCrawler implements BaseCrawler {

    private static final String ZDNET_COMPUTING_URL = "https://zdnet.co.kr/news/?lstcode=0020&page=1";
    private static final String ZDNET_BASE_URL = "https://zdnet.co.kr";
    private static final String SOURCE_NAME = "ZDNET 컴퓨팅";
    private static final int MAX_CRAWL_COUNT = 6;

    private final PostRepository postRepository;

    @Override
    public List<Post> crawl() {
        List<Post> posts = new ArrayList<>();

        try {
            log.info("ZDNET 컴퓨팅 인기뉴스 크롤링 시작: {}", ZDNET_COMPUTING_URL);

            Document doc = Jsoup.connect(ZDNET_COMPUTING_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // "인기뉴스" 섹션 찾기
            Element popularSection = doc.select("h2:contains(인기뉴스)").first();
            if (popularSection == null) {
                log.warn("인기뉴스 섹션을 찾을 수 없습니다.");
                return posts;
            }

            // 인기뉴스 바로 다음의 news_box
            Element newsBox = popularSection.nextElementSibling();
            while (newsBox != null && !newsBox.hasClass("news_box")) {
                newsBox = newsBox.nextElementSibling();
            }

            if (newsBox == null) {
                log.warn("인기뉴스 news_box를 찾을 수 없습니다.");
                return posts;
            }

            // newsPost 클래스를 가진 모든 기사 (big + small)
            Elements articles = newsBox.select(".newsPost");

            int count = 0;
            for (Element article : articles) {
                if (count >= MAX_CRAWL_COUNT) {
                    log.info("최대 크롤링 개수({})에 도달", MAX_CRAWL_COUNT);
                    break;
                }

                try {
                    // URL 추출
                    Element linkElement = article.selectFirst("a[href]");
                    if (linkElement == null) continue;

                    String relativeUrl = linkElement.attr("href");
                    String url = relativeUrl.startsWith("http") ? relativeUrl : ZDNET_BASE_URL + relativeUrl;

                    // 이미 DB에 있는 URL이면 건너뛰기 (중단하지 않고 계속)
                    if (postRepository.existsByOriginalUrl(url)) {
                        log.debug("이미 존재하는 URL 건너뛰기: {}", url);
                        continue;
                    }

                    // 제목 추출
                    Element titleElement = article.selectFirst("h3");
                    String title = titleElement != null ? titleElement.text() : "제목 없음";

                    // 요약 추출
                    Element summaryElement = article.selectFirst(".assetText > a > p");
                    String summary = summaryElement != null ? summaryElement.text().trim() : "";
                    if (summary.isEmpty()) {
                        summary = "요약 없음";
                    } else if (summary.length() > 500) {
                        summary = summary.substring(0, 500) + "...";
                    }

                    // 썸네일 추출
                    Element imgElement = article.selectFirst("img[data-src]");
                    String thumbnailUrl = null;
                    if (imgElement != null) {
                        thumbnailUrl = imgElement.attr("data-src");
                        if (thumbnailUrl.equals("/images/default.png")) {
                            thumbnailUrl = null; // 기본 이미지는 무시
                        }
                    }

                    // 날짜와 기자 추출
                    Element bylineElement = article.selectFirst("p.byline");
                    String author = "ZDNET";
                    LocalDateTime publishedAt = LocalDateTime.now();

                    if (bylineElement != null) {
                        String bylineText = bylineElement.text();

                        // 기자 추출 (예: "2025.11.19 AM 07:35김미정 기자" -> "김미정")
                        Pattern authorPattern = Pattern.compile("([가-힣]{2,4})\\s*기자");
                        Matcher authorMatcher = authorPattern.matcher(bylineText);
                        if (authorMatcher.find()) {
                            author = authorMatcher.group(1);
                        }

                        // 날짜 추출 (예: "2025.11.19 AM 07:35")
                        Pattern datePattern = Pattern.compile("(\\d{4})\\.(\\d{2})\\.(\\d{2})\\s+(AM|PM)\\s+(\\d{2}):(\\d{2})");
                        Matcher dateMatcher = datePattern.matcher(bylineText);
                        if (dateMatcher.find()) {
                            try {
                                int year = Integer.parseInt(dateMatcher.group(1));
                                int month = Integer.parseInt(dateMatcher.group(2));
                                int day = Integer.parseInt(dateMatcher.group(3));
                                String ampm = dateMatcher.group(4);
                                int hour = Integer.parseInt(dateMatcher.group(5));
                                int minute = Integer.parseInt(dateMatcher.group(6));

                                // AM/PM 처리
                                if (ampm.equals("PM") && hour != 12) {
                                    hour += 12;
                                } else if (ampm.equals("AM") && hour == 12) {
                                    hour = 0;
                                }

                                publishedAt = LocalDateTime.of(year, month, day, hour, minute);
                            } catch (Exception e) {
                                log.warn("날짜 파싱 실패: {}", bylineText);
                            }
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

            log.info("ZDNET 컴퓨팅 인기뉴스 크롤링 완료: {} 개", posts.size());

        } catch (Exception e) {
            log.error("ZDNET 크롤링 실패", e);
        }

        return posts;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}
