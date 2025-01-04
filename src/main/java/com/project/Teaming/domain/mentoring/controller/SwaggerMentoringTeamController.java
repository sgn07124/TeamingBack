package com.project.Teaming.domain.mentoring.controller;


import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.TeamResponseDto;
import com.project.Teaming.global.result.ResultDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "MentoringTeam", description = "멘토링 팀 관련 API")
public interface SwaggerMentoringTeamController {

    @Operation(summary = "멘토링 팀 저장", description = "멘토링 팀을 생성하고 저장할 수 있으며, 멘토링 팀을 생성한 유저는 팀의 리더가 된다,  " +
            "status는 RECRUITING(모집중), WORKING(진행중), COMPLETE(완료) / role은 MENTOR(멘토), MENTEE(멘티)로 요청 주시면 됩니다,  " +
            "저장된 팀의 id 반환")
    public ResultDetailResponse<String> saveMentoringTeam(@RequestBody @Valid RqTeamDto dto);

    @Operation(summary = "멘토링 팀 수정", description = "멘토링 팀을 수정할 수 있다.")
    public ResultDetailResponse<TeamResponseDto> updateMentoringTeam(@PathVariable Long team_id,
                                                                     @RequestBody @Valid RqTeamDto dto);

    @Operation(summary = "멘토링 팀 조회", description = "특정 멘토링 팀을 조회할 수 있다. " +
            "멘토링 팀페이지 조회용, authority가 LEADER면 팀장용페이지,CREW이면 팀원용페이지, NoAuth면 일반사용자용 페이지 띄워주세요 ")
    public ResultDetailResponse<TeamResponseDto> findMentoringTeam(@PathVariable Long team_id);

    @Operation(summary = "멘토링 팀 삭제", description = "특정 멘토링 팀을 삭제할 수 있다.")
    public ResultDetailResponse<Void> deleteMentoringTeam(@PathVariable Long team_id);
}
