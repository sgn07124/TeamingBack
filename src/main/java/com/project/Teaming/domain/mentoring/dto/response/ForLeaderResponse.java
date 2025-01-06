package com.project.Teaming.domain.mentoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForLeaderResponse {

    private List<TeamUserResponse> members;
    private List<TeamParticipationResponse> participations;
}
