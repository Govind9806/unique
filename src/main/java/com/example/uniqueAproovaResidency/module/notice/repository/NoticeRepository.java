package com.example.uniqueAproovaResidency.module.notice.repository;

import com.example.uniqueAproovaResidency.module.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, String> {
    List<Notice> findAllByOrderByIsPinnedDescCreatedAtDesc();
}
