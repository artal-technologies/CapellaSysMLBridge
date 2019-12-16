/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.patch.wrappers;

import org.eclipse.emf.diffmerge.bridge.api.ISymbolFunction;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQuery;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRule;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRuleIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.MappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryExecution;

import com.artal.capella.mapping.patch.CapellaQueryExecution;

/**
 * Used to simulate a different IRule (check PatchedMappingExecution)
 * 
 * @author YBI
 *
 * @param <S>
 * @param <T>
 */
public class RuleWrapper<S, T> implements IRule<S, S, T> {
	private IRule<S, S, T> _realRule;

	private IRuleIdentifier<S, S, T> _ruleIdWrapper;

	@SuppressWarnings("unchecked")
	public RuleWrapper(IRule<S, S, T> rule_p, QueryExecution queryExecution_p) {
		_realRule = rule_p;
		if (queryExecution_p instanceof CapellaQueryExecution) {
			_ruleIdWrapper = (IRuleIdentifier<S, S, T>) ((CapellaQueryExecution) queryExecution_p)
					.getCurrentIdentifierWrapper();
		}
	}

	@Override
	public IRuleIdentifier<S, S, T> getID() {
		return _ruleIdWrapper;
	}

	@Override
	public Object getSymbol(ISymbolFunction function_p) {
		return _realRule.getSymbol(function_p);
	}

	@Override
	public T createTarget(S source_p, IQueryExecution queryExecution_p) {
		return _realRule.createTarget(source_p, queryExecution_p);
	}

	@Override
	public void defineTarget(S source_p, T target_p, IQueryExecution queryExecution_p,
			IMappingExecution mappingExecution_p) {
		_realRule.defineTarget(source_p, target_p, queryExecution_p, mappingExecution_p);
	}

	@Override
	public IQuery<?, ? extends S> getInputProvider() {
		return _realRule.getInputProvider();
	}
		
	public IRule<S, S, T> getRealRule() {
		return _realRule;
	}

	public S traceSource(S source_p) {
	    return source_p;
	}
}
