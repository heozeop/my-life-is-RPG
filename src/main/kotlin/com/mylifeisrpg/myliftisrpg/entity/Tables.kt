package com.mylifeisrpg.myliftisrpg.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.json
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

// Enums for type safety
enum class ItemType { WEAPON, ARMOR, ACCESSORY }
enum class ItemRarity { COMMON, RARE, EPIC, LEGENDARY }
enum class QuestType { YEARLY, MONTHLY, WEEKLY, DAILY }
enum class BuffType { EXP_MULTIPLIER, DROP_RATE_BOOST, STAT_BOOST, COIN_MULTIPLIER }

// Users table
object UsersTable : LongIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val apiKey = varchar("api_key", 255).uniqueIndex()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

// Characters table
object CharactersTable : LongIdTable("characters") {
    val userId = long("user_id").references(UsersTable.id)
    val name = varchar("name", 100)
    val level = integer("level").default(1)
    val exp = long("exp").default(0)
    val hp = integer("hp").default(100)
    val attack = integer("attack").default(10)
    val defense = integer("defense").default(5)
    val coins = integer("coins").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

// Items table
object ItemsTable : LongIdTable("items") {
    val name = varchar("name", 100)
    val type = enumeration("type", ItemType::class)
    val rarity = enumeration("rarity", ItemRarity::class)
    val statBonus = text("stat_bonus")
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

// Inventories table
object InventoriesTable : LongIdTable("inventories") {
    val userId = long("user_id").references(UsersTable.id)
    val itemId = long("item_id").references(ItemsTable.id)
    val quantity = integer("quantity").default(1)
    val equippedSlot = varchar("equipped_slot", 20).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

// Quests table
object QuestsTable : LongIdTable("quests") {
    val userId = long("user_id").references(UsersTable.id)
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val type = enumeration("type", QuestType::class)
    val rewardExp = integer("reward_exp").default(0)
    val rewardCoins = integer("reward_coins").default(0)
    val completed = bool("completed").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val completedAt = timestamp("completed_at").nullable()
}

// CompletedQuests table
object CompletedQuestsTable : LongIdTable("completed_quests") {
    val userId = long("user_id").references(UsersTable.id)
    val questId = long("quest_id").references(QuestsTable.id)
    val completedAt = timestamp("completed_at") // Keep for backwards compatibility
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

// DailyBuffs table
object DailyBuffsTable : LongIdTable("daily_buffs") {
    val userId = long("user_id").references(UsersTable.id)
    val date = date("date")
    val buffType = enumeration("buff_type", BuffType::class)
    val multiplier = decimal("multiplier", 3, 2).default(java.math.BigDecimal.valueOf(1.0))
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    
    init {
        uniqueIndex("unique_user_date_buff", userId, date, buffType)
    }
}

// Stages table
object StagesTable : LongIdTable("stages") {
    val ageStart = integer("age_start")
    val ageEnd = integer("age_end")
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}