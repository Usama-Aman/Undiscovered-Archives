package com.codingpixel.undiscoveredarchives.room

class MyDatabaseRepository(private val myDao: MyDao) {

    fun getCartList(userId: Int): List<UserCartTable> = myDao.getCartList(userId)

    suspend fun insertCart(cartTable: UserCartTable) {
        myDao.insertUserCart(cartTable)
    }

    suspend fun updateCartTable(productId: Int, variantId: Int, quantity: Int, userId: Int) {
        myDao.updateUserCart(productId, variantId, quantity, userId)
    }

    suspend fun deleteCart(userId: Int) {
        myDao.deleteUserCart(userId)
    }

    suspend fun deleteSingleCartItem(userId: Int, productId: Int, variantId: Int) {
        myDao.deleteSingleCartItem(userId, productId, variantId)
    }


}