package net.burningtnt.hmclfetcher.api.pagination;

import java.io.IOException;
import java.util.List;

public interface PaginationAccessor<R> {
    List<R> execute(PaginationConfig pagination) throws IOException;
}
