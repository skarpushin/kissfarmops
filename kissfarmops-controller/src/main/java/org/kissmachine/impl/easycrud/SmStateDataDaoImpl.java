package org.kissmachine.impl.easycrud;

import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.easycrud.JdbcJsonFieldSerializer;
import org.kissmachine.api.easycrud.SmStateDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.summerb.approaches.jdbccrud.api.ParameterSourceBuilder;
import org.summerb.approaches.jdbccrud.impl.EasyCrudDaoMySqlImpl;
import org.summerb.approaches.jdbccrud.impl.ParameterSourceBuilderBeanPropImpl;

public class SmStateDataDaoImpl extends EasyCrudDaoMySqlImpl<String, SmStateData> implements SmStateDataDao {
	@Autowired
	JdbcJsonFieldSerializer jdbcJsonFieldSerializer;

	public SmStateDataDaoImpl() {
		setDtoClass(SmStateData.class);
		setRowMapper(rowMapper);
		setParameterSourceBuilder(parameterSourceBuilder);
		setTableName("sm_state_data");
	}

	private RowMapper<SmStateData> rowMapper = new BeanPropertyRowMapper<SmStateData>(SmStateData.class) {
		@Override
		public SmStateData mapRow(java.sql.ResultSet rs, int rowNumber) throws java.sql.SQLException {
			SmStateData ret = super.mapRow(rs, rowNumber);

			ret.setParams(jdbcJsonFieldSerializer.deserialize(ret.getParams()));
			ret.setState(jdbcJsonFieldSerializer.deserialize(ret.getState()));
			ret.setResult(jdbcJsonFieldSerializer.deserialize(ret.getResult()));

			return ret;
		}
	};

	private ParameterSourceBuilder<SmStateData> parameterSourceBuilder = new ParameterSourceBuilderBeanPropImpl<SmStateData>() {
		@Override
		public SqlParameterSource buildParameterSource(SmStateData dto) {
			return new BeanPropertySqlParameterSource(dto) {
				@Override
				public int getSqlType(String paramName) {
					if (SmStateData.FN_PARAMS.equals(paramName) || SmStateData.FN_STATE.equals(paramName)
							|| SmStateData.FN_RESULT.equals(paramName)) {
						return StatementCreatorUtils.javaTypeToSqlParameterType(String.class);
					}
					return super.getSqlType(paramName);
				};

				@Override
				public Object getValue(String paramName) throws IllegalArgumentException {
					if (SmStateData.FN_PARAMS.equals(paramName)) {
						return jdbcJsonFieldSerializer.serializeObject(dto.getParams());
					} else if (SmStateData.FN_STATE.equals(paramName)) {
						return jdbcJsonFieldSerializer.serializeObject(dto.getState());
					} else if (SmStateData.FN_RESULT.equals(paramName)) {
						return jdbcJsonFieldSerializer.serializeObject(dto.getResult());
					}

					return super.getValue(paramName);
				}
			};
		}
	};
}
