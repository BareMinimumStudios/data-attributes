package net.bms.data_attributes.api.event

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Small loader-neutral event primitive used by the public Data Attributes API.
 *
 * Registration is thread-safe and invocation uses a stable snapshot. The
 * generated invoker is rebuilt only when listeners are added or removed.
 */
class DataAttributeEvent<T : Any>(private val invokerFactory: (List<T>) -> T) {
    private val listeners = CopyOnWriteArrayList<T>()

    @Volatile
    private var cachedInvoker: T = invokerFactory(emptyList())

    fun register(listener: T) {
        listeners.addIfAbsent(listener)
        rebuildInvoker()
    }

    fun unregister(listener: T): Boolean {
        val removed = listeners.remove(listener)
        if (removed) rebuildInvoker()
        return removed
    }

    fun clear() {
        if (listeners.isEmpty()) return
        listeners.clear()
        rebuildInvoker()
    }

    fun invoker(): T = cachedInvoker

    fun listenerCount(): Int = listeners.size

    private fun rebuildInvoker() {
        cachedInvoker = invokerFactory(listeners.toList())
    }
}
