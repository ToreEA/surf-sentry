package no.torand.surfsentry.service;

import no.torand.surfsentry.domain.AccessRule;
import no.torand.surfsentry.domain.Config;
import no.torand.surfsentry.domain.TimeInterval;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.time.DayOfWeek.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ConfigReaderTest {

    @Test
    public void shouldReadValidConfigFile() throws IOException {

        Config config = getConfig("/valid.conf");

        List<AccessRule> allowed = config.getAllowedAccessByPerson().get("Sigrun");
        assertThat(allowed, hasSize(1));
        assertThat(allowed.get(0).getHost(), is("*"));
        assertThat(allowed.get(0).getDaysOfWeek(), contains(MONDAY, TUESDAY, WEDNESDAY, FRIDAY, SATURDAY, SUNDAY));
        assertThat(allowed.get(0).getTimeInterval(), equalTo(new TimeInterval(9,22)));

        List<AccessRule> allowed2 = config.getAllowedAccessByPerson().get("Tore");
        assertThat(allowed2, hasSize(1));
        assertThat(allowed2.get(0).getHost(), is("*"));
        assertThat(allowed2.get(0).getDaysOfWeek(), contains(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY));
        assertThat(allowed2.get(0).getTimeInterval(), equalTo(new TimeInterval(0,23)));
    }

    private Config getConfig(String configName) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(configName)) {
            ConfigReader reader = new ConfigReader();
            return reader.read(stream);
        }
    }
}