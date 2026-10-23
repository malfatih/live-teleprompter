package com.example.data.repository

import com.example.data.dao.ScriptDao
import com.example.data.model.ScriptEntity
import kotlinx.coroutines.flow.Flow

class ScriptRepository(private val scriptDao: ScriptDao) {
    val allScripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()
    val favoriteScripts: Flow<List<ScriptEntity>> = scriptDao.getFavoriteScripts()

    fun getScriptById(id: Long): Flow<ScriptEntity?> = scriptDao.getScriptById(id)

    suspend fun getScriptByIdDirect(id: Long): ScriptEntity? = scriptDao.getScriptByIdDirect(id)

    fun searchScripts(query: String): Flow<List<ScriptEntity>> = scriptDao.searchScripts(query)

    suspend fun insert(script: ScriptEntity): Long = scriptDao.insertScript(script)

    suspend fun update(script: ScriptEntity) = scriptDao.updateScript(script)

    suspend fun delete(script: ScriptEntity) = scriptDao.deleteScript(script)

    suspend fun deleteById(id: Long) = scriptDao.deleteById(id)

    suspend fun updateLastUsed(id: Long, timestamp: Long = System.currentTimeMillis()) {
        scriptDao.updateLastUsed(id, timestamp)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        scriptDao.updateFavorite(id, isFavorite)
    }
}
