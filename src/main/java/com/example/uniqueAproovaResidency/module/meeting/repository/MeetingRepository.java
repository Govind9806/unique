package com.example.uniqueAproovaResidency.module.meeting.repository;

import com.example.uniqueAproovaResidency.module.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {
    List<Meeting> findAllByOrderByMeetingDateDesc();
}
