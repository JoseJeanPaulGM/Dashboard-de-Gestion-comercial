package com.gestioncomercial.api.shared.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PaginationTest {

    private static final Map<String, String> FIELDS = Map.of("name", "name", "createdAt", "createdAt");

    @Test
    void parsesAnAllowedSort() {
        PageCriteria result = Pagination.parse(1, 25, "createdAt,desc", FIELDS);

        assertThat(result).isEqualTo(new PageCriteria(1, 25, "createdAt", SortDirection.DESC));
    }

    @Test
    void rejectsInvalidPageAndSize() {
        assertThatThrownBy(() -> Pagination.parse(-1, 20, "name,asc", FIELDS))
                .isInstanceOf(InvalidPaginationException.class);
        assertThatThrownBy(() -> Pagination.parse(0, 101, "name,asc", FIELDS))
                .isInstanceOf(InvalidPaginationException.class);
    }

    @Test
    void rejectsUnknownFieldAndDirection() {
        assertThatThrownBy(() -> Pagination.parse(0, 20, "email,asc", FIELDS))
                .isInstanceOf(InvalidSortException.class);
        assertThatThrownBy(() -> Pagination.parse(0, 20, "name,sideways", FIELDS))
                .isInstanceOf(InvalidSortException.class);
    }
}
