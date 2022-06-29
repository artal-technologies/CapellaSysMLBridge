/**
 *
 *  Copyright (c) 2016, 2019 THALES GLOBAL SERVICES.
 *  
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
 *     Thales - initial API and implementation
 */

package org.polarsys.kitalpha.vp.requirements.Requirements.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.polarsys.kitalpha.emde.model.impl.ElementImpl;

import org.polarsys.kitalpha.vp.requirements.Requirements.RequirementsPackage;
import org.polarsys.kitalpha.vp.requirements.Requirements.SharedDirectAttributes;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Shared Direct Attributes</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.polarsys.kitalpha.vp.requirements.Requirements.impl.SharedDirectAttributesImpl#getReqIFName <em>Req IF Name</em>}</li>
 *   <li>{@link org.polarsys.kitalpha.vp.requirements.Requirements.impl.SharedDirectAttributesImpl#getReqIFPrefix <em>Req IF Prefix</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class SharedDirectAttributesImpl extends ElementImpl implements SharedDirectAttributes {

	/**
	 * The default value of the '{@link #getReqIFName() <em>Req IF Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReqIFName()
	 * @generated
	 * @ordered
	 */
	protected static final String REQ_IF_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReqIFName() <em>Req IF Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReqIFName()
	 * @generated
	 * @ordered
	 */
	protected String reqIFName = REQ_IF_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getReqIFPrefix() <em>Req IF Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReqIFPrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String REQ_IF_PREFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReqIFPrefix() <em>Req IF Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReqIFPrefix()
	 * @generated
	 * @ordered
	 */
	protected String reqIFPrefix = REQ_IF_PREFIX_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SharedDirectAttributesImpl() {

		super();

	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RequirementsPackage.Literals.SHARED_DIRECT_ATTRIBUTES;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */

	@Override
	public String getReqIFName() {

		return reqIFName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */

	@Override
	public void setReqIFName(String newReqIFName) {

		String oldReqIFName = reqIFName;
		reqIFName = newReqIFName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_NAME, oldReqIFName, reqIFName));

	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */

	@Override
	public String getReqIFPrefix() {

		return reqIFPrefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */

	@Override
	public void setReqIFPrefix(String newReqIFPrefix) {

		String oldReqIFPrefix = reqIFPrefix;
		reqIFPrefix = newReqIFPrefix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_PREFIX, oldReqIFPrefix, reqIFPrefix));

	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_NAME:
			return getReqIFName();
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_PREFIX:
			return getReqIFPrefix();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_NAME:
			setReqIFName((String) newValue);
			return;
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_PREFIX:
			setReqIFPrefix((String) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_NAME:
			setReqIFName(REQ_IF_NAME_EDEFAULT);
			return;
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_PREFIX:
			setReqIFPrefix(REQ_IF_PREFIX_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_NAME:
			return REQ_IF_NAME_EDEFAULT == null ? reqIFName != null : !REQ_IF_NAME_EDEFAULT.equals(reqIFName);
		case RequirementsPackage.SHARED_DIRECT_ATTRIBUTES__REQ_IF_PREFIX:
			return REQ_IF_PREFIX_EDEFAULT == null ? reqIFPrefix != null : !REQ_IF_PREFIX_EDEFAULT.equals(reqIFPrefix);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (ReqIFName: "); //$NON-NLS-1$
		result.append(reqIFName);
		result.append(", ReqIFPrefix: "); //$NON-NLS-1$
		result.append(reqIFPrefix);
		result.append(')');
		return result.toString();
	}

} //SharedDirectAttributesImpl