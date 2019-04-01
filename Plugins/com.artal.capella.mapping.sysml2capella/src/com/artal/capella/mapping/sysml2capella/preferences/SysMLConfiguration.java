/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.preferences;

/**
 * {@link SysMLConfiguration} provides the specific SysML strucutural data
 * 
 * @author YBI
 *
 */
public class SysMLConfiguration {

	private String _partPath = "03 Structure/Parts";
	private String productPath = "03 Structure/Product";
	private String useCasesPath = "02 Behavior/02 Use Cases";
	private String activitiesPath = "02 Behavior/02 Functional Architecture";

	/**
	 * Get the path package containing the Blocks "part"
	 * 
	 * @return the partPath
	 */
	public String getPartPath() {
		return _partPath;
	}

	/**
	 * Set the path package containing the Blocks "part".
	 * 
	 * @param partPath
	 *            the partPath to set
	 */
	public void setPartPath(String partPath) {
		_partPath = partPath;
	}

	/**
	 * Get the path package containing the Block "Product"
	 * 
	 * @return the productPath
	 */
	public String getProductPath() {
		return productPath;
	}

	/**
	 * Set the path package containing the Block "Product"
	 * 
	 * @param productPath
	 *            the productPath to set
	 */
	public void setProductPath(String productPath) {
		this.productPath = productPath;
	}

	/**
	 * Get the path package containing the Actors, Capabilities and
	 * Associations.
	 * 
	 * @return the useCasesPath
	 */
	public String getUseCasesPath() {
		return useCasesPath;
	}

	/**
	 * Set the path package containing the Actors, Capabilities and
	 * Associations.
	 * 
	 * @param useCasesPath
	 *            the useCasesPath to set
	 */
	public void setUseCasesPath(String useCasesPath) {
		this.useCasesPath = useCasesPath;
	}

	/**
	 * Get the path package containing the Activities (CallBehavior activities).
	 * 
	 * @return the activitiesPah
	 */
	public String getActivitiesPath() {
		return activitiesPath;
	}

	/**
	 * Set he path package containing the Activities (CallBehavior activities).
	 * 
	 * @param activitiesPah
	 *            the activitiesPah to set
	 */
	public void setActivitiesPath(String activitiesPah) {
		this.activitiesPath = activitiesPah;
	}

}
