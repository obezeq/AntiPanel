package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Paymento webhook (IPN) callback payload.
 * Paymento sends this to the configured IPN URL when payment status changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentoWebhookPayload {

    /**
     * Unique payment token.
     */
    @JsonProperty("Token")
    private String token;

    /**
     * Paymento's internal payment ID.
     */
    @JsonProperty("PaymentId")
    private String paymentId;

    /**
     * Merchant's internal order ID (our invoice ID).
     */
    @JsonProperty("OrderId")
    private String orderId;

    /**
     * Payment status code.
     * 0 = Initialize
     * 1 = Pending
     * 2 = PartialPaid
     * 3 = WaitingToConfirm
     * 4 = Timeout
     * 5 = UserCanceled
     * 7 = Paid
     * 8 = Approve
     * 9 = Reject
     */
    @JsonProperty("OrderStatus")
    private Integer orderStatus;

    /**
     * Additional metadata from original request.
     */
    @JsonProperty("AdditionalData")
    private String additionalData;

    // ============ Status Check Methods ============

    /**
     * Checks if payment was successfully completed.
     * Status 7 (Paid) indicates blockchain confirmation.
     *
     * @return true if payment is paid
     */
    public boolean isPaid() {
        return orderStatus != null && orderStatus == 7;
    }

    /**
     * Checks if payment was approved by merchant.
     * Status 8 (Approve) indicates merchant verification.
     *
     * @return true if payment is approved
     */
    public boolean isApproved() {
        return orderStatus != null && orderStatus == 8;
    }

    /**
     * Checks if payment is in a successful final state.
     *
     * @return true if payment is completed (Paid or Approved)
     */
    public boolean isSuccessful() {
        return isPaid() || isApproved();
    }

    /**
     * Checks if payment failed (timeout, cancelled, or rejected).
     *
     * @return true if payment failed
     */
    public boolean isFailed() {
        return orderStatus != null &&
                (orderStatus == 4 || orderStatus == 5 || orderStatus == 9);
    }

    /**
     * Checks if payment timed out.
     *
     * @return true if payment timed out (status 4)
     */
    public boolean isTimedOut() {
        return orderStatus != null && orderStatus == 4;
    }

    /**
     * Checks if payment was cancelled by user.
     *
     * @return true if user cancelled (status 5)
     */
    public boolean isCancelled() {
        return orderStatus != null && orderStatus == 5;
    }

    /**
     * Checks if payment was rejected.
     *
     * @return true if rejected (status 9)
     */
    public boolean isRejected() {
        return orderStatus != null && orderStatus == 9;
    }

    /**
     * Checks if payment is still pending/processing.
     * Statuses: 0 (Initialize), 1 (Pending), 2 (PartialPaid), 3 (WaitingToConfirm)
     *
     * @return true if payment is still processing
     */
    public boolean isPending() {
        return orderStatus != null &&
                (orderStatus == 0 || orderStatus == 1 || orderStatus == 2 || orderStatus == 3);
    }

    /**
     * Checks if payment is partially paid.
     *
     * @return true if partially paid (status 2)
     */
    public boolean isPartiallyPaid() {
        return orderStatus != null && orderStatus == 2;
    }

    /**
     * Gets human-readable status name.
     *
     * @return Status name
     */
    public String getStatusName() {
        if (orderStatus == null) {
            return "Unknown";
        }
        return switch (orderStatus) {
            case 0 -> "Initialize";
            case 1 -> "Pending";
            case 2 -> "PartialPaid";
            case 3 -> "WaitingToConfirm";
            case 4 -> "Timeout";
            case 5 -> "UserCanceled";
            case 7 -> "Paid";
            case 8 -> "Approve";
            case 9 -> "Reject";
            default -> "Unknown (" + orderStatus + ")";
        };
    }
}
