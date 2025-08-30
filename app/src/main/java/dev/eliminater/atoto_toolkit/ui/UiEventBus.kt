package dev.eliminater.atoto_toolkit.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object UiEventBus {
    private val channel = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    suspend fun emit(event: UiEvent) = channel.send(event)
}
