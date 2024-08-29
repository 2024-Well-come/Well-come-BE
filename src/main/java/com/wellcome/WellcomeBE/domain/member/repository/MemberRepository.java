package com.wellcome.WellcomeBE.domain.member.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByKakaoId(Long kakaoId);

}

