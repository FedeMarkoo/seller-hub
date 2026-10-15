package com.sellerhub.application.service;

import com.sellerhub.domain.model.SimilarityLexicon;
import com.sellerhub.domain.port.SimilarityLexiconProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionSimilarityService {
    private final SimilarityLexiconProvider lexiconProvider;

    public boolean similar(String left, String right) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) return left.equalsIgnoreCase(right);

        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);
        Set<String> union = new HashSet<>(leftTokens);
        union.addAll(rightTokens);
        return (double) intersection.size() / union.size() >= 0.5
                || (double) intersection.size() / Math.min(leftTokens.size(), rightTokens.size()) >= 0.8;
    }

    private Set<String> tokens(String text) {
        SimilarityLexicon lexicon = lexiconProvider.get();
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ");
        Set<String> result = new HashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (token.length() > 2 && !lexicon.stopWords().contains(token)) {
                result.add(lexicon.synonyms().getOrDefault(token, token));
            }
        }
        return result;
    }
}
