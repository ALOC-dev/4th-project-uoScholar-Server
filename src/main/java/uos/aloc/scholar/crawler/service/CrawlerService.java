package uos.aloc.scholar.crawler.service;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.repository.NoticeRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlerService {

    private final NoticeRepository noticeRepository;

    public CrawlerService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public void crawlNotices() {
        String baseUrl = "https://www.uos.ac.kr/korNotice/view.do";
        //int seq = 14600; // 시작 seq 번호 (예: 2024-01-04 기준)
        int seq =15400;
        int missingCount = 0; // 연속으로 게시물이 없는 경우 카운트
        int retryCount = 0;   // read timeout 재시도 카운트
        int lastSuccessfulSeq = -1; // 마지막 성공한 seq 번호
        final int maxRetries = 3;

        while (true) {
            try {
                // HTTP 응답을 받아서 Content-Type 확인
                Connection.Response response = Jsoup.connect(baseUrl)
                        .data("list_id", "20013DA1",
                              "seq", String.valueOf(seq),
                              "sort", "1",
                              "pageIndex", "1",
                              "searchCnd", "",
                              "searchWrd", "",
                              "cate_id", "",
                              "viewAuth", "Y",
                              "writeAuth", "Y",
                              "board_list_num", "10",
                              "lpageCount", "12",
                              "menuid", "")
                        .userAgent("Mozilla/5.0")
                        .timeout(5000)
                        .execute();


                // HTML 페이지를 파싱
                Document doc = response.parse();

                // 제목 추출: <div class="vw-tibx"><h4>제목</h4></div>
                String title = doc.select("div.vw-tibx h4").text();
                if (title == null || title.trim().isEmpty()) {
                    missingCount++;
                    System.out.println("Seq " + seq + " : 제목이 없어 게시물이 없는 것으로 판단. 연속 없음 횟수: " + missingCount);
                    if (missingCount >= 3) {
                        System.out.println("3개 연속 게시물이 없음. 크롤링 중단");
                        break;
                    }
                    seq++;
                    continue;
                } else {
                    missingCount = 0; // 정상 게시물이면 카운트 초기화
                }

                // 작성부서 및 작성일자 추출
                Elements infoSpans = doc.select("div.vw-tibx div.zl-bx div.da span");
                String department = "";
                String dateText = "";
                if (infoSpans.size() >= 3) {
                    department = infoSpans.get(1).text();
                    dateText = infoSpans.get(2).text();
                }

                // 날짜 추출: "YYYY-MM-DD" 패턴
                Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
                Matcher matcher = pattern.matcher(dateText);
                String extractedDate = "";
                if (matcher.find()) {
                    extractedDate = matcher.group(1);
                }
                LocalDate postedDate = LocalDate.now();
                if (!extractedDate.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    postedDate = LocalDate.parse(extractedDate, formatter);
                }

                // 게시글번호 추출: <input name="seq" value="...">
                String postNumberStr = doc.select("input[name=seq]").attr("value");
                if (postNumberStr.isEmpty()) {
                    postNumberStr = String.valueOf(seq);
                }
                Integer postNumber = Integer.parseInt(postNumberStr);

                // 링크 구성
                String link = baseUrl + "?list_id=20013DA1&seq=" + seq;

                // DB 중복 검사
                if (noticeRepository.existsByPostNumber(postNumber)) {
                    System.out.println("Seq " + seq + " (게시글번호: " + postNumber + ") 이미 존재하여 패스합니다.");
                    seq++;
                    continue;
                }

                // Notice 객체 생성 및 DB 저장
                Notice notice = new Notice();
                notice.setPostNumber(postNumber);
                notice.setTitle(title);
                notice.setLink(link);
                notice.setPostedDate(postedDate);
                notice.setDepartment(department);

                noticeRepository.save(notice);
                lastSuccessfulSeq = seq;
                System.out.println("Seq " + seq + " 크롤링 및 저장 성공");

                Thread.sleep(300);
                seq++;
                retryCount = 0; // 성공하면 재시도 카운트 초기화

            } catch (IOException e) {
                // IOException 전체에 대해 최대 maxRetries까지 재시도
                retryCount++;
                System.err.println("Seq " + seq + " IOException: " + e.getMessage() +
                        " (retry " + retryCount + " of " + maxRetries + ")");
                if (retryCount >= maxRetries) {
                    System.out.println("Seq " + seq + " 최대 재시도 횟수 초과, 건너뜁니다.");
                    seq++;
                    retryCount = 0;
                }
                continue;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Seq " + seq + " 인터럽트 발생: " + e.getMessage());
                break;
            } catch (Exception e) {
                // 기타 예외에 대해서도 최대 maxRetries까지 재시도
                retryCount++;
                System.err.println("Seq " + seq + " 기타 예외: " + e.getMessage() +
                        " (retry " + retryCount + " of " + maxRetries + ")");
                if (retryCount >= maxRetries) {
                    System.out.println("Seq " + seq + " 최대 재시도 횟수 초과, 건너뜁니다.");
                    seq++;
                    retryCount = 0;
                }
                continue;
            }
        }
        System.out.println("크롤링 종료. 마지막 seq: " + (lastSuccessfulSeq));
    }
}
