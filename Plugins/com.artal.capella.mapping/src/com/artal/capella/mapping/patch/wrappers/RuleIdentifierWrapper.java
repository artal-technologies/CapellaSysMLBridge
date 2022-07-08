/*******************************************************************************
 * Copyright (c) 2019 - 2022 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.patch.wrappers;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.RuleIdentifier;

/**
 * @author YBI
 *
 * @param <S>
 * @param <T>
 */
public class RuleIdentifierWrapper<S, T> extends RuleIdentifier<S, T> implements IQueryIdentifier<S>
{
	private IQueryIdentifier<S> _queryID_p;

	public RuleIdentifierWrapper(IQueryIdentifier<S> queryID_p)
	{
		_queryID_p = queryID_p;
	}

	public IQueryIdentifier<S> getRealIdentifier()
	{
		return _queryID_p;
	}
}
