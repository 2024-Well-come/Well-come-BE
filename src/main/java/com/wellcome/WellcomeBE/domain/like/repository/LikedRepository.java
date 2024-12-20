package com.wellcome.WellcomeBE.domain.like.repository;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikedRepository extends JpaRepository<Liked,Long> {
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN TRUE ELSE FALSE END FROM Liked l WHERE l.wellnessInfo = :wellnessInfo AND l.member = :member")
    boolean existsByWellnessInfoAndMember(@Param("wellnessInfo") WellnessInfo wellnessInfo, @Param("member") Member member);

    Optional<Liked> findByMemberIdAndWellnessInfoId(Long memberId, Long wellnessInfoId);

    @Query("SELECT new com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo(w.parentId, w.id, w.thumbnailUrl, w.title, w.thema, w.address) " +
            "FROM Liked l " +
            "JOIN l.wellnessInfo w " +
            "WHERE l.member = :member AND w.thema IN :thema")
    Page<LikeWellnessInfoVo> findByMemberIdAndThemaIn(@Param("member") Member member, @Param("thema") List<Thema> thema,Pageable pageable);


    @Query("SELECT DISTINCT w.thema from Liked l join l.wellnessInfo w where l.member = :member ")
    List<Thema> findLikedThemaByMember(@Param("member") Member member);

    @Query(
            "SELECT w.thema " +
            "FROM Liked l " +
            "JOIN l.wellnessInfo w " +
            "WHERE l.member = :member " +
            "GROUP BY w.thema " +
            "ORDER BY COUNT(l.id) DESC" //좋아요 수에 따라 정렬
    )
    List<Thema> findThemaByMemberOrderByLikedCount(@Param("member") Member member);


}
