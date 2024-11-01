package com.auction.common.constants;

public class RabbitMQConst {
    public static final String AUCTION_EXCHANGE = "exchange.auction";
    public static final String AUCTION_DLX = "auction.dlx";
    public static final String AUCTION_DLQ = "auction.dlq";

    public static final String AUCTION_QUEUE_PREFIX = "auction.queue.";
    public static final String[] queueNames
            = new String[]{AUCTION_QUEUE_PREFIX + 1, AUCTION_QUEUE_PREFIX + 2, AUCTION_QUEUE_PREFIX + 3};

    public static final String AUCTION_ROUTING_KEY_PREFIX = "auction.routing.";
    public static final String[] routingKeys
            = new String[]{AUCTION_ROUTING_KEY_PREFIX + 1, AUCTION_ROUTING_KEY_PREFIX + 2, AUCTION_ROUTING_KEY_PREFIX + 3};
}
