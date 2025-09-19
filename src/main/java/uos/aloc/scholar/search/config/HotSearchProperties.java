package uos.aloc.scholar.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "search.hot")
public class HotSearchProperties {

    /**
     * HOT 섹션 조회 시 포함할 게시글의 최대 경과 일수.
     * 기본값은 30일(약 한 달)로 설정한다.
     */
    private long lookbackDays = 30;

    public long getLookbackDays() {
        return lookbackDays;
    }

    public void setLookbackDays(long lookbackDays) {
        this.lookbackDays = Math.max(0, lookbackDays);
    }
}
