package net.ajpappas.discord.templatebot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import net.ajpappas.discord.common.util.ErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TemplateListener {

    @Autowired
    public TemplateListener(GatewayDiscordClient client) {
        client.on(ButtonInteractionEvent.class)
                .flatMap(event -> handle(event)
                        .onErrorResume(error -> ErrorHandler.handleError(error, msg -> event.reply(msg).withEphemeral(true))))
                .subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {
            return Mono.empty();
    }
}
