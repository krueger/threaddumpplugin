/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.threaddump;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.options.GlobalPreferences;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;
import com.sun.tools.visualvm.tools.jmx.JvmMXBeans;
import com.sun.tools.visualvm.tools.jmx.JvmMXBeansFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author krueger
 */
public class ThreadDumpViewController {

    public static final Thread.State[] STATES = new Thread.State[]{
        null,
        Thread.State.BLOCKED,
        Thread.State.WAITING,
        Thread.State.TIMED_WAITING,
        Thread.State.RUNNABLE,
        Thread.State.NEW,
        Thread.State.TERMINATED
    };

    ThreadDumpView view;

    Application application;

    ThreadMXBean threadBean;

    ThreadStackPrinter printer = new ThreadStackPrinter();

    ThreadDumpRunnable currentRunnable;

    long intervall;

    public ThreadDumpViewController(ThreadDumpView threadDumpView, Application application) {
        this.view = threadDumpView;
        this.application = application;

        JvmFactory.getDefault().getModel(application);
        JmxModel model = JmxModelFactory.getJmxModelFor(application);
        JvmMXBeans beans = JvmMXBeansFactory.getJvmMXBeans(model, GlobalPreferences.sharedInstance().getThreadsPoll() * 1000);

        threadBean = beans.getThreadMXBean();
        intervall = GlobalPreferences.sharedInstance().getThreadsPoll();
    }

    public void dump(final Thread.State state, final String filter, final int elements, final boolean continuously, final boolean autoStop) {
        stop();

        if (continuously) {
            dumpThread(state, filter, elements, autoStop);
            view.stopButton.setEnabled(true);
        } else {
            String s = createDump(state, filter, elements);
            view.dumpArea.setText(s);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    view.dumpAreaScrollPane.getVerticalScrollBar().setValue(0);
                }
            });
            view.stopButton.setEnabled(false);
        }
    }

    public void dumpThread(final Thread.State state, final String filter, final int elements, final boolean autoStop) {
        stop();

        currentRunnable = new ThreadDumpRunnable(state, filter, elements, autoStop);
        Thread t = new Thread(currentRunnable);
        t.start();
    }

    public String createDump(Thread.State state, String nameFilter, int elements) {
        long[] allThreads = threadBean.getAllThreadIds();

        ThreadInfo[] threadInfos = threadBean.getThreadInfo(allThreads, elements);

        Set<String> waitingOnThreads = new HashSet<String>();
        StringBuilder sb = new StringBuilder();
        for (ThreadInfo info : threadInfos) {

            if (filterThread(info, state, nameFilter)) {
                continue;
            }

            printer.print16Thread(sb, threadBean, info, waitingOnThreads);
        }

        if (!waitingOnThreads.isEmpty()) {
            sb.append("\n");
            sb.append("Lockowner:\n");
            for (ThreadInfo info : threadInfos) {

                boolean found = false;
                for (String s : waitingOnThreads) {
                    if (info != null && info.getThreadName().contains(s)) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    printer.print16Thread(sb, threadBean, info, new HashSet<String>());
                }
            }
        }

        return sb.toString();
    }

    private boolean filterThread(ThreadInfo info, Thread.State state, String nameFilter) {
        if (info == null) {
            return true;
        }

        if (state != null) {
            if (!info.getThreadState().equals(state)) {
                return true;
            }
        }

        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            nameFilter = nameFilter.trim();
            String id = info.getThreadId() + "";
            if (!info.getThreadName().contains(nameFilter) && !id.contains(nameFilter)) {
                return true;
            }
        }

        return false;
    }

    public void stop() {
        if (currentRunnable != null) {
            currentRunnable.stop();
            view.dumpButton.setEnabled(true);
            view.stopButton.setEnabled(false);
        }
    }

    private Monitor monitor = new Monitor();

    private class Monitor {

        private boolean running = false;

        public boolean running() {
            return running;
        }

        public void start() {
            running = true;
        }

        public void stop() {
            synchronized (this) {
                running = false;
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void finished() {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private class ThreadDumpRunnable implements Runnable {

        private boolean stop = false;

        private Thread.State state;
        private String filter;
        private int elements;
        private boolean autostop;

        public ThreadDumpRunnable(Thread.State state, String filter, int elements, boolean autostop) {
            this.state = state;
            this.filter = filter;
            this.elements = elements;
            this.autostop = autostop;
        }

        public void stop() {
            stop = true;
        }

        @Override
        public void run() {
            while (!stop) {
                final String text = createDump(state, filter, elements);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int value = view.dumpAreaScrollPane.getVerticalScrollBar().getValue();
                        view.dumpArea.setText(text);
                        view.dumpAreaScrollPane.getVerticalScrollBar().setValue(value);
                    }
                });

                if (autostop && !text.trim().isEmpty()) {
                    ThreadDumpViewController.this.stop();
                    return;
                }

                try {
                    synchronized (this) {
                        this.wait(intervall);
                    }
                } catch (InterruptedException ex) {
                    String msg = ex.getMessage();
                    view.dumpArea.setText(view.dumpArea.getText() + "\n" + msg);
                }
            }
        }
    }

}
