package com.codingpixel.undiscoveredarchives.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCart(userCart: UserCartTable)

    @Query("SELECT * FROM CartTable WHERE user_id == :userId")
    fun getCartList(userId: Int): List<UserCartTable>

    @Query("UPDATE CartTable SET quantity = :quantity WHERE product_id == :productId AND variant_id=:variantId AND user_id == :userId")
    suspend fun updateUserCart(productId: Int, variantId: Int, quantity: Int, userId: Int)

    @Query("DELETE FROM CartTable WHERE user_id = :userId")
    suspend fun deleteUserCart(userId: Int)

    @Query("DELETE FROM CartTable WHERE user_id = :userId AND product_id == :productId AND variant_id=:variantId")
    suspend fun deleteSingleCartItem(userId: Int,productId: Int, variantId: Int)
}