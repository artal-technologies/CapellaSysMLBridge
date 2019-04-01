/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.actions;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
//import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.artal.capella.mapping.sysml2capella.preferences.SysmlCapellaPreferenceUtils;

/**
 * @author YBI
 *
 */
public class SysMLCapellaLaunchDialog extends TitleAreaDialog {

	private String _umlPath;
	private String _configPath;

	public SysMLCapellaLaunchDialog(Shell parent) {
		super(parent);
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Launch Import Sysml to Capella");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		setTitle("Select the UML input file.");
		Group sysmlGroup = new Group(container, SWT.NONE);
		sysmlGroup.setLayout(new GridLayout(3, false));
		sysmlGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		sysmlGroup.setText("Sysml selection");

		Label label = new Label(sysmlGroup, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText("Select an UML Cameo file");

		Text umlText = new Text(sysmlGroup, SWT.BORDER);
		umlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		umlText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				_umlPath = umlText.getText();
				validate();

			}
		});
		Button button = new Button(sysmlGroup, SWT.NONE);
		button.setLayoutData(new GridData());
		button.setText("Browse..");

		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
				dialog.setFilterExtensions(new String[] { "*.uml" });
				String filePath = dialog.open();
				umlText.setText(filePath);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});
		Text configText = SysmlCapellaPreferenceUtils.createPreferenceComposite(container, false);
		configText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				_configPath = configText.getText();
				validate();

			}
		});

		return container;

	}

	@Override
	protected Control createButtonBar(Composite parent) {

		Control createButtonBar = super.createButtonBar(parent);
		validate();
		return createButtonBar;

	}

	/**
	 * @return the umlText
	 */
	public String getUmlPath() {
		return _umlPath;
	}

	/**
	 * @return the configText
	 */
	public String getConfigPath() {
		return _configPath;
	}

	private void validate() {
		String errorMessage = null;
		if (_umlPath == null || _umlPath.trim().isEmpty()) {
			errorMessage = "No input file.";
		} else {
			File file = new File(_umlPath);
			if (!file.exists()) {
				errorMessage = "Invalid input file.";
			}
		}
		setErrorMessage(errorMessage);
	}

	@Override
	public void setErrorMessage(String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			if (newErrorMessage != null) {
				button.setEnabled(false);
			} else {
				button.setEnabled(true);
			}
		}
	}

}
