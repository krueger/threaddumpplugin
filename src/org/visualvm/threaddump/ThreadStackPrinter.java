/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.threaddump;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Set;

/**
 * Copied from JMXSupport.
 *
 * @author krueger
 */
public class ThreadStackPrinter {

    private static final String SPACE = "        ";

    public void print16Thread(final StringBuilder sb, final ThreadMXBean threadMXBean, final ThreadInfo thread, Set<String> waitingOnThreads) {
        MonitorInfo[] monitors = null;
        if (threadMXBean.isObjectMonitorUsageSupported()) {
            monitors = thread.getLockedMonitors();
        }

        sb.append("\n\"" + thread.getThreadName() + // NOI18N
                "\" - Thread t@" + thread.getThreadId() + "\n");    // NOI18N
        sb.append("   java.lang.Thread.State: " + thread.getThreadState()); // NOI18N
        sb.append("\n");   // NOI18N
        int index = 0;
        for (StackTraceElement st : thread.getStackTrace()) {
            LockInfo lock = thread.getLockInfo();
            String lockOwner = thread.getLockOwnerName();

            sb.append(SPACE + "at " + st.toString() + "\n");    // NOI18N
            if (index == 0) {
                if ("java.lang.Object".equals(st.getClassName()) && // NOI18N
                        "wait".equals(st.getMethodName())) {                // NOI18N
                    if (lock != null) {
                        sb.append(SPACE + "- waiting on ");    // NOI18N
                        printLock(sb, lock);

                        if (lockOwner != null) {
                            sb.append(lockOwner + "\n");
                            waitingOnThreads.add(lockOwner);
                        }
                        sb.append("\n");    // NOI18N
                    }
                } else if (lock != null) {
                    if (lockOwner == null) {
                        sb.append(SPACE + "- parking to wait for ");      // NOI18N
                        printLock(sb, lock);
                        sb.append("\n");            // NOI18N
                    } else {
                        sb.append(SPACE + "- waiting to lock ");      // NOI18N
                        printLock(sb, lock);
                        sb.append(" owned by \"" + lockOwner + "\" t@" + thread.getLockOwnerId() + "\n");   // NOI18N
                        waitingOnThreads.add(lockOwner);
                    }
                }
            }
            printMonitors(sb, monitors, index);
            index++;
        }
        StringBuilder jnisb = new StringBuilder();
        printMonitors(jnisb, monitors, -1);
        if (jnisb.length() > 0) {
            sb.append("   JNI locked monitors:\n");
            sb.append(jnisb);
        }
        if (threadMXBean.isSynchronizerUsageSupported()) {
            sb.append("\n   Locked ownable synchronizers:");    // NOI18N
            LockInfo[] synchronizers = thread.getLockedSynchronizers();
            if (synchronizers == null || synchronizers.length == 0) {
                sb.append("\n" + SPACE + "- None\n");  // NOI18N
            } else {
                for (LockInfo li : synchronizers) {
                    sb.append("\n" + SPACE + "- locked ");         // NOI18N
                    printLock(sb, li);
                    sb.append("\n");  // NOI18N
                }
            }
        }
    }

    private void printLock(StringBuilder sb, LockInfo lock) {
        String id = Integer.toHexString(lock.getIdentityHashCode());
        String className = lock.getClassName();

        sb.append("<" + id + "> (a " + className + ")");       // NOI18N
    }

    private void printMonitors(final StringBuilder sb, final MonitorInfo[] monitors, final int index) {
        if (monitors != null) {
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == index) {
                    sb.append(SPACE + "- locked ");   // NOI18N
                    printLock(sb, mi);
                    sb.append("\n");    // NOI18N
                }
            }
        }
    }

}
