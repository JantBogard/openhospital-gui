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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.utils.jobjects.OhDefaultCellRenderer;

public class MedicalPicker extends JPanel {

    OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
    private static final long serialVersionUID = 1L;
    private Collection<Medical> medicalMapValues = new ArrayList<Medical>();
    private String searchCode;
    private JButton jButtonSelect;
    private JButton jButtonQuit;
    private JLabel jLabelImage;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JScrollPane jScrollPane1;
    private JTable jTableData;
    private JTextField jTextFieldFind;
    private int selectedRow = -1;
    private JDialog parentFrame;

    private int getSelectedRow() {
        return selectedRow;
    }

    public Medical getSelectedMedical() {
        StockMedModel model = (StockMedModel) jTableData.getModel();
        return model.getMedicalAtRow(this.getSelectedRow());
    }

    private void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public JDialog getParentFrame() {
        return parentFrame;
    }

    public void setParentFrame(JDialog parentFrame) {
        this.parentFrame = parentFrame;
    }

    public MedicalPicker(StockMedModel model, List<Medical> medicals) {
        initComponents(model);
        this.medicalMapValues = medicals;
    }

    private void initComponents(TableModel model) {

        jPanel3 = new JPanel();
        jPanel1 = new JPanel();

        jPanel3.setBackground(new Color(240, 240, 240));

        jPanel1.setBackground(new Color(240, 240, 240));

        setLayout(new BorderLayout(10, 10));
        add(jPanel1, BorderLayout.CENTER);
        GridBagLayout gbl_jPanel1 = new GridBagLayout();
        gbl_jPanel1.columnWidths = new int[] { 575, 0 };
        gbl_jPanel1.rowHeights = new int[] { 268, 0 };
        gbl_jPanel1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_jPanel1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
        jPanel1.setLayout(gbl_jPanel1);
        jScrollPane1 = new JScrollPane();
        jTableData = new JTable();

        jTableData.setDefaultRenderer(Object.class, cellRenderer);
        jTableData.setDefaultRenderer(Double.class, cellRenderer);
        jTableData.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JTable aTable = (JTable) e.getSource();
                int itsRow = aTable.rowAtPoint(e.getPoint());
                if (itsRow >= 0) {
                    cellRenderer.setHoveredRow(itsRow);
                } else {
                    cellRenderer.setHoveredRow(-1);
                }
                aTable.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
        jTableData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                cellRenderer.setHoveredRow(-1);
            }
        });

        jTableData.setModel(model);
        jTableData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableData.setShowVerticalLines(false);
        jTableData.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                jTableDataMouseClicked(evt);
            }

            public void mousePressed(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    validateSelection();
                }
            }
        });

        jTableData.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    validateSelection();
                }
                super.keyPressed(e);
            }
        });

        jScrollPane1.setViewportView(jTableData);
        GridBagConstraints gbc_jScrollPane1 = new GridBagConstraints();
        gbc_jScrollPane1.insets = new Insets(0, 15, 0, 15);
        gbc_jScrollPane1.fill = GridBagConstraints.BOTH;
        gbc_jScrollPane1.gridx = 0;
        gbc_jScrollPane1.gridy = 0;
        jPanel1.add(jScrollPane1, gbc_jScrollPane1);
        add(jPanel3, BorderLayout.NORTH);
        GridBagLayout gbl_jPanel3 = new GridBagLayout();
        gbl_jPanel3.columnWidths = new int[] { 90, 237, 0 };
        gbl_jPanel3.rowHeights = new int[] { 50, 0 };
        gbl_jPanel3.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_jPanel3.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        jPanel3.setLayout(gbl_jPanel3);

        jLabelImage = new JLabel();

        jLabelImage.setText(MessageBundle.getMessage("angal.common.search.txt"));
        GridBagConstraints gbc_jLabelImage = new GridBagConstraints();
        gbc_jLabelImage.anchor = GridBagConstraints.WEST;
        gbc_jLabelImage.insets = new Insets(0, 15, 0, 5);
        gbc_jLabelImage.gridx = 0;
        gbc_jLabelImage.gridy = 0;
        jPanel3.add(jLabelImage, gbc_jLabelImage);
        jTextFieldFind = new JTextField();
        jTextFieldFind.setText(searchCode);
        GridBagConstraints gbc_jTextFieldFind = new GridBagConstraints();
        gbc_jTextFieldFind.insets = new Insets(0, 0, 0, 15);
        gbc_jTextFieldFind.fill = GridBagConstraints.HORIZONTAL;
        gbc_jTextFieldFind.gridx = 1;
        gbc_jTextFieldFind.gridy = 0;
        jPanel3.add(jTextFieldFind, gbc_jTextFieldFind);
        jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = jTextFieldFind.getText();
                ArrayList<Medical> medList = new ArrayList<Medical>();
                for (Medical aMed : medicalMapValues) {
                    medList.add(aMed);
                }
                StockMedModel model = new StockMedModel(medList);
                jTableData.setModel(model);
                TableColumn column;
                for (int i = 0; i < 2; i++) {
                    column = jTableData.getColumnModel().getColumn(i);
                    if (i == 1) {
                        column.setPreferredWidth(375);
                    } else {
                        column.setPreferredWidth(100);
                    }
                }
                model.filter(text);
                if (jTableData.getRowCount() > 0) {
                    jTableData.setRowSelectionInterval(0, 0);
                }
                jTableData.updateUI();

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = jTextFieldFind.getText();
                StockMedModel model = (StockMedModel) jTableData.getModel();
                model.filter(text);
                if (jTableData.getRowCount() > 0) {
                    jTableData.setRowSelectionInterval(0, 0);
                }
                jTableData.updateUI();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        jTextFieldFind.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    validateSelection();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int selectedRow = jTableData.getSelectedRow();
                    if (jTableData.getRowCount() > 0 && selectedRow >= 0) {
                        selectedRow++;
                        if (jTableData.getRowCount() > selectedRow) {
                            jTableData.setRowSelectionInterval(selectedRow, selectedRow);
                            jTableData.scrollRectToVisible(new Rectangle(jTableData.getCellRect(selectedRow, 0, true)));
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int selectedRow = jTableData.getSelectedRow();
                    if (jTableData.getRowCount() > 0 && selectedRow > 0) {
                        selectedRow--;
                        jTableData.setRowSelectionInterval(selectedRow, selectedRow);
                        jTableData.scrollRectToVisible(new Rectangle(jTableData.getCellRect(selectedRow, 0, true)));
                    }
                }
                jTableData.updateUI();
                super.keyPressed(e);
            }
        });

        jPanel2 = new JPanel();
        jButtonSelect = new JButton();
        jButtonQuit = new JButton();
        jPanel2.setBackground(new Color(240, 240, 240));
        jButtonSelect.setText(MessageBundle.getMessage("angal.common.select.btn"));
        jButtonSelect.setMnemonic(MessageBundle.getMnemonic("angal.common.select.btn.key"));
        jButtonSelect.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                jButtonSelectMouseClicked(evt);
            }
        });
        jButtonSelect.addActionListener(actionEvent -> {
            jButtonSelectActionPerformed(actionEvent);
        });
        jButtonQuit.setText(MessageBundle.getMessage("angal.common.cancel.btn"));
        jButtonQuit.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
        jButtonQuit.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                jButtonQuitMouseClicked(evt);
            }
        });
        add(jPanel2, BorderLayout.SOUTH);
        jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        jPanel2.add(jButtonSelect);
        jPanel2.add(jButtonQuit);

        if (jTableData.getRowCount() > 0) {
            jTableData.setRowSelectionInterval(0, 0);
        }

    }

    private void jTableDataMouseClicked(MouseEvent evt) {

    }

    private void validateSelection() {
        this.setSelectedRow(this.jTableData.getSelectedRow());
        this.setVisible(false);
        this.getParentFrame().dispose();
    }

    private void jButtonSelectActionPerformed(ActionEvent evt) {

    }

    private void jButtonSelectMouseClicked(MouseEvent evt) {
        this.setSelectedRow(this.jTableData.getSelectedRow());
        this.setVisible(false);
        this.getParentFrame().dispose();
    }

    private void jButtonQuitMouseClicked(MouseEvent evt) {
        this.setVisible(false);
        this.getParentFrame().dispose();
    }
}