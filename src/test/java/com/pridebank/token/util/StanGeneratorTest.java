package com.pridebank.token.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StanGeneratorTest {

    @Test
    void globalSequential() {
        StanGenerator g = new StanGenerator();
        String s1 = g.generateStan();
        String s2 = g.generateStan();
        assertThat(Integer.parseInt(s2)).isEqualTo(Integer.parseInt(s1) + 1);
    }

    @Test
    void terminalIndependent() {
        StanGenerator g = new StanGenerator();
        String a1 = g.generateStanForTerminal("T1");
        String b1 = g.generateStanForTerminal("T2");
        String a2 = g.generateStanForTerminal("T1");
        assertThat(Integer.parseInt(a2)).isEqualTo(Integer.parseInt(a1) + 1);
        assertThat(Integer.parseInt(b1)).isEqualTo(1);
    }
}