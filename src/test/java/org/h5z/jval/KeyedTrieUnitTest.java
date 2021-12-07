package org.h5z.jval;

import org.h5z.jval.Core.Validator;
import org.h5z.jval.KeyedTrie.KeyedValidator;
import org.h5z.jval.TreeModule.Trie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.h5z.jval.TreeModule.*;
import static org.h5z.jval.KeyedTrie.keyed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.organicdesign.fp.StaticImports.*;


public class KeyedTrieUnitTest {

    @Nested
    @DisplayName("keyed")
    class Keyed {
        
        @Test
        @DisplayName("Returns a keyed validator") void t0() {
            Validator<String, String> containsA = 
                s -> s.contains("a") 
                    ? Core.valid(s) 
                    : Core.invalid("Does not contains a");
            
            KeyedValidator<String, String> keyedContainsA = keyed("x", containsA);
            Trie<String> validated = keyedContainsA.validate("");

            assertThat(validated)
                .isEqualTo(
                    trie(
                        vec(),
                        map(
                            tup("x", trie(
                                vec("Does not contains a"),
                                map()
                            ))
                        )
                    )
                );
        }

    }

}
