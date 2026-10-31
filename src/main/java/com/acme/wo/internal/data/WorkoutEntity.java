package com.acme.wo.internal.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WorkoutEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private DayOfWeek plannedDay;

    @ManyToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL, optional = true)
    private WeekEntity week;

    @OneToMany(mappedBy = "workout", fetch =  FetchType.EAGER, cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<WorkoutExerciseEntity> exercises;
}
