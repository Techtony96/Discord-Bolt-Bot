package net.ajpappas.discord.boltbot.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import net.ajpappas.discord.common.event.EventListener;
import net.ajpappas.discord.common.util.EventFilters;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@Log4j2
public class CountingListener implements EventListener<MessageCreateEvent> {
    /*
     * TODO Add highscores
     * TODO more precision
     * TODO command to see current count
     * TODO more math operands
     */

    private static final String CHANNEL_NAME = "counting";

    private static final ReactionEmoji COUNTING_SUCCESS = ReactionEmoji.unicode("\u2705");
    private static final ReactionEmoji COUNTING_ERROR = ReactionEmoji.unicode("\u274C");
    private static final ReactionEmoji CAN_NOT_EVALUATE = ReactionEmoji.unicode("\u2753");

    private static final String COUNTING_ERROR_RESPONSE = "%s RUINED IT! The count got to **%s**";
    private static final String NEW_HIGH_SCORE = "New highscore! \uD83C\uDF89 How much higher can you count?";

    // Channel ID -> Counting Data
    private final Map<Snowflake, CountingData> currentCountMap = new HashMap<>();

    @Data
    private static class CountingData {
        private BigDecimal count;
        private Snowflake lastUser;
        private BigDecimal highscore;

        CountingData() {
            this.count = BigDecimal.ZERO;
        }
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Predicate<? super MessageCreateEvent> filters() {
        return EventFilters.NO_BOTS;
    }

    @Override
    public Function<? super MessageCreateEvent, ? extends Publisher<Boolean>> asyncFilters() {
        return event -> event.getMessage().getChannel().ofType(TextChannel.class).map(TextChannel::getName).map(CHANNEL_NAME::equalsIgnoreCase);
    }

    @Override
    public Mono<Void> error(Throwable throwable) {
        log.error("Error while processing counting message", throwable);
        return Mono.empty();
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        Snowflake channelId = event.getMessage().getChannelId();
        String message = event.getMessage().getContent().strip();
        String numbers = message.replaceAll("[^0-9.]", "");
        String expression = message.replaceAll("[^0-9.()^%/*+-]", "");

        // Check if message should be ignored - ie normal chat message
        if (message.matches(".*[A-Za-z].*"))
            return Mono.empty();

        // No expression, simple count
        BigDecimal newCount;
        if (Objects.equals(numbers, expression)) {
            newCount = new BigDecimal(numbers);
        } else {
            try {
                newCount = BigDecimal.valueOf(new ExpressionBuilder(expression).build().evaluate());
            } catch (Exception e) {
                return event.getMessage().addReaction(CAN_NOT_EVALUATE);
            }
        }

        CountingData countingData = currentCountMap.computeIfAbsent(channelId, s -> new CountingData());
        BigDecimal lastCount = countingData.getCount();

        if (isValid(lastCount, newCount) && !event.getMember().get().getId().equals(countingData.getLastUser()))  {
            // Count was successfully increased by 1 or less
            countingData.setCount(newCount);
            countingData.setLastUser(event.getMember().get().getId());
            return event.getMessage().addReaction(COUNTING_SUCCESS);
        } else {
            // New count is wrong
            countingData.setCount(BigDecimal.ZERO);
            countingData.setLastUser(null);
            return event.getMessage().addReaction(COUNTING_ERROR)
                    .and(event.getMessage().getChannel().ofType(TextChannel.class)
                            .flatMap(c -> c.createMessage(String.format(COUNTING_ERROR_RESPONSE, event.getMember().get().getMention(), lastCount.toPlainString())))
                    );
        }
    }

    private boolean isValid(BigDecimal lastCount, BigDecimal newCount) {
        // new count is greater than last count
        if (newCount.compareTo(lastCount) <= 0)
            return false;

        // new count is within +1 of last count
        if (newCount.subtract(lastCount).compareTo(BigDecimal.ONE) > 0)
            return false;

        return true;
    }
}
