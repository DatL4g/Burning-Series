package dev.datlag.burningseries.shared.common

import com.vanniktech.blurhash.BlurHash

fun BlurHash.random(): String {
    val list = listOf(
        "LEHLh[WB2yk8pyoJadR*.7kCMdnj",
        "LKN]Rv%2Tw=w]~RBVZRi};RPxuwH",
        "L4D9hwcE00~WL#V@%~\$%00ad~qIA",
        "LBFh@Z=wrY\$_~obE\$+t6NPIpIqa7",
        "LLJGsB{y0NNY^jn\$RPXTso\$zt7R*",
        "LSNAhr00.TbI%M-=%NayS\$f9IUM{"
    )
    return list.random()
}