package net.burningtnt.hmclfetcher.api.structure.pagination;

import java.io.IOException;
import java.util.Iterator;

public interface GitHubPagination<R> {
    boolean hasNext() throws IOException;

    R next() throws IOException;

    static <R> GitHubPagination<R> of(GitHubPaginationAccessor<R> runnable) {
        return new GitHubPagination<R>() {
            private static final class EmptyIterator<T> implements Iterator<T> {
                private static final EmptyIterator<?> NOT_INITIALIZED = new EmptyIterator<>();
                private static final EmptyIterator<?> FINISHED = new EmptyIterator<>();

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new AssertionError();
                }
            }

            private final GitHubPaginationConfig config = new GitHubPaginationConfig();

            private Iterator<R> cache;

            {
                setCache(EmptyIterator.NOT_INITIALIZED);
            }

            @Override
            public boolean hasNext() throws IOException {
                fillCache();

                return cache.hasNext();
            }

            @Override
            public R next() throws IOException {
                fillCache();

                return cache.next();
            }

            private void fillCache() throws IOException {
                if (cache == EmptyIterator.FINISHED || cache.hasNext()) {
                    return;
                }

                cache = runnable.execute(config).iterator();
                config.next();

                if (!cache.hasNext()) {
                    setCache(EmptyIterator.FINISHED);
                }
            }

            @SuppressWarnings("unchecked")
            private void setCache(EmptyIterator<?> value) {
                this.cache = (Iterator<R>) value;
            }
        };
    }
}
