package com.wellcome.WellcomeBE.domain.Article.service;

import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.Article.dto.ArticleResponse;
import com.wellcome.WellcomeBE.domain.Article.repository.ArticleRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.wellcome.WellcomeBE.domain.Article.Article.create;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ARTICLE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private GoogleMapInfoService googleMapInfoService;

    @InjectMocks
    private ArticleService articleService;

    @Captor
    private ArgumentCaptor<LocalDateTime> dateCaptor;

    @Test
    void getPopularArticles_조회수높은아티클이있을때 (){
        //given
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        WellnessInfo wellnessInfo = mock(WellnessInfo.class);

        Article article1 = create(1L, "Title 1", "Content 1", "", 100, wellnessInfo);
        ReflectionTestUtils.setField(article1, "createdAt", oneMonthAgo.plusDays(5)); // 25일 전
        Article article2 = create(2L, "Title 2", "Content 2", "", 90, wellnessInfo);
        ReflectionTestUtils.setField(article2, "createdAt", oneMonthAgo.plusDays(10)); // 20일 전

        List<Article> mockArticles = List.of(article1, article2);

        when(articleRepository.findTopArticles(any(LocalDateTime.class), eq(PageRequest.of(0, 5))))
                .thenReturn(mockArticles);
        // when
        ArticleResponse.ArticleBest result = articleService.getPopularArticles();

        // then
        assertThat(result.getArticleItem()).hasSize(2);
        assertThat(result.getArticleItem().get(0).getTitle()).isEqualTo("Title 1");
        assertThat(result.getArticleItem().get(1).getTitle()).isEqualTo("Title 2");

        // ArgumentCaptor로 실제 값 캡처
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(articleRepository, times(1)).findTopArticles(captor.capture(), any(PageRequest.class));

        // 실제 호출된 LocalDateTime 값을 초 단위까지 비교
        assertThat(captor.getValue().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(oneMonthAgo.truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void getPopularArticles_조회수5미만이면랜덤아티클가져오기() {
        // given
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        WellnessInfo wellnessInfo = mock(WellnessInfo.class);

        Article article = create(3L, "Random Title", "Random Content", "", 3, wellnessInfo);
        ReflectionTestUtils.setField(article, "createdAt", oneMonthAgo.plusDays(2));
        List<Article> mockArticles = List.of(article);

        // when
        // findRandomArticles 스텁 설정
        when(articleRepository.findRandomArticles()).thenReturn(mockArticles);
        when(articleRepository.findTopArticles(any(LocalDateTime.class), eq(PageRequest.of(0, 5))))
                .thenReturn(Collections.emptyList());

        // ArticleService의 getPopularArticles 호출
        ArticleResponse.ArticleBest result = articleService.getPopularArticles();

        // then
        assertThat(result.getArticleItem()).hasSize(1);
        assertThat(result.getArticleItem().get(0).getTitle()).isEqualTo("Random Title");

        // verify 메서드로 호출된 횟수 및 인자 확인
        verify(articleRepository, times(1)).findTopArticles(any(LocalDateTime.class), eq(PageRequest.of(0, 5)));  // 정확한 인자 비교
        verify(articleRepository, times(1)).findRandomArticles();
    }


    @Test
    void getPopularArticles_한달_기준_점검 (){
        // given
        LocalDateTime now = LocalDateTime.now();
        WellnessInfo wellnessInfo = mock(WellnessInfo.class);
        LocalDateTime expectedOneMonthAgo = now.minus(30, ChronoUnit.DAYS);

        Article article1 = create(1L, "Title 1", "Content 1", "", 100, wellnessInfo);
        ReflectionTestUtils.setField(article1, "createdAt", now.plusDays(5)); // 25일 전
        Article article2 = create(2L, "Title 2", "Content 2", "", 90, wellnessInfo);
        ReflectionTestUtils.setField(article2, "createdAt", now.plusDays(10)); // 20일 전
        List<Article> mockArticles = List.of(article1, article2);


        when(articleRepository.findTopArticles(any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(mockArticles);

        // when
        articleService.getPopularArticles();

        // then
        verify(articleRepository).findTopArticles(dateCaptor.capture(), any(PageRequest.class));

        LocalDateTime actualOneMonthAgo = dateCaptor.getValue();

        // 30일 전인지 검증 (1초 정도 차이가 날 수 있으므로 isCloseTo 사용)
        assertThat(actualOneMonthAgo)
                .isCloseTo(expectedOneMonthAgo, within(1, ChronoUnit.SECONDS));

        // 반환된 아티클이 1개월 이내인지 검증
        for (Article article : mockArticles) {
            assertThat(article.getCreatedAt()).isAfterOrEqualTo(actualOneMonthAgo);
        }
    }


    @Test
    void getArticles_페이지별아티클조회_최신순() {
        // given
        int page = 1;
        Pageable pageable = PageRequest.of(page, 5);
        LocalDateTime now = LocalDateTime.now();

        // 최신 아티클이 먼저 오도록 생성
        WellnessInfo wellnessInfo = mock(WellnessInfo.class);
        Article article1 = create(1L, "Title 1", "Content 1", "", 100, wellnessInfo);
        ReflectionTestUtils.setField(article1, "createdAt", now.minusDays(1));
        Article article2 = create(2L, "Title 2", "Content 2", "", 90, wellnessInfo);
        ReflectionTestUtils.setField(article2, "createdAt", now.minusDays(2));
        List<Article> Articles = List.of(article1, article2);

        Page<Article> mockPage = new PageImpl<>(Articles, pageable, Articles.size());
        when(articleRepository.findAllArticleByCreatedAt(pageable)).thenReturn(mockPage);

        // when
        ArticleResponse.ArticleBrief result = articleService.getArticles(page);

        // then
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getTitle()).isEqualTo("Title 1");  // 최신 아티클이 먼저 나와야 함
        assertThat(result.getData().get(1).getTitle()).isEqualTo("Title 2");
        assertThat(result.getTotalCount()).isEqualTo(7);
        assertThat(result.getPageNum()).isEqualTo(2);

        // repository 메서드가 정확히 호출되었는지 검증
        verify(articleRepository, times(1)).findAllArticleByCreatedAt(pageable);
    }

    @Test
    void getArticleDetail_성공() {
        // Given
        WellnessInfo wellnessInfo = WellnessInfo.builder()
                .parentId("test-parent-id")
                .category(Category.RESTAURANT)
                .thema(Thema.FOOD)
                .build();

        Article article = create(1L, "Title 1", "Content 1", "", 10, wellnessInfo);
        ReflectionTestUtils.setField(article, "createdAt", LocalDateTime.now());


        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        PlaceReviewResponse placeReviewResponse = mock(PlaceReviewResponse.class);
        PlaceReviewResponse.PlaceResult placeResult = mock(PlaceReviewResponse.PlaceResult.class);
        when(placeReviewResponse.getResult()).thenReturn(placeResult);
        when(placeResult.getRating()).thenReturn(4.5);
        when(googleMapInfoService.getPlaceDetails("test-parent-id")).thenReturn(Mono.just(placeReviewResponse));

        // When
        ArticleResponse.ArticleDetail result = articleService.getArticleDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getArticleId());
        verify(articleRepository).findById(1L);
        verify(googleMapInfoService).getPlaceDetails("test-parent-id");

        // 조회수가 1 증가했는지 확인
        assertEquals(11, article.getView());
    }

    @Test
    void getArticleDetail_articleNotFound_에러() {
        // given
        when(articleRepository.findById(2L)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> articleService.getArticleDetail(2L));
        assertEquals(ARTICLE_NOT_FOUND, exception.getCustomErrorCode());

        verify(articleRepository).findById(2L);
        verify(googleMapInfoService, never()).getPlaceDetails(anyString());
    }
}