package uos.aloc.scholar.crawler.service;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.repository.NoticeRepository;
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
        // 테스트를 위해 100개 정도 크롤링 (예: 15300부터 15399까지)
        int startSeq = 15300;
        int endSeq = startSeq + 99;

        for (int seq = startSeq; seq <= endSeq; seq++) {
            try {
                // GET 요청 시 필요한 파라미터 설정
                Document doc = Jsoup.connect(baseUrl)
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
                        .get();

                // 1. 제목: <div class="vw-tibx"><h4>제목</h4></div>
                String title = doc.select("div.vw-tibx h4").text();

                // 2. 작성자, 작성부서, 작성일자: <div class="vw-tibx"> 내 
                //    <div class="zl-bx clearfix"><div class="da"><span>작성자</span><span>부서</span><span>날짜정보</span></div></div>
                Elements infoSpans = doc.select("div.vw-tibx div.zl-bx div.da span");
                String department = "";
                String dateText = "";
                if (infoSpans.size() >= 3) {
                    department = infoSpans.get(1).text();
                    dateText = infoSpans.get(2).text();
                }

                // 3. 날짜 추출: 정규표현식을 사용하여 "YYYY-MM-DD" 형태만 추출
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

                // 4. 게시글번호 추출: hidden input 필드 (예: <input name="seq" value="15388">)
                String postNumberStr = doc.select("input[name=seq]").attr("value");
                if (postNumberStr.isEmpty()) {
                    postNumberStr = String.valueOf(seq);
                }
                Integer postNumber = Integer.parseInt(postNumberStr);

                // 5. 링크: 현재 요청한 URL 기반으로 생성
                String link = baseUrl + "?list_id=20013DA1&seq=" + seq;

                // 동일한 게시글번호가 이미 DB에 있으면 저장하지 않음
                if (noticeRepository.existsByPostNumber(postNumber)) {
                    System.out.println("Seq " + seq + " (게시글번호: " + postNumber + ") 이미 존재하여 패스합니다.");
                    continue;
                }

                // Notice 객체 생성 후 DB 저장
                Notice notice = new Notice();
                notice.setPostNumber(postNumber);
                notice.setTitle(title);
                notice.setLink(link);
                notice.setPostedDate(postedDate);
                notice.setDepartment(department);

                noticeRepository.save(notice);
                System.out.println("Seq " + seq + " 크롤링 및 저장 성공");

                // 서버 부담 완화를 위한 딜레이 (300ms)
                Thread.sleep(300);

            } catch (IOException e) {
                System.err.println("Seq " + seq + " 접속 실패: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Seq " + seq + " 인터럽트 발생: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Seq " + seq + " 기타 오류: " + e.getMessage());
            }
        }
    }
}
