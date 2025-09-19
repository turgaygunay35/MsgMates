package com.msgmates.app.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat

object PhoneExt {
    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun toE164(raw: String, region: String = "TR"): String? =
        try { util.format(util.parse(raw, region), PhoneNumberFormat.E164) } catch (_: NumberParseException) { null }

    fun maskE164(e164: String): String =
        if (e164.length > 6) e164.take(4) + "****" + e164.takeLast(2) else "****"
}
