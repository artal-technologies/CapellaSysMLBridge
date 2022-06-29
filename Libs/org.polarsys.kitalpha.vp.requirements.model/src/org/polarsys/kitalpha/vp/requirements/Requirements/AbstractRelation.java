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

package org.polarsys.kitalpha.vp.requirements.Requirements;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Abstract Relation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.polarsys.kitalpha.vp.requirements.Requirements.AbstractRelation#getRelationType <em>Relation Type</em>}</li>
 *   <li>{@link org.polarsys.kitalpha.vp.requirements.Requirements.AbstractRelation#getRelationTypeProxy <em>Relation Type Proxy</em>}</li>
 * </ul>
 *
 * @see org.polarsys.kitalpha.vp.requirements.Requirements.RequirementsPackage#getAbstractRelation()
 * @model abstract="true"
 * @generated
 */

public interface AbstractRelation extends ReqIFElement {

	/**
	 * Returns the value of the '<em><b>Relation Type</b></em>' reference.
	
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relation Type</em>' reference.
	 * @see #setRelationType(RelationType)
	 * @see org.polarsys.kitalpha.vp.requirements.Requirements.RequirementsPackage#getAbstractRelation_RelationType()
	 * @model
	 * @generated
	 */

	RelationType getRelationType();

	/**
	 * Sets the value of the '{@link org.polarsys.kitalpha.vp.requirements.Requirements.AbstractRelation#getRelationType <em>Relation Type</em>}' reference.
	
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Relation Type</em>' reference.
	 * @see #getRelationType()
	 * @generated
	 */

	void setRelationType(RelationType value);

	/**
	 * Returns the value of the '<em><b>Relation Type Proxy</b></em>' attribute.
	
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relation Type Proxy</em>' attribute.
	 * @see #setRelationTypeProxy(String)
	 * @see org.polarsys.kitalpha.vp.requirements.Requirements.RequirementsPackage#getAbstractRelation_RelationTypeProxy()
	 * @model
	 * @generated
	 */

	String getRelationTypeProxy();

	/**
	 * Sets the value of the '{@link org.polarsys.kitalpha.vp.requirements.Requirements.AbstractRelation#getRelationTypeProxy <em>Relation Type Proxy</em>}' attribute.
	
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Relation Type Proxy</em>' attribute.
	 * @see #getRelationTypeProxy()
	 * @generated
	 */

	void setRelationTypeProxy(String value);

} // AbstractRelation
