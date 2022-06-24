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

import org.eclipse.emf.diffmerge.bridge.api.IBridge;
import org.eclipse.emf.diffmerge.bridge.api.IBridgeExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingBridge;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRule;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.MappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.operations.MappingBridgeOperation;

import com.artal.capella.mapping.patch.wrappers.RuleWrapper;

/**
 * @author YBI
 *
 */
public class CapellaMappingBridgeOperation extends MappingBridgeOperation {
	public CapellaMappingBridgeOperation(Object sourceDataSet_p, Object targetDataSet_p, IMappingBridge<?, ?> bridge_p,
			IBridgeExecution execution_p) {
		super(sourceDataSet_p, targetDataSet_p, bridge_p, execution_p);
	}

	protected QueryExecution createQueryExecution() {
		return new CapellaQueryExecution();
	}

//	@Override
//	protected <S,T> void handleRuleForTargetCreation(IRule<S,TRS, T> rule_p, IBridge<S, T> bridge_p, Object source_p,
//			Object targetDataSet_p, QueryExecution queryExecution_p, MappingExecution execution_p) {
//		RuleWrapper<S,T> mirrorRule = new RuleWrapper<S,T>(rule_p, queryExecution_p);
//		super.handleRuleForTargetCreation(mirrorRule, bridge_p, source_p, targetDataSet_p, queryExecution_p,
//				execution_p);
//
//	}
}
