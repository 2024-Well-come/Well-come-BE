package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long> {

    // [목록 조회] - 로그인한 경우
    @Query(
            "SELECT w, " +
                "CASE WHEN l.id IS NOT NULL THEN TRUE ELSE FALSE END " +
            "FROM WellnessInfo w " +
            "LEFT JOIN Liked l ON l.wellnessInfo = w AND l.member = :member " +
            "WHERE w.thema IN :themaList AND w.sigungu IN :sigunguList " +
            "ORDER BY w.view DESC"
    )
    Page<Object[]> findByThemaAndSigungu(PageRequest pageRequest,
                                             @Param("member") Member member,
                                             @Param("themaList") List<Thema> themaList,
                                             @Param("sigunguList") List<Sigungu> sigunguList);

    // [목록 조회] - 로그인 하지 않은 경우
    @Query(
            "SELECT w " +
            "FROM WellnessInfo w " +
            //"WHERE w.thema IN :themaList AND w.sigungu IN :sigunguList"
            "WHERE (:themaList IS NULL OR w.thema IN :themaList) " +
            "AND (:sigunguList IS NULL OR w.sigungu IN :sigunguList) " +
            "ORDER BY w.view DESC"
    )
    Page<WellnessInfo> findByThemaAndSigunguWithoutLikes(PageRequest pageRequest,
                                         @Param("themaList") List<Thema> themaList,
                                         @Param("sigunguList") List<Sigungu> sigunguList);



}
