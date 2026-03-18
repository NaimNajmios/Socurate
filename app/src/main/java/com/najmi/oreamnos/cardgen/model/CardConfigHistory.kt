package com.najmi.oreamnos.cardgen.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue

@Stable
class CardConfigHistory(
    initialConfig: CardConfig,
    private val maxHistorySize: Int = 50
) {
    private val _undoStack = mutableStateListOf<CardConfig>()
    private val _redoStack = mutableStateListOf<CardConfig>()
    
    private var _currentConfig by androidx.compose.runtime.mutableStateOf(initialConfig, neverEqualPolicy())
    
    var currentConfig: CardConfig
        get() = _currentConfig
        set(value) {
            if (value != _currentConfig) {
                _undoStack.add(_currentConfig)
                trimHistory(_undoStack, maxHistorySize)
                _redoStack.clear()
                _currentConfig = value
            }
        }
    
    val canUndo: Boolean get() = _undoStack.isNotEmpty()
    val canRedo: Boolean get() = _redoStack.isNotEmpty()
    
    fun undo(): CardConfig? {
        if (_undoStack.isEmpty()) return null
        
        val previousConfig = _undoStack.removeLast()
        _redoStack.add(_currentConfig)
        trimHistory(_redoStack, maxHistorySize)
        
        val result = _currentConfig
        _currentConfig = previousConfig
        return result
    }
    
    fun redo(): CardConfig? {
        if (_redoStack.isEmpty()) return null
        
        val nextConfig = _redoStack.removeLast()
        _undoStack.add(_currentConfig)
        trimHistory(_undoStack, maxHistorySize)
        
        val result = _currentConfig
        _currentConfig = nextConfig
        return result
    }
    
    fun pushSnapshot(config: CardConfig) {
        if (config != _currentConfig) {
            _undoStack.add(_currentConfig)
            trimHistory(_undoStack, maxHistorySize)
            _redoStack.clear()
            _currentConfig = config
        }
    }
    
    fun clear() {
        _undoStack.clear()
        _redoStack.clear()
    }
    
    private fun trimHistory(stack: MutableList<CardConfig>, maxSize: Int) {
        while (stack.size > maxSize) {
            stack.removeAt(0)
        }
    }
    
    val undoStackSize: Int get() = _undoStack.size
    val redoStackSize: Int get() = _redoStack.size
}
