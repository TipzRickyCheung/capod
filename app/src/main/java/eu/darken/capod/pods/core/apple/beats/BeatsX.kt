package eu.darken.capod.pods.core.apple.beats

import eu.darken.capod.common.bluetooth.BleScanResult
import eu.darken.capod.common.debug.logging.logTag
import eu.darken.capod.pods.core.PodDevice
import eu.darken.capod.pods.core.apple.ApplePods
import eu.darken.capod.pods.core.apple.BasicSingleApplePods
import eu.darken.capod.pods.core.apple.BasicSingleApplePodsFactory
import eu.darken.capod.pods.core.apple.protocol.ProximityPairing
import java.time.Instant
import javax.inject.Inject

data class BeatsX(
    override val identifier: PodDevice.Id = PodDevice.Id(),
    override val lastSeenAt: Instant = Instant.now(),
    override val firstSeenAt: Instant = Instant.now(),
    override val scanResult: BleScanResult,
    override val proximityMessage: ProximityPairing.Message,
    override val confidence: Float = PodDevice.BASE_CONFIDENCE,
    private val rssiAverage: Int? = null,
) : BasicSingleApplePods {

    override val model: PodDevice.Model = PodDevice.Model.BEATS_X

    override val rssi: Int
        get() = rssiAverage ?: super.rssi

    class Factory @Inject constructor() : BasicSingleApplePodsFactory(TAG) {

        override fun isResponsible(proximityMessage: ProximityPairing.Message): Boolean =
            proximityMessage.getModelInfo().full == DEVICE_CODE

        override fun create(scanResult: BleScanResult, proximityMessage: ProximityPairing.Message): ApplePods {
            val basic = BeatsX(scanResult = scanResult, proximityMessage = proximityMessage)
            val result = searchHistory(basic)

            updateHistory(basic)

            if (result == null) return basic

            return basic.copy(
                identifier = result.id,
                firstSeenAt = result.firstSeenAt,
                confidence = result.confidence,
                rssiAverage = result.averageRssi(basic.rssi),
            )
        }

    }

    companion object {
        private val DEVICE_CODE = 0x0520.toUShort()
        private val TAG = logTag("PodDevice", "Beats", "X")
    }
}