package integr.org.statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kissmachine.api.dto.SmData;
import org.kissmachine.api.dto.SmStateData;
import org.kissmachine.api.easycrud.SmDataService;
import org.kissmachine.api.easycrud.SmStateDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * This test is used only to test communication with remote repos which requires
 * authentication.
 * 
 * All tests cases which tests ConfigProvider logic is placed in other test
 * suite that works with bundles from src/test/resources
 * 
 * @author Sergey Karpushin
 *
 */
@ContextConfiguration("classpath:test-statemachine-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@ProfileValueSourceConfiguration(SystemProfileValueSource.class)
@Transactional
public class StateMachineEasyCrudTest {

	@Autowired
	private SmDataService smDataService;
	@Autowired
	private SmStateDataService smStateDataService;

	@Test
	public void testMachineCRUD() throws Exception {
		SmData machine = new SmData();
		machine.setMachineType("testMachine");
		machine.setCurrentStateName("curStateName");
		machine.setException(true);
		machine.setFinished(true);
		machine.setSubjectId("subjId");
		ArrayOfInts numbers = new ArrayOfInts(Arrays.asList(1, 2, 3));
		machine.setVars(numbers);
		machine = smDataService.create(machine);

		machine = smDataService.findById(machine.getId());
		assertEquals("testMachine", machine.getMachineType());
		assertEquals("curStateName", machine.getCurrentStateName());
		assertEquals("subjId", machine.getSubjectId());
		assertTrue(machine.isException());
		assertTrue(machine.isFinished());
		assertEquals(numbers, machine.getVars());
	}

	@Test
	public void testSmStateDataCRUD() throws Exception {
		SmData machine = new SmData();
		machine.setMachineType("testMachine");
		machine = smDataService.create(machine);

		ArrayOfInts numbers = new ArrayOfInts(Arrays.asList(1, 2, 3));
		String result = "result string";

		SmStateData state = new SmStateData();
		state.setMachineId(machine.getId());
		state.setMachineType("testMachine");
		state.setStateName("S1");
		state.setParams(numbers);
		state.setResult(result);
		state = smStateDataService.create(state);
		state = smStateDataService.findById(state.getId());

		assertEquals("S1", state.getStateName());
		assertEquals(result, state.getResult());
		assertEquals(numbers, state.getParams());
	}

}
