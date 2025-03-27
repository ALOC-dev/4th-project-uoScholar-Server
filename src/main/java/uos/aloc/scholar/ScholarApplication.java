package uos.aloc.scholar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(
	exclude = {DataSourceAutoConfiguration.class}		//데이터베이스 로드 안하고 실행하는 부분 - 데이터베이스 추가되면 지우세요!
)
public class ScholarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScholarApplication.class, args);
	}

}
