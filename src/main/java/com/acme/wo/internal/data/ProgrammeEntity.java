package com.acme.wo.internal.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "programme")
public class ProgrammeEntity {

    @Id @GeneratedValue
    private Long id;
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "programme", orphanRemoval = true)
    private List<WeekEntity> weeks;
}
