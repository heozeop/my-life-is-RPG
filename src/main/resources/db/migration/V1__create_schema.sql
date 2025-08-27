-- MyLiftIsRPG Database Schema
-- Version 1.0 - Initial schema creation

-- Users table for authentication and user management
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Characters table for RPG character management
CREATE TABLE characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    level INT DEFAULT 1,
    exp BIGINT DEFAULT 0,
    hp INT DEFAULT 100,
    attack INT DEFAULT 10,
    defense INT DEFAULT 5,
    coins INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Items table for game items
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('WEAPON', 'ARMOR', 'ACCESSORY') NOT NULL,
    rarity ENUM('COMMON', 'RARE', 'EPIC', 'LEGENDARY') NOT NULL,
    stat_bonus JSON,
    description TEXT
);

-- Inventories table for user item ownership
CREATE TABLE inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    equipped_slot VARCHAR(20),
    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quests table for user goals and objectives
CREATE TABLE quests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type ENUM('YEARLY', 'MONTHLY', 'WEEKLY', 'DAILY') NOT NULL,
    reward_exp INT DEFAULT 0,
    reward_coins INT DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);

-- CompletedQuests table for quest completion history
CREATE TABLE completed_quests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quest_id BIGINT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DailyBuffs table for daily fortune system
CREATE TABLE daily_buffs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    buff_type ENUM('EXP_MULTIPLIER', 'DROP_RATE_BOOST', 'STAT_BOOST', 'COIN_MULTIPLIER') NOT NULL,
    multiplier DECIMAL(3,2) DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_date_buff (user_id, date, buff_type)
);

-- Stages table for life timeline/world map
CREATE TABLE stages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    age_start INT NOT NULL,
    age_end INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT
);

-- Create indexes for better query performance
CREATE INDEX idx_characters_user_id ON characters (user_id);
CREATE INDEX idx_inventories_user_id ON inventories (user_id);
CREATE INDEX idx_inventories_item_id ON inventories (item_id);
CREATE INDEX idx_quests_user_id ON quests (user_id);
CREATE INDEX idx_quests_type ON quests (type);
CREATE INDEX idx_completed_quests_user_id ON completed_quests (user_id);
CREATE INDEX idx_completed_quests_quest_id ON completed_quests (quest_id);
CREATE INDEX idx_daily_buffs_user_date ON daily_buffs (user_id, date);

-- Insert initial stage data for life timeline
INSERT INTO stages (age_start, age_end, title, description) VALUES
(0, 9, '유년기 (Childhood)', '순수하고 무한한 가능성의 시대. 모든 것을 배우고 꿈꾸는 시간.'),
(10, 19, '성장기 (Adolescence)', '정체성을 찾고 미래를 준비하는 시대. 학습과 도전의 시간.'),
(20, 29, '청년기 (Young Adult)', '독립하고 커리어를 시작하는 시대. 열정과 야망의 시간.'),
(30, 39, '초기 성인기 (Early Adulthood)', '안정을 찾고 가정을 꾸리는 시대. 책임과 성취의 시간.'),
(40, 49, '중년 전기 (Mid-life)', '경험과 지혜를 쌓는 시대. 성찰과 재평가의 시간.'),
(50, 59, '중년 후기 (Later Mid-life)', '전문성을 발휘하고 후배를 이끄는 시대. 멘토링과 기여의 시간.'),
(60, 69, '초기 노년기 (Early Senior)', '은퇴와 새로운 삶의 시작. 여유와 취미의 시간.'),
(70, 100, '황혼기 (Golden Years)', '인생의 완성과 지혜의 전수. 평안과 회고의 시간.');

-- Insert sample items for the game
INSERT INTO items (name, type, rarity, stat_bonus, description) VALUES
('나무 검', 'WEAPON', 'COMMON', '{"attack": 5}', '초보자용 나무로 만든 검입니다.'),
('철 검', 'WEAPON', 'RARE', '{"attack": 12}', '단단한 철로 만든 검입니다.'),
('가죽 갑옷', 'ARMOR', 'COMMON', '{"defense": 8, "hp": 20}', '가죽으로 만든 기본적인 갑옷입니다.'),
('강철 갑옷', 'ARMOR', 'RARE', '{"defense": 18, "hp": 50}', '강철로 만든 튼튼한 갑옷입니다.'),
('행운의 반지', 'ACCESSORY', 'EPIC', '{"exp_bonus": 1.2}', '경험치 획득량을 증가시키는 신비한 반지입니다.'),
('용사의 목걸이', 'ACCESSORY', 'LEGENDARY', '{"attack": 10, "defense": 10, "exp_bonus": 1.5}', '전설적인 용사가 착용했던 목걸이입니다.');