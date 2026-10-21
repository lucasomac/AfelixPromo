package br.com.lucolimac.afelixpromo.domain.model

/**
 * A partner store and the affiliate offers currently available for it.
 *
 * @property name Store name displayed in the catalog.
 * @property iconUrl Optional store logo URL from the catalog source.
 * @property offers Offers associated with this store.
 */
data class Store(
    /** Store name displayed in the catalog. */
    val name: String,
    /** Offers associated with this store. */
    val offers: List<Offer>,
    /** Optional store logo URL from the catalog source. */
    val iconUrl: String? = null,
)

/**
 * An affiliate offer published for a [Store].
 *
 * @property id Stable identifier for this offer within the loaded catalog.
 * @property couponCode Coupon to copy, or `null` when no coupon is available.
 * @property affiliateUrl HTTP(S) destination used to access the partner store.
 * @property discountValue Discount value as supplied by the catalog source.
 * @property discountType Meaning/type of the discount as supplied by the source.
 * @property description Optional additional terms or details for this offer.
 */
data class Offer(
    /** Stable identifier for this offer within the loaded catalog. */
    val id: String,
    /** Coupon to copy, or `null` when no coupon is available. */
    val couponCode: String?,
    /** HTTP(S) destination used to access the partner store. */
    val affiliateUrl: String,
    /** Discount value as supplied by the catalog source. */
    val discountValue: String,
    /** Meaning/type of the discount as supplied by the source. */
    val discountType: String,
    /** Optional additional terms or details for this offer. */
    val description: String? = null,
    /** Optional customized share message for this offer. */
    val customizedShareMessage: String? = null,
)
