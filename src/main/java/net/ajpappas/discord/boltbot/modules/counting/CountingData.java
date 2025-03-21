package net.ajpappas.discord.boltbot.modules.counting;

import discord4j.common.util.Snowflake;
import lombok.Data;

@Data
class CountingData {
    private Long count;
    private Snowflake lastUser;
    private Long highscore;

    CountingData() {
        this.count = 0L;
    }

    CountingData(long startingCount) {
        this.count = startingCount;
    }
}
