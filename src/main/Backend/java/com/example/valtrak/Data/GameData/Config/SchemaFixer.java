package com.example.valtrak.Data.GameData.Config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Turns the database's native ENUM columns into plain text columns, before the card data is loaded.
 * <p>
 * Hibernate creates a native ENUM column (H2, MySQL) listing the enum's values at the moment the table is first created,
 * and {@code ddl-auto=update} never widens it. So the day a new value is added to a card enum (a new ability, effect,
 * ammunition...), inserting it fails with "Value not permitted for column". As plain text columns they accept any value;
 * the Java enums stay the only list of what is allowed. This runs on every start and does nothing once the columns are text.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SchemaFixer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaFixer.class);

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        try {
            int fixed = fix();
            if (fixed > 0) log.info("Converted {} enum column(s) to plain text so new enum values can be stored.", fixed);
        } catch (RuntimeException e) {
            log.warn("Could not check the database's enum columns: {}", e.getMessage());
        }
    }

    /** @return how many columns were converted */
    public int fix() {
        String product = jdbc.execute((ConnectionCallback<String>) c -> c.getMetaData().getDatabaseProductName());
        boolean h2 = "H2".equalsIgnoreCase(product);
        boolean mysql = product != null && product.toLowerCase().contains("mysql");
        if (!h2 && !mysql) return 0;

        List<Map<String, Object>> columns = h2
                ? jdbc.queryForList("select table_schema, table_name, column_name, is_nullable from information_schema.columns "
                        + "where data_type = 'ENUM' and table_schema <> 'INFORMATION_SCHEMA' and table_name not like 'HTE\\_%' escape '\\'")
                : jdbc.queryForList("select table_schema, table_name, column_name, is_nullable from information_schema.columns "
                        + "where data_type = 'enum' and table_schema = database()");
        for (Map<String, Object> col : columns) {
            String schema = String.valueOf(col.get("TABLE_SCHEMA"));
            String table = String.valueOf(col.get("TABLE_NAME"));
            String column = String.valueOf(col.get("COLUMN_NAME"));
            boolean nullable = !"NO".equalsIgnoreCase(String.valueOf(col.get("IS_NULLABLE")));
            if (h2) {
                jdbc.execute("alter table \"" + schema + "\".\"" + table + "\" alter column \"" + column + "\" set data type varchar(255)");
            } else {
                jdbc.execute("alter table `" + schema + "`.`" + table + "` modify column `" + column + "` varchar(255)" + (nullable ? "" : " not null"));
            }
            log.info("Converted {}.{} from an enum to text.", table, column);
        }
        return columns.size();
    }
}
