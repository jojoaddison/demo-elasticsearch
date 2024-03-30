package demo.jojoaddison.domain;

import static demo.jojoaddison.domain.StatTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import demo.jojoaddison.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class StatTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Stat.class);
        Stat stat1 = getStatSample1();
        Stat stat2 = new Stat();
        assertThat(stat1).isNotEqualTo(stat2);

        stat2.setId(stat1.getId());
        assertThat(stat1).isEqualTo(stat2);

        stat2 = getStatSample2();
        assertThat(stat1).isNotEqualTo(stat2);
    }
}
