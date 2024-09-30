package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.Article.dto.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class WellnessInfoArticleResponse {
    private List<ArticleResponse.ArticleItem> articleList;
}
