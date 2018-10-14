package org.kissfarm.controller.config;

import java.util.Arrays;

import javax.sql.DataSource;

import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.FarmConfigPackager;
import org.kissfarm.controller.config.api.GitAbstraction;
import org.kissfarm.controller.config.dto.GitConfig;
import org.kissfarm.controller.config.impl.FarmConfigFolderReaderImpl;
import org.kissfarm.controller.config.impl.FarmConfigPackagerImpl;
import org.kissfarm.controller.config.impl.GitAbstractionImpl;
import org.kissfarm.controller.config.impl.GitConfigValidationStrategyImpl;
import org.kissfarm.controller.config.smachine.dtos.FarmConfigMachineVariables;
import org.kissfarm.controller.config.smachine.states.ChangingRepoStateImpl;
import org.kissfarm.controller.config.smachine.states.ClonningConfigStateImpl;
import org.kissfarm.controller.config.smachine.states.CommittingConfigChangeStateImpl;
import org.kissfarm.controller.config.smachine.states.DeliveringConfigToNodesStateImpl;
import org.kissfarm.controller.config.smachine.states.NoConfigStateImpl;
import org.kissfarm.controller.config.smachine.states.PullingUpdatesStateImpl;
import org.kissfarm.controller.config.smachine.states.ReadyStateImpl;
import org.kissfarm.controller.config.smachine.states.TearDownMessageFlowStateImpl;
import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.easycrud.JdbcJsonFieldSerializer;
import org.kissmachine.api.easycrud.SmDataAuthStrategy;
import org.kissmachine.api.easycrud.SmDataDao;
import org.kissmachine.api.easycrud.SmDataService;
import org.kissmachine.api.easycrud.SmStateDataAuthStrategy;
import org.kissmachine.api.easycrud.SmStateDataDao;
import org.kissmachine.api.easycrud.SmStateDataService;
import org.kissmachine.api.machine.StateMachine;
import org.kissmachine.api.state.SmState;
import org.kissmachine.impl.SmStateDataTrimWireTap;
import org.kissmachine.impl.StateMachineImpl;
import org.kissmachine.impl.easycrud.JdbcJsonFieldSerializerImpl;
import org.kissmachine.impl.easycrud.SmDataAuthStrategyImpl;
import org.kissmachine.impl.easycrud.SmDataDaoImpl;
import org.kissmachine.impl.easycrud.SmDataServiceImpl;
import org.kissmachine.impl.easycrud.SmStateDataAuthStrategyImpl;
import org.kissmachine.impl.easycrud.SmStateDataDaoImpl;
import org.kissmachine.impl.easycrud.SmStateDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.summerb.approaches.jdbccrud.api.query.Query;
import org.summerb.approaches.jdbccrud.impl.EasyCrudValidationStrategyAbstract;
import org.summerb.approaches.jdbccrud.impl.wireTaps.EasyCrudWireTapDelegatingImpl;
import org.summerb.approaches.jdbccrud.impl.wireTaps.EasyCrudWireTapTableAuthImpl;
import org.summerb.approaches.jdbccrud.scaffold.api.EasyCrudScaffold;
import org.summerb.approaches.springmvc.security.apis.ElevationRunner;

import com.google.common.eventbus.EventBus;

/**
 * Java-based config for FarmConfig beans.
 * 
 * NOTE: I've decided to try to move away from XML-based context config.
 * Although it's my favorite, it appears Spring is moving away from it big time
 * (new STS wont even have support for it).
 * 
 * @author Sergey Karpushin
 *
 */
@Configuration
public class FarmConfigSpringConfig {
	@Autowired
	private EasyCrudScaffold easyCrudScaffold;
	@Autowired
	private EventBus entityChangesEventBus;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private EventBus eventBus;
	@Autowired
	private ElevationRunner backgroundProcessRightsElevation;
	@Value("${git.workingPath}")
	private String workingPath;

	@Bean
	public SmDataAuthStrategy smDataAuthStrategy() {
		return new SmDataAuthStrategyImpl();
	}

	@Bean
	public JdbcJsonFieldSerializer jdbcJsonFieldSerializer() {
		return new JdbcJsonFieldSerializerImpl();
	}

	@Bean
	public SmDataDao smDataDao() {
		SmDataDaoImpl ret = new SmDataDaoImpl();
		ret.setDataSource(dataSource);
		return ret;
	}

	@Bean
	public SmDataService smDataService() {
		SmDataServiceImpl ret = new SmDataServiceImpl();
		ret.setDao(smDataDao());
		ret.setWireTap(new EasyCrudWireTapDelegatingImpl<String, SmData>(
				Arrays.asList(new EasyCrudWireTapTableAuthImpl<String, SmData>(smDataAuthStrategy()))));
		return ret;
	}

	@Bean
	public SmStateDataAuthStrategy smStateDataAuthStrategy() {
		return new SmStateDataAuthStrategyImpl();
	}

	@Bean
	public SmStateDataDao smStateDataDao() {
		SmStateDataDaoImpl ret = new SmStateDataDaoImpl();
		ret.setDataSource(dataSource);
		return ret;
	}

	@Bean
	public SmStateDataService smStateDataService() {
		SmStateDataServiceImpl ret = new SmStateDataServiceImpl();
		ret.setDao(smStateDataDao());
		ret.setWireTap(new EasyCrudWireTapDelegatingImpl<String, SmStateData>(
				Arrays.asList(new EasyCrudWireTapTableAuthImpl<String, SmStateData>(smStateDataAuthStrategy()),
						new SmStateDataTrimWireTap())));

		return ret;
	}

	@Bean
	public SmState noConfigState() {
		return new NoConfigStateImpl();
	}

	@Bean
	public SmState clonningConfigState() {
		return new ClonningConfigStateImpl(workingPath);
	}

	@Bean
	public SmState deliveringConfigToNodesState() {
		return new DeliveringConfigToNodesStateImpl();
	}

	@Bean
	public SmState tearDownMessageFlowState() {
		return new TearDownMessageFlowStateImpl();
	}

	@Bean
	public SmState committingConfigChangeState() {
		return new CommittingConfigChangeStateImpl();
	}

	@Bean
	public SmState readyState() {
		return new ReadyStateImpl();
	}

	@Bean
	public SmState pullingUpdatesState() {
		return new PullingUpdatesStateImpl(workingPath);
	}

	@Bean
	public SmState changingRepoState() {
		return new ChangingRepoStateImpl(workingPath);
	}

	@Bean
	public StateMachine farmConfigStateMachine(SmDataService smDataService) {
		StateMachineImpl ret = new StateMachineImpl();
		ret.setSmDataService(smDataService());
		ret.setSmStateDataService(smStateDataService());
		ret.setMachineType(FarmConfigMachineVariables.MACHINE_TYPE);
		ret.setAllStates(Arrays.asList(noConfigState(), clonningConfigState(), deliveringConfigToNodesState(),
				tearDownMessageFlowState(), committingConfigChangeState(), readyState(), pullingUpdatesState(),
				changingRepoState()));
		ret.setInitialState(noConfigState());
		backgroundProcessRightsElevation.runElevated(() -> getOrCreateMachineData(smDataService, ret));
		return ret;
	}

	private void getOrCreateMachineData(SmDataService smDataService, StateMachineImpl ret) {
		try {
			SmData existingMachine = smDataService
					.findOneByQuery(Query.n().eq(SmData.FN_MACHINE_TYPE, FarmConfigMachineVariables.MACHINE_TYPE));
			if (existingMachine == null) {
				ret.startNew(null, new FarmConfigMachineVariables());
			} else {
				ret.resumeExisting(existingMachine);
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to initialize State machine", t);
		}
	}

	@Bean
	public EasyCrudValidationStrategyAbstract<GitConfig> gitConfigValidationStrategy() {
		return new GitConfigValidationStrategyImpl();
	}

	@Bean
	public GitAbstraction gitAbstraction() {
		return new GitAbstractionImpl();
	}

	@Bean
	public FarmConfigFolderReader farmConfigFolderReader() {
		return new FarmConfigFolderReaderImpl();
	}

	@Bean
	public FarmConfigPackager farmConfigPackager(@Value("${farmConfig.packagesFolder}") String packagesFolder) {
		return new FarmConfigPackagerImpl(packagesFolder);
	}

	public String getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(String workingPath) {
		this.workingPath = workingPath;
	}
}
