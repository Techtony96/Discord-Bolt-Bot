package net.ajpappas.discord.boltbot.modules.counting;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.ajpappas.discord.common.event.EventListener;
import net.ajpappas.discord.common.util.EventFilters;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@Log4j2
public class CountingListener implements EventListener<MessageCreateEvent> {
    /*
     * TODO Add highscores
     * TODO more math operands
     */

    static final String CHANNEL_NAME = "counting";

    private static final ReactionEmoji COUNTING_SUCCESS = ReactionEmoji.unicode("\u2705");
    private static final ReactionEmoji COUNTING_ERROR = ReactionEmoji.unicode("\u274C");
    private static final ReactionEmoji CAN_NOT_EVALUATE = ReactionEmoji.unicode("\u2753");

    private static final String COUNTING_ERROR_RESPONSE = "%s RUINED IT! The count got to **%s**";
    private static final String NEW_HIGH_SCORE = "New highscore! \uD83C\uDF89 How much higher can you count?";

    // Channel ID -> Counting Data
    @Getter(AccessLevel.PROTECTED)
    private final Map<Snowflake, CountingData> currentCountMap = new HashMap<>();

    private final ExpressionEvaluator evaluator;

    @Autowired
    public CountingListener(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
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
        String message = event.getMessage().getContent();

        if (message == null || message.isBlank())
            return Mono.empty();

        long newCount;
        try {
            newCount = Long.parseLong(message);
        } catch (Exception e) {
            try {
                newCount = evaluator.evaluate(message).setScale(0, RoundingMode.HALF_UP).longValueExact();
            } catch (Exception e2) {
                return event.getMessage().addReaction(CAN_NOT_EVALUATE);
            }
        }

        // Check if we restarted, load from last message
        if (!currentCountMap.containsKey(channelId)) {
            String lastMessage = event.getMessage().getChannel().flatMap(MessageChannel::getLastMessage).map(Message::getContent).block();
            long forgottenCount = 0;
            try {
                forgottenCount = Long.parseLong(lastMessage);
            } catch (Exception e) {
                try {
                    forgottenCount = evaluator.evaluate(lastMessage).setScale(0, RoundingMode.HALF_UP).longValueExact();
                } catch (Exception e2) {
                    // Last message wasn't a count. Ignore.
                }
            }
            if (forgottenCount > 0) {
                currentCountMap.put(channelId, new CountingData(forgottenCount));
                String msg = "Count forgotten, re-initialized count back to " + forgottenCount + ".";
                event.getMessage().getChannel().map(c -> c.createMessage(msg)).block();
            }
        }
        CountingData countingData = currentCountMap.computeIfAbsent(channelId, s -> new CountingData());
        long lastCount = countingData.getCount();

        if (isValid(lastCount, newCount) /*&& !event.getMember().get().getId().equals(countingData.getLastUser())*/)  {
            // Count was successfully increased by 1 or less
            countingData.setCount(newCount);
            countingData.setLastUser(event.getMember().get().getId());
            return event.getMessage().addReaction(COUNTING_SUCCESS);
        } else {
            // New count is wrong
            countingData.setCount(0L);
            countingData.setLastUser(null);
            return event.getMessage().addReaction(COUNTING_ERROR)
                    .and(event.getMessage().getChannel().ofType(TextChannel.class)
                            .flatMap(c -> c.createMessage(String.format(COUNTING_ERROR_RESPONSE, event.getMember().get().getMention(), lastCount)))
                    );
        }
    }

    private boolean isValid(long lastCount, long newCount) {
        // new count is greater than last count
        if (newCount <= lastCount)
            return false;

        // new count is within +1 of last count
        if (newCount - lastCount > 1)
            return false;

        return true;
    }
}
