package sqltool;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;

import sqltool.common.SqlToolkit;


public class JvmLogger {

	/**
	 * Stuff that should be displayed the first time we come in ... when
	 * the application starts up.
	 */
	protected static void displayStartupInfo() {
		SqlToolkit.appLogger.logDebug(" -----------------------------------------------------------------");
		displayRuntimeInfo(true);
		displayOsInfo(true);
		displayMemoryInfo(true);
		displayThreadInfo(true);
		displayThreadInfoOld(true);
		SqlToolkit.appLogger.logDebug(" -----------------------------------------------------------------");
	}


	/**
	 * Stuff that should be displayed at period intervals, just so we
	 * can see what is going on ...
	 */
	protected static void displayCurrentInfo() {
		SqlToolkit.appLogger.logDebug(" -----------------------------------------------------------------");
		displayRuntimeInfo(false);
		displayOsInfo(false);
		displayMemoryInfo(false);
		displayThreadInfo(false);
		displayThreadInfoOld(false);
		SqlToolkit.appLogger.logDebug(" -----------------------------------------------------------------");
	}


	/**
	 * Display some JVM data, "RuntimeMXBean" stuff
	 */
	private static void displayRuntimeInfo(boolean showAll) {
		RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();
		SqlToolkit.appLogger.logDebug("Java Runtime information: " + new java.util.Date());
		if (showAll) {
			SqlToolkit.appLogger.logDebug("   name-name=" + rtBean.getName());
			SqlToolkit.appLogger.logDebug("   boot-path=" + rtBean.getBootClassPath());
			SqlToolkit.appLogger.logDebug("   clss-path=" + rtBean.getClassPath());
			SqlToolkit.appLogger.logDebug("   vmvm-name=" + rtBean.getVmName());
			SqlToolkit.appLogger.logDebug("   vmvm-vend=" + rtBean.getVmVendor());
			SqlToolkit.appLogger.logDebug("   vmvm-vers=" + rtBean.getVmVersion());
			SqlToolkit.appLogger.logDebug("   spec-name=" + rtBean.getSpecName());
			SqlToolkit.appLogger.logDebug("   spec-vend=" + rtBean.getSpecVendor());
			SqlToolkit.appLogger.logDebug("   spec-vers=" + rtBean.getSpecVersion());
		}
		SqlToolkit.appLogger.logDebug("   strt-time=" + new java.util.Date(rtBean.getStartTime()));
		SqlToolkit.appLogger.logDebug("   upup-time=" + rtBean.getUptime() + "ms");
	}


	/**
	 * Display some OS data, "OperatingSystemMXBean" stuff
	 */
	private static void displayOsInfo(boolean showAll) {
		if (showAll) {
			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
			SqlToolkit.appLogger.logDebug("Operation-System information");
			SqlToolkit.appLogger.logDebug("   name-name=" + osBean.getName());
			SqlToolkit.appLogger.logDebug("   arch-name=" + osBean.getArch());
			SqlToolkit.appLogger.logDebug("   arch-proc=" + osBean.getAvailableProcessors());
			SqlToolkit.appLogger.logDebug("   arch-vers=" + osBean.getVersion());
		}
	}

	/**
	 * Memory characteristics of this application ...
	 */
	private static void displayMemoryInfo(boolean showAll) {
		MemoryMXBean mmBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage  heapUse = mmBean.getHeapMemoryUsage();
		MemoryUsage  noHpUse = mmBean.getNonHeapMemoryUsage();
		SqlToolkit.appLogger.logDebug("Memory and Pool information");
		SqlToolkit.appLogger.logDebug("   heap-init=" + heapUse.getInit() + ";  mmax=" + heapUse.getMax() +
				";  comm=" + heapUse.getCommitted() + ";  used=" + heapUse.getUsed());
		SqlToolkit.appLogger.logDebug("   nohp-init=" + noHpUse.getInit() + ";  mmax=" + noHpUse.getMax() +
				";  comm=" + noHpUse.getCommitted() + ";  used=" + noHpUse.getUsed());
		List<MemoryManagerMXBean> memBeans = ManagementFactory.getMemoryManagerMXBeans();
		for (MemoryManagerMXBean memBean : memBeans) {
			SqlToolkit.appLogger.logDebug("   memb-name=" + memBean.getName());
			if (showAll) {
				for (String poolName : memBean.getMemoryPoolNames()) {
					SqlToolkit.appLogger.logDebug("          pool=" + poolName);
				}
			}
		}
	}

	/**
	 * Thread characteristics of this application ...
	 */
	@SuppressWarnings("unused")
	private static void displayThreadInfo(boolean showAll) {
		ThreadMXBean thrBean = ManagementFactory.getThreadMXBean();
		SqlToolkit.appLogger.logDebug("Thread details");
		SqlToolkit.appLogger.logDebug("   thrd-ccnt=" + thrBean.getThreadCount());
		SqlToolkit.appLogger.logDebug("   dmon-ccnt=" + thrBean.getDaemonThreadCount());
		SqlToolkit.appLogger.logDebug("   thrd-totl=" + thrBean.getTotalStartedThreadCount());
		SqlToolkit.appLogger.logDebug("   thrd-ccpu=" + thrBean.getCurrentThreadCpuTime() + "ms");
		SqlToolkit.appLogger.logDebug("   thrd-user=" + thrBean.getCurrentThreadUserTime() + "ms");
		long[] thrIDs = thrBean.getAllThreadIds();
		for (long thrID : thrIDs) {
			ThreadInfo thrInfo = thrBean.getThreadInfo(thrID);
			SqlToolkit.appLogger.logDebug("THREAD: " + thrID + ";  name=" + thrInfo.getThreadName() +
					";  state=" + thrInfo.getThreadState() + ";  block-cnt=" + thrInfo.getBlockedCount() +
					";  wait-cnt=" + thrInfo.getWaitedCount());
			StackTraceElement[] stkTrace = thrInfo.getStackTrace();
//			for (StackTraceElement trcElem : stkTrace) {
//				SqlToolkit.appLogger.logDebug("              =" + trcElem);
//			}
		}
	}


	private static void displayThreadInfoOld(boolean showAll) {
		Thread[] tArray = new Thread[100];
		Thread main = Thread.currentThread();
		ThreadGroup group = main.getThreadGroup();
		int cnt = group.enumerate(tArray, true);
		SqlToolkit.appLogger.logDebug("Thread Group: " + group);
		for (int i=0;  i<cnt;  i++) {
			SqlToolkit.appLogger.logDebug("   thr: " + tArray[i].getId() + " . " + tArray[i] + " . IsAlive=" + tArray[i].isAlive());
		}
		try { Thread.sleep(60000); } catch (Exception ex) { }
	}
}