package com.wellcome.WellcomeBE.domain.wellnessInfoImg;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WellnessInfoImg extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wellness_info_img_id")
    private Long wellnessInfoImgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wellness_info_id", nullable = false)
    private WellnessInfo wellnessInfo;

    @Column(nullable = false, unique = true, length = 512)
    private String imgUrl;

    private String imgName;
    private String contentid;

}
