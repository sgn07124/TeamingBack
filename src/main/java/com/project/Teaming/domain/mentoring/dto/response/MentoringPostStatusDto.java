package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.PostStatus;
import lombok.Data;

@Data
public class MentoringPostStatusDto {

    private PostStatus status;

    public MentoringPostStatusDto(PostStatus status) {
        this.status = status;
    }
}
