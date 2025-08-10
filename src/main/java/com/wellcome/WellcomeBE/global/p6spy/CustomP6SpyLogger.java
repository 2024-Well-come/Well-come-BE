package com.wellcome.WellcomeBE.global.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

@Configuration
public class CustomP6SpyLogger implements MessageFormattingStrategy {
    private static final String NEW_LINE = "\n";
    private static final String TAB = "\t";
    private static final String CREATE = "create";
    private static final String ALTER = "alter";
    private static final String DROP = "drop";
    private static final String COMMENT = "comment";

    public static final ThreadLocal<List<QueryInfo>> queryLogHolder = ThreadLocal.withInitial(ArrayList::new);
    public static final ThreadLocal<String> endpointHolder = new ThreadLocal<>();

    public static List<QueryInfo> getAndClearQueryInfo() {
        List<QueryInfo> copy = new ArrayList<>(queryLogHolder.get());
        queryLogHolder.remove();
        endpointHolder.remove();
        return copy;
    }

    public static void setEndpoint(String endpoint) {
        endpointHolder.set(endpoint);
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
                                String prepared, String sql, String url) {
        //System.out.println("✅ formatMessage called");

        if (sql == null || sql.trim().isEmpty()) return "";

        // ThreadLocal에 저장
        QueryInfo queryInfo = new QueryInfo(sql.trim(), elapsed, endpointHolder.get());
        queryLogHolder.get().add(queryInfo);

        // 로그 포맷팅
        String formattedSql = formatSql(sql, category);
        return formattedSql + additionalMessages(elapsed);
    }

    private String formatSql(String sql, String category) {
        String lowerSql = sql.trim().toLowerCase(Locale.ROOT);
        Formatter formatter = isDDL(lowerSql, category) ? FormatStyle.DDL.getFormatter() : FormatStyle.BASIC.getFormatter();
        String prefix = isDDL(lowerSql, category) ? "Execute DDL" : "Execute DML";
        return NEW_LINE + prefix + " :" + NEW_LINE + formatter.format(sql);
    }

    private boolean isDDL(String lowerSql, String category) {
        return "statement".equalsIgnoreCase(category) && (
                lowerSql.startsWith(CREATE) ||
                        lowerSql.startsWith(ALTER) ||
                        lowerSql.startsWith(DROP) ||
                        lowerSql.startsWith(COMMENT)
        );
    }

    private String additionalMessages(long elapsed) {
        return NEW_LINE +
                NEW_LINE +
                String.format("Execution Time: %d ms", elapsed) +
                NEW_LINE +
                "----------------------------------------------------------------------------------------------------";
    }

    public record QueryInfo(String sql, long elapsedTime, String endpoint) {}
}