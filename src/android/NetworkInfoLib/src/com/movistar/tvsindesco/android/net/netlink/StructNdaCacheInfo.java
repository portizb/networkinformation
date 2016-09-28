package com.movistar.tvsindesco.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */
import com.movistar.tvsindesco.android.system.Os;
import com.movistar.tvsindesco.android.system.OsConstants;

import java.nio.ByteBuffer;
/**
 * struct nda_cacheinfo
 *
 * see: &lt;linux_src&gt;/include/uapi/linux/neighbour.h
 *
 * @hide
 */
public class StructNdaCacheInfo {
    // Already aligned.
    public static final int STRUCT_SIZE = 16;

    private static final long CLOCK_TICKS_PER_SECOND = Os.sysconf(OsConstants._SC_CLK_TCK);

    /**
     * Explanatory notes, for reference.
     *
     * Before being returned to user space, the neighbor entry times are
     * converted to clock_t's like so:
     *
     * ndm_used = jiffies_to_clock_t(now - neigh->used);
     * ndm_confirmed = jiffies_to_clock_t(now - neigh->confirmed);
     * ndm_updated = jiffies_to_clock_t(now - neigh->updated);
     *
     * meaning that these values are expressed as "clock ticks ago". To
     * convert these clock ticks to seconds divide by sysconf(_SC_CLK_TCK).
     * When _SC_CLK_TCK is 100, for example, the ndm_* times are expressed
     * in centiseconds.
     *
     * These values are unsigned, but fortunately being expressed as "some
     * clock ticks ago", these values are typically very small (and
     * 2^31 centiseconds = 248 days).
     *
     * By observation, it appears that:
     * ndm_used: the last time ARP/ND took place for this neighbor
     * ndm_confirmed: the last time ARP/ND succeeded for this neighbor OR
     * higher layer confirmation (TCP or MSG_CONFIRM)
     * was received
     * ndm_updated: the time when the current NUD state was entered
     */
    public int ndm_used;
    public int ndm_confirmed;
    public int ndm_updated;
    public int ndm_refcnt;

    public StructNdaCacheInfo() {}

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructNdaCacheInfo parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the possible
// exception of usage within unittests.
        final StructNdaCacheInfo struct = new StructNdaCacheInfo();
        struct.ndm_used = byteBuffer.getInt();
        struct.ndm_confirmed = byteBuffer.getInt();
        struct.ndm_updated = byteBuffer.getInt();
        struct.ndm_refcnt = byteBuffer.getInt();

        return struct;
    }

    // TODO: investigate whether this can change during device runtime and
// decide what (if anything) should be done about that.

    private static long ticksToMilliSeconds(int intClockTicks) {
        final long longClockTicks = (long) intClockTicks & 0xffffffff;
        return (longClockTicks * 1000) / CLOCK_TICKS_PER_SECOND;
    }

    public long lastUsed() {
        return ticksToMilliSeconds(ndm_used);
    }

    public long lastConfirmed() {
        return ticksToMilliSeconds(ndm_confirmed);
    }

    public long lastUpdated() {
        return ticksToMilliSeconds(ndm_updated);
    }

    @Override
    public String toString() {
        return "NdaCacheInfo{ "
                + "ndm_used{" + lastUsed() + "}, "
                + "ndm_confirmed{" + lastConfirmed() + "}, "
                + "ndm_updated{" + lastUpdated() + "}, "
                + "ndm_refcnt{" + ndm_refcnt + "} "
                + "}";
    }
}
