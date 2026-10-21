package nelbiasoftware.clinic_bot.core.db;

import nelbiasoftware.clinic_bot.core.Entity;
import org.apache.ibatis.annotations.*;

import java.util.Map;

@Mapper
public interface PatientMapper {

    String PATIENT = "patient";

    @Select("SELECT * FROM patient WHERE user_id = #{userId}")
    @Results(id = PATIENT, value = {
            @Result(property = Entity.Patient.USER_ID, column = "user_id", typeHandler = org.apache.ibatis.type.LongTypeHandler.class),
            @Result(property = Entity.Patient.PHONE_NUMBER, column = "phone_number", typeHandler = org.apache.ibatis.type.LongTypeHandler.class),
            @Result(property = Entity.Patient.FIRST_NAME, column = "first_name", typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    })
    Map<String, Object> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM patient WHERE phone_number = #{phoneNumber}")
    @ResultMap(PATIENT)
    Map<String, Object> selectByPhoneNumber(@Param("phoneNumber") Long phoneNumber);

    @Insert("""
            INSERT INTO patient (user_id, phone_number, first_name) VALUES (
                        #{patient.userId, typeHandler = org.apache.ibatis.type.LongTypeHandler},
                        #{patient.phoneNumber, typeHandler = org.apache.ibatis.type.LongTypeHandler},
                        #{patient.firstName, typeHandler = org.apache.ibatis.type.StringTypeHandler}
                        )
            """)
    void insertPatient(@Param("patient") Map<String, Object> patient);

    default boolean existsByPhoneNumber(Long phoneNumber) {
        return selectByPhoneNumber(phoneNumber) != null;
    }
}
