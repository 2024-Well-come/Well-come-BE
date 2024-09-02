package com.wellcome.WellcomeBE.domain.Article;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
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
    @Column(name = "trip_plan_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Lob
    @Column(nullable = false)
    private String thumbnailUrl;

    @Builder.Default
    @Column(nullable = false)
    private Long view = 0L;

}
