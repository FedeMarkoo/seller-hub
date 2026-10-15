package com.sellerhub.domain.model;

import java.util.Map;
import java.util.Set;

public record SimilarityLexicon(Set<String> stopWords, Map<String, String> synonyms) {
}
