package com.wellcome.WellcomeBE.domain.Article;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class Article extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(name = "content", columnDefinition = "TEXT",nullable = false)
    private String content;

    @Lob
    @Column(nullable = false)
    private String thumbnailUrl;

    @Builder.Default
    @Column(nullable = false)
    private Long view = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wellness_info_id")
    private WellnessInfo wellnessInfo;

    // 조회수 증가 메서드
    public void incrementView() {
        this.view++;
    }

}
