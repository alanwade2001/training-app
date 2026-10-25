package com.acme.wo.model;

import java.util.List;

public record Programme(
        Long id,
        String name,
        List<Week> weeks
) {
}
