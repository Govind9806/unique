package com.example.uniqueAproovaResidency.module.work.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_votes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_work_vote_flat", columnNames = {"work_id", "flat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkVote extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Column(name = "flat_number", nullable = false, length = 50)
    private String flatNumber;

    @Column(nullable = false, length = 20)
    private String vote; // APPROVE or REJECT

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "voted_by_user_id")
    private User votedBy;

    @Column(name = "voted_at", nullable = false)
    @Builder.Default
    private LocalDateTime votedAt = LocalDateTime.now();
}
