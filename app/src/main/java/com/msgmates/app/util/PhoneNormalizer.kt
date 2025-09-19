package com.msgmates.app.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import timber.log.Timber

object PhoneNormalizer {

    private val phoneUtil = PhoneNumberUtil.getInstance()
    private const val DEFAULT_REGION = "TR"

    /**
     * Normalizes a phone number to E.164 format
     * @param rawNumber The raw phone number string
     * @return E.164 formatted number or null if invalid
     */
    fun normalizeToE164(rawNumber: String): String? {
        return try {
            val phoneNumber = phoneUtil.parse(rawNumber, DEFAULT_REGION)
            if (phoneUtil.isValidNumber(phoneNumber)) {
                phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            } else {
                null
            }
        } catch (e: NumberParseException) {
            Timber.w("Failed to parse phone number: $rawNumber", e)
            null
        }
    }

    /**
     * Checks if a phone number is valid for the default region
     */
    fun isValidNumber(rawNumber: String): Boolean {
        return try {
            val phoneNumber = phoneUtil.parse(rawNumber, DEFAULT_REGION)
            phoneUtil.isValidNumber(phoneNumber)
        } catch (e: NumberParseException) {
            false
        }
    }

    /**
     * Formats a phone number for display
     */
    fun formatForDisplay(rawNumber: String): String {
        return try {
            val phoneNumber = phoneUtil.parse(rawNumber, DEFAULT_REGION)
            if (phoneUtil.isValidNumber(phoneNumber)) {
                phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            } else {
                rawNumber
            }
        } catch (e: NumberParseException) {
            rawNumber
        }
    }

    /**
     * Extracts country code from phone number
     */
    fun getCountryCode(rawNumber: String): Int? {
        return try {
            val phoneNumber = phoneUtil.parse(rawNumber, DEFAULT_REGION)
            if (phoneUtil.isValidNumber(phoneNumber)) {
                phoneNumber.countryCode
            } else {
                null
            }
        } catch (e: NumberParseException) {
            null
        }
    }
}
