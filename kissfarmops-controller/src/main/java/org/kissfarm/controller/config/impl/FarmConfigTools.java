package org.kissfarm.controller.config.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kissfarm.controller.config.dto.FarmConfig;
import org.kissfarm.shared.config.dto.ActionConfig;
import org.kissfarm.shared.config.dto.AppDefConfig;
import org.springframework.util.ObjectUtils;
import org.summerb.approaches.jdbccrud.impl.relations.DomLoaderImpl.Pair;

import com.google.common.base.Preconditions;

public final class FarmConfigTools {

	public static void assertFarmConfigChangeValid(FarmConfig oldConfig, FarmConfig newConfig) {
		// NOTE: That's how ugly Java Stream could get

		// Make sure status fields didn't change types
		List<Pair<AppDefConfig, AppDefConfig>> appPairs = oldConfig.getAppDefs().values().stream()
				.map(a -> Pair.of(a, newConfig.getAppDefs().get(a.getName()))).filter(x -> x.getValue() != null)
				.collect(Collectors.toList());

		appPairs.forEach(appPair -> {
			Map<String, String> a = appPair.getKey().getStatusSchema();
			if (a == null) {
				return;
			}
			Map<String, String> b = appPair.getValue().getStatusSchema();

			List<Pair<String, Pair<String, String>>> statusPairs = a.entrySet().stream()
					.map(aEntry -> Pair.of(aEntry.getKey(), Pair.of(aEntry.getValue(), b.get(aEntry.getKey()))))
					.filter(statusPair -> statusPair.getValue().getValue() != null).collect(Collectors.toList());

			statusPairs.forEach(statusPair -> {
				Preconditions.checkArgument(statusPair.getValue().getKey().equals(statusPair.getValue().getValue()),
						"AppDef's %s status %s type changed from % to %. That kind of change is not supported",
						appPair.getKey().getName(), statusPair.getKey(), statusPair.getValue().getKey(),
						statusPair.getValue().getValue());
			});
		});

		// Make sure action parameters weren't changed
		appPairs.forEach(appPair -> {
			List<Pair<ActionConfig, ActionConfig>> actionPairs = appPair.getKey().getActions().values().stream()
					.map(x -> Pair.of(x, appPair.getValue().getActions().get(x.getName())))
					.filter(x -> x.getValue() != null).collect(Collectors.toList());

			actionPairs.forEach(actionPair -> {
				List<String> pA = actionPair.getKey().getParameterNames();
				List<String> pB = actionPair.getValue().getParameterNames();
				if (pA == null && pB == null) {
					return;
				}
				Preconditions.checkArgument(ObjectUtils.nullSafeEquals(pA, pB),
						"Action %s parameter names are different %s != %s", actionPair.getKey().getName(), pA, pB);
			});
		});

		// TBD: Reject new config if it will result in nodes with zero applications
	}
}
