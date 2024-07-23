package com.wellcome.WellcomeBE.domain.wellnessInfo;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.global.type.Area;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class WellnessInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wellness_info_id")
    private Long id;

    // 기본 정보
    @Column(nullable = false)
    private String title;

    private String content;

    private String tel;

    @Lob
    private String thumbnailUrl;

    // 분류
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sigungu sigungu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Thema thema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    // 주소
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double mapX;

    @Column(nullable = false)
    private Double mapY;

    @Column(nullable = false)
    private Point location;

}
