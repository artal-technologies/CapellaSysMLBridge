/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.actions;

import java.io.File;

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

/**
 * @author YBI
 *
 */
public class CapellaSysmlLaunchDialog extends TitleAreaDialog {

	private String _umlPath = "";

	public CapellaSysmlLaunchDialog(Shell parent) {
		super(parent);
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Launch Export Capella to Sysml");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		setTitle("Select a target UML file.");
		Group selectUMLGroup = new Group(container, SWT.NONE);
		selectUMLGroup.setText("SysML selection");
		selectUMLGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		selectUMLGroup.setLayout(new GridLayout(3, false));

		Label label = new Label(selectUMLGroup, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText("Select a target UML Cameo file");

		Text umlText = new Text(selectUMLGroup, SWT.BORDER);
		umlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		umlText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				_umlPath = umlText.getText();
				validate();

			}
		});
		Button button = new Button(selectUMLGroup, SWT.NONE);
		button.setLayoutData(new GridData());
		button.setText("Browse..");

		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
				dialog.setFilterExtensions(new String[] { "*.uml" });
				String filePath = dialog.open();
				if (!filePath.endsWith(".uml")) {
					filePath += ".uml";
				}
				umlText.setText(filePath);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});

		return container;
	}

	public String getUmlPath() {
		return _umlPath;
	}

	@Override
	protected Control createButtonBar(Composite parent) {

		Control createButtonBar = super.createButtonBar(parent);
		validate();
		return createButtonBar;

	}

	private void validate() {
		String errorMessage = null;
		if (_umlPath == null || _umlPath.trim().isEmpty()) {
			errorMessage = "No input file.";
		} else {
			File file = new File(_umlPath);
			File parentFile = file.getParentFile();
			if (parentFile==null || !parentFile.exists()) {
				errorMessage = "Invalid parent folder.";
			}
		}
		setErrorMessage(errorMessage);
	}
}
