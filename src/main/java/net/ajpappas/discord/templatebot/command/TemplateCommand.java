package net.ajpappas.discord.templatebot.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.PermissionSet;
import net.ajpappas.discord.common.command.SlashCommand;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TemplateCommand implements SlashCommand {

    @Override
    public String getName() {
        return "template";
    }

    @Override
    public PermissionSet requiredPermissions() {
        return PermissionSet.none();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.reply("Template command");
    }
}
