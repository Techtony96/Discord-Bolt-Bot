package net.ajpappas.discord.boltbot.modules.counting;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import net.ajpappas.discord.common.command.GuildSlashCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

@Component
public class CountCommand implements GuildSlashCommand {

    private final CountingListener countingListener;

    @Autowired
    public CountCommand(CountingListener countingListener) {
        this.countingListener = countingListener;
    }

    @Override
    public ImmutableApplicationCommandRequest.Builder requestBuilder() {
        return ApplicationCommandRequest.builder()
                .name("count")
                .description("Get the current count.");
    }

    @Override
    public Mono<Void> handle(ApplicationCommandInteractionEvent event) {
        Snowflake channelId = event.getInteraction().getChannelId();
        long count = countingListener.getCurrentCountMap().getOrDefault(channelId, new CountingData()).getCount();
        return event.reply("Current count: " + count).withEphemeral(true);
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventClassType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Collection<Long> guilds() {
        return Collections.singleton(110927581070512128L);
    }
}
