package dev.datlag.burningseries.ui.theme

import dev.datlag.burningseries.SharedRes
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.getImageByFileName

object CountryImage {

    fun getByCode(code: String): ImageResource {
        return when {
            code.equals("AE", true) -> SharedRes.images.AE
            code.equals("AF", true) -> SharedRes.images.AF
            code.equals("AG", true) -> SharedRes.images.AG
            code.equals("AL", true) -> SharedRes.images.AL
            code.equals("AM", true) -> SharedRes.images.AM
            code.equals("AR", true) -> SharedRes.images.AR
            code.equals("AT", true) -> SharedRes.images.AT
            code.equals("AU", true) -> SharedRes.images.AU
            code.equals("AZ", true) -> SharedRes.images.AZ

            code.equals("BA", true) -> SharedRes.images.BA
            code.equals("BB", true) -> SharedRes.images.BA
            code.equals("BD", true) -> SharedRes.images.BD
            code.equals("BE", true) -> SharedRes.images.BE
            code.equals("BF", true) -> SharedRes.images.BF
            code.equals("BG", true) -> SharedRes.images.BG
            code.equals("BH", true) -> SharedRes.images.BH
            code.equals("BI", true) -> SharedRes.images.BI
            code.equals("BJ", true) -> SharedRes.images.BJ
            code.equals("BO", true) -> SharedRes.images.BO
            code.equals("BR", true) -> SharedRes.images.BR
            code.equals("BS", true) -> SharedRes.images.BS
            code.equals("BW", true) -> SharedRes.images.BW
            code.equals("BY", true) -> SharedRes.images.BY

            code.equals("CA", true) -> SharedRes.images.CA
            code.equals("CD", true) -> SharedRes.images.CD
            code.equals("CF", true) -> SharedRes.images.CF
            code.equals("CG", true) -> SharedRes.images.CG
            code.equals("CH", true) -> SharedRes.images.CH
            code.equals("CI", true) -> SharedRes.images.CI
            code.equals("CK", true) -> SharedRes.images.CK
            code.equals("CL", true) -> SharedRes.images.CL
            code.equals("CM", true) -> SharedRes.images.CM
            code.equals("CN", true) -> SharedRes.images.CN
            code.equals("CO", true) -> SharedRes.images.CO
            code.equals("CR", true) -> SharedRes.images.CR
            code.equals("CTR", true) -> SharedRes.images.CTR
            code.equals("CU", true) -> SharedRes.images.CU
            code.equals("CV", true) -> SharedRes.images.CV
            code.equals("CZ", true) -> SharedRes.images.CZ

            code.equals("DE", true) -> SharedRes.images.DE
            code.equals("DJ", true) -> SharedRes.images.DJ
            code.equals("DK", true) -> SharedRes.images.DK
            code.equals("DO", true) -> SharedRes.images.COUNTRY_DO
            code.equals("DZ", true) -> SharedRes.images.DZ

            code.equals("EE", true) -> SharedRes.images.EE
            code.equals("EG", true) -> SharedRes.images.EG
            code.equals("ES", true) -> SharedRes.images.ES
            code.equals("EU", true) || code.equals("Europe", true) -> SharedRes.images.EU

            code.equals("FI", true) -> SharedRes.images.COUNTRY_FI
            code.equals("FJ", true) -> SharedRes.images.FJ
            code.equals("FM", true) -> SharedRes.images.FM
            code.equals("FR", true) -> SharedRes.images.FR

            code.equals("GA", true) -> SharedRes.images.GA
            code.equals("GE", true) -> SharedRes.images.GE
            code.equals("GH", true) -> SharedRes.images.GH
            code.equals("GL", true) -> SharedRes.images.GL
            code.equals("GM", true) -> SharedRes.images.GM
            code.equals("GN", true) -> SharedRes.images.GN
            code.equals("GR", true) -> SharedRes.images.GR
            code.equals("GW", true) -> SharedRes.images.GW
            code.equals("GY", true) -> SharedRes.images.GY

            code.equals("HK", true) -> SharedRes.images.HK
            code.equals("HN", true) -> SharedRes.images.HN
            code.equals("HR", true) -> SharedRes.images.HR
            code.equals("HT", true) -> SharedRes.images.HT
            code.equals("HU", true) -> SharedRes.images.HU

            code.equals("ID", true) -> SharedRes.images.ID
            code.equals("IE", true) -> SharedRes.images.IE
            code.equals("IL", true) -> SharedRes.images.IL
            code.equals("IN", true) -> SharedRes.images.COUNTRY_IN
            code.equals("IT", true) -> SharedRes.images.IT

            code.equals("JM", true) -> SharedRes.images.JM
            code.equals("JO", true) -> SharedRes.images.JO
            code.equals("JP", true) -> SharedRes.images.JP

            code.equals("KM", true) -> SharedRes.images.KM
            code.equals("KN", true) -> SharedRes.images.KN
            code.equals("KP", true) -> SharedRes.images.KP
            code.equals("KR", true) -> SharedRes.images.KR
            code.equals("KR-EXTRA", true) -> SharedRes.images.KR_EXTRA
            code.equals("KRD", true) -> SharedRes.images.KRD
            code.equals("KW", true) -> SharedRes.images.KW

            code.equals("LA", true) -> SharedRes.images.LA
            code.equals("LI", true) -> SharedRes.images.LI
            code.equals("LR", true) -> SharedRes.images.LR
            code.equals("LT", true) -> SharedRes.images.LT
            code.equals("LU", true) -> SharedRes.images.LU
            code.equals("LV", true) -> SharedRes.images.LV

            code.equals("MA", true) -> SharedRes.images.MA
            code.equals("MC", true) -> SharedRes.images.MC
            code.equals("ME", true) -> SharedRes.images.ME
            code.equals("MG", true) -> SharedRes.images.MG
            code.equals("MH", true) -> SharedRes.images.MH
            code.equals("MK", true) -> SharedRes.images.MK
            code.equals("ML", true) -> SharedRes.images.ML
            code.equals("MM", true) -> SharedRes.images.MM
            code.equals("MR", true) -> SharedRes.images.MR
            code.equals("MU", true) -> SharedRes.images.MU
            code.equals("MV", true) -> SharedRes.images.MV
            code.equals("MX", true) -> SharedRes.images.MX
            code.equals("MY", true) -> SharedRes.images.MY

            code.equals("NA", true) -> SharedRes.images.NA
            code.equals("NE", true) -> SharedRes.images.NE
            code.equals("NG", true) -> SharedRes.images.NG
            code.equals("NI", true) -> SharedRes.images.NI
            code.equals("NKR", true) -> SharedRes.images.NKR
            code.equals("NL", true) -> SharedRes.images.NL
            code.equals("NO", true) -> SharedRes.images.NO
            code.equals("NR", true) -> SharedRes.images.NR
            code.equals("NU", true) -> SharedRes.images.NU
            code.equals("NZ", true) -> SharedRes.images.NZ

            code.equals("PA", true) -> SharedRes.images.PA
            code.equals("PE", true) -> SharedRes.images.PE
            code.equals("PG", true) -> SharedRes.images.PG
            code.equals("PH", true) -> SharedRes.images.PH
            code.equals("PK", true) -> SharedRes.images.PK
            code.equals("PL", true) -> SharedRes.images.PL
            code.equals("PMR", true) -> SharedRes.images.PMR
            code.equals("PS", true) -> SharedRes.images.PS
            code.equals("PT", true) -> SharedRes.images.PT
            code.equals("PW", true) -> SharedRes.images.PW

            code.equals("QA", true) -> SharedRes.images.QA

            code.equals("RO", true) -> SharedRes.images.RO
            code.equals("RS", true) -> SharedRes.images.RS
            code.equals("RU", true) -> SharedRes.images.RU

            code.equals("SE", true) -> SharedRes.images.SE
            code.equals("SG", true) -> SharedRes.images.SG
            code.equals("SH", true) -> SharedRes.images.SH
            code.equals("SI", true) -> SharedRes.images.SI
            code.equals("SK", true) -> SharedRes.images.SK
            code.equals("SO", true) -> SharedRes.images.SO
            code.equals("ST", true) -> SharedRes.images.ST
            code.equals("SY", true) -> SharedRes.images.SY

            code.equals("TH", true) -> SharedRes.images.TH
            code.equals("TL", true) -> SharedRes.images.TL
            code.equals("TN", true) -> SharedRes.images.TN
            code.equals("TO", true) -> SharedRes.images.TO
            code.equals("TR", true) -> SharedRes.images.TR
            code.equals("TW", true) -> SharedRes.images.TW

            code.equals("UA", true) -> SharedRes.images.UA
            code.equals("UK", true) -> SharedRes.images.UK
            code.equals("US", true) -> SharedRes.images.US
            code.equals("UY", true) -> SharedRes.images.UY
            code.equals("UZ", true) -> SharedRes.images.UZ

            code.equals("VA", true) -> SharedRes.images.VA
            code.equals("VE", true) -> SharedRes.images.VE
            code.equals("VN", true) -> SharedRes.images.VN

            code.equals("WS", true) -> SharedRes.images.WS

            code.equals("XK", true) -> SharedRes.images.XK

            code.equals("YE", true) -> SharedRes.images.YE

            else -> SharedRes.images.COUNTRY_UNKNOWN
        }
    }

    fun getByFlag(code: String): List<ImageResource> {
        val defaultByCode = getByCode(code)
        return if (defaultByCode == SharedRes.images.COUNTRY_UNKNOWN) {
            when {
                code.equals("DES", true) -> listOf(
                    SharedRes.images.US,
                    SharedRes.images.DE
                )
                code.equals("EN", true) -> listOf(
                    SharedRes.images.US
                )
                code.equals("JPS", true) -> listOf(
                    SharedRes.images.JP,
                    SharedRes.images.US
                )
                else -> listOf(defaultByCode)
            }
        } else {
            listOf(defaultByCode)
        }
    }
}