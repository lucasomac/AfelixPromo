package br.com.lucolimac.afelixpromo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform