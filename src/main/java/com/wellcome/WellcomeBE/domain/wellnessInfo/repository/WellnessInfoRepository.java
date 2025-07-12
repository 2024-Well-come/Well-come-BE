package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long>, WellnessInfoRepositoryCustom {

    /**
     * 임시 반경 계산
     */
    @Query(value = "SELECT * FROM wellness_info wi " +
            "WHERE wi.wellness_info_id != :wellness_info_id " +
            "AND (6371 * acos(cos(radians(:mapY)) * cos(radians(wi.mapy)) * cos(radians(wi.mapx) - radians(:mapX)) + sin(radians(:mapY)) * sin(radians(wi.mapy)))) < :radius " +
            "ORDER BY (6371 * acos(cos(radians(:mapY)) * cos(radians(wi.mapy)) * cos(radians(wi.mapx) - radians(:mapX)) + sin(radians(:mapY)) * sin(radians(wi.mapy)))) " +
            "LIMIT 6", nativeQuery = true)
    List<WellnessInfo> findTop6NearbyWellnessInfo(@Param("mapX") Double mapX,
                                                  @Param("mapY") Double mapY,
                                                  @Param("wellness_info_id") Long wellnessInfoId,
                                                  @Param("radius") Double radius);



    List<WellnessInfo> findByThumbnailUrlNotNull();

    // 후기 게시글 내부 웰니스 장소 정보
//    @Query(
//            "SELECT w, tpp, " +
//                    "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END, " +
//                    "CASE WHEN s.id IS NOT NULL THEN TRUE ELSE FALSE END " +
//                    "FROM WellnessInfo w " +
//                    "LEFT JOIN TripPlanPlace tpp ON tpp.wellnessInfo = w " +
//                    "LEFT JOIN TripPlan tp ON tp.id = tpp.tripPlan.id " +
//                    "LEFT JOIN Community c ON c.tripPlan.id = tp.id " +
//                    "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
//                    "LEFT JOIN Support s ON s.wellnessInfo = w AND s.member = :member " +
//                    "WHERE c.id = :communityId " +
//                    "AND (tpp.review IS NOT NULL OR tpp.rating IS NOT NULL)"
//    )
//    List<Object[]> findWellnessInfoWithSupportAndLikedByCommunityId(
//            @Param("communityId") Long communityId,
//            @Param("member") Member member
//    );


    //Page<WellnessInfo> findAll(Pageable pageable);

    @Query("SELECT new com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly(w.contentId) FROM WellnessInfo w")
    List<WellnessContentIdOnly> findAllContentIdOnly();
    Optional<WellnessInfo> findByContentId(String contentId);
}
