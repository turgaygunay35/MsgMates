package com.msgmates.app.security

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

class PhoneValidator {

    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun isValid(e164OrLocal: String, defaultRegion: String = "TR"): Boolean {
        return try {
            val pn = util.parse(e164OrLocal, defaultRegion)
            util.isValidNumber(pn)
        } catch (_: NumberParseException) {
            false
        }
    }

    fun normalizeToE164(e164OrLocal: String, defaultRegion: String = "TR"): String? {
        return try {
            val pn = util.parse(e164OrLocal, defaultRegion)
            util.format(pn, PhoneNumberFormat.E164)
        } catch (_: NumberParseException) {
            null
        }
    }

    fun typeOf(e164OrLocal: String, defaultRegion: String = "TR"): PhoneNumberType? {
        return try {
            val pn = util.parse(e164OrLocal, defaultRegion)
            util.getNumberType(pn)
        } catch (_: NumberParseException) {
            null
        }
    }
}
