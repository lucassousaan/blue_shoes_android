package com.faz.tudo.blueshoes.util

import androidx.recyclerview.selection.ItemKeyProvider
import com.faz.tudo.blueshoes.domain.NavMenuItem

class NavMenuItemKeyProvider( val items: List<NavMenuItem> ) :
    ItemKeyProvider<Long>( ItemKeyProvider.SCOPE_MAPPED ) {

    /*
     * Retorna a chave de seleção na posição fornecida do adaptador ou
     * então retorna null.
     * */
    override fun getKey( position: Int ) = items[ position ].id

    /*
     * Retorna a posição correspondente à chave de seleção, ou
     * RecyclerView.NO_POSITION em caso de null em getKey().
     * */
    override fun getPosition( key: Long )
            = items.indexOf(
        items.filter{
                item -> item.id == key
        }.single()
    )
}