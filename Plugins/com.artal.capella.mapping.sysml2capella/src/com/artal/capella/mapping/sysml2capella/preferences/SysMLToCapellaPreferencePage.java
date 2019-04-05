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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * @author YBI
 *
 */
public class SysMLToCapellaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text _pathText;

	/**
	 * Default constructor.
	 */
	public SysMLToCapellaPreferencePage() {
	}

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            the page title
	 */
	public SysMLToCapellaPreferencePage(String title) {
		super(title);
	}

	/**
	 * Constructor
	 * 
	 * @param titl
	 *            the page title
	 * @param image
	 *            the header image
	 */
	public SysMLToCapellaPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout());
		 _pathText = SysmlCapellaPreferenceUtils.createPreferenceComposite(main, true);
		return main;
	}

	@Override
	public boolean performOk() {

		if (_pathText != null) {
			PlatformUI.getPreferenceStore().setValue(SysmlCapellaPreferenceUtils.SYSML_STRUCTURE_TEMPLATE,
					_pathText.getText());
		}

		return super.performOk();
	}

	@Override
	protected void performApply() {
		if (_pathText != null) {
			PlatformUI.getPreferenceStore().setValue(SysmlCapellaPreferenceUtils.SYSML_STRUCTURE_TEMPLATE,
					_pathText.getText());
		}
		super.performApply();
	}

}
