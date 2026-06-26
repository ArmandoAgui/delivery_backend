package sv.edu.uca.delivery.backend.util;

import org.springframework.data.domain.Pageable;

import java.util.List;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static <T> PageResponse<T> toPage(List<T> items, Pageable pageable) {
        int start = Math.toIntExact(Math.min(pageable.getOffset(), items.size()));
        int end = Math.min(start + pageable.getPageSize(), items.size());
        int totalPages = pageable.getPageSize() == 0
                ? 0
                : (int) Math.ceil((double) items.size() / pageable.getPageSize());
        int pageNumber = pageable.getPageNumber();
        return new PageResponse<>(
                items.subList(start, end),
                pageNumber,
                pageable.getPageSize(),
                items.size(),
                totalPages,
                pageNumber == 0,
                totalPages == 0 || pageNumber >= totalPages - 1
        );
    }
}
