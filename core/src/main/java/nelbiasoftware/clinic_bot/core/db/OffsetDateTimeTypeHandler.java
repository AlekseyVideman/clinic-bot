package nelbiasoftware.clinic_bot.core.db;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER})
public class OffsetDateTimeTypeHandler implements TypeHandler<OffsetDateTime> {

    private static final ZoneId ZONE = ZoneOffset.UTC;
    private static final ZoneId KRSK_ZONE = ZoneId.of("Asia/Krasnoyarsk");

    @Override
    public void setParameter(PreparedStatement ps, int i, OffsetDateTime parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, java.sql.Types.BIGINT);
        } else {
            ps.setLong(i, parameter.atZoneSameInstant(ZONE).toInstant().getEpochSecond());
        }
    }

    @Override
    public OffsetDateTime getResult(ResultSet rs, String columnName) throws SQLException {
        long epochSecond = rs.getLong(columnName);
        return rs.wasNull() ? null : OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), KRSK_ZONE);
    }

    @Override
    public OffsetDateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
        long epochSecond = rs.getLong(columnIndex);
        return rs.wasNull() ? null : OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), KRSK_ZONE);
    }

    @Override
    public OffsetDateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
        long epochSecond = cs.getLong(columnIndex);
        return cs.wasNull() ? null : OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), KRSK_ZONE);
    }
}
