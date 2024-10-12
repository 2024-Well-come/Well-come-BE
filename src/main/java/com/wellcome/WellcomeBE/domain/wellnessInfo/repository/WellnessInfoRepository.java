package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long> {

    /**
     * 목록 조회
     */
    // 테마나 지역을 모두 선택한 경우
    @Query(
            "SELECT w, " +
                "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END " +
            "FROM WellnessInfo w " +
            "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
            "WHERE w.thema IN :themaList AND w.sigungu IN :sigunguList " +
            "ORDER BY w.view DESC"
    )
    Page<Object[]> findByThemaAndSigungu(Pageable pageable,
                                         @Param("member") Member member,
                                         @Param("themaList") List<Thema> themaList,
                                         @Param("sigunguList") List<Sigungu> sigunguList);

    // 테마, 지역 모두 선택하지 않은 경우
    @Query("SELECT w, " +
                "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END " +
            "FROM WellnessInfo w " +
            "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
            "ORDER BY w.view DESC")
    Page<Object[]> findAllByOrderByViewDesc(Pageable pageable,
                                            @Param("member") Member member);

    // 테마만 선택한 경우
    @Query("SELECT w, " +
                "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END " +
            "FROM WellnessInfo w " +
            "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
            "WHERE w.thema IN :themaList " +
            "ORDER BY w.view DESC")
    Page<Object[]> findByThema(Pageable pageable,
                               @Param("member") Member member,
                               @Param("themaList") List<Thema> themaList);

    // 지역만 선택한 경우
    @Query("SELECT w, " +
                "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END " +
            "FROM WellnessInfo w " +
            "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
            "WHERE w.sigungu IN :sigunguList " +
            "ORDER BY w.view DESC")
    Page<Object[]> findBySigungu(Pageable pageable,
                                 @Param("member") Member member,
                                 @Param("sigunguList") List<Sigungu> sigunguList);

    /**
     * 테마, 지역 조회
     */

    // 테마, 지역 모두 선택한 경우
    @Query(
            "SELECT DISTINCT w.thema, w.sigungu " +
            "FROM WellnessInfo w " +
            "WHERE w.thema IN :themaList AND w.sigungu IN :sigunguList"
    )
    List<Object[]> findDistinctThemaAndSigungu(
            @Param("themaList") List<Thema> themaList,
            @Param("sigunguList") List<Sigungu> sigunguList
    );

    // 테마, 지역 모두 선택하지 않은 경우
    @Query(
            "SELECT DISTINCT w.thema, w.sigungu " +
            "FROM WellnessInfo w"
    )
    List<Object[]> findDistinctAllThemaAndSigungu();

    // 테마만 선택한 경우
    @Query(
            "SELECT DISTINCT w.thema, w.sigungu " +
            "FROM WellnessInfo w " +
            "WHERE w.thema IN :themaList"
    )
    List<Object[]> findDistinctThemaAndSigunguByThema(
            @Param("themaList") List<Thema> themaList
    );

    // 지역만 선택한 경우
    @Query(
            "SELECT DISTINCT w.thema, w.sigungu " +
            "FROM WellnessInfo w " +
            "WHERE w.sigungu IN :sigunguList"
    )
    List<Object[]> findDistinctThemaAndSigunguBySigungu(
            @Param("sigunguList") List<Sigungu> sigunguList
    );


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
    @Query(
            "SELECT w, tpp, " +
                    "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END, " +
                    "CASE WHEN s.id IS NOT NULL THEN TRUE ELSE FALSE END " +
                    "FROM WellnessInfo w " +
                    "LEFT JOIN TripPlanPlace tpp ON tpp.wellnessInfo = w " +
                    "LEFT JOIN TripPlan tp ON tp.id = tpp.tripPlan.id " +
                    "LEFT JOIN Community c ON c.tripPlan.id = tp.id " +
                    "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
                    "LEFT JOIN Support s ON s.wellnessInfo = w AND s.member = :member " +
                    "WHERE c.id = :communityId " +
                    "AND (tpp.review IS NOT NULL OR tpp.rating IS NOT NULL)"
    )
    List<Object[]> findWellnessInfoWithSupportAndLikedByCommunityId(
            @Param("communityId") Long communityId,
            @Param("member") Member member
    );


    Page<WellnessInfo> findAll(Pageable pageable);
}
