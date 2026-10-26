package com.acme.wo.internal.data;

import com.acme.wo.model.Week;
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
public class ProgrammeEntity {

    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "programme", orphanRemoval = true)
    private List<WeekEntity> weeks;
}
