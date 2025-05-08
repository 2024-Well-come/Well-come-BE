package com.wellcome.WellcomeBE.domain.community.repository;

import com.wellcome.WellcomeBE.domain.community.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityRepositoryCustom {
    Page<Community> findByPostTypeOrderBySupportCount(Pageable pageable, Community.PostType postType);
}

