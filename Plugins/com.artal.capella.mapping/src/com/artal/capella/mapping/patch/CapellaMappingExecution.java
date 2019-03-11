package com.artal.capella.mapping.patch;

import java.util.Map.Entry;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IRule;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRuleIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.MappingExecution;

import com.artal.capella.mapping.patch.wrappers.RuleIdentifierWrapper;

/**
 * Patch of MappingExecution creator to bypass restriction (make it really
 * tolerant to several access to the same target (tolerantToDuplicates))
 * 
 * @author JLA
 */
public class CapellaMappingExecution extends MappingExecution {
	public CapellaMappingExecution(org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace.Editable trace_p) {
		super(trace_p);
	}

	/**
	 * Bypass the Mapping bug. This method must return "false" but the variable
	 * must be set to "true" (default value) to REALLY be tolerant to duplicates
	 */
	public boolean isTolerantToDuplicates() {
		return false;
	}

	@Override
	@Deprecated
	public <S, T> T get(S source_p, IRuleIdentifier<S, T> ruleID_p) {
		return super.get(source_p, ruleID_p);
	}

	@SuppressWarnings("rawtypes")
	public <S, T> T getFirst(S source_p, IRuleIdentifier<S, T> realRuleId) {
		T result = null;

		for (Entry<IRuleIdentifier<?, ?>, IRule<?, ?>> ruleMapEntry : _ruleMap.entrySet()) {
			IRuleIdentifier<?, ?> ruleIdWrapper = ruleMapEntry.getKey();

			if (ruleIdWrapper instanceof RuleIdentifierWrapper<?, ?>
					&& ((RuleIdentifierWrapper) ruleIdWrapper).getRealIdentifier().equals(realRuleId)) {
				result = get(source_p, (IRuleIdentifier<S, T>) ruleIdWrapper);
				if (result != null) {
					return result;
				}
			}
		}
		return null;

	}
}
