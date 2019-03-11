package com.artal.capella.mapping.patch.wrappers;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.RuleIdentifier;

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
