package com.example.valtrak.Data.GameData.Config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A database created before a new enum value existed must accept that value after the fix. */
class SchemaFixerTest {

    @Test
    void anOldEnumColumnAcceptsNewValuesAfterTheFixAndItIsAWarmRestartSafe() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:schemafixer;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("create table special_item_cards (id bigint primary key, effect enum('ERA_PROTECTION', 'DRAW_CARDS'), note varchar(20))");
        jdbc.execute("insert into special_item_cards (id, effect) values (1, 'DRAW_CARDS')");
        assertThatThrownBy(() -> jdbc.execute("insert into special_item_cards (id, effect) values (2, 'AIRDROP')"))
                .hasMessageContaining("Value not permitted");

        SchemaFixer fixer = new SchemaFixer(jdbc);
        assertThat(fixer.fix()).isEqualTo(1);

        jdbc.execute("insert into special_item_cards (id, effect) values (2, 'AIRDROP')");
        assertThat(jdbc.queryForObject("select effect from special_item_cards where id = 1", String.class)).isEqualTo("DRAW_CARDS");
        assertThat(jdbc.queryForObject("select effect from special_item_cards where id = 2", String.class)).isEqualTo("AIRDROP");
        assertThat(fixer.fix()).as("nothing left to convert").isZero();
    }
}
