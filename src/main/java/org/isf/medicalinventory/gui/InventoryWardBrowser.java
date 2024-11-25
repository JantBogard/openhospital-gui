/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.medicalinventory.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalinventory.gui.InventoryWardEdit.InventoryListener;
import org.isf.medicalinventory.manager.MedicalInventoryManager;
import org.isf.medicalinventory.model.InventoryStatus;
import org.isf.medicalinventory.model.InventoryType;
import org.isf.medicalinventory.model.MedicalInventory;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.springframework.data.domain.Page;

public class InventoryWardBrowser extends ModalJFrame implements InventoryListener {

	private static final long serialVersionUID = 1L;
	private static final int PAGE_SIZE = 50;
	private final String[] pColums = {
					MessageBundle.getMessage("angal.inventory.referenceshow.col").toUpperCase(),
					MessageBundle.getMessage("angal.common.ward.col").toUpperCase(),
					MessageBundle.getMessage("angal.common.date.col").toUpperCase(),
					MessageBundle.getMessage("angal.inventory.status.col").toUpperCase(),
					MessageBundle.getMessage("angal.common.user.col").toUpperCase()
	};
	private final int[] pColumwidth = { 150, 150, 100, 100, 150 };
	private final MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
	private final WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	JButton next;
	JButton previous;
	JComboBox<Integer> pagesCombo = new JComboBox<>();
	JLabel under = new JLabel("/ 0 " + MessageBundle.getMessage("angal.inventory.page.text"));
	private GoodDateChooser jCalendarTo;
	private GoodDateChooser jCalendarFrom;
	private LocalDateTime dateFrom = TimeTools.getDateToday0();
	private LocalDateTime dateTo = TimeTools.getDateToday24();
	private JLabel jLabelTo;
	private JLabel jLabelFrom;
	private JPanel panelHeader;
	private JPanel panelFooter;
	private JPanel panelContent;
	private JButton closeButton;
	private JButton newButton;
	private JButton editButton;
	private JButton deleteButton;
	private JButton viewButton;
	private JScrollPane scrollPaneInventory;
	private JTable jTableInventory;
	private JComboBox<String> statusComboBox;
	private JLabel statusLabel;
	private int startIndex;
	private int totalRows;
	private List<MedicalInventory> inventoryList;

	public InventoryWardBrowser() {
		initComponents();
	}

	private void initComponents() {

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(850, 550));
		setLocationRelativeTo(null); // center
		setTitle(MessageBundle.getMessage("angal.inventory.managementward.title"));

		panelHeader = getPanelHeader();
		getContentPane().add(panelHeader, BorderLayout.NORTH);

		panelContent = getPanelContent();
		getContentPane().add(panelContent, BorderLayout.CENTER);

		panelFooter = getPanelFooter();
		getContentPane().add(panelFooter, BorderLayout.SOUTH);

		ajustWidth();

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		pagesCombo.setEditable(true);
		previous.setEnabled(false);
		next.setEnabled(false);
		next.addActionListener(actionEvent -> {
			if (!previous.isEnabled()) {
				previous.setEnabled(true);
			}
			startIndex += PAGE_SIZE;
			jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
			if (startIndex + PAGE_SIZE > totalRows) {
				next.setEnabled(false);
			}
			pagesCombo.setSelectedItem(startIndex / PAGE_SIZE + 1);
		});

		previous.addActionListener(actionEvent -> {
			if (!next.isEnabled()) {
				next.setEnabled(true);
			}
			startIndex -= PAGE_SIZE;
			jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
			if (startIndex < PAGE_SIZE) {
				previous.setEnabled(false);
			}
			pagesCombo.setSelectedItem(startIndex / PAGE_SIZE + 1);
		});
		pagesCombo.addItemListener(itemEvent -> {
			int eventID = itemEvent.getStateChange();

			if (eventID == ItemEvent.SELECTED) {
				int page_number = (Integer) pagesCombo.getSelectedItem();
				startIndex = (page_number - 1) * PAGE_SIZE;

				next.setEnabled(startIndex + PAGE_SIZE <= totalRows);
				previous.setEnabled(page_number != 1);
				pagesCombo.setSelectedItem(startIndex / PAGE_SIZE + 1);
				jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));

				pagesCombo.setEnabled(true);
			}
		});
	}

	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelHeader.setBorder(new EmptyBorder(5, 0, 0, 5));
			GridBagLayout gbl_panelHeader = new GridBagLayout();
			gbl_panelHeader.columnWidths = new int[] { 65, 103, 69, 105, 77, 146, 0 };
			gbl_panelHeader.rowHeights = new int[] { 32, 0 };
			gbl_panelHeader.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
			gbl_panelHeader.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panelHeader.setLayout(gbl_panelHeader);
			GridBagConstraints gbc_jLabelFrom = new GridBagConstraints();
			gbc_jLabelFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_jLabelFrom.insets = new Insets(0, 0, 0, 5);
			gbc_jLabelFrom.gridx = 0;
			gbc_jLabelFrom.gridy = 0;
			panelHeader.add(getJLabelFrom(), gbc_jLabelFrom);
			GridBagConstraints gbc_jCalendarFrom = new GridBagConstraints();
			gbc_jCalendarFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarFrom.insets = new Insets(0, 0, 0, 5);
			gbc_jCalendarFrom.gridx = 1;
			gbc_jCalendarFrom.gridy = 0;
			panelHeader.add(getJCalendarFrom(), gbc_jCalendarFrom);
			GridBagConstraints gbc_jLabelTo = new GridBagConstraints();
			gbc_jLabelTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_jLabelTo.insets = new Insets(0, 0, 0, 5);
			gbc_jLabelTo.gridx = 2;
			gbc_jLabelTo.gridy = 0;
			panelHeader.add(getJLabelTo(), gbc_jLabelTo);
			GridBagConstraints gbc_jCalendarTo = new GridBagConstraints();
			gbc_jCalendarTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarTo.insets = new Insets(0, 0, 0, 5);
			gbc_jCalendarTo.gridx = 3;
			gbc_jCalendarTo.gridy = 0;
			panelHeader.add(getJCalendarTo(), gbc_jCalendarTo);
			GridBagConstraints gbc_stateLabel = new GridBagConstraints();
			gbc_stateLabel.fill = GridBagConstraints.HORIZONTAL;
			gbc_stateLabel.insets = new Insets(0, 0, 0, 5);
			gbc_stateLabel.gridx = 4;
			gbc_stateLabel.gridy = 0;
			panelHeader.add(getStatusLabel(), gbc_stateLabel);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 5;
			gbc_comboBox.gridy = 0;
			panelHeader.add(getComboBox(), gbc_comboBox);
		}
		return panelHeader;
	}

	private JPanel getPanelContent() {
		if (panelContent == null) {
			panelContent = new JPanel();
			GridBagLayout gbl_panelContent = new GridBagLayout();
			gbl_panelContent.columnWidths = new int[] { 452, 0 };
			gbl_panelContent.rowHeights = new int[] { 402, 0 };
			gbl_panelContent.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
			gbl_panelContent.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
			panelContent.setLayout(gbl_panelContent);
			GridBagConstraints gbc_scrollPaneInventory = new GridBagConstraints();
			gbc_scrollPaneInventory.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneInventory.gridx = 0;
			gbc_scrollPaneInventory.gridy = 0;
			panelContent.add(getScrollPaneInventory(), gbc_scrollPaneInventory);
		}
		return panelContent;
	}

	private JPanel getPanelFooter() {
		if (panelFooter == null) {
			panelFooter = new JPanel();
			next = new JButton(MessageBundle.getMessage("angal.inventory.nextarrow.btn"));
			next.setMnemonic(KeyEvent.VK_RIGHT);
			previous = new JButton(MessageBundle.getMessage("angal.inventory.previousarrow.btn"));
			next.setMnemonic(KeyEvent.VK_LEFT);

			panelFooter.add(previous);
			panelFooter.add(pagesCombo);
			panelFooter.add(under);
			panelFooter.add(next);

			panelFooter.add(getNewButton());
			panelFooter.add(getViewButton());
			panelFooter.add(getEditButton());
			panelFooter.add(getDeleteButton());
			panelFooter.add(getCloseButton());
		}
		return panelFooter;
	}

	private GoodDateChooser getJCalendarFrom() {
		if (jCalendarFrom == null) {
			jCalendarFrom = new GoodDateChooser(LocalDate.now(), false, false);
			jCalendarFrom.addDateChangeListener(event -> {
				dateFrom = jCalendarFrom.getDateStartOfDay();
				InventoryBrowsingModel inventoryModel = new InventoryBrowsingModel();
				totalRows = inventoryModel.getRowCount();
				startIndex = 0;
				previous.setEnabled(false);
				next.setEnabled(totalRows > PAGE_SIZE);
				jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
				initialiseCombo(totalRows);
			});
		}
		return jCalendarFrom;
	}

	private GoodDateChooser getJCalendarTo() {
		if (jCalendarTo == null) {
			jCalendarTo = new GoodDateChooser(LocalDate.now(), false, false);
			jCalendarTo.addDateChangeListener(event -> {
				dateTo = jCalendarTo.getDateEndOfDay();
				InventoryBrowsingModel inventoryModel = new InventoryBrowsingModel();
				totalRows = inventoryModel.getRowCount();
				startIndex = 0;
				previous.setEnabled(false);
				next.setEnabled(totalRows > PAGE_SIZE);
				jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
				initialiseCombo(totalRows);
			});
		}
		return jCalendarTo;
	}

	private JLabel getJLabelTo() {
		if (jLabelTo == null) {
			jLabelTo = new JLabel();
			jLabelTo.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelTo.setText(MessageBundle.getMessage("angal.common.dateto.label"));
		}
		return jLabelTo;
	}

	private JLabel getJLabelFrom() {
		if (jLabelFrom == null) {
			jLabelFrom = new JLabel();
			jLabelFrom.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelFrom.setText(MessageBundle.getMessage("angal.common.datefrom.label"));
		}
		return jLabelFrom;
	}

	private JButton getNewButton() {
		newButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		newButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		newButton.addActionListener(actionEvent -> {
			InventoryWardEdit inventoryWardEdit = new InventoryWardEdit();
			InventoryWardEdit.addInventoryListener(InventoryWardBrowser.this);
			inventoryWardEdit.showAsModal(InventoryWardBrowser.this);
		});
		return newButton;
	}

	private JButton getEditButton() {
		editButton = new JButton(MessageBundle.getMessage("angal.common.update.btn"));
		editButton.setMnemonic(MessageBundle.getMnemonic("angal.common.update.btn.key"));
		editButton.setEnabled(false);
		editButton.addActionListener(actionEvent -> {
			MedicalInventory inventory;
			if (jTableInventory.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventory.msg");
				return;
			}
			int selectedRow = jTableInventory.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectinventory.msg");
				return;
			}
			inventory = inventoryList.get(selectedRow);
			if (inventory.getStatus().equals(InventoryStatus.canceled.toString())) {
				MessageDialog.error(this, "angal.inventory.cancelednoteditable.msg");
				return;
			}
			InventoryWardEdit inventoryWardEdit = new InventoryWardEdit(inventory, "update");
			InventoryWardEdit.addInventoryListener(InventoryWardBrowser.this);
			inventoryWardEdit.showAsModal(InventoryWardBrowser.this);
		});
		return editButton;
	}

	private JButton getViewButton() {
		viewButton = new JButton(MessageBundle.getMessage("angal.inventory.view.btn"));
		viewButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.view.btn.key"));
		viewButton.setEnabled(false);
		viewButton.addActionListener(actionEvent -> {
			MedicalInventory inventory;
			if (jTableInventory.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventory.msg");
				return;
			}
			int selectedRow = jTableInventory.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectinventory.msg");
				return;
			}
			inventory = inventoryList.get(selectedRow);
			InventoryWardEdit inventoryWardEdit = new InventoryWardEdit(inventory, "view");
			InventoryWardEdit.addInventoryListener(InventoryWardBrowser.this);
			inventoryWardEdit.showAsModal(InventoryWardBrowser.this);
		});
		return viewButton;
	}

	private JButton getDeleteButton() {
		deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(actionEvent -> {
			if (jTableInventory.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventory.msg");
				return;
			}
			int selectedRow = jTableInventory.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectinventory.msg");
				return;
			}
			MedicalInventory inventory = inventoryList.get(selectedRow);
			String currentStatus = inventory.getStatus();
			if (currentStatus.equalsIgnoreCase(InventoryStatus.validated.toString()) || currentStatus.equalsIgnoreCase(InventoryStatus.draft.toString())) {
				int response = MessageDialog.yesNo(this, "angal.inventory.deletion.confirm.msg");
				if (response == JOptionPane.YES_OPTION) {
					try {
						medicalInventoryManager.deleteInventory(inventory);
						MessageDialog.info(this, "angal.inventory.deletion.success.msg");
						jTableInventory.setModel(new InventoryBrowsingModel());
					} catch (OHServiceException e) {
						MessageDialog.error(this, "angal.inventory.deletion.error.msg");
					}
				}
			} else {
				MessageDialog.error(this, "angal.inventory.deletion.error.msg");
			}
		});
		return deleteButton;
	}

	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		return closeButton;
	}

	private JScrollPane getScrollPaneInventory() {
		if (scrollPaneInventory == null) {
			scrollPaneInventory = new JScrollPane();
			scrollPaneInventory.setViewportView(getJTableInventory());
		}
		return scrollPaneInventory;
	}

	private JTable getJTableInventory() {
		if (jTableInventory == null) {
			jTableInventory = new JTable();
			jTableInventory.setFillsViewportHeight(true);
			jTableInventory.setModel(new InventoryBrowsingModel());
			jTableInventory.getSelectionModel().addListSelectionListener(e -> {
				if (e.getValueIsAdjusting()) {
					int[] selectedRows = jTableInventory.getSelectedRows();
					if (selectedRows.length == 1) {
						editButton.setEnabled(true);
						viewButton.setEnabled(true);
						deleteButton.setEnabled(true);
					} else {
						editButton.setEnabled(false);
						viewButton.setEnabled(false);
						deleteButton.setEnabled(false);
					}
				}
			});
		}
		return jTableInventory;
	}

	private JComboBox<String> getComboBox() {
		if (statusComboBox == null) {
			statusComboBox = new JComboBox<>();
			statusComboBox.addItem("");
			for (InventoryStatus currentStatus : InventoryStatus.values()) {
				statusComboBox.addItem(MessageBundle.getMessage("angal.inventory." + currentStatus));
			}
			statusComboBox.addActionListener(actionEvent -> {
				InventoryBrowsingModel inventoryBrowsingModel = new InventoryBrowsingModel();
				totalRows = inventoryBrowsingModel.getRowCount();
				startIndex = 0;
				previous.setEnabled(false);
				next.setEnabled(totalRows > PAGE_SIZE);
				jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
				initialiseCombo(totalRows);
			});
		}
		return statusComboBox;
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel(MessageBundle.getMessage("angal.inventory.status.label"));
			statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return statusLabel;
	}

	private void ajustWidth() {
		for (int i = 0; i < pColumwidth.length; i++) {
			jTableInventory.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
		}
	}

	public void initialiseCombo(int total_rows) {
		int j = 0;

		pagesCombo.removeAllItems();
		for (int i = 0; i < total_rows / PAGE_SIZE; i++) {
			j = i + 1;
			pagesCombo.addItem(j);
		}
		if (j * PAGE_SIZE < total_rows) {
			pagesCombo.addItem(j + 1);
			under.setText("/" + (total_rows / PAGE_SIZE + 1 + " " + MessageBundle.getMessage("angal.inventory.pages.text")));
		} else {
			under.setText("/" + total_rows / PAGE_SIZE + " " + MessageBundle.getMessage("angal.inventory.pages.text"));
		}
	}

	@Override
	public void InventoryInserted(AWTEvent e) {
		jTableInventory.setModel(new InventoryBrowsingModel());
	}

	@Override
	public void InventoryUpdated(AWTEvent e) {
		jTableInventory.setModel(new InventoryBrowsingModel());
	}

	@Override
	public void InventoryCancelled(AWTEvent e) {
		jTableInventory.setModel(new InventoryBrowsingModel());
	}

	class InventoryBrowsingModel extends DefaultTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public InventoryBrowsingModel() {
			inventoryList = new ArrayList<>();
			String status = statusComboBox.getSelectedIndex() > 0 ? statusComboBox.getSelectedItem().toString().toLowerCase() : null;
			String type = InventoryType.ward.toString();
			try {
				inventoryList = medicalInventoryManager.getMedicalInventoryByParams(dateFrom, dateTo, status, type);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public InventoryBrowsingModel(int startIndex, int pageSize) {
			inventoryList = new ArrayList<>();
			String status = statusComboBox.getSelectedIndex() > 0 ? statusComboBox.getSelectedItem().toString().toLowerCase() : null;
			String type = InventoryType.ward.toString();
			try {
				Page<MedicalInventory> medInventorypage = medicalInventoryManager.getMedicalInventoryByParamsPageable(dateFrom, dateTo, status, type,
								startIndex,
								pageSize);
				inventoryList = medInventorypage.getContent();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public Class<?> getColumnClass(int c) {
			if (c == 0) {
				return String.class;
			} else if (c == 1) {
				return String.class;
			} else if (c == 2) {
				return String.class;
			} else if (c == 3) {
				return String.class;
			} else if (c == 4) {
				return String.class;
			}
			return null;
		}

		@Override
		public int getRowCount() {
			if (inventoryList == null) {
				return 0;
			}
			return inventoryList.size();
		}

		public String getColumnName(int c) {
			return pColums[c];
		}

		public int getColumnCount() {
			return pColums.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			MedicalInventory medInvt = inventoryList.get(r);

			if (c == -1) {
				return medInvt;
			} else if (c == 0) {
				return medInvt.getInventoryReference();
			} else if (c == 1) {
				Ward ward = new Ward();
				try {
					ward = wardBrowserManager.findWard(medInvt.getWard());
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				return ward == null ? "" : ward.getDescription();
			} else if (c == 2) {
				return medInvt.getInventoryDate().format(DATE_TIME_FORMATTER);
			} else if (c == 3) {
				return medInvt.getStatus();
			} else if (c == 4) {
				return medInvt.getUser();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

	}
}
