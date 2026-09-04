package com.example.uniqueAproovaResidency.module.work.repository;

import com.example.uniqueAproovaResidency.module.work.entity.WorkTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkTimelineRepository extends JpaRepository<WorkTimeline, String> {
    List<WorkTimeline> findByWorkIdOrderByCreatedAtAsc(String workId);
}
