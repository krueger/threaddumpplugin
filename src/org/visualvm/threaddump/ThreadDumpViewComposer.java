/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.threaddump;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.openide.util.NbBundle;

/**
 *
 * @author krueger
 */
public class ThreadDumpViewComposer {

    ThreadDumpViewController controller;
    ThreadDumpView view;

    public ThreadDumpViewComposer(ThreadDumpView threaddumpView, ThreadDumpViewController threadDumpViewController) {
        this.controller = threadDumpViewController;
        this.view = threaddumpView;
    }

    public JPanel createMainPanel() {
        initComponents();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(view.dumpAreaScrollPane, BorderLayout.CENTER);

        initActions();

        return panel;
    }

    private void initComponents() {
        view.continouslyCB = createContinouslyCB();
        view.dumpAreaScrollPane = createDumpPanel();
        view.nameFilter = createNameFilter();
        view.stacktraceLength = createStrackTraceLengthCB();
        view.stateChooser = createStateChooser();
        view.stopButton = createStopButton();
        view.stopIfNotEmpty = createStopIfNotEmpty();
        view.dumpButton = createButton("LBL_dump");
        view.clearButton = createButton("LBL_clear");
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel(message("LBL_state") + ":"));
        panel.add(view.stateChooser);
        panel.add(new JLabel(message("LBL_filter") + ":"));
        panel.add(view.nameFilter);
        panel.add(new JLabel(message("LBL_stacktrace") + ":"));
        panel.add(view.stacktraceLength);
        panel.add(view.continouslyCB);
        panel.add(view.stopIfNotEmpty);
        panel.add(view.dumpButton);
        panel.add(view.stopButton);
        panel.add(view.stopButton);
        panel.add(view.clearButton);

        return panel;
    }

    private void initActions() {
        view.dumpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createDump();
            }
        });

        view.continouslyCB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = view.continouslyCB.isSelected();
                view.stopIfNotEmpty.setEnabled(selected);
            }
        });

        view.clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                view.dumpArea.setText("");
            }
        });

        view.stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.stop();
                view.stopButton.setEnabled(false);
                view.dumpButton.setEnabled(true);
            }
        });
    }

    private JComboBox createStateChooser() {
        JComboBox stateChooser = new JComboBox(controller.STATES);
        stateChooser.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    setText("   ");
                    return this;
                }

                Thread.State state = (Thread.State) value;
                String label = "";
                switch (state) {
                    case BLOCKED:
                        label = message("LBL_blocked");
                        break;
                    case NEW:
                        label = message("LBL_new");
                        break;
                    case RUNNABLE:
                        label = message("LBL_runnable");
                        break;
                    case TERMINATED:
                        label = message("LBL_terminated");
                        break;
                    case TIMED_WAITING:
                        label = message("LBL_timed_waiting");
                        break;
                    case WAITING:
                        label = message("LBL_waiting");
                        break;
                }

                this.setText(label);
                return this;
            }
        });

        return stateChooser;
    }

    private JCheckBox createContinouslyCB() {
        JCheckBox cb = new JCheckBox(message("LBL_continuously"));
        return cb;
    }

    private JTextField createNameFilter() {
        JTextField nameFilter = new JTextField();
        nameFilter.setColumns(20);
        return nameFilter;
    }

    private JButton createButton(String labelKey) {
        return new JButton(message(labelKey));
    }

    private JScrollPane createDumpPanel() {
        JTextArea dumpArea = new JTextArea();
        dumpArea.setEditable(false);
        dumpArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        view.dumpArea = dumpArea;

        JScrollPane dumpAreaScrollPane = new JScrollPane(dumpArea);
        dumpAreaScrollPane.setAutoscrolls(true);
        dumpAreaScrollPane.setWheelScrollingEnabled(true);
        dumpAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dumpAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dumpAreaScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        return dumpAreaScrollPane;
    }

    private void createDump() {
        view.dumpArea.setText("");
        Thread.State state = (Thread.State) view.stateChooser.getSelectedItem();
        String filter = view.nameFilter.getText();
        boolean continously = view.continouslyCB.isSelected();
        int elements = (Integer) view.stacktraceLength.getSelectedItem();
        boolean autoStop = view.stopIfNotEmpty.isSelected();

        controller.dump(state, filter, elements, continously, autoStop);

        view.dumpButton.setEnabled(!continously);
    }

    private JButton createStopButton() {
        JButton stopButton = new JButton(message("LBL_stop"));
        stopButton.setEnabled(false);

        return stopButton;
    }

    private JComboBox createStrackTraceLengthCB() {
        Integer[] lengths = new Integer[]{
            5, 10, 15, 20, Integer.MAX_VALUE
        };
        JComboBox stacktraceLength = new JComboBox(lengths);
        stacktraceLength.setSelectedIndex(0);

        stacktraceLength.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Integer i = (Integer) value;
                if (i == Integer.MAX_VALUE) {
                    setText(message("LBL_complete"));
                } else {
                    setText(value.toString());
                }

                return this;
            }

        });

        return stacktraceLength;
    }

    private JCheckBox createStopIfNotEmpty() {
        JCheckBox stopIfNotEmpty = new JCheckBox(message("LBL_autostop"));
        stopIfNotEmpty.setEnabled(false);
        stopIfNotEmpty.setSelected(false);

        return stopIfNotEmpty;
    }

    private String message(String key) {
        return NbBundle.getMessage(ThreadDumpView.class, key);
    }
}
