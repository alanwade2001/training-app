package com.acme.wo.internal.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WeekEntity {
    @Id
    @GeneratedValue
    private Long id;

    private int number;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ProgrammeEntity programme;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "week")
    private List<WorkoutEntity> workouts;
}
