/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.patch;

import java.util.Map.Entry;

import org.eclipse.emf.diffmerge.bridge.api.ICause;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRule;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRuleIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.MappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.util.TraceLoggingMessage;
import org.eclipse.emf.diffmerge.bridge.util.AbstractLoggingMessage;

import com.artal.capella.mapping.patch.wrappers.RuleIdentifierWrapper;

/**
 * Patch of MappingExecution creator to bypass restriction (make it really
 * tolerant to several access to the same target (tolerantToDuplicates))
 * 
 * @author YBI
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

	@Override
	protected AbstractLoggingMessage createTraceLoggingMessage(Object target_p, ICause<?> cause_p) {
		return new TraceLoggingMessage(target_p, cause_p) {
			@Override
			protected String getObjectLabel(Object object_p) {
				if (object_p == null) {
					return "";
				}
				return super.getObjectLabel(object_p);
			}
		};
	}
}
