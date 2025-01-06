package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.PostStatus;
import lombok.Data;

@Data
public class MentoringPostStatusResponse {

    private PostStatus status;

    public MentoringPostStatusResponse(PostStatus status) {
        this.status = status;
    }
}
