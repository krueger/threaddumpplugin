/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.threaddump;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;
import com.sun.tools.visualvm.core.ui.DataSourceViewsManager;

/**
 *
 * @author krueger
 */
public class ThreadDumpViewProvider extends DataSourceViewProvider<Application> {

    private static DataSourceViewProvider instance = new ThreadDumpViewProvider();

    @Override
    protected boolean supportsViewFor(Application x) {
        return true;
    }

    @Override
    protected DataSourceView createView(Application x) {
        return new ThreadDumpView(x);
    }

    static void initialize() {
        DataSourceViewsManager.sharedInstance().addViewProvider(instance, Application.class);
    }

    static void unregister() {
        DataSourceViewsManager.sharedInstance().removeViewProvider(instance);
    }

}
