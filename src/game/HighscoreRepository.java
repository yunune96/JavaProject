package game;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HighscoreRepository {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private static final DateTimeFormatter DATE_MIN_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public HighscoreRepository() {
        String host = envOrDefault("MYSQL_HOST", "127.0.0.1");
        String port = envOrDefault("MYSQL_PORT", "3306");
        String database = envOrDefault("MYSQL_DB", "game");
        this.dbUser = envOrDefault("MYSQL_USER", "root");
        this.dbPassword = envOrDefault("MYSQL_PASSWORD", "");
        this.dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database +
                     "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8";
    }

    public void init() {
        ensureDriverLoaded();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS highscores (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "nickname VARCHAR(32) NOT NULL, " +
                "elapsed_ms BIGINT NOT NULL, " +
                "cleared_at DATETIME NOT NULL, " +
                "seed_id INT NOT NULL, " +
                "PRIMARY KEY (id))"
            );
            try {
                st.executeUpdate("CREATE INDEX idx_highscores_elapsed ON highscores(elapsed_ms)");
            } catch (SQLException e) {
                String msg = e.getMessage();
                if (msg == null || !(msg.toLowerCase().contains("duplicate") || msg.toLowerCase().contains("exists"))) {
                    throw e;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB 초기화 실패: " + e.getMessage(), e);
        }
    }

    public void insertRecord(String nickname, long elapsedMillis, LocalDateTime clearedAt, int seedId) {
        ensureDriverLoaded();
        String sql = "INSERT INTO highscores(nickname, elapsed_ms, cleared_at, seed_id) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nickname);
            ps.setLong(2, elapsedMillis);
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(clearedAt));
            ps.setInt(4, seedId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("기록 저장 실패: " + e.getMessage(), e);
        }
    }

    public List<String> listTop(int limit) {
        ensureDriverLoaded();
        String sql = "SELECT nickname, elapsed_ms, cleared_at FROM highscores ORDER BY elapsed_ms ASC LIMIT ?";
        List<String> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 0;
                int shown = 0;
                long prevMs = -1;
                while (rs.next()) {
                    String nick = rs.getString(1);
                    long ms = rs.getLong(2);
                    java.sql.Timestamp ts = rs.getTimestamp(3);
                    String when = ts != null ? DATE_MIN_FMT.format(ts.toLocalDateTime()) : "";
                    if (ms != prevMs) {
                        rank = shown + 1;
                        prevMs = ms;
                    }
                    shown++;
                    rows.add(String.format("%s%2d) %s  %s  %s", medalForRank(rank), rank, nick, formatElapsed(ms), when));
                }
            }
        } catch (SQLException e) {
            rows.add("하이스코어 조회 실패: " + e.getMessage());
        }
        return rows;
    }

    public static String medalForRank(int rank) {
        switch (rank) {
            case 1: return "🥇 ";
            case 2: return "🥈 ";
            case 3: return "🥉 ";
            default: return "";
        }
    }

    public static String formatElapsed(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }

    public int computeRank(long elapsedMillis) {
        ensureDriverLoaded();
        String sql = "SELECT COUNT(*) FROM highscores WHERE elapsed_ms < ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, elapsedMillis);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long numBetter = rs.getLong(1);
                    long rank = numBetter + 1;
                    return (rank > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) rank;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("랭킹 계산 실패: " + e.getMessage(), e);
        }
        return 1;
    }

    private static volatile boolean driverLoaded = false;
    private static synchronized void ensureDriverLoaded() {
        if (driverLoaded) return;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC 드라이버를 찾을 수 없습니다. 클래스패스를 확인하세요.", e);
        }
    }

    private static String envOrDefault(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) return def;
        return v;
    }
}


