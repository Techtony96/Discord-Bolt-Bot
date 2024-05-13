package net.ajpappas.discord.boltbot.command;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import net.ajpappas.discord.common.command.GuildSlashCommand;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

@Component
public class BoltCommand implements GuildSlashCommand {

    @Override
    public ImmutableApplicationCommandRequest.Builder requestBuilder() {
        return ApplicationCommandRequest.builder()
                .name("bolt")
                .description("Test command");
    }

    @Override
    public Mono<Void> handle(ApplicationCommandInteractionEvent event) {
        return event.reply("It works!").withEphemeral(true);
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
