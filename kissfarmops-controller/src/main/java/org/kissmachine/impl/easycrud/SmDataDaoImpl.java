package org.kissmachine.impl.easycrud;

import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.easycrud.JdbcJsonFieldSerializer;
import org.kissmachine.api.easycrud.SmDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.summerb.approaches.jdbccrud.api.ParameterSourceBuilder;
import org.summerb.approaches.jdbccrud.impl.EasyCrudDaoMySqlImpl;
import org.summerb.approaches.jdbccrud.impl.ParameterSourceBuilderBeanPropImpl;

public class SmDataDaoImpl extends EasyCrudDaoMySqlImpl<String, SmData> implements SmDataDao {
	@Autowired
	JdbcJsonFieldSerializer jdbcJsonFieldSerializer;

	public SmDataDaoImpl() {
		setDtoClass(SmData.class);
		setRowMapper(rowMapper);
		setParameterSourceBuilder(parameterSourceBuilder);
		setTableName("sm_data");
	}

	private RowMapper<SmData> rowMapper = new BeanPropertyRowMapper<SmData>(SmData.class) {
		@Override
		public SmData mapRow(java.sql.ResultSet rs, int rowNumber) throws java.sql.SQLException {
			SmData ret = super.mapRow(rs, rowNumber);

			ret.setVars(jdbcJsonFieldSerializer.deserialize(ret.getVars()));

			return ret;
		}
	};

	private ParameterSourceBuilder<SmData> parameterSourceBuilder = new ParameterSourceBuilderBeanPropImpl<SmData>() {
		@Override
		public SqlParameterSource buildParameterSource(SmData dto) {
			return new BeanPropertySqlParameterSource(dto) {
				@Override
				public int getSqlType(String paramName) {
					if (SmData.FN_VARS.equals(paramName)) {
						return StatementCreatorUtils.javaTypeToSqlParameterType(String.class);
					}
					return super.getSqlType(paramName);
				};

				@Override
				public Object getValue(String paramName) throws IllegalArgumentException {
					if (SmData.FN_VARS.equals(paramName)) {
						return jdbcJsonFieldSerializer.serializeObject(dto.getVars());
					}

					return super.getValue(paramName);
				}
			};
		}
	};
}
