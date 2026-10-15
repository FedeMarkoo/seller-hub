package com.sellerhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupIndex {
    private String id;
    private String sellerId;
    private String representativeText;
    @Builder.Default
    private List<String> questionIds = new ArrayList<>();
}
