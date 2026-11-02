package com.acme.wo.internal.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "session_exercise")
public class SessionExerciseEntity {
    @Id
    @GeneratedValue
    private Long id;

    private int sequence;
    private String name;
    private int sets;
    private int reps;
    private int recoveryInSecs;

    @ManyToOne(optional = false)
    private SessionEntity session;
}
