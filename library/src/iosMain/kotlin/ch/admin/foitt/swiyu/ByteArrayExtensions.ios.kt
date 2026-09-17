@file:OptIn(ExperimentalForeignApi::class)

package ch.admin.foitt.swiyu.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.posix.memcpy
import kotlin.collections.isEmpty
import kotlin.toULong

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toData() : NSData {
	if (this.isEmpty()) {
		return NSData()
	}
	return this.usePinned {
		NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
	}
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
	return ByteArray(length.toInt()).apply {
		usePinned {
			memcpy(it.addressOf(0), bytes, length)
		}
	}
}
