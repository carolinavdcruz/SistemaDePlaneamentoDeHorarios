package com.example.frontend.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Room

object DatabaseProvider {

    @Volatile // garante que se várias threads tentarem aceder ao mesmo tempo todas veem o mesmo valor
    private var INSTANCE: AppDatabase? = null //onde a base de dados está a ser guardada (INSTANCE)

    // context = estado da aplicação
    fun getDatabase(context: Context): AppDatabase {
        //se dbjá existe, logo se já existe INSTANCE, retorna automaticamente
        return INSTANCE ?:
        synchronized(this) { // se ainda não existe bloqueira secçao para garantir que apenas 1 thread cria a db
            val instance = Room.databaseBuilder( // criação da db por parte do ROOM
                context.applicationContext,
                AppDatabase::class.java,
                "sph.db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}