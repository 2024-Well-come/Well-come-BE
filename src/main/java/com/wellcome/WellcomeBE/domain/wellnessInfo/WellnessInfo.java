package com.wellcome.WellcomeBE.domain.wellnessInfo;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.global.type.Area;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


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

    @Column(unique = true)
    private String contentId; // tour4.0 PK

    private String parentId; //Google Map PK

    private String tel;

    @Column(columnDefinition = "TEXT")
    private String originalThumbnailUrl; // Tour API 제공 이미지 URL

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl; // Tour API 제공 이미지를 S3에 업로드 후 생성되는 객체 URL

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

    @Column(nullable = false, columnDefinition = "GEOMETRY")
    private Point location;
    private long view;
    private boolean isDeleted;

    public void setParentId(String parentId){
        this.parentId = parentId;
    }

    public void updateThumbnailUrl(String s3ThumbnailUrl){
        this.thumbnailUrl = s3ThumbnailUrl;
    }
    public static WellnessInfo of(WellnessInfoTemp w, String thumbnailUrl, String parentId) throws ParseException {
        double mapX = w.getMapX();
        double mapY = w.getMapY();

        // WKTReader를 통해 WKT -> 실제 타입으로 변환
        String pointWKT = String.format("POINT(%s %s)", mapX, mapY);
        Point point = (Point) new WKTReader().read(pointWKT);

        return WellnessInfo.builder()
                // 기본 정보
                .title(w.getTitle())
                .contentId(w.getContentId())
                .tel(w.getTel())
                .originalThumbnailUrl(w.getOriginalThumbnailUrl())

                .thumbnailUrl(thumbnailUrl)
                .parentId(parentId)

                // 분류
                .area(w.getArea())
                .sigungu(w.getSigungu())
                .thema(w.getThema())
                .category(w.getCategory())

                // 주소
                .address(w.getAddress())
                .mapX(w.getMapX())
                .mapY(w.getMapY())
                .location(point)

                .build();
    }

}
