package com.sellerhub.infrastructure.persistence.memory;

import com.sellerhub.domain.model.SimilarityLexicon;
import com.sellerhub.domain.port.SimilarityLexiconProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class InMemorySimilarityLexiconProvider implements SimilarityLexiconProvider {
    private final SimilarityLexicon lexicon = new SimilarityLexicon(Set.of(), Map.of());

    @Override
    public SimilarityLexicon get() {
        return lexicon;
    }
}
