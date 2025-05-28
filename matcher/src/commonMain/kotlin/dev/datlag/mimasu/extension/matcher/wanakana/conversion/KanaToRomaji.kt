package dev.datlag.mimasu.extension.matcher.wanakana.conversion

import dev.datlag.mimasu.extension.matcher.wanakana.common.mapping
import dev.datlag.mimasu.extension.matcher.wanakana.utils.MappingTree

internal data object KanaToRomaji {

    private val BASIC_ROMAJI_TREE = mapping {
        "あ" to "a"; "い" to "i"; "う" to "u"; "え" to "e"; "お" to "o"
        "か" to "ka"; "き" to "ki"; "く" to "ku"; "け" to "ke"; "こ" to "ko"
        "さ" to "sa"; "し" to "shi"; "す" to "su"; "せ" to "se"; "そ" to "so"
        "た" to "ta"; "ち" to "chi"; "つ" to "tsu"; "て" to "te"; "と" to "to"
        "な" to "na"; "に" to "ni"; "ぬ" to "nu"; "ね" to "ne"; "の" to "no"
        "は" to "ha"; "ひ" to "hi"; "ふ" to "fu"; "へ" to "he"; "ほ" to "ho"
        "ま" to "ma"; "み" to "mi"; "む" to "mu"; "め" to "me"; "も" to "mo"
        "ら" to "ra"; "り" to "ri"; "る" to "ru"; "れ" to "re"; "ろ" to "ro"
        "や" to "ya"; "ゆ" to "yu"; "よ" to "yo"
        "わ" to "wa"; "ゐ" to "wi"; "ゑ" to "we"; "を" to "wo"
        "ん" to "n"
        "が" to "ga"; "ぎ" to "gi"; "ぐ" to "gu"; "げ" to "ge"; "ご" to "go"
        "ざ" to "za"; "じ" to "ji"; "ず" to "zu"; "ぜ" to "ze"; "ぞ" to "zo"
        "だ" to "da"; "ぢ" to "ji"; "づ" to "zu"; "で" to "de"; "ど" to "do"
        "ば" to "ba"; "び" to "bi"; "ぶ" to "bu"; "べ" to "be"; "ぼ" to "bo"
        "ぱ" to "pa"; "ぴ" to "pi"; "ぷ" to "pu"; "ぺ" to "pe"; "ぽ" to "po"
        "ゔぁ" to "va"; "ゔぃ" to "vi"; "ゔ" to "vu"; "ゔぇ" to "ve"; "ゔぉ" to "vo"
    }

    private val SPECIAL_SYMBOLS = mapOf(
        '。' to '.',
        '、' to ',',
        '：' to ':',
        '・' to '/',
        '！' to '!',
        '？' to '?',
        '〜' to '~',
        'ー' to '-',
        '「' to '‘',
        '」' to '’',
        '『' to '“',
        '』' to '”',
        '［' to '[',
        '］' to ']',
        '（' to '(',
        '）' to ')',
        '｛' to '{',
        '｝' to '}',
        '　' to ' ',
    )

    private val AMBIGUOUS_VOWELS = listOf('あ', 'い', 'う', 'え', 'お', 'や', 'ゆ', 'よ')
    private val SMALL_Y = mapOf('ゃ' to "ya", 'ゅ' to "yu", 'ょ' to "yo")
    private val SMALL_Y_EXTRA = mapOf('ぃ' to "yi", 'ぇ' to "ye")
    private val SMALL_AEIOU = mapOf(
        'ぁ' to 'a',
        'ぇ' to 'e',
        'ぃ' to 'i',
        'ぉ' to 'o',
        'ぅ' to 'u',
    )
    private val YOON_KANA = listOf('き', 'に', 'ひ', 'み', 'り', 'ぎ', 'び', 'ぴ', 'ゔ', 'く', 'ふ')
    private val YOON_EXCEPTIONS = mapOf(
        'し' to "sh",
        'ち' to "ch",
        'じ' to "j",
        'ぢ' to "j",
    )
    private val SMALL_KANA = mapOf(
        'っ' to "",
        'ゃ' to "ya",
        'ゅ' to "yu",
        'ょ' to "yo",
        'ぁ' to "a",
        'ぃ' to "i",
        'ぅ' to "u",
        'ぇ' to "e",
        'ぉ' to "o",
    )
    private val SOKUON_WHITELIST = mapOf(
        'b' to 'b',
        'c' to 't',
        'd' to 'd',
        'f' to 'f',
        'g' to 'g',
        'h' to 'h',
        'j' to 'j',
        'k' to 'k',
        'm' to 'm',
        'p' to 'p',
        'q' to 'q',
        'r' to 'r',
        's' to 's',
        't' to 't',
        'v' to 'v',
        'w' to 'w',
        'x' to 'x',
        'z' to 'z',
    )

    val kanaToHepburnMap by lazy { createKanaToHepburnMap() }

    private fun createKanaToHepburnMap(): MappingTree {
        val romajiTree = BASIC_ROMAJI_TREE

        SPECIAL_SYMBOLS.forEach { (jSymbol, symbol) ->
            romajiTree.setSubTreeValue(jSymbol.toString(), symbol.toString())
        }

        (SMALL_Y + SMALL_AEIOU).forEach { (roma, kana) ->
            romajiTree.setSubTreeValue(roma.toString(), kana.toString())
        }

        YOON_KANA.forEach { kana ->
            val firstRomajiChar = romajiTree.subTreeOf(kana.toString()).value?.firstOrNull() ?: return@forEach

            SMALL_Y.forEach { (yKana, yRoma) ->
                romajiTree.setSubTreeValue("$kana$yKana", firstRomajiChar + yRoma)
            }

            SMALL_Y_EXTRA.forEach { (yKana, yRoma) ->
                romajiTree.setSubTreeValue("$kana$yKana", firstRomajiChar + yRoma)
            }
        }

        YOON_EXCEPTIONS.forEach { (kana, roma) ->
            SMALL_Y.forEach { (yKana, yRoma) ->
                romajiTree.setSubTreeValue("$kana$yKana", roma + yRoma[1])
            }

            romajiTree.setSubTreeValue("${kana}ぃ", "${roma}yi")
            romajiTree.setSubTreeValue("`${kana}ぇ", "${roma}e")
        }

        romajiTree['っ'] = romajiTree.duplicate().also(::updateTsu)

        SMALL_KANA.forEach { (kana, roma) ->
            romajiTree.setSubTreeValue(kana.toString(), roma)
        }

        AMBIGUOUS_VOWELS.forEach { kana ->
            romajiTree.setSubTreeValue("ん${kana}", "n'${romajiTree[kana]!!.value}")
        }

        return romajiTree.toImmutable()
    }

    private fun updateTsu(tree: MappingTree.Mutable) {
        fun newValue(value: String): String {
            val consent = value.first()
            val whiteListValue = SOKUON_WHITELIST[consent]
            return if (whiteListValue != null) whiteListValue + value else value
        }

        tree.subTrees.values.forEach { updateTsu(it) }
        tree.value?.let { tree.value = newValue(it) }
    }
}