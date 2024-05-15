package net.ajpappas.discord.boltbot.modules.counting;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import net.ajpappas.discord.common.command.GlobalSlashCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CountCommand implements GlobalSlashCommand {

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
    public PermissionSet requiredPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES); // Temporary fix because none() results in Discord requiring admin
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

}
