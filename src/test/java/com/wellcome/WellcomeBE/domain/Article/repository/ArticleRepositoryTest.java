package com.wellcome.WellcomeBE.domain.Article.repository;

import com.wellcome.WellcomeBE.domain.Article.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ArticleRepositoryTest {


    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void 랜덤_아티클_5개_정상_조회() {
        // given
        for (int i = 1; i <= 10; i++) {
            articleRepository.save(
                    Article.builder()
                            .title("Test " + i)
                            .content("Content " + i)
                            .build()
            );
        }

        // when
        List<Article> result = articleRepository.findRandomArticles();

        // then
        assertThat(result).hasSize(5); // 정확히 5개만 조회되는지
        assertThat(result).allMatch(article -> article.getId() != null); // id가 있는지
    }

    @Test
    void 아티클_수가_요청보다_적을_경우_정상조회() {
        // given
        for (int i = 1; i <= 3; i++) {
            articleRepository.save(
                    Article.builder()
                            .title("Short " + i)
                            .content("Short content " + i)
                            .build()
            );
        }

        // when
        List<Article> result = articleRepository.findRandomArticles();

        // then
        assertThat(result).hasSize(3); // 존재하는 개수만큼만 조회
    }

    @Test
    void 랜덤성이_있는지_확인() {
        // given
        for (int i = 1; i <= 10; i++) {
            articleRepository.save(
                    Article.builder()
                            .title("Shuffle " + i)
                            .content("Content " + i)
                            .build()
            );
        }

        // when
        List<Article> result1 = articleRepository.findRandomArticles();
        List<Article> result2 = articleRepository.findRandomArticles();

        // then (단순 비교. 완전한 무작위 검증은 어려움)
        assertThat(result1).isNotEqualTo(result2); // 순서 혹은 조합 차이 발생 가능
    }

}