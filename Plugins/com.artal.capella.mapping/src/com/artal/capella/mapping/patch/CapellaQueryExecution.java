package com.artal.capella.mapping.patch;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IRuleIdentifier;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryExecution;

import com.artal.capella.mapping.patch.wrappers.RuleIdentifierWrapper;

/**
 * @author JLA
 *
 * @param <C>
 *            Kind of the current level
 * @param <P>
 *            Kind of the previous level
 */
public class CapellaQueryExecution<C, P> extends QueryExecution
{
	private IQueryIdentifier<C> _realIdentifier;

	private RuleIdentifierWrapper<C, ?> _identifierWrapper;

	private CapellaQueryExecution<P, ?> _superQueryExecution;

	private C _value;

	public CapellaQueryExecution()
	{
		super();
	}

	CapellaQueryExecution(CapellaQueryExecution<P, ?> superExecution_p,
		RuleIdentifierWrapper<C, ?> queryID_p, C value_p)
	{
		super(superExecution_p, queryID_p, value_p);

		// Hierarchy information
		_superQueryExecution = superExecution_p;

		// Identification data (real and wrapper)
		_realIdentifier = queryID_p.getRealIdentifier();
		_identifierWrapper = queryID_p;

		// Value
		_value = value_p;
	}

	@Override
	public <O> QueryExecution newWith(IQueryIdentifier<O> queryID_p, O value_p)
	{
		return new CapellaQueryExecution<>(this, new RuleIdentifierWrapper<>(queryID_p), value_p);
	}

	@SuppressWarnings("unchecked")
	public <O> O get(IQueryIdentifier<O> queryID_p)
	{
		if (queryID_p.equals(_realIdentifier))
		{
			return (O) _value;
		}

		return _superQueryExecution.get(queryID_p);
	}

	public RuleIdentifierWrapper<?, ?> getCurrentIdentifierWrapper()
	{
		return _identifierWrapper;
	}

	@SuppressWarnings("unchecked")
	public <S> RuleIdentifierWrapper<S, ?> getLastIdentifierWrapper(IQueryIdentifier<S> ruleID)
	{
		if (ruleID.equals(_realIdentifier))
		{
			return (RuleIdentifierWrapper<S, ?>) _identifierWrapper;
		}
		return _superQueryExecution.getLastIdentifierWrapper(ruleID);
	}

	@SuppressWarnings("unchecked")
	public <S, T> T getLastTargetObject(IMappingExecution mappingExecution_p,
		IRuleIdentifier<S, T> ruleID)
	{
		if (ruleID.equals(_realIdentifier))
		{
			return (T) mappingExecution_p.get((S) _value,
				(IRuleIdentifier<S, T>) _identifierWrapper);
		}
		if (_superQueryExecution != null)
		{
			return _superQueryExecution.getLastTargetObject(mappingExecution_p, ruleID);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <S, T> T getTargetObject(IMappingExecution mappingExecution_p,
		IRuleIdentifier<S, T> ruleID, S source)
	{
		if (ruleID.equals(_realIdentifier))
		{
			T target = mappingExecution_p.get(source, (IRuleIdentifier<S, T>) _identifierWrapper);

			if (target != null)
			{
				return target;
			}
		}
		if (_superQueryExecution != null)
		{
			return _superQueryExecution.getTargetObject(mappingExecution_p, ruleID, source);
		}
		return null;
	}

}
