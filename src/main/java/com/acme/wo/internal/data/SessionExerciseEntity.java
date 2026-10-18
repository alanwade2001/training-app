package com.acme.wo.internal.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SessionExerciseEntity {
    @Id
    @GeneratedValue
    private Long id;

    private int sequence;
    private String name;
    private int sets;
    private int reps;
    private int recoveryInSecs;

    @ManyToOne
    private SessionEntity session;
}
