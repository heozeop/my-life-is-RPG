package com.mylifeisrpg.myliftisrpg.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    @JsonIgnore
    val passwordHash: String,
    val apiKey: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Character(
    val id: Long? = null,
    val userId: Long,
    val name: String,
    val level: Int = 1,
    val exp: Long = 0,
    val hp: Int = 100,
    val attack: Int = 10,
    val defense: Int = 5,
    val coins: Int = 0,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Item(
    val id: Long? = null,
    val name: String,
    val type: ItemType,
    val rarity: ItemRarity,
    val statBonus: String,
    val description: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Inventory(
    val id: Long? = null,
    val userId: Long,
    val itemId: Long,
    val quantity: Int = 1,
    val equippedSlot: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Quest(
    val id: Long? = null,
    val userId: Long,
    val title: String,
    val description: String? = null,
    val type: QuestType,
    val rewardExp: Int = 0,
    val rewardCoins: Int = 0,
    val completed: Boolean = false,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val completedAt: LocalDateTime? = null
)

data class CompletedQuest(
    val id: Long? = null,
    val userId: Long,
    val questId: Long,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val completedAt: LocalDateTime = LocalDateTime.now(), // Keep for backwards compatibility
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class DailyBuff(
    val id: Long? = null,
    val userId: Long,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val buffType: BuffType,
    val multiplier: BigDecimal = BigDecimal.valueOf(1.0),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Stage(
    val id: Long? = null,
    val ageStart: Int,
    val ageEnd: Int,
    val title: String,
    val description: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)