package com.antipanel.backend.dto.provider.api;

import lombok.*;

/**
 * DTO for building order requests to Dripfeed Panel API.
 * Used internally to construct the form data for action=add endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedOrderRequest {

    /**
     * Service ID from the provider
     */
    private Integer serviceId;

    /**
     * Target link/URL for the order
     */
    private String link;

    /**
     * Order quantity
     */
    private Integer quantity;

    /**
     * Number of runs for drip-feed orders
     */
    private Integer runs;

    /**
     * Interval between runs in minutes
     */
    private Integer interval;

    /**
     * Comments for custom comment services (newline separated)
     */
    private String comments;

    /**
     * Usernames for mention services (newline separated)
     */
    private String usernames;

    /**
     * Keywords for keyword services (newline separated)
     */
    private String keywords;

    /**
     * Hashtag for mention by hashtag services
     */
    private String hashtag;

    /**
     * Username for subscription services
     */
    private String username;

    /**
     * Min quantity for subscription services
     */
    private Integer min;

    /**
     * Max quantity for subscription services
     */
    private Integer max;

    /**
     * Number of posts for subscription services
     */
    private Integer posts;

    /**
     * Include old posts (0 or 1)
     */
    private Integer oldPosts;

    /**
     * Delay in minutes for subscription services
     */
    private Integer delay;

    /**
     * Expiry date in d/m/Y format
     */
    private String expiry;

    /**
     * Country code for web traffic services
     */
    private String country;

    /**
     * Device type for web traffic (1-5)
     */
    private Integer device;

    /**
     * Traffic type for web traffic (1-3)
     */
    private Integer typeOfTraffic;

    /**
     * Google keyword for web traffic type 1
     */
    private String googleKeyword;

    /**
     * Referring URL for web traffic type 2
     */
    private String referringUrl;

    /**
     * Answer number for poll services
     */
    private Integer answerNumber;

    /**
     * Groups for group services (newline separated)
     */
    private String groups;

    /**
     * Media URL for media likers services
     */
    private String media;

    /**
     * Creates a simple default order request
     */
    public static DripfeedOrderRequest defaultOrder(Integer serviceId, String link, Integer quantity) {
        return DripfeedOrderRequest.builder()
                .serviceId(serviceId)
                .link(link)
                .quantity(quantity)
                .build();
    }

    /**
     * Creates a drip-feed order request
     */
    public static DripfeedOrderRequest dripFeedOrder(Integer serviceId, String link, Integer quantity,
                                                      Integer runs, Integer interval) {
        return DripfeedOrderRequest.builder()
                .serviceId(serviceId)
                .link(link)
                .quantity(quantity)
                .runs(runs)
                .interval(interval)
                .build();
    }
}
