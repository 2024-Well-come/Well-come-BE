package com.wellcome.WellcomeBE.domain.like.repository;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikedRepository extends JpaRepository<Liked,Long> {
    Boolean findLikedByWellnessInfoAndMember(WellnessInfo wellnessInfo, Member member);

    Optional<Liked> findByMemberIdAndWellnessInfoId(Long memberId, Long wellnessInfoId);

    @Query("SELECT new com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo(w.parentId, w.id, w.thumbnailUrl, w.title, w.thema, w.address) " +
            "FROM Liked l " +
            "JOIN l.wellnessInfo w " +
            "WHERE l.member = :member AND w.thema IN :thema")
    List<LikeWellnessInfoVo> findByMemberIdAndThemaIn(@Param("member") Member member, @Param("thema") List<Thema> thema);


    @Query("SELECT w.thema from Liked l join l.wellnessInfo w where l.member = :member ")
    List<Thema> findLikedThemaByMember(@Param("member") Member member);


}
