package me.htna.project.chatdecorator;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class HardwareMonitor {

    private static HardwareMonitor instance;
    public static HardwareMonitor getInstance() {
        if (instance == null)
            instance = new HardwareMonitor();
        return instance;
    }

    private OperatingSystemMXBean osBean;

    private HardwareMonitor() {
        osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    }

    /**
     * Get cpu load
     *
     * @return 0 ~ 1 CPU load level
     */
    public double getSystemCpuLoad() {
        return osBean.getSystemCpuLoad();
    }

    /**
     * Get system memory free space
     *
     * @return Memory free space (unit: byte)
     */
    public long getSystemFreeMemory() {
        return osBean.getFreePhysicalMemorySize();
    }

    /**
     * Get system memory total space
     *
     * @return Memory total space (unit: byte)
     */
    public long getSystemTotalMemory() {
        return osBean.getTotalPhysicalMemorySize();
    }

    /**
     * Get system memory use space
     *
     * @return Memory use space (unit: byte)
     */
    public long getSystemUseMemory() {
        return getSystemTotalMemory() - getSystemFreeMemory();
    }
}
