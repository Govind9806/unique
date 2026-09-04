package com.example.uniqueAproovaResidency.module.work.repository;

import com.example.uniqueAproovaResidency.module.work.entity.WorkVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkVoteRepository extends JpaRepository<WorkVote, String> {
    List<WorkVote> findByWorkId(String workId);
    Optional<WorkVote> findByWorkIdAndFlatNumber(String workId, String flatNumber);
    boolean existsByWorkIdAndFlatNumber(String workId, String flatNumber);
}
