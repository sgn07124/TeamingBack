package com.project.Teaming.global.result.pagenateResponse;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {

    private List<T> content;       // 실제 데이터
    private int totalPages;        // 총 페이지 수
    private long totalElements;    // 총 데이터 수
    private int size;              // 페이지 크기
    private int number;            // 현재 페이지 번호
    private boolean first;         // 첫 페이지 여부
    private boolean last;          // 마지막 페이지 여부
    private int numberOfElements;  // 현재 페이지 데이터 수
}
