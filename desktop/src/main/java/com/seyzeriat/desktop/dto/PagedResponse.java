package com.seyzeriat.desktop.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a paginated response from the Checkpoint backend.
 *
 * @param <T> the type of content in the page
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResponse<T> {
    private List<T> content;
    private PageMetadata metadata;

    public PagedResponse() {}

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public PageMetadata getMetadata() { return metadata; }
    public void setMetadata(PageMetadata metadata) { this.metadata = metadata; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PageMetadata() {}

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
