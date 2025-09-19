package com.msgmates.app.security

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

object PhoneUtils {
    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun normalizeOrNull(input: String, defaultRegion: String = "TR"): String? = try {
        util.format(util.parse(input, defaultRegion), PhoneNumberFormat.E164)
    } catch (_: NumberParseException) { null }

    fun normalize(input: String, defaultRegion: String = "TR"): String =
        normalizeOrNull(input, defaultRegion) ?: input

    fun isMobile(input: String, defaultRegion: String = "TR"): Boolean = try {
        val pn = util.parse(input, defaultRegion)
        val t = util.getNumberType(pn)
        t == PhoneNumberType.MOBILE || t == PhoneNumberType.FIXED_LINE_OR_MOBILE
    } catch (_: NumberParseException) { false }
}
