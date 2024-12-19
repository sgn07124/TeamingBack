package com.project.Teaming.global.result.pagenateResponse;

import java.util.List;
import lombok.Data;

@Data
public class PaginatedCursorResponse<T> {
    private List<T> content;    // 데이터 리스트
    private Long nextCursor;   // 다음 커서 값
    private int pageSize;      // 페이지 크기
    private boolean isLast;    // 마지막 페이지 여부

    public PaginatedCursorResponse(List<T> content, Long nextCursor, int pageSize, boolean isLast) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.pageSize = pageSize;
        this.isLast = isLast;
    }
}
