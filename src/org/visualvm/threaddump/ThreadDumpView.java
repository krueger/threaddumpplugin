/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.threaddump;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author krueger
 */
public class ThreadDumpView extends DataSourceView {

    private DataViewComponent dvc;
    //Reusing an image from the sources:
    private static final String IMAGE_PATH = "com/sun/tools/visualvm/coredump/resources/coredump.png"; // NOI18N

    private ThreadDumpViewController controller;
    private ThreadDumpViewComposer composer;

    public ThreadDumpView(Application application) {
        super(application, NbBundle.getMessage(ThreadDumpView.class, "LBL_title"), new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 60, false);
        controller = new ThreadDumpViewController(this, application);
        composer = new ThreadDumpViewComposer(this, controller);
    }

    @Override
    protected DataViewComponent createComponent() {

        //Panel, which we'll reuse in all four of our detail views for this sample:
        JPanel panel = composer.createMainPanel();

        //Master view:
        DataViewComponent.MasterView masterView = new DataViewComponent.MasterView(NbBundle.getMessage(ThreadDumpView.class, "LBL_overview.title"), null, panel);

        //Configuration of master view:
        DataViewComponent.MasterViewConfiguration masterConfiguration
                = new DataViewComponent.MasterViewConfiguration(true);

        //Add the master view and configuration view to the component:
        dvc = new DataViewComponent(masterView, masterConfiguration);

        return dvc;

    }

    JComboBox stateChooser;
    JTextField nameFilter;
    JComboBox stacktraceLength;
    JCheckBox stopIfNotEmpty;
    JCheckBox continouslyCB;
    JButton stopButton;
    JButton dumpButton;
    JButton clearButton;

    JScrollPane dumpAreaScrollPane;
    JTextArea dumpArea;
}
