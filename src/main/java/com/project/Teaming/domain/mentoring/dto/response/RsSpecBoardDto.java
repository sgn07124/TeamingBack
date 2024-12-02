package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RsSpecBoardDto {

    private Long id;
    private String title;
    private String mentoringTeamName;
    private String deadLine;
    private String startDate;
    private String endDate;
    private MentoringRole role;
    private int mentoringCnt;
    private String link;
    private List<String> category;
    private String contents;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private MentoringAuthority authority;


    @Builder
    public RsSpecBoardDto(Long id, String title, String mentoringTeamName, String deadLine, String startDate, String endDate, MentoringRole role, int mentoringCnt, String link, List<String> category, String contents, LocalDateTime createdDate, LocalDateTime modifiedDate, MentoringAuthority authority) {
        this.id = id;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.deadLine = deadLine;
        this.startDate = startDate;
        this.endDate = endDate;
        this.role = role;
        this.mentoringCnt = mentoringCnt;
        this.link = link;
        this.category = category;
        this.contents = contents;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.authority = authority;
    }
}
